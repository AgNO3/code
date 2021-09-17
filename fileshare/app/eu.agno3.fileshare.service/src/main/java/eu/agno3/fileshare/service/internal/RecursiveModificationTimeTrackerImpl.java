/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    RecursiveModificationTimeTracker.class
} )
public class RecursiveModificationTimeTrackerImpl implements RecursiveModificationTimeTracker {

    private static final Logger log = Logger.getLogger(RecursiveModificationTimeTrackerImpl.class);

    private Map<EntityKey, RuntimeModificationEntry> runtime;
    private int modificationCacheSize = 10000;
    private Set<RecursiveModificationListener> listeners = new HashSet<>();


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.runtime = Collections.synchronizedMap(new LRUMap<EntityKey, RuntimeModificationEntry>(this.modificationCacheSize));
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindListener ( RecursiveModificationListener l ) {
        this.listeners.add(l);
    }


    protected synchronized void unbindListener ( RecursiveModificationListener l ) {
        this.listeners.remove(l);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker#trackUpdate(eu.agno3.fileshare.vfs.VFSContext,
     *      eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public void trackUpdate ( VFSContext v, VFSEntity e ) throws FileshareException {
        VFSContainerEntity cur = e instanceof VFSContainerEntity ? (VFSContainerEntity) e : v.getParent(e);
        while ( cur != null ) {
            if ( e.getLastModified() != null && !haveLastModified(v, cur, e.getLastModified().getMillis()) ) {
                return;
            }
            cur = v.getParent(cur);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker#getRecursiveLastModifiedTime(eu.agno3.fileshare.vfs.VFSContext,
     *      eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public Long getRecursiveLastModifiedTime ( VFSContext v, VFSContainerEntity e ) throws FileshareException {
        RuntimeModificationEntry runtimeLastModEntity = this.runtime.get(e.getEntityKey());
        if ( runtimeLastModEntity != null ) {
            return runtimeLastModEntity.getLastModification();
        }

        DateTime lastMod = v.getRecursiveLastModified(e);
        Long millis;
        if ( lastMod != null ) {
            millis = lastMod.getMillis();
        }
        else {
            millis = doComputeRecursiveLastModified(v, e);
        }
        haveLastModified(v, e, millis);
        return millis;
    }


    /**
     * @param v
     * @param e
     * @return
     * @throws FileshareException
     */
    private Long doComputeRecursiveLastModified ( VFSContext v, VFSContainerEntity e ) throws FileshareException {
        if ( log.isDebugEnabled() ) {
            log.debug("Computing recursive last modified time", e); //$NON-NLS-1$
        }
        Long max = e.getLastModified() != null ? e.getLastModified().getMillis() : null;
        for ( VFSEntity ch : v.getChildren(e) ) {
            Long lastMod = ch.getLastModified() != null ? ch.getLastModified().getMillis() : null;
            if ( max == null || ( lastMod != null && lastMod < max ) ) {
                max = lastMod;
            }

            if ( ch instanceof VFSContainerEntity ) {
                lastMod = getRecursiveLastModifiedTime(v, (VFSContainerEntity) ch);
                if ( max == null || ( lastMod != null && lastMod < max ) ) {
                    max = lastMod;
                }
            }
        }
        return max;
    }


    /**
     * @param v
     * @param e
     * @param lastMod
     * @return
     */
    private boolean haveLastModified ( VFSContext v, VFSContainerEntity e, Long lastMod ) {
        RuntimeModificationEntry modEntry;
        boolean res = false;
        synchronized ( this.runtime ) {
            modEntry = this.runtime.get(e.getEntityKey());
            if ( modEntry == null ) {
                modEntry = new RuntimeModificationEntry(e.getEntityKey(), lastMod);
                this.runtime.put(e.getEntityKey(), modEntry);
                res = true;
            }
        }

        res |= modEntry.updateLastModification(lastMod);

        if ( res && log.isTraceEnabled() ) {
            log.trace(String.format("Have new last modified for %s %s", e.getEntityKey(), lastMod)); //$NON-NLS-1$
        }

        if ( v.trackRecursiveLastModificationTimes() ) {
            if ( res ) {
                notifyListeners(modEntry);
            }
        }

        return res;
    }


    /**
     * @param modEntry
     */
    private void notifyListeners ( RuntimeModificationEntry modEntry ) {
        for ( RecursiveModificationListener recursiveNotificationListener : this.listeners ) {
            recursiveNotificationListener.notifyChange(modEntry);
        }
    }
}
