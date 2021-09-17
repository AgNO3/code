/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.storage;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.hostconfig.storage.MountEntry;


/**
 * @author mbechler
 *
 */
public class MountEntryComparator implements Comparator<MountEntry>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7711779742684008944L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( MountEntry o1, MountEntry o2 ) {
        if ( o1.getAlias() == null && o2.getAlias() == null ) {
            return 0;
        }
        else if ( o1.getAlias() == null ) {
            return -1;
        }
        else if ( o2.getAlias() == null ) {
            return 1;
        }

        return o1.getAlias().compareTo(o2.getAlias());
    }

}
