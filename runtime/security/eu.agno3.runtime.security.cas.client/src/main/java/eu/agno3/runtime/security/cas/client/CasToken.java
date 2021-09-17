/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.runtime.security.cas.client;


import org.apache.shiro.authc.AuthenticationToken;


/**
 * @author mbechler
 *
 */
public class CasToken implements AuthenticationToken {

    /**
     * 
     */
    private static final long serialVersionUID = 2442616165202116945L;
    private String ticket;
    private String principal;


    /**
     * @param ticket
     */
    public CasToken ( String ticket ) {
        this.ticket = ticket;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getCredentials()
     */
    @Override
    public Object getCredentials () {
        return this.ticket;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authc.AuthenticationToken#getPrincipal()
     */
    @Override
    public Object getPrincipal () {
        return this.principal;
    }


    /**
     * @param principal
     */
    public void setPrincipal ( String principal ) {
        this.principal = principal;
    }
}
