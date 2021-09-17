/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.12.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;


/**
 * @author mbechler
 *
 */
@Component (
    service = PersistenceUnitDescriptor.class,
    configurationPid = ConfiguredPersistenceUnitDescriptor.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ConfiguredPersistenceUnitDescriptor implements PersistenceUnitDescriptor {

    private static final String TRANSACTION_TYPE = "transactionType"; //$NON-NLS-1$
    private static final String SHARED_CACHE_MODE = "sharedCacheMode"; //$NON-NLS-1$
    private static final String VALIDATION_MODE = "validationMode"; //$NON-NLS-1$
    private static final String EXCLUDE_UNLISTED_CLASSES = "excludeUnlistedClasses"; //$NON-NLS-1$
    private static final String AUTO_APPLY = "autoApply"; //$NON-NLS-1$
    private static final String DATA_SOURCE_USER = "dataSourceUser"; //$NON-NLS-1$
    private static final String DATA_SOURCE_NAME = "dataSourceName"; //$NON-NLS-1$
    private static final String PERSISTENCE_UNIT = "persistenceUnit"; //$NON-NLS-1$

    private static final Set<String> ALL_PROPERTIES = new HashSet<>();
    private static final Set<String> OSGI_PROPERTIES = new HashSet<>();

    static {
        ALL_PROPERTIES.add(TRANSACTION_TYPE);
        ALL_PROPERTIES.add(SHARED_CACHE_MODE);
        ALL_PROPERTIES.add(VALIDATION_MODE);
        ALL_PROPERTIES.add(EXCLUDE_UNLISTED_CLASSES);
        ALL_PROPERTIES.add(AUTO_APPLY);
        ALL_PROPERTIES.add(DATA_SOURCE_USER);
        ALL_PROPERTIES.add(DATA_SOURCE_NAME);
        ALL_PROPERTIES.add(PERSISTENCE_UNIT);
        OSGI_PROPERTIES.add(Constants.SERVICE_ID);
        OSGI_PROPERTIES.add(Constants.SERVICE_PID);
        OSGI_PROPERTIES.add(Constants.SERVICE_ID);
    }

    /**
     * 
     */
    public static final String PID = "db.orm"; //$NON-NLS-1$
    private boolean autoApply = true;
    private boolean pooled = true;
    private boolean excludeUnlistedClasses = true;
    private ValidationMode validationMode = ValidationMode.NONE;
    private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.JTA;
    private SharedCacheMode sharedCacheMode = SharedCacheMode.UNSPECIFIED;
    private Properties extraProperties = new Properties();

    private String puName;
    private String dsName;
    private String dsUser; // $NON-NLS-1$


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        String puNameSpec = (String) ctx.getProperties().get(PERSISTENCE_UNIT);
        if ( StringUtils.isBlank(puNameSpec) ) {
            throw new PersistenceException("persistenceUnit property is required"); //$NON-NLS-1$
        }
        this.puName = puNameSpec.trim();

        String dsNameSpec = (String) ctx.getProperties().get(DATA_SOURCE_NAME);
        if ( StringUtils.isBlank(dsNameSpec) ) {
            throw new PersistenceException("dataSourceName property is required"); //$NON-NLS-1$
        }
        this.dsName = dsNameSpec.trim();

        String dsUserSpec = (String) ctx.getProperties().get(DATA_SOURCE_USER);
        if ( !StringUtils.isBlank(dsUserSpec) ) {
            this.dsUser = dsUserSpec.trim();
        }

        this.parseOptions(ctx);
        this.parseExtraProperties(ctx);
    }


    /**
     * @param ctx
     */
    private void parseExtraProperties ( ComponentContext ctx ) {
        Properties props = new Properties();

        Enumeration<String> keys = ctx.getProperties().keys();

        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();

            if ( ALL_PROPERTIES.contains(key) || OSGI_PROPERTIES.contains(key) ) {
                continue;
            }

            Object value = ctx.getProperties().get(key);
            props.setProperty(key, value.toString());
        }

        this.extraProperties = props;
    }


    /**
     * @param ctx
     */
    private void parseOptions ( ComponentContext ctx ) {
        String autoApplySpec = (String) ctx.getProperties().get(AUTO_APPLY);
        if ( !StringUtils.isBlank(autoApplySpec) ) {
            this.autoApply = Boolean.parseBoolean(autoApplySpec.trim());
        }

        String excludeUnlistedClassesSpec = (String) ctx.getProperties().get(EXCLUDE_UNLISTED_CLASSES);
        if ( !StringUtils.isBlank(excludeUnlistedClassesSpec) ) {
            this.excludeUnlistedClasses = Boolean.parseBoolean(excludeUnlistedClassesSpec.trim());
        }

        String validationModeSpec = (String) ctx.getProperties().get(VALIDATION_MODE);
        if ( !StringUtils.isBlank(validationModeSpec) ) {
            this.validationMode = ValidationMode.valueOf(validationModeSpec.trim().toUpperCase());
        }

        String sharedCacheModeSpec = (String) ctx.getProperties().get(SHARED_CACHE_MODE);
        if ( !StringUtils.isBlank(sharedCacheModeSpec) ) {
            this.sharedCacheMode = SharedCacheMode.valueOf(sharedCacheModeSpec.trim().toUpperCase());
        }

        String transactionTypeSpec = (String) ctx.getProperties().get(TRANSACTION_TYPE);
        if ( !StringUtils.isBlank(transactionTypeSpec) ) {
            this.transactionType = PersistenceUnitTransactionType.valueOf(transactionTypeSpec.trim().toUpperCase());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getDataSourceName()
     */
    @Override
    public String getDataSourceName () {
        return this.dsName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getDataSourceUser()
     */
    @Override
    public String getDataSourceUser () {
        return this.dsUser;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getDataSourceType()
     */
    @Override
    public String getDataSourceType () {
        return getTransactionType() == PersistenceUnitTransactionType.JTA ? "xa" : //$NON-NLS-1$
                ( this.pooled ? "pooled" : //$NON-NLS-1$
                        "plain" ); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getPersistenceUnitName()
     */
    @Override
    public String getPersistenceUnitName () {
        return this.puName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getProperties()
     */
    @Override
    public Properties getProperties () {
        return this.extraProperties;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getSharedCacheMode()
     */
    @Override
    public SharedCacheMode getSharedCacheMode () {
        return this.sharedCacheMode;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getTransactionType()
     */
    @Override
    public PersistenceUnitTransactionType getTransactionType () {
        return this.transactionType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#getValidationMode()
     */
    @Override
    public ValidationMode getValidationMode () {
        return this.validationMode;
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
        return this.excludeUnlistedClasses;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.PersistenceUnitDescriptor#isAutoApply()
     */
    @Override
    public boolean isAutoApply () {
        return this.autoApply;
    }

}
