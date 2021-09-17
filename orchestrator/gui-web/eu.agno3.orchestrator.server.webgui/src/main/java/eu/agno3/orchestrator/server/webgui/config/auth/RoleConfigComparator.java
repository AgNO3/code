/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.auth.RoleConfig;


/**
 * @author mbechler
 *
 */
public class RoleConfigComparator implements Comparator<RoleConfig>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 223947718042181543L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( RoleConfig o1, RoleConfig o2 ) {

        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return -1;
        }
        else if ( o2 == null ) {
            return 1;
        }

        String i1 = o1.getRoleId();
        String i2 = o2.getRoleId();

        if ( i1 == null && i2 == null ) {
            return 0;
        }
        else if ( i1 == null ) {
            return -1;
        }
        else if ( i2 == null ) {
            return 1;
        }

        return i1.compareTo(i2);
    }

}
