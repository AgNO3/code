/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;
import eu.agno3.orchestrator.system.monitor.service.MonitoringService;


/**
 * 
 * This cache is shared among all users so no access control is performed -
 * this is intentional as the leaked information is really not valuable.
 * 
 * @author mbechler
 */
@Named ( "serviceStateTracker" )
@ApplicationScoped
public class ServiceStateTracker {

    private static final Logger log = Logger.getLogger(ServiceStateTracker.class);

    private static final int CACHE_SIZE = 20;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private AgentStateTracker agentState;

    @Inject
    private StructureCacheBean structureCache;

    private Map<UUID, StateCacheEntry> stateCache = Collections.synchronizedMap(new LRUMap<>(CACHE_SIZE));


    /**
     * @param instance
     * @return whether the agent is online
     */
    public boolean isServiceOnline ( ServiceStructuralObject instance ) {
        ServiceRuntimeStatus st = getServiceState(instance);
        return st == ServiceRuntimeStatus.ACTIVE || st == ServiceRuntimeStatus.WARNING;
    }


    public ServiceRuntimeStatus getCachedState ( InstanceStructuralObject instance ) {
        StateCacheEntry cached = this.stateCache.get(instance.getId());
        if ( cached == null ) {
            return ServiceRuntimeStatus.UNKNOWN;
        }

        return cached.getState();
    }


    /**
     * @param service
     * @return the agent state
     */
    public ServiceRuntimeStatus getServiceState ( ServiceStructuralObject service ) {
        StateCacheEntry cached = this.stateCache.get(service.getId());

        StructuralObject inst = null;
        try {
            inst = this.structureCache.getParentFor(service);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        if ( HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(service.getServiceType()) ) {
            ComponentState as = this.agentState.getAgentState((InstanceStructuralObject) inst);
            if ( as == ComponentState.CONNECTED ) {
                return ServiceRuntimeStatus.ACTIVE;
            }
            return ServiceRuntimeStatus.ERROR;
        }

        if ( ! ( inst instanceof InstanceStructuralObject )
                || this.agentState.getAgentState((InstanceStructuralObject) inst) != ComponentState.CONNECTED ) {
            return ServiceRuntimeStatus.UNKNOWN;
        }

        if ( cached == null || cached.getTimestamp() < System.currentTimeMillis() - getMaxAge(cached.getState()) ) {
            try {
                cached = new StateCacheEntry(ServiceRuntimeStatus.valueOf(this.ssp.getService(MonitoringService.class).getServiceStatus(service)));
            }
            catch ( Exception e ) {
                log.debug("Failed to get component state", e); //$NON-NLS-1$
                cached = new StateCacheEntry(ServiceRuntimeStatus.UNKNOWN);
            }
            this.stateCache.put(service.getId(), cached);
        }
        return cached.getState();
    }


    public ServiceRuntimeStatus forceRefresh ( ServiceStructuralObject instance ) {
        if ( instance.getId() != null ) {
            this.stateCache.remove(instance.getId());
            return getServiceState(instance);
        }
        return null;
    }


    /**
     * @param state
     * @return
     */
    private static long getMaxAge ( ServiceRuntimeStatus state ) {
        if ( state == ServiceRuntimeStatus.ACTIVE || state == ServiceRuntimeStatus.DISABLED || state == ServiceRuntimeStatus.WARNING ) {

            return 30 * 1000;
        }
        return 5 * 1000;
    }

    public static class StateCacheEntry {

        private ServiceRuntimeStatus state;
        private long timestamp;


        /**
         * @param state
         * 
         */
        public StateCacheEntry ( ServiceRuntimeStatus state ) {
            this.state = state;
            this.timestamp = System.currentTimeMillis();
        }


        /**
         * @return the state
         */
        public ServiceRuntimeStatus getState () {
            return this.state;
        }


        /**
         * @return the timestamp
         */
        public long getTimestamp () {
            return this.timestamp;
        }
    }


    /**
     * @param inst
     * @return the events to listen to for agent state
     */
    public Collection<String> getStateListenTo ( InstanceStructuralObject inst ) {
        return Collections.singleton("/instance/" + inst.getId() + "/agent_status"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
