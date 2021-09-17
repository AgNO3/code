/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2015 by mbechler
 */
package eu.agno3.runtime.jmx;


import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Set;

import javax.management.remote.JMXPrincipal;


/**
 * @author mbechler
 *
 */
public final class JMXSecurityUtil {

    /**
     * 
     */
    private JMXSecurityUtil () {}


    /**
     * @return whether the current call is coming from the JMX management interface
     */
    public static boolean isManagementCall () {
        AccessControlContext acc = AccessController.getContext();
        if ( acc == null ) {
            return false;
        }
        javax.security.auth.Subject subject = javax.security.auth.Subject.getSubject(acc);

        if ( subject != null ) {
            Set<JMXPrincipal> principals = subject.getPrincipals(JMXPrincipal.class);
            if ( principals != null && !principals.isEmpty() ) {
                return true;
            }
        }

        return false;
    }
}
