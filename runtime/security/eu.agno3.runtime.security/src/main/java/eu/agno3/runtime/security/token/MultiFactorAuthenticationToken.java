/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Aug 5, 2016 by mbechler
 */
package eu.agno3.runtime.security.token;


import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;


/**
 * @author mbechler
 *
 */
public class MultiFactorAuthenticationToken implements AuthenticationToken {

    /**
     * 
     */
    private static final long serialVersionUID = 903164219347685851L;
    private AuthenticationInfo info;


    /**
     * @param info
     */
    public MultiFactorAuthenticationToken ( AuthenticationInfo info ) {
        this.info = info;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getCredentials()
     */
    @Override
    public Object getCredentials () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getPrincipal()
     */
    @Override
    public Object getPrincipal () {
        return null;
    }


    /**
     * @return the info
     */
    public AuthenticationInfo getInfo () {
        return this.info;
    }
}
