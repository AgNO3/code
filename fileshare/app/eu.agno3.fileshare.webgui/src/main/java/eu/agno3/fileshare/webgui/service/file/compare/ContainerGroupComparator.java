/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.compare;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.VFSContainerEntity;


/**
 * @author mbechler
 *
 */
public class ContainerGroupComparator implements Comparator<VFSContainerEntity>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6153179301480404653L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( VFSContainerEntity a, VFSContainerEntity b ) {

        if ( a == null && b == null ) {
            return 0;
        }
        else if ( a == null ) {
            return -1;
        }
        else if ( b == null ) {
            return 1;
        }

        return FileSortHelpers.sortBySubject(a.getOwner(), b.getOwner());
    }

}
