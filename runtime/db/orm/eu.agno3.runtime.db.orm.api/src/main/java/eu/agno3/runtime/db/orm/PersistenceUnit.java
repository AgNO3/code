/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
public @interface PersistenceUnit {

    /**
     * 
     * @return name of the persistence unit
     */
    String name();


    /**
     * 
     * @return datasource to use for the persistence unit
     */
    String dataSourceName();


    /**
     * 
     * @return user to use
     */
    String dataSourceUser();


    /**
     * 
     * @return extra properties for persistence unit (format key=value)
     */
    String[] properties() default {};


    /**
     * 
     * @return shared cache mode configuration
     */
    SharedCacheMode sharedCacheMode() default SharedCacheMode.UNSPECIFIED;


    /**
     * 
     * @return transaction type configuration
     */
    PersistenceUnitTransactionType transactionType() default PersistenceUnitTransactionType.JTA;


    /**
     * 
     * @return validation mode configuration
     */
    ValidationMode validationMode() default ValidationMode.NONE;


    /**
     * 
     * @return exclude not explicitly registered classes
     */
    boolean excludeUnlistedClasses() default true;


    /**
     * 
     * @return whether to automatically apply schema changes in development
     */
    boolean autoApply() default true;

}
