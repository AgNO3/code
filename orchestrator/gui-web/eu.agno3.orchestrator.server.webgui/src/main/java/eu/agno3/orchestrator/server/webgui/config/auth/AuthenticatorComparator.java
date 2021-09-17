/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;


/**
 * @author mbechler
 *
 */
public class AuthenticatorComparator implements Comparator<AuthenticatorConfig>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8066389771910414349L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( AuthenticatorConfig o1, AuthenticatorConfig o2 ) {

        if ( o1.getRealm() == null && o2.getRealm() == null ) {
            return o1.getId().compareTo(o2.getId());
        }
        else if ( o1.getRealm() == null ) {
            return -1;
        }
        else if ( o2.getRealm() == null ) {
            return 1;
        }
        return o1.getRealm().compareTo(o2.getRealm());
    }

}
