/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth.impl;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter;


/**
 * @author mbechler
 * 
 */
public class VoterComparator implements Comparator<DestinationAccessVoter>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -332476861580940320L;


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( DestinationAccessVoter arg0, DestinationAccessVoter arg1 ) {

        if ( arg0.getPriority() == arg1.getPriority() ) {
            return Integer.compare(System.identityHashCode(arg0), System.identityHashCode(arg1));
        }

        return Integer.compare(arg0.getPriority(), arg1.getPriority());
    }

}
