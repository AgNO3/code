/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2013 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import java.io.File;
import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.console.ssh.SSHServiceConfiguration;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    KeyPairProvider.class
}, configurationPid = SSHServiceConfiguration.PID_HOSTKEY, configurationPolicy = ConfigurationPolicy.REQUIRE, properties = {
    SSHServiceConfiguration.HOSTKEY + "=" + SimpleHostkeyProviderFactoryImpl.DUMMY_PROVIDER, Constants.SERVICE_RANKING + "=-100"
} )
public class SimpleHostkeyProviderFactoryImpl implements KeyPairProvider {

    private static final String DUMMY_HOSTKEY_PATH = "/dev-hostkey.pem"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SimpleHostkeyProviderFactoryImpl.class);

    protected static final String DUMMY_PROVIDER = "dummy"; //$NON-NLS-1$

    private KeyPairProvider instance;
    private BundleContext bundleContext;


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    /**
     * @return the context
     */
    public BundleContext getBundleContext () {
        return this.bundleContext;
    }


    @Activate
    @Modified
    protected void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
        String hostkeySpec = (String) context.getProperties().get(SSHServiceConfiguration.HOSTKEY);
        if ( hostkeySpec != null && hostkeySpec.equals(DUMMY_PROVIDER) ) {
            getLog().warn("Using insecure dummy host key for SSH daemon"); //$NON-NLS-1$
            this.instance = new DummyHostKeyProvider(this.bundleContext.getBundle().getEntry(DUMMY_HOSTKEY_PATH));
        }
        else if ( hostkeySpec != null ) {
            File f = new File(hostkeySpec.trim());
            if ( f.exists() && !f.canRead() ) {
                getLog().error("Cannot read SSH hostkey file " + f); //$NON-NLS-1$
            }
            else if ( !f.exists() && !f.getParentFile().canWrite() ) {
                getLog().error("Cannot create non-existant SSH hostkey file " + f); //$NON-NLS-1$
            }
            this.instance = new SimpleGeneratorHostKeyProvider(f);
        }

    }


    /**
     * @return
     */
    private KeyPairProvider getDelegate () {
        if ( this.instance == null ) {
            return new SimpleGeneratorHostKeyProvider();
        }
        return this.instance;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.keyprovider.KeyPairProvider#getKeyTypes()
     */
    @Override
    public Iterable<String> getKeyTypes () {
        return this.getDelegate().getKeyTypes();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.keyprovider.KeyPairProvider#loadKey(java.lang.String)
     */
    @Override
    public KeyPair loadKey ( String key ) {
        return this.getDelegate().loadKey(key);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.common.keyprovider.KeyPairProvider#loadKeys()
     */
    @Override
    public Iterable<KeyPair> loadKeys () {
        return this.getDelegate().loadKeys();
    }

}
