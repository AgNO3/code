/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.compare;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public class SubjectGrantComparator implements Comparator<SubjectGrant>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2622012666253458131L;


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

        VFSEntity entityA = a.getEntity();
        VFSEntity entityB = b.getEntity();
        return FileSortHelpers.sortByFileNameInternal(entityA, entityB);
    }

}
