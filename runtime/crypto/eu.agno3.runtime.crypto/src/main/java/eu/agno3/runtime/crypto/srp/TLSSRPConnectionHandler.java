/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.srp;


import java.io.IOException;


/**
 * @author mbechler
 *
 */
public interface TLSSRPConnectionHandler {

    /**
     * 
     * @param s
     * @throws IOException
     */
    public void handle ( TLSSRPSocket s ) throws IOException;
}
