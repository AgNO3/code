/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2015 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.util.Map;

import javax.management.remote.rmi.RMIConnector;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIServer;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.newsclub.net.unix.rmi.AFUNIXNaming;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.jmx.JMXClient;
import eu.agno3.runtime.jmx.JMXClientFactory;
import eu.agno3.runtime.jmx.JMXConnectionPool;
import eu.agno3.runtime.util.serialization.SerializationFilterClassLoader;


/**
 * @author mbechler
 *
 */
@Component ( service = JMXClientFactory.class )
public class JMXClientFactoryImpl implements JMXClientFactory {

    @Override
    public JMXClient getConnection ( URI serverUri, TLSContext tlsContext ) throws IOException, NotBoundException {
        return this.getConnection(serverUri, true, tlsContext);
    }


    @Override
    public JMXConnectionPool createConnectionPool ( URI serverUri, TLSContext tlsContext ) {
        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        cfg.setJmxEnabled(true);
        cfg.setJmxNamePrefix("jmx-" + serverUri); //$NON-NLS-1$
        cfg.setBlockWhenExhausted(false);
        cfg.setMaxTotal(5);
        cfg.setMaxWaitMillis(1000);
        cfg.setTestOnBorrow(true);
        cfg.setTestOnReturn(true);
        return new JMXConnectionPoolImpl(this, serverUri, tlsContext, cfg);
    }


    @SuppressWarnings ( "resource" )
    @Override
    public JMXClient getConnection ( URI serverUri, boolean autoClose, TLSContext tlsContext ) throws IOException, NotBoundException {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(new SerializationFilterClassLoader(oldTCCL));
            Map<String, Object> env = JMXEnvironmentFactory.createClientEnv(serverUri, tlsContext);
            Registry registry = getRegistry(serverUri, env);
            Remote lookup = registry.lookup(JMXConstants.JMXRMI);
            RMIConnector conn = new RMIConnector((RMIServer) lookup, env);
            return new JMXClientWrapper(conn, autoClose);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * @param serverUri
     * @param env
     * @return
     * @throws RemoteException
     */
    private static Registry getRegistry ( URI serverUri, Map<String, Object> env ) throws RemoteException {
        Registry registry;
        if ( JMXConstants.SOCKET_SCHEME.equals(serverUri.getScheme()) ) {
            registry = AFUNIXNaming.getInstance(new File(serverUri.getPath()), false).getRegistry();
        }
        else {
            registry = LocateRegistry.getRegistry(
                serverUri.getHost(),
                serverUri.getPort(),
                (RMIClientSocketFactory) env.get(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE));
        }
        return registry;
    }

}
