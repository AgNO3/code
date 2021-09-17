/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.auth.internal;


import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.security.SecurityContext;

import eu.agno3.orchestrator.server.component.auth.AuthConstants;
import eu.agno3.orchestrator.server.component.auth.SystemPrincipal;
import eu.agno3.orchestrator.server.component.auth.SystemSecurityContext;


/**
 * @author mbechler
 *
 */
public class SystemSecurityContextImpl extends SecurityContext implements SystemSecurityContext {

    /**
     * 
     */
    public SystemSecurityContextImpl () {
        super(AuthConstants.SYSTEM_USER);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.security.SecurityContext#getPrincipals()
     */
    @Override
    public Set<Principal> getPrincipals () {
        return new HashSet<>(Arrays.asList((Principal) new SystemPrincipal()));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.security.SecurityContext#isBrokerContext()
     */
    @Override
    public boolean isBrokerContext () {
        return true;
    }
}
