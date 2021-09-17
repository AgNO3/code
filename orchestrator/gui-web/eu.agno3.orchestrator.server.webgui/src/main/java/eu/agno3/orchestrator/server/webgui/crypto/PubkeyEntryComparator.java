/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;


/**
 * @author mbechler
 *
 */
public class PubkeyEntryComparator implements Comparator<PublicKeyEntry>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1018570346832691279L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( PublicKeyEntry o1, PublicKeyEntry o2 ) {
        if ( o1.getDerivedId() == null && o2.getDerivedId() == null ) {
            return 0;
        }
        else if ( o1.getDerivedId() == null ) {
            return -1;
        }
        else if ( o2.getDerivedId() == null ) {
            return 1;
        }
        return o1.getDerivedId().compareTo(o2.getDerivedId());
    }

}
