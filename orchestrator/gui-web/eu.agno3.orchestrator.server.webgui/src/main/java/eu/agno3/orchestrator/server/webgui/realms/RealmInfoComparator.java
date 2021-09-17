/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.realms.RealmInfo;


/**
 * @author mbechler
 *
 */
public class RealmInfoComparator implements Comparator<RealmInfo>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8401973202595682243L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( RealmInfo o1, RealmInfo o2 ) {
        return o1.getRealmName().compareTo(o2.getRealmName());
    }

}
