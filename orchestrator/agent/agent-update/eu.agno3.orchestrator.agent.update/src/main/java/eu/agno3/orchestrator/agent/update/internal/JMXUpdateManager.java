/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.eclipse.core.runtime.IProgressMonitor;

import eu.agno3.runtime.jmx.JMXClient;
import eu.agno3.runtime.update.Feature;
import eu.agno3.runtime.update.FeatureUpdate;
import eu.agno3.runtime.update.LocalUpdateManagerMBean;
import eu.agno3.runtime.update.UpdateException;
import eu.agno3.runtime.update.UpdateManager;


/**
 * @author mbechler
 *
 */
public class JMXUpdateManager implements ClosableUpdateManager {

    private JMXClient client;
    private LocalUpdateManagerMBean updateManager;
    private boolean closed;


    /**
     * @param um
     * @param jmx
     */
    public JMXUpdateManager ( UpdateManager um, JMXClient jmx ) {
        this.updateManager = um;
        this.client = jmx;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener ( NotificationListener listener, NotificationFilter filter, Object handback ) throws IllegalArgumentException {
        this.updateManager.addNotificationListener(listener, filter, handback);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.NotificationBroadcaster#getNotificationInfo()
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo () {
        return this.updateManager.getNotificationInfo();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.NotificationBroadcaster#removeNotificationListener(javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener ( NotificationListener listener ) throws ListenerNotFoundException {
        this.updateManager.removeNotificationListener(listener);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.NotificationEmitter#removeNotificationListener(javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener ( NotificationListener listener, NotificationFilter filter, Object handback )
            throws ListenerNotFoundException {
        this.updateManager.removeNotificationListener(listener, filter, handback);
    }


    /**
     * 
     */
    private void checkClosed () {
        if ( this.closed ) {
            throw new IllegalStateException("Already closed"); //$NON-NLS-1$
        }
    }


    @Override
    public URI[] listRepositories () throws UpdateException {
        checkClosed();
        return this.updateManager.listRepositories();
    }


    @Override
    public Set<Feature> listInstalledFeatures () throws UpdateException {
        checkClosed();
        return this.updateManager.listInstalledFeatures();
    }


    @Override
    public void installAllUpdates ( Set<URI> repositories ) throws UpdateException {
        checkClosed();
        this.updateManager.installAllUpdates(repositories);
    }


    @Override
    public void installAllUpdates ( Set<URI> repositories, IProgressMonitor monitor ) throws UpdateException {
        checkClosed();
        NotificationListener l = new JMXUpdateManagerNotificationListener(monitor);
        try {
            this.updateManager.addNotificationListener(l, null, null);
            this.updateManager.installAllUpdates(repositories);
        }
        finally {
            try {
                this.updateManager.removeNotificationListener(l);
            }
            catch ( ListenerNotFoundException e ) {
                throw new UpdateException("Failed to remove listener"); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void installUpdates ( Set<Feature> updates, Set<URI> repositories ) throws UpdateException {
        checkClosed();
        this.updateManager.installUpdates(updates, repositories);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#installUpdates(java.util.Set, java.util.Set,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void installUpdates ( Set<Feature> updates, Set<URI> repositories, IProgressMonitor monitor ) throws UpdateException {
        checkClosed();
        NotificationListener l = new JMXUpdateManagerNotificationListener(monitor);
        try {
            this.updateManager.addNotificationListener(l, null, null);
            this.updateManager.installUpdates(updates, repositories);
        }
        finally {
            monitor.done();
            try {
                this.updateManager.removeNotificationListener(l);
            }
            catch ( ListenerNotFoundException e ) {
                throw new UpdateException("Failed to remove listener"); //$NON-NLS-1$
            }
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
        checkClosed();
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
        checkClosed();
        NotificationListener l = new JMXUpdateManagerNotificationListener(monitor);
        try {
            this.updateManager.addNotificationListener(l, null, null);
            return this.updateManager.prepareUpdate(repositories, targets);
        }
        finally {
            monitor.done();
            try {
                this.updateManager.removeNotificationListener(l);
            }
            catch ( ListenerNotFoundException e ) {
                throw new UpdateException("Failed to remove listener"); //$NON-NLS-1$
            }
        }
    }


    @Override
    public Set<FeatureUpdate> checkForUpdates () throws UpdateException {
        checkClosed();
        return this.updateManager.checkForUpdates();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#checkForUpdates(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public Set<FeatureUpdate> checkForUpdates ( IProgressMonitor monitor ) throws UpdateException {
        checkClosed();
        NotificationListener l = new JMXUpdateManagerNotificationListener(monitor);
        try {
            this.updateManager.addNotificationListener(l, null, null);
            return this.updateManager.checkForUpdates();
        }
        finally {
            monitor.done();
            try {
                this.updateManager.removeNotificationListener(l);
            }
            catch ( ListenerNotFoundException e ) {
                throw new UpdateException("Failed to remove listener", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void runGarbageCollection () throws UpdateException {
        checkClosed();
        this.updateManager.runGarbageCollection();
    }


    @Override
    public Set<Feature> getInstallableFeatures () throws UpdateException {
        checkClosed();
        return this.updateManager.getInstallableFeatures();
    }


    @Override
    public void installFeature ( String spec ) throws UpdateException {
        checkClosed();
        this.updateManager.installFeature(spec);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#installFeature(java.lang.String,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void installFeature ( String spec, IProgressMonitor monitor ) throws UpdateException {
        checkClosed();
        NotificationListener l = new JMXUpdateManagerNotificationListener(monitor);
        try {
            this.updateManager.addNotificationListener(l, null, null);
            this.updateManager.installFeature(spec);
        }
        finally {
            monitor.done();
            try {
                this.updateManager.removeNotificationListener(l);
            }
            catch ( ListenerNotFoundException e ) {
                throw new UpdateException("Failed to remove listener"); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void removeFeature ( String spec ) throws UpdateException {
        checkClosed();
        this.updateManager.removeFeature(spec);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#removeFeature(java.lang.String,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void removeFeature ( String spec, IProgressMonitor monitor ) throws UpdateException {
        checkClosed();
        NotificationListener l = new JMXUpdateManagerNotificationListener(monitor);
        try {
            this.updateManager.addNotificationListener(l, null, null);
            this.updateManager.removeFeature(spec);
        }
        finally {
            monitor.done();
            try {
                this.updateManager.removeNotificationListener(l);
            }
            catch ( ListenerNotFoundException e ) {
                throw new UpdateException("Failed to remove listener"); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#doApplyUpdate()
     */
    @Override
    public void doApplyUpdate () throws UpdateException {
        checkClosed();
        this.updateManager.doApplyUpdate();
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {
        this.closed = true;
        try {
            this.client.close();
        }
        catch ( IOException e ) {
            throw new RuntimeException(e);
        }
    }

}
