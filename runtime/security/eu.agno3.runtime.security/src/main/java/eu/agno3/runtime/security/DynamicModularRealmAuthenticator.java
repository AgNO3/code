/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.security;


import java.util.Collection;
import java.util.List;

import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.realm.Realm;

import eu.agno3.runtime.security.login.LoginRealm;


/**
 * @author mbechler
 *
 */
public interface DynamicModularRealmAuthenticator extends Authenticator {

    /**
     * @return the known realms
     */
    Collection<Realm> getRealms ();


    /**
     * @param primary
     * @return the auth stack for the given primary realm
     */
    List<LoginRealm> getStack ( LoginRealm primary );

}
