/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.orch.common.config.FileshareUserTrustLevel;


/**
 * @author mbechler
 *
 */
public class TrustLevelComparator implements Comparator<FileshareUserTrustLevel>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8228244225285504402L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( FileshareUserTrustLevel o1, FileshareUserTrustLevel o2 ) {
        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return 1;
        }
        else if ( o2 == null ) {
            return -1;
        }

        if ( o1.getTrustLevelId() == null && o2.getTrustLevelId() == null ) {
            return 0;
        }
        else if ( o1.getTrustLevelId() == null ) {
            return -1;
        }
        else if ( o2.getTrustLevelId() == null ) {
            return 1;
        }
        return o1.getTrustLevelId().compareTo(o2.getTrustLevelId());
    }
}
