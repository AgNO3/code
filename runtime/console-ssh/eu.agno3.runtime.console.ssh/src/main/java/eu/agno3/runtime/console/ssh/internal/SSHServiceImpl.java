/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2013 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.signature.Signature;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.runtime.console.ssh.SSHService;
import eu.agno3.runtime.console.ssh.SSHServiceConfiguration;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true, configurationPid = SSHServiceConfiguration.PID, service = {
    SSHService.class
} )
public class SSHServiceImpl implements SSHService {

    private static final Logger log = Logger.getLogger(SSHServiceImpl.class);

    private SshServer sshd;

    private PasswordAuthenticator passwordAuthenticator;
    private PublickeyAuthenticator pubkeyAuthenticator;

    private Factory<Command> shellFactory;
    private CommandFactory commandFactory;

    private KeyPairProvider keyPairProvider;

    private boolean enabled = false;


    /**
     * 
     */
    public SSHServiceImpl () {
        this.sshd = SshServer.setUpDefaultServer();
    }


    @Modified
    @Activate
    protected void activate ( ComponentContext context ) {
        try {
            if ( this.sshd.isOpen() ) {
                this.sshd.stop();
            }

            this.configureServer(context);

            if ( this.enabled ) {
                log.info(String.format("Starting SSH daemon on %s:%d", this.sshd.getHost(), this.sshd.getPort())); //$NON-NLS-1$
                this.sshd.start();
            }
        }
        catch ( Exception e ) {
            log.error("Failed to start SSH service:", e); //$NON-NLS-1$
        }
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        try {
            log.debug("SSH daemon deactivate called"); //$NON-NLS-1$
            this.sshd.stop();
        }
        catch ( Exception e ) {
            log.error("Failed to stop SSH service:", e); //$NON-NLS-1$
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setPasswordAuthenticator ( PasswordAuthenticator passAuth ) {
        this.passwordAuthenticator = passAuth;
    }


    protected synchronized void unsetPasswordAuthenticator ( PasswordAuthenticator passAuth ) {
        if ( this.passwordAuthenticator == passAuth ) {
            this.passwordAuthenticator = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setPubkeyAuthenticator ( PublickeyAuthenticator keyAuth ) {
        this.pubkeyAuthenticator = keyAuth;
    }


    protected synchronized void unsetPubkeyAuthenticator ( PublickeyAuthenticator keyAuth ) {
        if ( this.pubkeyAuthenticator == keyAuth ) {
            this.pubkeyAuthenticator = null;
        }
    }


    @Reference
    protected synchronized void setShellFactory ( ShellFactory sf ) {
        this.shellFactory = sf;
    }


    protected synchronized void unsetShellFactory ( ShellFactory sf ) {
        if ( this.shellFactory == sf ) {
            this.shellFactory = null;
        }
    }


    @Reference
    protected synchronized void setCommandFactory ( CommandFactory cf ) {
        this.commandFactory = cf;
    }


    protected synchronized void unsetCommandFactory ( CommandFactory cf ) {
        if ( this.commandFactory == cf ) {
            this.commandFactory = null;
        }
    }


    @Reference
    protected synchronized void setHostkeyProvider ( KeyPairProvider provider ) {
        this.keyPairProvider = provider;
    }


    protected synchronized void unsetHostkeyProvider ( KeyPairProvider factory ) {
        if ( this.keyPairProvider == factory ) {
            this.keyPairProvider = null;
        }
    }


    private void configureServer ( ComponentContext context ) {

        log.debug("SSH daemon configuration updated"); //$NON-NLS-1$

        String enabledSpec = (String) context.getProperties().get(SSHServiceConfiguration.ENABLE);

        log.trace(String.format("enable: %s", enabledSpec)); //$NON-NLS-1$

        if ( enabledSpec != null && enabledSpec.equals(Boolean.TRUE.toString()) ) {
            this.enabled = true;
        }
        else {
            this.enabled = false;
        }

        String portSpec = (String) context.getProperties().get(SSHServiceConfiguration.PORT);
        log.trace(String.format("port: %s", portSpec)); //$NON-NLS-1$

        if ( portSpec != null ) {
            this.sshd.setPort(Integer.parseInt(portSpec));
        }
        else {
            this.sshd.setPort(8022);
        }

        String bindSpec = (String) context.getProperties().get(SSHServiceConfiguration.BIND);
        log.trace(String.format("bind: %s", bindSpec)); //$NON-NLS-1$

        if ( bindSpec != null ) {
            try {
                this.sshd.setHost(InetAddress.getByName(bindSpec).getHostAddress());
            }
            catch ( UnknownHostException e ) {
                log.error("Cannot resolve bind address", e); //$NON-NLS-1$
            }
        }
        else {
            this.sshd.setHost(InetAddress.getLoopbackAddress().getHostAddress());
        }

        if ( this.passwordAuthenticator != null ) {
            this.sshd.setPasswordAuthenticator(this.passwordAuthenticator);
        }
        else {
            this.sshd.setPasswordAuthenticator(new NoPasswordAuthenticator());
        }

        String threadsSpec = (String) context.getProperties().get(SSHServiceConfiguration.NUM_THREADS);
        if ( !StringUtils.isBlank(threadsSpec) ) {
            this.sshd.setNioWorkers(Integer.parseInt(threadsSpec.trim()));
        }
        else {
            this.sshd.setNioWorkers(1);
        }

        this.sshd.setPublickeyAuthenticator(this.pubkeyAuthenticator);
        this.sshd.setShellFactory(this.shellFactory);
        this.sshd.setCommandFactory(this.commandFactory);

        this.sshd.setFileSystemFactory(new VirtualFileSystemFactory());
        this.sshd.setForwardingFilter(new NoForwardingFilter());
        this.sshd.setKeyPairProvider(this.keyPairProvider);

        this.sshd.setSignatureFactories(Arrays.asList((NamedFactory<Signature>) new RSAProviderSignatureFactory()));

    }
}
