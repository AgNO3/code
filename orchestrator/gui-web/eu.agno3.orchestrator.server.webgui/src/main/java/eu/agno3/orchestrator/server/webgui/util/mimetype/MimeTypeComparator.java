/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.util.mimetype;


import java.io.Serializable;
import java.util.Comparator;


/**
 * @author mbechler
 *
 */
public class MimeTypeComparator implements Comparator<String>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3799928722485838696L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( String o1, String o2 ) {

        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return -1;
        }
        else if ( o2 == null ) {
            return 1;
        }

        int sep1 = o1.indexOf('/');
        int sep2 = o2.indexOf('/');

        if ( sep1 == -1 && sep2 == -1 ) {
            return o1.compareTo(o2);
        }
        else if ( sep1 == -1 ) {
            return -1;
        }
        else if ( sep2 == -1 ) {
            return 1;
        }

        String type1 = o1.substring(0, sep1);
        String type2 = o2.substring(0, sep2);

        int res = type1.compareTo(type2);
        if ( res != 0 ) {
            return res;
        }

        String subtype1 = o1.substring(sep1 + 1);
        String subtype2 = o2.substring(sep2 + 1);

        return subtype1.compareTo(subtype2);
    }

}
