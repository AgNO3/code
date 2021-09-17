/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.08.2013 by mbechler
 */
package eu.agno3.runtime.db;


import java.util.Properties;

import javax.sql.DataSource;


/**
 * @author mbechler
 * 
 */
public interface DataSourceListener {

    /**
     * Called when a datasource becomes active
     * 
     * @param dsName
     * @param dsUser
     * @param ds
     * @param dsProps
     */
    void dataSourceActive ( String dsName, String dsUser, DataSource ds, Properties dsProps );


    /**
     * Called when a datasource is about to be shut down
     * 
     * 
     * @param dsName
     * @param dsUser
     * @param ds
     * @param dsProps
     */
    void dataSourceShutdown ( String dsName, String dsUser, DataSource ds, Properties dsProps );

}
