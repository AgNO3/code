/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.service.config.PolicyConfiguration;


/**
 * @author mbechler
 *
 */
public class PolicyConfigurationComparator implements Comparator<PolicyConfiguration>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -9023952216578341077L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( PolicyConfiguration o1, PolicyConfiguration o2 ) {

        int res = Float.compare(o1.getSortOrder(), o2.getSortOrder());
        if ( res != 0 ) {
            return res;
        }

        return o1.getLabel().compareTo(o2.getLabel());
    }

}
