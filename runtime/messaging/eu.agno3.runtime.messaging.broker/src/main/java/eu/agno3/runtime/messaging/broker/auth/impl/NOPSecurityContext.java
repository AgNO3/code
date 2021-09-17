/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth.impl;


import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.security.SecurityContext;


/**
 * @author mbechler
 *
 */
class NOPSecurityContext extends SecurityContext {

    /**
     * @param info
     */
    NOPSecurityContext ( ConnectionInfo info ) {
        super(info.getUserName());
    }


    @Override
    public Set<Principal> getPrincipals () {
        Set<Principal> principals = new HashSet<>();

        principals.addAll(Arrays.asList(new Principal() {

            @Override
            public String getName () {
                return "AUTHED"; //$NON-NLS-1$
            }

        }));
        return principals;
    }
}