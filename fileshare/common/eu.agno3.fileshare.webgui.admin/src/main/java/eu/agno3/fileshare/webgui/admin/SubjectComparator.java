/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.util.SubjectCompareUtil;


/**
 * @author mbechler
 *
 */
public class SubjectComparator implements Comparator<SubjectInfo>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1694870317113046544L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( SubjectInfo a, SubjectInfo b ) {
        return SubjectCompareUtil.sortBySubject(a, b);
    }

}
