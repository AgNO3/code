/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.compare;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public class SharedEntityComparator extends VFSEntityComparator implements Comparator<VFSEntity>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2726875727519720533L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( VFSEntity a, VFSEntity b ) {
        int nameCompare = super.compare(a, b);

        if ( nameCompare != 0 ) {
            return nameCompare;
        }

        return FileSortHelpers.sortBySubject(a.getOwner(), b.getOwner());
    }
}
