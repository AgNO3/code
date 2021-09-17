/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.realms.KeytabEntry;


/**
 * @author mbechler
 *
 */
public class KeytabEntryComparator implements Comparator<KeytabEntry>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7863204266724569263L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( KeytabEntry o1, KeytabEntry o2 ) {

        if ( o1.getKeytabId() == null && o2.getKeytabId() == null ) {
            return 0;
        }
        else if ( o1.getKeytabId() == null ) {
            return -1;
        }
        else if ( o2.getKeytabId() == null ) {
            return 1;
        }

        return o1.getKeytabId().compareTo(o2.getKeytabId());
    }

}
