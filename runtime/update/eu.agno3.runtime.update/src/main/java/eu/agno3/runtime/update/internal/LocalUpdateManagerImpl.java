/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.net.URI;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.jmx.MBean;
import eu.agno3.runtime.update.Feature;
import eu.agno3.runtime.update.FeatureUpdate;
import eu.agno3.runtime.update.LocalUpdateManager;
import eu.agno3.runtime.update.LocalUpdateManagerMBean;
import eu.agno3.runtime.update.UpdateException;
import eu.agno3.runtime.update.UpdateManager;
import eu.agno3.runtime.update.UpdateManagerProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = MBean.class, property = {
    "objectName=eu.agno3.runtime.update:type=UpdateManager"
} )
public class LocalUpdateManagerImpl extends LocalUpdateManager implements LocalUpdateManagerMBean, MBean {

    private UpdateManagerProvider updateManagerProvider;


    @Reference
    protected synchronized void setUpdateManagerProvider ( UpdateManagerProvider ump ) {
        this.updateManagerProvider = ump;
    }


    protected synchronized void unsetUpdateManagerProvider ( UpdateManagerProvider ump ) {
        if ( this.updateManagerProvider == ump ) {
            this.updateManagerProvider = null;
        }
    }


    /**
     * @return the updateManager
     * @throws UpdateException
     */
    private UpdateManager getUpdateManager () throws UpdateException {
        return this.updateManagerProvider.getLocalUpdateManager();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#listRepositories()
     */
    @Override
    public URI[] listRepositories () throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            return updateManager.listRepositories();
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#listInstalledFeatures()
     */
    @Override
    public Set<Feature> listInstalledFeatures () throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            return updateManager.listInstalledFeatures();
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#installAllUpdates(java.util.Set)
     */
    @Override
    public void installAllUpdates ( Set<URI> repositories ) throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            updateManager.installAllUpdates(repositories);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#installUpdates(java.util.Set, java.util.Set)
     */
    @Override
    public void installUpdates ( Set<Feature> updates, Set<URI> repositories ) throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            updateManager.installUpdates(updates, repositories, new JMXProgressMonitor(this));
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#checkForUpdates()
     */
    @Override
    public Set<FeatureUpdate> checkForUpdates () throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            return updateManager.checkForUpdates(new JMXProgressMonitor(this));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UpdateException
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#prepareUpdate(java.util.Set, java.util.Set)
     */
    @Override
    public boolean prepareUpdate ( Set<URI> repositories, Set<Feature> targets ) throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            return updateManager.prepareUpdate(repositories, targets, new JMXProgressMonitor(this));
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#runGarbageCollection()
     */
    @Override
    public void runGarbageCollection () throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            updateManager.runGarbageCollection();
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#installFeature(java.lang.String)
     */
    @Override
    public void installFeature ( String spec ) throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            updateManager.installFeature(spec, new JMXProgressMonitor(this));
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#removeFeature(java.lang.String)
     */
    @Override
    public void removeFeature ( String spec ) throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            updateManager.removeFeature(spec, new JMXProgressMonitor(this));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#getInstallableFeatures()
     */
    @Override
    public Set<Feature> getInstallableFeatures () throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            return updateManager.getInstallableFeatures();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#doApplyUpdate()
     */
    @Override
    public void doApplyUpdate () throws UpdateException {
        try ( UpdateManager updateManager = this.getUpdateManager() ) {
            updateManager.doApplyUpdate();
        }
    }

}
