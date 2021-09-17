/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.DataSourceMetaData;
import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;
import eu.agno3.runtime.db.schema.SchemaManagedDataSource;


/**
 * @author mbechler
 * 
 */
@Component (
    service = BasePersistenceUnitInfo.class,
    configurationPid = BasePersistenceUnitInfo.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class BasePersistenceUnitInfo implements PersistenceUnitInfo {

    /**
     * 
     */
    private static final String VALIDATE = "validate"; //$NON-NLS-1$

    private static final String PERSISTENCE_XML_SCHEMA_VERSION = "2.1"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String PID = "eu.agno3.runtime.db.orm.internal.BasePersistenceUnitInfo"; //$NON-NLS-1$

    private PersistenceUnitDescriptor descriptor;
    private DataSource dataSource;
    private DataSourceUtil dsUtil;


    @Reference
    protected synchronized void setPersistenceUnitDescriptor ( PersistenceUnitDescriptor desc ) {
        this.descriptor = desc;
    }


    protected synchronized void unsetPersistenceUnitDescriptor ( PersistenceUnitDescriptor desc ) {
        if ( this.descriptor == desc ) {
            this.descriptor = null;
        }
    }


    @Reference
    protected synchronized void setDataSource ( SchemaManagedDataSource ds ) {
        this.dataSource = ds;
    }


    protected synchronized void unsetDataSource ( SchemaManagedDataSource ds ) {
        if ( this.dataSource == ds ) {
            this.dataSource = null;
        }
    }


    @Reference
    protected synchronized void setDsUtil ( DataSourceUtil util ) {
        this.dsUtil = util;
    }


    protected synchronized void unsetDsUtil ( DataSourceUtil util ) {
        if ( this.dsUtil == util ) {
            this.dsUtil = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#addTransformer(javax.persistence.spi.ClassTransformer)
     */
    @Override
    public void addTransformer ( ClassTransformer arg0 ) {
        throw new IllegalArgumentException("Unimplemented"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#excludeUnlistedClasses()
     */
    @Override
    public boolean excludeUnlistedClasses () {
        return this.descriptor.isExcludeUnlistedClasses();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getClassLoader()
     */
    @Override
    public ClassLoader getClassLoader () {
        return this.descriptor.getBaseClassLoader();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getJarFileUrls()
     */
    @Override
    public List<URL> getJarFileUrls () {
        return new ArrayList<>();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getJtaDataSource()
     */
    @Override
    public DataSource getJtaDataSource () {
        return this.dataSource;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getManagedClassNames()
     */
    @Override
    public List<String> getManagedClassNames () {
        return new ArrayList<>();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getMappingFileNames()
     */
    @Override
    public List<String> getMappingFileNames () {
        return new ArrayList<>();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getNewTempClassLoader()
     */
    @Override
    public ClassLoader getNewTempClassLoader () {
        return this.descriptor.getBaseClassLoader();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getNonJtaDataSource()
     */
    @Override
    public DataSource getNonJtaDataSource () {
        return this.dataSource;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getPersistenceProviderClassName()
     */
    @Override
    public String getPersistenceProviderClassName () {
        return HibernatePersistenceProvider.class.getName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getPersistenceUnitName()
     */
    @Override
    public String getPersistenceUnitName () {
        return this.descriptor.getPersistenceUnitName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getPersistenceUnitRootUrl()
     */
    @Override
    public URL getPersistenceUnitRootUrl () {
        return this.getClass().getResource("/empty.jar"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getPersistenceXMLSchemaVersion()
     */
    @Override
    public String getPersistenceXMLSchemaVersion () {
        return PERSISTENCE_XML_SCHEMA_VERSION;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getProperties()
     */
    @Override
    public Properties getProperties () {
        Properties baseProps = new Properties();

        baseProps.setProperty("dataSourceName", this.descriptor.getDataSourceName()); //$NON-NLS-1$

        if ( this.descriptor.getDataSourceUser() != null ) {
            baseProps.setProperty("dataSourceUser", this.descriptor.getDataSourceUser()); //$NON-NLS-1$
        }
        baseProps.setProperty(AvailableSettings.HBM2DDL_AUTO, VALIDATE);
        baseProps.setProperty(AvailableSettings.GENERATE_STATISTICS, Boolean.TRUE.toString());

        // disable for HHH-11230
        // baseProps.setProperty("hibernate.collection_join_subquery", Boolean.FALSE.toString()); //$NON-NLS-1$

        DataSourceMetaData dbMeta = this.dsUtil.createMetadata();

        if ( dbMeta.getDefaultCatalog() != null ) {
            baseProps.setProperty(AvailableSettings.DEFAULT_CATALOG, dbMeta.getDefaultCatalog());
        }

        if ( dbMeta.getDefaultSchema() != null ) {
            baseProps.setProperty(AvailableSettings.DEFAULT_SCHEMA, dbMeta.getDefaultSchema());
        }

        baseProps.setProperty(AvailableSettings.DIALECT, dbMeta.getHibernateDialect());
        baseProps.putAll(this.descriptor.getProperties());
        return baseProps;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getSharedCacheMode()
     */
    @Override
    public SharedCacheMode getSharedCacheMode () {
        return this.descriptor.getSharedCacheMode();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getTransactionType()
     */
    @Override
    public PersistenceUnitTransactionType getTransactionType () {
        return this.descriptor.getTransactionType();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.persistence.spi.PersistenceUnitInfo#getValidationMode()
     */
    @Override
    public ValidationMode getValidationMode () {
        return this.descriptor.getValidationMode();
    }


    /**
     * @return the descriptor
     */
    public PersistenceUnitDescriptor getDescriptor () {
        return this.descriptor;
    }

}
