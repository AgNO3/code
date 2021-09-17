/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.05.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


import eu.agno3.fileshare.model.Grant;


/**
 * @author mbechler
 *
 */
public class GrantAuthenticationRequiredException extends AuthenticationException {

    /**
     * 
     */
    private static final long serialVersionUID = -6435874364133376766L;

    private Grant grant;


    /**
     * 
     */
    public GrantAuthenticationRequiredException () {
        super();
    }


    /**
     * @param g
     * @param msg
     * @param t
     */
    public GrantAuthenticationRequiredException ( Grant g, String msg, Throwable t ) {
        super(msg, t);
        this.grant = g;
    }


    /**
     * @param g
     * @param msg
     */
    public GrantAuthenticationRequiredException ( Grant g, String msg ) {
        super(msg);
        this.grant = g;
    }


    /**
     * @param g
     * @param cause
     */
    public GrantAuthenticationRequiredException ( Grant g, Throwable cause ) {
        super(cause);
        this.grant = g;
    }


    /**
     * @return the grant
     */
    public Grant getGrant () {
        return this.grant;
    }
}
