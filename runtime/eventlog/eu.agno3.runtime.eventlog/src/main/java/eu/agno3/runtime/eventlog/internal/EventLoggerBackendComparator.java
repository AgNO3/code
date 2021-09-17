/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.util.Comparator;

import eu.agno3.runtime.eventlog.EventLoggerBackend;


/**
 * @author mbechler
 *
 */
public class EventLoggerBackendComparator implements Comparator<EventLoggerBackend> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( EventLoggerBackend o1, EventLoggerBackend o2 ) {
        int res = Integer.compare(o1.getPriority(), o2.getPriority());
        if ( res != 0 ) {
            return -1 * res;
        }

        res = o1.getClass().getName().compareTo(o2.getClass().getName());
        if ( res != 0 ) {
            return res;
        }

        return Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
    }

}
