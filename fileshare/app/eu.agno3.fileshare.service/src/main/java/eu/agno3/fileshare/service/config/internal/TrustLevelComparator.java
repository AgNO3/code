/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.05.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.TrustLevel;


/**
 * @author mbechler
 *
 */
public class TrustLevelComparator implements Comparator<TrustLevel>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6333018522670811429L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( TrustLevel o1, TrustLevel o2 ) {
        int res = Float.compare(o1.getPriority(), o2.getPriority());
        if ( res == 0 ) {
            return o1.getId().compareTo(o2.getId());
        }
        return res;
    }

}
