/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.08.2013 by mbechler
 */
package eu.agno3.runtime.db;


/**
 * @author mbechler
 * 
 */
public interface DataSourceMetaData {

    /**
     * Get the default catalgo used in this datasource
     * 
     * @return catalog name, null if not applicable
     */
    String getDefaultCatalog ();


    /**
     * Get the default schema used in this datasource
     * 
     * @return schema name, null if not applicable
     */
    String getDefaultSchema ();


    /**
     * @return class name of hibernate dialect
     */
    String getHibernateDialect ();


    /**
     * @return the class name of the JDBC driver used
     */
    String getDriverClassName ();
}
