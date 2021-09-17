/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2015 by mbechler
 */
package eu.agno3.runtime.jmx;


import java.io.IOException;
import java.net.URI;
import java.rmi.NotBoundException;

import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public interface JMXClientFactory {

    /**
     * @param serverUri
     * @param tlsContext
     * @return the mbean server connection
     * @throws IOException
     * @throws NotBoundException
     */
    JMXClient getConnection ( URI serverUri, TLSContext tlsContext ) throws IOException, NotBoundException;


    /**
     * @param serverUri
     * @param autoClose
     * @param tlsContext
     * @return the mbean server connection
     * @throws IOException
     * @throws NotBoundException
     */
    JMXClient getConnection ( URI serverUri, boolean autoClose, TLSContext tlsContext ) throws IOException, NotBoundException;


    /**
     * @param serverUri
     * @param tlsContext
     * @return a connection pool
     */
    JMXConnectionPool createConnectionPool ( URI serverUri, TLSContext tlsContext );

}
