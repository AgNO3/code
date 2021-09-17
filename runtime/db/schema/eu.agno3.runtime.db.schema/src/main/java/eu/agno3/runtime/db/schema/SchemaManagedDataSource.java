/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema;


import javax.sql.DataSource;

import eu.agno3.runtime.db.DataSourceUtil;


/**
 * @author mbechler
 * 
 */
public interface SchemaManagedDataSource extends DataSource {

    /**
     * Ensures the schema is up to date, possibly performing updates
     * 
     * @throws SchemaException
     */
    void ensureUpToDate () throws SchemaException;


    /**
     * @return the schema manager
     */
    SchemaManager getSchemaManager ();


    /**
     * @return the data source util
     */
    DataSourceUtil getDataSourceUtil ();
}
