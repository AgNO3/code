/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2015 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.newsclub.net.unix.rmi.AFUNIXNaming;
import org.newsclub.net.unix.rmi.AFUNIXRMIPorts;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.jmx.CredentialChecker;
import eu.agno3.runtime.jmx.JMXPermissions;
import eu.agno3.runtime.jmx.JMXServer;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.serialization.SerializationFilterClassLoader;


/**
 * @author mbechler
 *
 */
@Component ( service = JMXServer.class, immediate = true, configurationPid = JMXServer.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class JMXServerImpl implements JMXServer {

    private static final Logger log = Logger.getLogger(JMXServerImpl.class);

    /**
     * 
     */
    public static final String JMX_SERVER_DAEMON = "jmx.remote.x.daemon"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String EXPORTER_ATTRIBUTE = "com.sun.jmx.remote.rmi.exporter"; //$NON-NLS-1$

    private MBeanServer mbeanServer;
    private JMXConnectorServer rmiConnectorServer;
    private RMIJRMPServerImpl rmiServerImpl;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws IOException {

        String listenSpec = (String) ctx.getProperties().get("listen"); //$NON-NLS-1$

        if ( StringUtils.isBlank(listenSpec) ) {
            log.error("listen is a required parameter"); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Launching JMX server on " + listenSpec); //$NON-NLS-1$
        }

        URI listenerUri;
        try {
            listenerUri = new URI(listenSpec.trim());
        }
        catch ( URISyntaxException e ) {
            log.warn("Failed to parse listener URI " + listenSpec, e); //$NON-NLS-1$
            return;
        }
        boolean readOnly = ConfigUtil.parseBoolean(
            ctx.getProperties(),
            "readOnly", //$NON-NLS-1$
            !JMXConstants.SOCKET_SCHEME.equals(listenerUri.getScheme()));

        String userSpec = (String) ctx.getProperties().get("username"); //$NON-NLS-1$
        String passSpec = ConfigUtil.parseSecret(ctx.getProperties(), "password", null); //$NON-NLS-1$

        boolean requireAuth = !StringUtils.isBlank(userSpec) || !StringUtils.isBlank(passSpec);
        CredentialChecker credentialChecker = null;
        if ( requireAuth ) {
            log.debug("Setting up authentication"); //$NON-NLS-1$
            credentialChecker = new SimpleCredentialChecker(userSpec, passSpec);
        }

        JMXServiceURL jmxUrl = makeJMXServiceURL(listenerUri);

        if ( log.isDebugEnabled() ) {
            log.debug("JMX URL is " + jmxUrl); //$NON-NLS-1$
        }

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            // should now properly filter themselves, but better safe than sorry
            Thread.currentThread().setContextClassLoader(createFilterClassLoader());
            Map<String, Object> env = makeEnv(listenerUri, requireAuth, credentialChecker);
            env.put(JMX_SERVER_DAEMON, Boolean.TRUE.toString());
            startRMIServer(listenerUri, jmxUrl, env, this.mbeanServer, readOnly);
        }
        catch ( Exception e ) {
            log.error("Failed to start JMX listener", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }

    }


    /**
     * @return
     */
    protected SerializationFilterClassLoader createFilterClassLoader () {
        return AccessController.doPrivileged(new PrivilegedAction<SerializationFilterClassLoader>() {

            @Override
            public SerializationFilterClassLoader run () {
                return new SerializationFilterClassLoader(JMXServerImpl.this.getClass().getClassLoader());
            }

        });

    }


    /**
     * @param listenerUri
     * @return
     * @throws IOException
     */
    protected Map<String, Object> makeEnv ( URI listenerUri, boolean requireAuth, CredentialChecker cc ) throws IOException {
        return JMXEnvironmentFactory.createServerEnv(listenerUri, null, requireAuth, cc);
    }


    @Reference
    protected synchronized void setMBeanServer ( MBeanServer mbs ) {
        this.mbeanServer = mbs;
    }


    protected synchronized void unsetMBeanServer ( MBeanServer mbs ) {
        if ( this.mbeanServer == mbs ) {
            this.mbeanServer = null;
        }
    }


    /**
     * @param listenerUri
     * @param jmxUrl
     * @param env
     */
    private void startRMIServer ( URI listenerUri, JMXServiceURL jmxUrl, Map<String, Object> env, MBeanServer mbs, boolean readOnly ) {
        log.debug("Creating RMI/JMX Server"); //$NON-NLS-1$
        try {

            String bindHost;
            int port;
            if ( JMXConstants.SOCKET_SCHEME.equals(listenerUri.getScheme()) ) {
                port = 0;
                bindHost = null;
            }
            else if ( listenerUri.getPort() > 0 ) {
                port = listenerUri.getPort();
                bindHost = listenerUri.getHost();
            }
            else {
                port = 1099;
                bindHost = listenerUri.getHost();
            }

            this.rmiServerImpl = new RMIJRMPServerWrapper(
                bindHost,
                port,
                (RMIClientSocketFactory) env.get(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE),
                (RMIServerSocketFactory) env.get(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE),
                env,
                readOnly ? EnumSet.of(JMXPermissions.READ, JMXPermissions.NOTIFY) : EnumSet.allOf(JMXPermissions.class),
                new DefaultMethodPermissionMapper());
        }
        catch ( IOException e ) {
            log.error("Failed to create RMI server", e); //$NON-NLS-1$
            return;
        }

        try {

            this.rmiConnectorServer = new RMIConnectorServer(jmxUrl, env, this.rmiServerImpl, mbs);
            this.rmiConnectorServer.start();

            if ( log.isDebugEnabled() ) {
                log.debug("Direct connector address " + this.rmiConnectorServer.getAddress()); //$NON-NLS-1$
            }
            bindToRegistry(listenerUri, env);
        }
        catch (
            IOException |
            AlreadyBoundException e ) {
            log.error("Failed to create RMI connector server", e); //$NON-NLS-1$
        }
    }


    /**
     * @param listenerUri
     * @param jmxUrl
     * @param env
     * @throws RemoteException
     * @throws AlreadyBoundException
     * @throws AccessException
     * @throws IOException
     */
    private void bindToRegistry ( URI listenerUri, Map<String, Object> env ) throws AlreadyBoundException, IOException {
        Registry registry;
        if ( JMXConstants.SOCKET_SCHEME.equals(listenerUri.getScheme()) ) {
            registry = AFUNIXNaming.getInstance(new File(listenerUri.getPath()), true).getRegistry();
            registry.bind(JMXConstants.JMXRMI, this.rmiServerImpl.toStub());
        }
        else {
            registry = new JMXRegistry(
                listenerUri.getHost(),
                listenerUri.getPort(),
                (RMIClientSocketFactory) env.get(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE),
                (RMIServerSocketFactory) env.get(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE),
                Collections.singletonMap(JMXConstants.JMXRMI, this.rmiServerImpl.toStub()));
        }
    }


    /**
     * @param listenerUri
     * @return
     */
    private static JMXServiceURL makeJMXServiceURL ( URI listenerUri ) throws MalformedURLException {
        if ( JMXConstants.SOCKET_SCHEME.equals(listenerUri.getScheme()) ) {
            return new JMXServiceURL("rmi", null, AFUNIXRMIPorts.ANONYMOUS_PORT_BASE); //$NON-NLS-1$
        }

        return new JMXServiceURL("rmi", listenerUri.getHost(), 0); //$NON-NLS-1$
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {

        log.debug("Shutting down RMI and MBean server"); //$NON-NLS-1$

        if ( this.rmiConnectorServer != null ) {
            try {
                this.rmiConnectorServer.stop();
            }
            catch ( IOException e ) {
                log.error("Failed to stop rmi connector server", e); //$NON-NLS-1$
            }

            this.rmiConnectorServer = null;
        }

        this.rmiServerImpl = null;

    }

}
