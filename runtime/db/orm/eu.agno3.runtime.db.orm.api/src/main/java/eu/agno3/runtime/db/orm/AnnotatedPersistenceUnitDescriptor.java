/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm;


import java.util.Properties;
import java.util.regex.Pattern;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;

import eu.agno3.runtime.db.DatabaseConfigurationException;


/**
 * @author mbechler
 * 
 */
public class AnnotatedPersistenceUnitDescriptor implements PersistenceUnitDescriptor {

    PersistenceUnit pu;


    /**
     * @throws DatabaseConfigurationException
     */
    public AnnotatedPersistenceUnitDescriptor () throws DatabaseConfigurationException {
        this.pu = this.getClass().getAnnotation(PersistenceUnit.class);

        if ( this.pu == null ) {
            throw new DatabaseConfigurationException("No PersistenceUnit annotation specified"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getDataSourceName()
     */
    @Override
    public String getDataSourceName () {
        return this.pu.dataSourceName();
    }


    @Override
    public String getDataSourceUser () {
        return this.pu.dataSourceUser();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getProperties()
     */
    @Override
    public Properties getProperties () {
        Properties props = new Properties();

        for ( String propSpec : this.pu.properties() ) {
            String[] parts = propSpec.split(Pattern.quote("="), 2); //$NON-NLS-1$

            if ( parts.length != 2 ) {
                throw new IllegalArgumentException("Property specification invalid: " + propSpec); //$NON-NLS-1$
            }

            props.put(parts[ 0 ], parts[ 1 ]);
        }

        return props;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getSharedCacheMode()
     */
    @Override
    public SharedCacheMode getSharedCacheMode () {
        return this.pu.sharedCacheMode();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getTransactionType()
     */
    @Override
    public PersistenceUnitTransactionType getTransactionType () {
        return this.pu.transactionType();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getDataSourceType()
     */
    @Override
    public String getDataSourceType () {
        return getTransactionType() == PersistenceUnitTransactionType.JTA ? "xa" : //$NON-NLS-1$
                "plain"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getValidationMode()
     */
    @Override
    public ValidationMode getValidationMode () {
        return this.pu.validationMode();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getPersistenceUnitName()
     */
    @Override
    public String getPersistenceUnitName () {
        return this.pu.name();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getBaseClassLoader()
     */
    @Override
    public ClassLoader getBaseClassLoader () {
        return this.getClass().getClassLoader();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#isExcludeUnlistedClasses()
     */
    @Override
    public boolean isExcludeUnlistedClasses () {
        return this.pu.excludeUnlistedClasses();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#isAutoApply()
     */
    @Override
    public boolean isAutoApply () {
        return this.pu.autoApply();
    }

}
