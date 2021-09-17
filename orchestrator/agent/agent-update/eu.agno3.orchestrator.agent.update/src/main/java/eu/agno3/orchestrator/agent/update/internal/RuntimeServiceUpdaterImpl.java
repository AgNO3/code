/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.net.URI;
import java.util.Set;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.agent.update.RuntimeServiceUpdater;
import eu.agno3.orchestrator.agent.update.units.JobOutputProgressMonitor;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;
import eu.agno3.runtime.jmx.JMXClient;
import eu.agno3.runtime.update.Feature;
import eu.agno3.runtime.update.FeatureUpdate;
import eu.agno3.runtime.update.UpdateConfiguration;
import eu.agno3.runtime.update.UpdateException;
import eu.agno3.runtime.update.UpdateManager;
import eu.agno3.runtime.update.UpdateManagerProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    RuntimeServiceUpdater.class, SystemService.class
} )
@SystemServiceType ( RuntimeServiceUpdater.class )
public class RuntimeServiceUpdaterImpl implements RuntimeServiceUpdater {

    private static final Logger log = Logger.getLogger(RuntimeServiceUpdaterImpl.class);

    private ServiceManager serviceManager;
    private UpdateManagerProvider updateManagerProvider;

    private ExecutionConfigProperties executionConfig;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setServiceManager ( ServiceManager rsm ) {
        this.serviceManager = rsm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager rsm ) {
        if ( this.serviceManager == rsm ) {
            this.serviceManager = null;
        }
    }


    @Reference
    protected synchronized void setUpdateManagerProvider ( UpdateManagerProvider ump ) {
        this.updateManagerProvider = ump;
    }


    protected synchronized void unsetUpdateManagerProvider ( UpdateManagerProvider ump ) {
        if ( this.updateManagerProvider == ump ) {
            this.updateManagerProvider = null;
        }
    }


    @Reference
    protected synchronized void setExecutionConfig ( ExecutionConfigProperties ec ) {
        this.executionConfig = ec;
    }


    protected synchronized void unsetExecutionConfig ( ExecutionConfigProperties ec ) {
        if ( this.executionConfig == ec ) {
            this.executionConfig = null;
        }
    }


    @Override
    public Set<Feature> getInstalled ( StructuralObjectReference service ) throws UpdateException, ServiceManagementException {
        try ( ClosableUpdateManager updateManager = getUpdateManager(service, false) ) {
            return updateManager.listInstalledFeatures();
        }
        catch ( Exception e ) {
            throw new UpdateException("Failed to get installed software", e); //$NON-NLS-1$
        }
    }


    @Override
    public Set<FeatureUpdate> getAllUpdates ( StructuralObjectReference service, IProgressMonitor monitor )
            throws ServiceManagementException, UpdateException {
        try ( ClosableUpdateManager updateManager = getUpdateManager(service, false) ) {
            return updateManager.checkForUpdates(monitor);
        }
        catch ( Exception e ) {
            throw new UpdateException("Failed to check for updates", e); //$NON-NLS-1$
        }
    }


    @Override
    public void installUpdates ( StructuralObjectReference service, Set<URI> repositories, Set<Feature> updates, IProgressMonitor monitor )
            throws UpdateException {
        try ( ClosableUpdateManager updateManager = getUpdateManager(service, false) ) {
            updateManager.installUpdates(updates, repositories, monitor);
        }
        catch ( Exception e ) {
            throw new UpdateException("Failed to install updates", e); //$NON-NLS-1$
        }
    }


    @Override
    public void installAllUpdates ( StructuralObjectReference service, Set<URI> repositories, IProgressMonitor monitor )
            throws ServiceManagementException, UpdateException {
        try ( ClosableUpdateManager updateManager = getUpdateManager(service, false) ) {
            updateManager.installAllUpdates(repositories, monitor);
        }
        catch ( Exception e ) {
            throw new UpdateException("Failed to install updates", e); //$NON-NLS-1$
        }
    }


    @Override
    public void runGarbageCollection ( StructuralObjectReference service ) throws ServiceManagementException, UpdateException {
        try ( ClosableUpdateManager updateManager = getUpdateManager(service, false) ) {
            updateManager.runGarbageCollection();
        }
        catch ( Exception e ) {
            throw new UpdateException("Failed to run garbage collection", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.RuntimeServiceUpdater#doApplyUpdates(eu.agno3.orchestrator.config.model.realm.StructuralObjectReference)
     */
    @Override
    public void doApplyUpdates ( StructuralObjectReference service ) throws ServiceManagementException, UpdateException {
        try ( ClosableUpdateManager updateManager = getUpdateManager(service, true) ) {
            updateManager.doApplyUpdate();
        }
        catch ( Exception e ) {
            throw new UpdateException("Failed apply config changes", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.RuntimeServiceUpdater#doApplyOffline(eu.agno3.orchestrator.config.model.realm.StructuralObjectReference)
     */
    @Override
    public void doApplyOffline ( StructuralObjectReference service ) throws ServiceManagementException, UpdateException {
        try ( ClosableUpdateManager updateManager = getUpdateManager(service, false) ) {
            updateManager.doApplyUpdate();
        }
        catch ( Exception e ) {
            throw new UpdateException("Failed to apply config changes", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.RuntimeServiceUpdater#prepareUpdate(eu.agno3.orchestrator.config.model.realm.StructuralObjectReference,
     *      java.util.Set, java.util.Set, eu.agno3.orchestrator.agent.update.units.JobOutputProgressMonitor)
     */
    @Override
    public boolean prepareUpdate ( StructuralObjectReference targetService, Set<URI> repositories, Set<Feature> targets,
            JobOutputProgressMonitor monitor ) throws ServiceManagementException, UpdateException {
        try ( ClosableUpdateManager updateManager = getUpdateManager(targetService, false) ) {
            return updateManager.prepareUpdate(repositories, targets, monitor);
        }
        catch ( Exception e ) {
            throw new UpdateException("Failed to prepare update", e); //$NON-NLS-1$
        }
    }


    private ClosableUpdateManager getUpdateManager ( StructuralObjectReference service, boolean onlineTarget )
            throws UpdateException, ServiceManagementException {

        if ( service.getType() != StructuralObjectType.SERVICE ) {
            throw new ServiceManagementException("Not a service"); //$NON-NLS-1$
        }

        if ( HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(service.getLocalType()) ) {
            // this means the local agent
            return getUpdateManagerLocal();
        }

        RuntimeServiceManager sm = this.serviceManager.getServiceManager(service, RuntimeServiceManager.class);
        ServiceRuntimeStatus runtimeStatus = sm.getRuntimeStatus(null);
        switch ( runtimeStatus ) {
        case ACTIVE:
        case WARNING:
            if ( onlineTarget ) {
                log.debug("Using online update manager"); //$NON-NLS-1$
                return getUpdateManagerOnline(sm);
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Using offline update manager for state " + runtimeStatus); //$NON-NLS-1$
            }
            return getUpdateManagerOffline(sm);
        case TRANSIENT:
        case DISABLED:
        case ERROR:
        case UNKNOWN:
            if ( log.isDebugEnabled() ) {
                log.debug("Using offline update manager for state " + runtimeStatus); //$NON-NLS-1$
            }
            return getUpdateManagerOffline(sm);
        default:
            throw new ServiceManagementException("Unknown service state " + runtimeStatus); //$NON-NLS-1$

        }
    }


    /**
     * @param sm
     * @return
     * @throws UpdateException
     */
    private ClosableUpdateManager getUpdateManagerOnline ( RuntimeServiceManager sm ) throws UpdateException {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            return getUpdateManagerOnline(sm.getJMXConnection());
        }
        catch (
            MalformedObjectNameException |
            ServiceManagementException |
            IllegalArgumentException e ) {
            throw new UpdateException("Failed to get update manager", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * @param jmx
     * @return
     * @throws MalformedObjectNameException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    private static JMXUpdateManager getUpdateManagerOnline ( JMXClient jmx ) throws MalformedObjectNameException {
        return new JMXUpdateManager(JMX.newMBeanProxy(
            jmx,
            new ObjectName("eu.agno3.runtime.update:type=UpdateManager"), //$NON-NLS-1$
            UpdateManager.class), jmx);
    }


    /**
     * @return
     * @throws UpdateException
     */
    private ClosableUpdateManager getUpdateManagerLocal () throws UpdateException {
        return new LocalUpdateManager(this.updateManagerProvider.getLocalUpdateManager());
    }


    /**
     * @param sm
     * @return
     * @throws UpdateException
     */
    private ClosableUpdateManager getUpdateManagerOffline ( RuntimeServiceManager sm ) throws UpdateException {
        UpdateConfiguration cfg = new ServiceUpdateConfiguration(sm, this.executionConfig);
        return new LocalUpdateManager(this.updateManagerProvider.getUpdateManager(cfg));

    }
}
