/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.io.Serializable;
import java.util.Comparator;


/**
 * @author mbechler
 *
 */
public class MatchEntryComparator implements Comparator<MatchEntry>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 846826156432460486L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( MatchEntry o1, MatchEntry o2 ) {
        int res = o1.getStartPos() - o2.getStartPos();

        if ( res != 0 ) {
            return res;
        }

        return o1.getEndPos() - o2.getEndPos();
    }

}
