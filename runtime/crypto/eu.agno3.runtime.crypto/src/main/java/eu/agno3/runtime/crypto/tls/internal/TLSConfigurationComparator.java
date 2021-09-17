/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.runtime.crypto.tls.TLSConfiguration;


/**
 * @author mbechler
 *
 */
public class TLSConfigurationComparator implements Comparator<TLSConfiguration>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5573030304194996777L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( TLSConfiguration o1, TLSConfiguration o2 ) {
        int res = Integer.compare(o1.getPriority(), o2.getPriority());

        if ( res != 0 ) {
            return -1 * res;
        }

        res = o1.getClass().getName().compareTo(o2.getClass().getName());
        if ( res != 0 ) {
            return res;
        }

        return o1.getId().compareTo(o2.getId());
    }

}
