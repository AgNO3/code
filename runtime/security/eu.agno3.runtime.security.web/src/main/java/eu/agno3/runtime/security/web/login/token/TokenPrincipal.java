/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login.token;


import java.io.Serializable;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class TokenPrincipal implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3760248908307654260L;
    private DateTime expires;
    private Serializable data;
    private String token;


    /**
     * @param token
     * @param expires
     * @param data
     */
    public TokenPrincipal ( String token, DateTime expires, Serializable data ) {
        this.token = token;
        this.expires = expires;
        this.data = data;
    }


    /**
     * @return the token
     */
    public String getToken () {
        return this.token;
    }


    /**
     * @return the expires
     */
    public DateTime getExpires () {
        return this.expires;
    }


    /**
     * @return the data
     */
    public Serializable getData () {
        return this.data;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("Token: %s (expires: %s, data: %s)", this.token, this.expires, this.data); //$NON-NLS-1$
    }
}
