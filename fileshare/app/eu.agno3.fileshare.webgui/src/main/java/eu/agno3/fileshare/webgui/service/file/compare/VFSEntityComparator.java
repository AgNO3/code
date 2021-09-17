/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.compare;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public class VFSEntityComparator implements Comparator<VFSEntity>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8583876636454512357L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( VFSEntity a, VFSEntity b ) {
        return FileSortHelpers.sortByFileNameInternal(a, b);
    }

}
