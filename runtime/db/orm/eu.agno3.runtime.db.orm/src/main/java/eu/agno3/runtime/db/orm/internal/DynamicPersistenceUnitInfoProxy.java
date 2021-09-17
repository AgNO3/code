/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.07.2013 by mbechler
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


/**
 * @author mbechler
 * 
 */
public class DynamicPersistenceUnitInfoProxy implements PersistenceUnitInfo {

    private PersistenceUnitInfo proxied;
    private ClassLoader currentClassloader;


    /**
     * @param proxied
     */
    public DynamicPersistenceUnitInfoProxy ( PersistenceUnitInfo proxied ) {
        this.proxied = proxied;
    }


    @Override
    public String getPersistenceUnitName () {
        return this.proxied.getPersistenceUnitName();
    }


    @Override
    public String getPersistenceProviderClassName () {
        return this.proxied.getPersistenceProviderClassName();
    }


    @Override
    public PersistenceUnitTransactionType getTransactionType () {
        return this.proxied.getTransactionType();
    }


    @Override
    public DataSource getJtaDataSource () {
        return this.proxied.getJtaDataSource();
    }


    @Override
    public DataSource getNonJtaDataSource () {
        return this.proxied.getNonJtaDataSource();
    }


    @Override
    public List<String> getMappingFileNames () {
        return this.proxied.getMappingFileNames();
    }


    @Override
    public List<URL> getJarFileUrls () {
        return new ArrayList<>();
    }


    @Override
    public URL getPersistenceUnitRootUrl () {
        return this.getClass().getResource("/empty.jar"); //$NON-NLS-1$
    }


    @Override
    public List<String> getManagedClassNames () {
        return this.proxied.getManagedClassNames();
    }


    @Override
    public boolean excludeUnlistedClasses () {
        return true;
    }


    @Override
    public SharedCacheMode getSharedCacheMode () {
        return this.proxied.getSharedCacheMode();
    }


    @Override
    public ValidationMode getValidationMode () {
        return this.proxied.getValidationMode();
    }


    @Override
    public Properties getProperties () {
        return this.proxied.getProperties();
    }


    @Override
    public String getPersistenceXMLSchemaVersion () {
        return this.proxied.getPersistenceXMLSchemaVersion();
    }


    @Override
    public ClassLoader getClassLoader () {
        if ( this.currentClassloader == null ) {
            return this.proxied.getClassLoader();
        }
        return this.currentClassloader;
    }


    /**
     * @param classloader
     *            active classloader
     */
    public void setClassLoader ( ClassLoader classloader ) {
        this.currentClassloader = classloader;
    }


    @Override
    public void addTransformer ( ClassTransformer transformer ) {
        this.proxied.addTransformer(transformer);
    }


    @Override
    public ClassLoader getNewTempClassLoader () {
        if ( this.currentClassloader == null ) {
            return this.proxied.getClassLoader();
        }
        return this.currentClassloader;
    }

}
