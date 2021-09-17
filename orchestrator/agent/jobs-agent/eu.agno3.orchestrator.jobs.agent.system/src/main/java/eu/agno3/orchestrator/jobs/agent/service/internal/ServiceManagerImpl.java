/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service.internal;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ServiceManager.class, SystemService.class
} )
@SystemServiceType ( ServiceManager.class )
public class ServiceManagerImpl implements ServiceManager {

    private static final Logger log = Logger.getLogger(ServiceManagerImpl.class);
    private Map<String, BaseServiceManager> managers = new HashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindServiceManager ( BaseServiceManager bsm ) {
        if ( this.managers.put(bsm.getServiceType(), bsm) != null ) {
            log.warn("Multiple service managers for " + bsm.getServiceType()); //$NON-NLS-1$
        }
    }


    protected synchronized void unbindServiceManager ( BaseServiceManager bsm ) {
        this.managers.remove(bsm.getServiceType(), bsm);
    }


    @Override
    public void disableService ( StructuralObjectReference service ) throws ServiceManagementException {
        this.getServiceManager(service, BaseServiceManager.class).disable(service.getId());
    }


    @Override
    public void enableService ( StructuralObjectReference service ) throws ServiceManagementException {
        this.getServiceManager(service, BaseServiceManager.class).enable(service.getId());
    }


    @Override
    public void restartService ( StructuralObjectReference service ) throws ServiceManagementException {
        this.getServiceManager(service, BaseServiceManager.class).restart(service.getId());
    }


    @Override
    public void stopService ( StructuralObjectReference service ) throws ServiceManagementException {
        this.getServiceManager(service, BaseServiceManager.class).stop(service.getId());
    }


    @Override
    public void startService ( StructuralObjectReference service ) throws ServiceManagementException {
        this.getServiceManager(service, BaseServiceManager.class).start(service.getId());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.ServiceManager#forceReloadAll(java.lang.String)
     */
    @Override
    public void forceReloadAll ( String string ) {
        for ( BaseServiceManager sm : this.managers.values() ) {
            try {
                sm.forceReloadAll(string);
            }
            catch ( ServiceManagementException e ) {
                log.warn("Failed to force config reload of " + sm.getServiceType()); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.ServiceManager#hasServiceManagerFor(eu.agno3.orchestrator.config.model.realm.StructuralObjectReference)
     */
    @Override
    public boolean hasServiceManagerFor ( StructuralObjectReference ref ) {
        if ( ref == null || ref.getType() != StructuralObjectType.SERVICE ) {
            return false;
        }

        BaseServiceManager manager = this.managers.get(ref.getLocalType());
        if ( manager == null ) {
            return false;
        }

        return true;
    }


    @Override
    public <T extends BaseServiceManager> boolean isServiceManagerType ( StructuralObjectReference service, Class<T> managerType )
            throws ServiceManagementException {
        if ( service == null || service.getType() != StructuralObjectType.SERVICE ) {
            throw new ServiceManagementException("No service specified"); //$NON-NLS-1$
        }

        BaseServiceManager manager = this.managers.get(service.getLocalType());
        if ( manager == null ) {
            return false;
        }

        return managerType.isAssignableFrom(manager.getClass());
    }


    @Override
    @SuppressWarnings ( "unchecked" )
    public <T extends BaseServiceManager> @NonNull T getServiceManager ( StructuralObjectReference service, Class<T> managerType )
            throws ServiceManagementException {

        if ( service == null || service.getType() != StructuralObjectType.SERVICE ) {
            throw new ServiceManagementException("No service specified"); //$NON-NLS-1$
        }

        BaseServiceManager manager = this.managers.get(service.getLocalType());
        if ( manager == null ) {
            throw new ServiceManagementException("No manager found for " + service.getLocalType()); //$NON-NLS-1$
        }

        if ( !managerType.isAssignableFrom(manager.getClass()) ) {
            throw new ServiceManagementException("Incompatible manager type " + manager.getClass()); //$NON-NLS-1$
        }

        return (T) manager;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.service.ServiceManager#getSingletonServiceManager(java.lang.String,
     *      java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends BaseServiceManager> T getSingletonServiceManager ( String serviceType, Class<T> managerType )
            throws ServiceManagementException {
        BaseServiceManager manager = this.managers.get(serviceType);
        if ( manager == null ) {
            throw new ServiceManagementException("No manager found for " + serviceType); //$NON-NLS-1$
        }

        if ( !managerType.isAssignableFrom(manager.getClass()) ) {
            throw new ServiceManagementException("Incompatible manager type " + manager.getClass()); //$NON-NLS-1$
        }

        return (T) manager;
    }
}
