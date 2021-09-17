/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink.internal;


import java.util.Comparator;

import eu.agno3.orchestrator.system.logsink.LogProcessorPlugin;


/**
 * @author mbechler
 *
 */
public class LogProcessorPluginComparator implements Comparator<LogProcessorPlugin> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( LogProcessorPlugin o1, LogProcessorPlugin o2 ) {
        int res = Float.compare(o1.getPriority(), o2.getPriority());
        if ( res != 0 ) {
            return -1 * res;
        }
        res = o1.getClass().getName().compareTo(o2.getClass().getName());
        if ( res != 0 ) {
            return res;
        }
        return Long.compare(System.identityHashCode(o1), System.identityHashCode(o2));
    }

}
