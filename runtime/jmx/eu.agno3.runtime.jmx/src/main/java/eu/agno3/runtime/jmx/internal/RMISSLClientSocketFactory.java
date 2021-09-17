/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 3, 2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import javax.net.SocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public class RMISSLClientSocketFactory implements RMIClientSocketFactory, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7716523049864495954L;

    private transient final TLSContext context;


    /**
     * @param tc
     * 
     */
    public RMISSLClientSocketFactory ( TLSContext tc ) {
        this.context = tc;
    }


    @Override
    public Socket createSocket ( String host, int port ) throws IOException {
        try {
            if ( this.context == null ) {
                throw new IOException("Missing context"); //$NON-NLS-1$
            }
            final SocketFactory sslSocketFactory = this.context.getSocketFactory();
            return sslSocketFactory.createSocket(host, port);
        }
        catch ( CryptoException e ) {
            throw new IOException("Failed to create TLS socket", e); //$NON-NLS-1$
        }
    }


    @SuppressWarnings ( "static-method" )
    private Object writeReplace () {
        return new SslRMIClientSocketFactory();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.context.hashCode();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        return obj instanceof RMISSLClientSocketFactory && ( (RMISSLClientSocketFactory) obj ).context.equals(this.context);
    }
}
