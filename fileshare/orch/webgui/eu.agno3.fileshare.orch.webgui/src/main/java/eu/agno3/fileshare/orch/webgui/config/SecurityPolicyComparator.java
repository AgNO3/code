/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.util.Comparator;

import eu.agno3.fileshare.orch.common.config.FileshareSecurityPolicy;


/**
 * @author mbechler
 *
 */
public class SecurityPolicyComparator implements Comparator<FileshareSecurityPolicy> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( FileshareSecurityPolicy o1, FileshareSecurityPolicy o2 ) {

        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return -1;
        }
        else if ( o2 == null ) {
            return 1;
        }

        if ( o1.getSortPriority() != null && o2.getSortPriority() != null ) {
            return Integer.compare(o1.getSortPriority(), o2.getSortPriority());
        }
        else if ( o1.getSortPriority() == null ) {
            return -1;
        }
        else if ( o2.getSortPriority() == null ) {
            return 1;
        }

        if ( o1.getLabel() == null && o2.getLabel() == null ) {
            return 0;
        }
        else if ( o1.getLabel() == null ) {
            return -1;
        }
        else if ( o2.getLabel() == null ) {
            return 1;
        }

        return o1.getLabel().compareTo(o2.getLabel());
    }

}
