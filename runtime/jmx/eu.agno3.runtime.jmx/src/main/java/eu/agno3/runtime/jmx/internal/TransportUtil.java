/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.newsclub.net.unix.rmi.AFUNIXRMISocketFactory;

import eu.agno3.runtime.util.net.LocalHostUtil;

import sun.rmi.transport.tcp.TCPEndpoint;
import sun.rmi.transport.tcp.TCPTransport;


/**
 * @author mbechler
 *
 */
public final class TransportUtil {

    private static final Logger log = Logger.getLogger(TransportUtil.class);


    /**
     * 
     */
    private TransportUtil () {}

    private static Constructor<TCPTransport> TRANSPORT_CONS;
    private static Field ENDP_LISTEN_PORT;
    private static Field ENDP_TRANSPORT;
    private static Field ENDP_TABLE;


    static {

        try {
            TRANSPORT_CONS = TCPTransport.class.getDeclaredConstructor(LinkedList.class);
            TRANSPORT_CONS.setAccessible(true);

            ENDP_LISTEN_PORT = TCPEndpoint.class.getDeclaredField("listenPort"); //$NON-NLS-1$
            ENDP_LISTEN_PORT.setAccessible(true);

            ENDP_TRANSPORT = TCPEndpoint.class.getDeclaredField("transport"); //$NON-NLS-1$
            ENDP_TRANSPORT.setAccessible(true);

            ENDP_TABLE = TCPEndpoint.class.getDeclaredField("localEndpoints"); //$NON-NLS-1$
            ENDP_TABLE.setAccessible(true);
        }
        catch (
            NoSuchMethodException |
            SecurityException |
            NoSuchFieldException e ) {
            log.error("Incompatible VM", e); //$NON-NLS-1$
        }
    }


    /**
     * @param host
     * @param port
     * @param csf
     * @param ssf
     * @return existing on new transport
     * @throws IOException
     */
    public static TCPEndpoint getOrCreateTransport ( String host, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf )
            throws IOException {
        try {
            String exportAddr = null;
            if ( ! ( csf instanceof AFUNIXRMISocketFactory ) && ! ( ssf instanceof AFUNIXRMISocketFactory ) ) {
                if ( StringUtils.isBlank(host) || "0.0.0.0".equals(host) ) { //$NON-NLS-1$
                    exportAddr = LocalHostUtil.guessPrimaryAddress().getHostAddress();
                }
                else {
                    try {
                        exportAddr = InetAddress.getByName(host).getHostAddress();
                    }
                    catch ( UnknownHostException e ) {
                        log.warn("Failed to get listener export address " + host, e); //$NON-NLS-1$
                        exportAddr = host;
                    }
                }
            }
            else {
                return TCPEndpoint.getLocalEndpoint(port, csf, ssf);
            }

            @SuppressWarnings ( "unchecked" )
            Map<TCPEndpoint, LinkedList<TCPEndpoint>> eps = (Map<TCPEndpoint, LinkedList<TCPEndpoint>>) ENDP_TABLE.get(null);
            TCPEndpoint ep = new TCPEndpoint(exportAddr, port, csf, ssf);
            synchronized ( eps ) {
                if ( !eps.containsKey(ep) ) {
                    log.debug(String.format("Creating new transport on %s:%d (bind to %s)", exportAddr, port, host)); //$NON-NLS-1$
                    LinkedList<TCPEndpoint> epList = new LinkedList<>();
                    epList.add(ep);
                    ENDP_LISTEN_PORT.set(ep, port);
                    ENDP_TRANSPORT.set(ep, TRANSPORT_CONS.newInstance(epList));
                    eps.put(ep, epList);
                    return ep;
                }
                return eps.get(ep).getLast();
            }
        }
        catch ( Exception e ) {
            throw new IOException("Failed to create TCP endpoint", e); //$NON-NLS-1$
        }
    }
}
