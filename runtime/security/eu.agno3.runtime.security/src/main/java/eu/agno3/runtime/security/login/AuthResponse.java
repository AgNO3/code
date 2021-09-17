/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.08.2016 by mbechler
 */
package eu.agno3.runtime.security.login;


import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationInfo;


/**
 * @author mbechler
 *
 */
public class AuthResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2356602248738826018L;

    private final AuthResponseType type;
    private transient final AuthenticationInfo authInfo;


    /**
     * @param t
     */
    public AuthResponse ( AuthResponseType t ) {
        this(t, null);
    }


    /**
     * @param t
     * @param i
     */
    public AuthResponse ( AuthResponseType t, AuthenticationInfo i ) {
        this.type = t;
        this.authInfo = i;
    }


    /**
     * @return the type
     */
    public AuthResponseType getType () {
        return this.type;
    }


    /**
     * @return the authInfo
     */
    public AuthenticationInfo getAuthInfo () {
        return this.authInfo;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.type + ( this.authInfo != null ? ": " + this.authInfo : StringUtils.EMPTY ); //$NON-NLS-1$
    }
}
