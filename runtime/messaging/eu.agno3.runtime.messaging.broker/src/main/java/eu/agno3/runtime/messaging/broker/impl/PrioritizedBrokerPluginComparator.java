/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.impl;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin;


/**
 * @author mbechler
 * 
 */
public class PrioritizedBrokerPluginComparator implements Comparator<PrioritizedBrokerPlugin>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4422984204667982098L;


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( PrioritizedBrokerPlugin arg0, PrioritizedBrokerPlugin arg1 ) {

        if ( arg0.getPriority() == arg1.getPriority() ) {
            return Integer.compare(System.identityHashCode(arg0), System.identityHashCode(arg1));
        }

        return Integer.compare(arg0.getPriority(), arg1.getPriority());
    }

}
