/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2013 by mbechler
 */
package eu.agno3.runtime.db.embedded;


import java.sql.SQLException;

import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 * 
 */
public interface EmbeddedDBServer extends DataSourceFactory {

    /**
     * Start the server
     * 
     * @param bundleContext
     * 
     * @throws SQLException
     * @throws CryptoException
     */
    void start () throws SQLException, CryptoException;


    /**
     * Stop the server
     * 
     * @throws SQLException
     * @throws CryptoException
     */
    void stop () throws SQLException, CryptoException;

}
