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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.InstanceService;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * 
 * This cache is shared among all users so no access control is performed -
 * this is intentional as the leaked information is really not valuable.
 * 
 * @author mbechler
 */
@Named ( "agentStateTracker" )
@ApplicationScoped
public class AgentStateTracker {

    private static final Logger log = Logger.getLogger(AgentStateTracker.class);

    private static final int CACHE_SIZE = 20;

    @Inject
    private ServerServiceProvider ssp;

    private Map<UUID, StateCacheEntry> stateCache = Collections.synchronizedMap(new LRUMap<>(CACHE_SIZE));


    /**
     * @param instance
     * @return whether the agent is online
     */
    public boolean isAgentOnline ( InstanceStructuralObject instance ) {
        return getAgentState(instance) == ComponentState.CONNECTED;

    }


    public ComponentState getCachedState ( InstanceStructuralObject instance ) {
        StateCacheEntry cached = this.stateCache.get(instance.getId());
        if ( cached == null ) {
            return ComponentState.UNKNOWN;
        }

        return cached.getState();
    }


    /**
     * @param instance
     * @return the agent state
     */
    public ComponentState getAgentState ( InstanceStructuralObject instance ) {
        if ( instance == null ) {
            return ComponentState.UNKNOWN;
        }
        StateCacheEntry cached = this.stateCache.get(instance.getId());
        if ( cached == null || cached.getTimestamp() < System.currentTimeMillis() - getMaxAge(cached.getState()) ) {
            try {
                cached = new StateCacheEntry(ComponentState.valueOf(this.ssp.getService(InstanceService.class).getAgentState(instance)));
            }
            catch ( Exception e ) {
                log.debug("Failed to get component state", e); //$NON-NLS-1$
                cached = new StateCacheEntry(ComponentState.UNKNOWN);
            }
            this.stateCache.put(instance.getId(), cached);
        }
        return cached.getState();
    }


    public ComponentState forceRefresh ( InstanceStructuralObject instance ) {
        UUID id = instance.getId();
        if ( id != null ) {
            this.stateCache.remove(id);
            return getAgentState(instance);
        }
        return null;
    }


    /**
     * @param state
     * @return
     */
    private static long getMaxAge ( ComponentState state ) {
        if ( state == ComponentState.CONNECTED || state == ComponentState.DISCONNECTED ) {
            return 30 * 1000;
        }
        return 5 * 1000;
    }

    public static class StateCacheEntry {

        private ComponentState state;
        private long timestamp;


        /**
         * @param state
         * 
         */
        public StateCacheEntry ( ComponentState state ) {
            this.state = state;
            this.timestamp = System.currentTimeMillis();
        }


        /**
         * @return the state
         */
        public ComponentState getState () {
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


    public String getStateListenToString ( InstanceStructuralObject inst ) {
        return StringUtils.join(getStateListenTo(inst), '|');
    }

}
