/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security;


import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface AuthorizationInfoProvider {

    /**
     * @param princs
     * @return the authorization info for the given principals
     */
    AuthorizationInfo fetchAuthorizationInfo ( PrincipalCollection princs );


    /**
     * @param princs
     */
    void clearCaches ( UserPrincipal princs );

}
