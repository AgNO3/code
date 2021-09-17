/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 18, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.util.Comparator;

import eu.agno3.runtime.crypto.tls.SNIHandler;


/**
 * @author mbechler
 *
 */
public class SNIHandlerComparator implements Comparator<SNIHandler> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( SNIHandler o1, SNIHandler o2 ) {
        int res = Float.compare(o1.getPriority(), o2.getPriority());
        if ( res != 0 ) {
            return res;
        }
        return Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
    }

}
