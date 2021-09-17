/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security;


import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface DynamicModularRealmAuthorizer extends Authorizer {

    /**
     * @param col
     * @return the combined authorization info for the given principal
     */
    SimpleAuthorizationInfo getAuthorizationInfo ( PrincipalCollection col );


    /**
     * @param collection
     * 
     */
    void clearCaches ( UserPrincipal collection );

}
