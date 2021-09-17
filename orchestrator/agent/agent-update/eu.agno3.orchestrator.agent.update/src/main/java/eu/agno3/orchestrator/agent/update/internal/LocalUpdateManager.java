/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.net.URI;
import java.util.Set;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.eclipse.core.runtime.IProgressMonitor;

import eu.agno3.runtime.update.Feature;
import eu.agno3.runtime.update.FeatureUpdate;
import eu.agno3.runtime.update.UpdateException;
import eu.agno3.runtime.update.UpdateManager;


/**
 * @author mbechler
 *
 */
public class LocalUpdateManager implements ClosableUpdateManager {

    private UpdateManager updateManager;


    /**
     * @param updateManager
     */
    public LocalUpdateManager ( UpdateManager updateManager ) {
        this.updateManager = updateManager;
    }


    @Override
    public URI[] listRepositories () throws UpdateException {
        return this.updateManager.listRepositories();
    }


    @Override
    public Set<Feature> listInstalledFeatures () throws UpdateException {
        return this.updateManager.listInstalledFeatures();
    }


    @Override
    public void installAllUpdates ( Set<URI> repositories ) throws UpdateException {
        this.updateManager.installAllUpdates(repositories);
    }


    @Override
    public void installUpdates ( Set<Feature> updates, Set<URI> repositories ) throws UpdateException {
        this.updateManager.installUpdates(updates, repositories);
        this.updateManager.doApplyUpdate();
    }


    @Override
    public Set<FeatureUpdate> checkForUpdates () throws UpdateException {
        return this.updateManager.checkForUpdates();
    }


    @Override
    public void runGarbageCollection () throws UpdateException {
        this.updateManager.runGarbageCollection();
    }


    @Override
    public Set<Feature> getInstallableFeatures () throws UpdateException {
        return this.updateManager.getInstallableFeatures();
    }


    @Override
    public void installFeature ( String spec ) throws UpdateException {
        this.updateManager.installFeature(spec);
    }


    @Override
    public void removeFeature ( String spec ) throws UpdateException {
        this.updateManager.removeFeature(spec);
    }


    @Override
    public void installAllUpdates ( Set<URI> repositories, IProgressMonitor monitor ) throws UpdateException {
        this.updateManager.installAllUpdates(repositories, monitor);
    }


    @Override
    public void installUpdates ( Set<Feature> updates, Set<URI> repositories, IProgressMonitor monitor ) throws UpdateException {
        this.updateManager.installUpdates(updates, repositories, monitor);
    }


    @Override
    public Set<FeatureUpdate> checkForUpdates ( IProgressMonitor monitor ) throws UpdateException {
        return this.updateManager.checkForUpdates(monitor);
    }


    @Override
    public void installFeature ( String spec, IProgressMonitor monitor ) throws UpdateException {
        this.updateManager.installFeature(spec, monitor);
    }


    @Override
    public void removeFeature ( String spec, IProgressMonitor monitor ) throws UpdateException {
        this.updateManager.removeFeature(spec, monitor);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#prepareUpdate(java.util.Set, java.util.Set)
     */
    @Override
    public boolean prepareUpdate ( Set<URI> repositories, Set<Feature> targets ) throws UpdateException {
        return this.updateManager.prepareUpdate(repositories, targets);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#prepareUpdate(java.util.Set, java.util.Set,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public boolean prepareUpdate ( Set<URI> repositories, Set<Feature> targets, IProgressMonitor monitor ) throws UpdateException {
        return this.updateManager.prepareUpdate(repositories, targets, monitor);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#doApplyUpdate()
     */
    @Override
    public void doApplyUpdate () throws UpdateException {}


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {
        this.updateManager.close();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener ( NotificationListener listener, NotificationFilter filter, Object handback )
            throws IllegalArgumentException {}


    /**
     * {@inheritDoc}
     *
     * @see javax.management.NotificationBroadcaster#getNotificationInfo()
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo () {
        return new MBeanNotificationInfo[] {};
    }


    @Override
    public void removeNotificationListener ( NotificationListener listener ) throws ListenerNotFoundException {}


    @Override
    public void removeNotificationListener ( NotificationListener listener, NotificationFilter filter, Object handback )
            throws ListenerNotFoundException {

    }

}
