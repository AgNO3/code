/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security;


import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;


/**
 * @author mbechler
 *
 */
public interface ExtendedCredentialsMatcher extends CredentialsMatcher {

    /**
     * 
     * @param info
     * @return whether the hash needs an update to conform with the configured policy
     */
    boolean hashNeedsUpdate ( AuthenticationInfo info );


    /**
     * 
     * @param tok
     * @param info
     * @return a new hash value for the current password
     */
    SaltedHash updateHash ( AuthenticationToken tok, AuthenticationInfo info );


    /**
     * @param password
     * @return a hash value for the given password
     */
    SaltedHash generatePasswordHash ( String password );

}