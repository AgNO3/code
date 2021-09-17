/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.03.2015 by mbechler
 */
package eu.agno3.runtime.security.db.impl;


import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import eu.agno3.runtime.security.SerializableByteSourceWrapper;


/**
 * @author mbechler
 *
 */
public class DatabaseAuthenticationInfo extends SimpleAuthenticationInfo {

    private UserQueryResult userResult;


    /**
     * @param r
     * @param principalCollectionForUser
     * @param credentials
     */
    public DatabaseAuthenticationInfo ( UserQueryResult r, PrincipalCollection principalCollectionForUser, char[] credentials ) {
        super(principalCollectionForUser, credentials);
        this.userResult = r;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6654688060181976208L;


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.SimpleAuthenticationInfo#getCredentialsSalt()
     */
    @Override
    public ByteSource getCredentialsSalt () {
        if ( this.userResult == null || this.userResult.getSalt() == null ) {
            return null;
        }
        // otherwise we might end up with non serializable AuthenticationInfo on merge
        return new SerializableByteSourceWrapper(ByteSource.Util.bytes(this.userResult.getSalt()));
    }


    /**
     * @return the userResult
     */
    public UserQueryResult getUserResult () {
        return this.userResult;
    }
}
