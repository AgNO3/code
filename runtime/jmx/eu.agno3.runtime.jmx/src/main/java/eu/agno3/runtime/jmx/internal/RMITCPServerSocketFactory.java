/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 3, 2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Objects;


/**
 * @author mbechler
 *
 */
public class RMITCPServerSocketFactory implements RMIServerSocketFactory {

    protected final String bindAddress;


    /**
     * @param bindAddress
     */
    public RMITCPServerSocketFactory ( String bindAddress ) {
        this.bindAddress = bindAddress;
    }


    @Override
    public ServerSocket createServerSocket ( int port ) throws IOException {
        if ( this.bindAddress == null ) {
            return new ServerSocket(port);
        }

        try {
            InetAddress addr = InetAddress.getByName(this.bindAddress);
            return new ServerSocket(port, 0, addr);
        }
        catch ( UnknownHostException e ) {
            throw new IOException("Failed to determine bind address", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.bindAddress != null ? this.bindAddress.hashCode() : 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        return obj instanceof RMITCPServerSocketFactory && Objects.equals( ( (RMITCPServerSocketFactory) obj ).bindAddress, this.bindAddress);
    }
}
