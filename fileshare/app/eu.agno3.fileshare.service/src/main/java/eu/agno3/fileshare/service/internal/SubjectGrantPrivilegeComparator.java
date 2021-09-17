/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public class SubjectGrantPrivilegeComparator implements Comparator<SubjectGrant>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3127771985673809703L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( SubjectGrant a, SubjectGrant b ) {

        if ( a == null && b == null ) {
            return 0;
        }
        else if ( a == null ) {
            return -1;
        }
        else if ( b == null ) {
            return 1;
        }

        int rs = compareEntity(a.getEntity(), b.getEntity());

        if ( rs != 0 ) {
            return rs;
        }

        return comparePermissions(a, b);
    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int comparePermissions ( SubjectGrant a, SubjectGrant b ) {
        // TODO:
        return Integer.compare(GrantPermission.toInt(a.getPermissions()), GrantPermission.toInt(b.getPermissions()));
    }


    /**
     * @param entity
     * @param entity2
     * @return
     */
    private static int compareEntity ( VFSEntity a, VFSEntity b ) {
        if ( a == null && b == null ) {
            return 0;
        }
        else if ( a == null ) {
            return -1;
        }
        else if ( b == null ) {
            return 1;
        }

        return a.getEntityKey().compareTo(b.getEntityKey());
    }
}
