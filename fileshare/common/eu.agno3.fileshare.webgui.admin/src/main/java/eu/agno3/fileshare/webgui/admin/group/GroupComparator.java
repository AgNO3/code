/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.group;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.util.SubjectCompareUtil;


/**
 * @author mbechler
 *
 */
public class GroupComparator implements Comparator<Group>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6108747670776420258L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( Group a, Group b ) {
        if ( a == null && b == null ) {
            return 0;
        }
        else if ( a == null ) {
            return -1;
        }
        else if ( b == null ) {
            return 1;
        }
        return SubjectCompareUtil.sortBySubject(a, b);
    }

}
