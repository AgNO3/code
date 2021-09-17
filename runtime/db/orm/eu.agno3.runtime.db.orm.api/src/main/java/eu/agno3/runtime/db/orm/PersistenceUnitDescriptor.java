/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm;


import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;


/**
 * @author mbechler
 * 
 */
public interface PersistenceUnitDescriptor {

    /**
     * @return datasource to use for this persistence unit
     */
    String getDataSourceName ();


    /**
     * @return the user to use for database connections
     */
    String getDataSourceUser ();


    /**
     * @return data source type to use
     */
    String getDataSourceType ();


    /**
     * @return name of this persistence unit
     */
    String getPersistenceUnitName ();


    /**
     * @return extra properties
     */
    Properties getProperties ();


    /**
     * @return shared cache mode for this persistence unit
     * @see javax.persistence.SharedCacheMode
     */
    SharedCacheMode getSharedCacheMode ();


    /**
     * @return transaction type for this persistence unit
     * @see javax.persistence.spi.PersistenceUnitTransactionType
     */
    PersistenceUnitTransactionType getTransactionType ();


    /**
     * @return validation mode for this persistence unit
     * @see javax.persistence.ValidationMode
     */
    ValidationMode getValidationMode ();


    /**
     * @return the base classloader for this persistence unit
     */
    ClassLoader getBaseClassLoader ();


    /**
     * 
     * @return whether to exclude classes not explicitly registered
     */
    boolean isExcludeUnlistedClasses ();


    /**
     * 
     * @return whether the schema for this persistence unit should be automatically updated (in development)
     */
    boolean isAutoApply ();

}
