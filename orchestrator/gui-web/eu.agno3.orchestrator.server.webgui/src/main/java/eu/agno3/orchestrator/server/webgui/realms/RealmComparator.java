/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.realms.RealmConfig;


/**
 * @author mbechler
 *
 */
public class RealmComparator implements Comparator<RealmConfig>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2302810518643909945L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( RealmConfig o1, RealmConfig o2 ) {

        if ( o1.getRealmName() == null && o2.getRealmName() == null ) {
            return 0;
        }
        else if ( o1.getRealmName() == null ) {
            return -1;
        }
        else if ( o2.getRealmName() == null ) {
            return 1;
        }

        return o1.getRealmName().compareTo(o2.getRealmName());
    }

}
