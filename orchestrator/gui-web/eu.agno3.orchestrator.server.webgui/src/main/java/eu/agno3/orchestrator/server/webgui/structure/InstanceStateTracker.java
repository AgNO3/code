/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.InstanceStatus;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.InstanceStateService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * 
 * This cache is shared among all users so no access control is performed -
 * this is intentional as the leaked information is really not valuable.
 * 
 * @author mbechler
 */
@Named ( "instanceStateTracker" )
@ApplicationScoped
public class InstanceStateTracker {

    private static final Logger log = Logger.getLogger(InstanceStateTracker.class);

    private static final int CACHE_SIZE = 50;

    @Inject
    private ServerServiceProvider ssp;

    private Map<UUID, StateCacheEntry> stateCache = Collections.synchronizedMap(new LRUMap<>(CACHE_SIZE));


    /**
     * @param inst
     * @return the agent state
     */
    public InstanceStatus getInstanceState ( StructuralObject inst ) {
        if ( ! ( inst instanceof InstanceStructuralObject ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Invalid object " + inst); //$NON-NLS-1$
            }
            return null;
        }
        StateCacheEntry cached = this.stateCache.get(inst.getId());
        if ( cached == null || cached.getTimestamp() < System.currentTimeMillis() - getMaxAge(cached) ) {
            try {
                cached = new StateCacheEntry(this.ssp.getService(InstanceStateService.class).getState((InstanceStructuralObject) inst));
            }
            catch ( Exception e ) {
                log.debug("Failed to get component state", e); //$NON-NLS-1$
                cached = new StateCacheEntry(new InstanceStatus());
            }
            this.stateCache.put(inst.getId(), cached);
        }
        return cached.getState();
    }


    public InstanceStatus forceRefresh ( InstanceStructuralObject instance ) {
        if ( instance.getId() != null ) {
            this.stateCache.remove(instance.getId());
            return getInstanceState(instance);
        }
        return null;
    }


    /**
     * @param cached
     * @return
     */
    private static long getMaxAge ( StateCacheEntry cached ) {
        return 10 * 1000;
    }

    public static class StateCacheEntry {

        private InstanceStatus state;
        private long timestamp;


        /**
         * @param instanceState
         * 
         */
        public StateCacheEntry ( InstanceStatus instanceState ) {
            this.state = instanceState;
            this.timestamp = System.currentTimeMillis();
        }


        /**
         * @return the state
         */
        public InstanceStatus getState () {
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
        return Arrays.asList(String.format("/instance/%s/status", inst.getId())); //$NON-NLS-1$
    }

}
