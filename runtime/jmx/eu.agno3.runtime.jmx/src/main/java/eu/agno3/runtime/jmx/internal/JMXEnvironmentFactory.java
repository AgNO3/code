/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.rmi.RMIConnectorServer;

import org.newsclub.net.unix.rmi.AFUNIXRMISocketFactory;

import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.jmx.CredentialChecker;


/**
 * @author mbechler
 *
 */
public class JMXEnvironmentFactory {

    /**
     * 
     * @param listenerUri
     * @param tlsContext
     * @param requireAuth
     * @param cc
     * @return the environment
     * @throws IOException
     */
    public static Map<String, Object> createServerEnv ( URI listenerUri, TLSContext tlsContext, boolean requireAuth, CredentialChecker cc )
            throws IOException {
        return createEnv(listenerUri, tlsContext, true, requireAuth, cc);
    }


    /**
     * @param listenerUri
     * @param tlsContext
     * @return the environment
     * @throws IOException
     */
    public static Map<String, Object> createClientEnv ( URI listenerUri, TLSContext tlsContext ) throws IOException {
        return createEnv(listenerUri, tlsContext, false, false, null);
    }


    /**
     * @param listenerUri
     * @param tlsContext
     * @param server
     * @return the environment
     * @throws IOException
     */
    private static Map<String, Object> createEnv ( URI listenerUri, TLSContext tlsContext, boolean server, boolean requireAuth, CredentialChecker cc )
            throws IOException {

        Map<String, Object> env = new HashMap<>();

        if ( JMXConstants.SOCKET_SCHEME.equals(listenerUri.getScheme()) ) {
            setupUnixSocketFactory(listenerUri, env, server);
        }
        else if ( JMXConstants.SSL_SCHEME.equals(listenerUri.getScheme()) ) {
            setupSSLSocketFactory(listenerUri, tlsContext, env, server, requireAuth, cc);
        }
        else {
            setupTCPSocketFactory(listenerUri, env, server, requireAuth, cc);
        }
        return env;
    }


    private static void setupTCPSocketFactory ( URI listenerUri, Map<String, Object> env, boolean server, boolean requireAuth,
            CredentialChecker cc ) {
        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new RMITCPClientSocketFactory());
        if ( server ) {
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, new RMITCPServerSocketFactory(listenerUri.getHost()));
            env.put(JMXConnectorServer.AUTHENTICATOR, new JMXTCPAuthenticator(!requireAuth, cc));
        }
    }


    /**
     * @param listenerUri
     * @param tlsContext
     * @param env
     * @param server
     * @throws IOException
     */
    private static void setupSSLSocketFactory ( URI listenerUri, TLSContext tlsContext, Map<String, Object> env, boolean server, boolean requireAuth,
            CredentialChecker cc ) throws IOException {

        if ( tlsContext == null ) {
            throw new IOException("Cannot create SSL sockets without a TLSContext"); //$NON-NLS-1$
        }

        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new RMISSLClientSocketFactory(tlsContext));

        if ( server ) {
            try {
                env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, new RMISSLServerSocketFactory(tlsContext, listenerUri.getHost()));
            }
            catch ( IllegalArgumentException e ) {
                throw new IOException("Failed to initialize ssl socket factory", e); //$NON-NLS-1$
            }

            env.put(JMXConnectorServer.AUTHENTICATOR, new JMXSSLAuthenticator(!requireAuth, cc));
        }
    }


    /**
     * @param listenerUri
     * @param env
     * @param server
     * @throws IOException
     */
    private static void setupUnixSocketFactory ( URI listenerUri, Map<String, Object> env, boolean server ) throws IOException {
        File socketDir = new File(listenerUri.getPath());
        if ( !socketDir.exists() ) {
            socketDir.mkdirs();
        }
        AFUNIXRMISocketFactory sockFactory = new AFUNIXRMISocketFactory(socketDir, server);
        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, sockFactory);
        if ( server ) {
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, sockFactory);
            env.put(JMXConnectorServer.AUTHENTICATOR, new JMXSocketAuthenticator());
        }
    }

}
