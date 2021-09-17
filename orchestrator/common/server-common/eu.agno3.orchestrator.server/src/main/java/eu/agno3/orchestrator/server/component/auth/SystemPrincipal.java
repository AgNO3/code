/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.auth;


import java.security.Principal;


/**
 * @author mbechler
 *
 */
public class SystemPrincipal implements Principal {

    /**
     * {@inheritDoc}
     *
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName () {
        return AuthConstants.SYSTEM_USER;
    }

}
