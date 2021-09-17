/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import javax.net.SocketFactory;


/**
 * @author mbechler
 *
 */
public class RMITCPClientSocketFactory implements RMIClientSocketFactory, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2379728111806780255L;


    /**
     * {@inheritDoc}
     *
     * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String, int)
     */
    @Override
    public Socket createSocket ( String host, int port ) throws IOException {
        return SocketFactory.getDefault().createSocket(host, port);
    }


    @SuppressWarnings ( "static-method" )
    private Object writeReplace () {
        return null;
    }
}
