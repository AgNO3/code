/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2015 by mbechler
 */
package eu.agno3.runtime.security.token;


/**
 * @author mbechler
 *
 */
public class FallbackRealmUserPasswordChangeToken extends RealmUserPasswordChangeToken {

    /**
     * 
     */
    private static final long serialVersionUID = 696822168119208083L;


    /**
     * 
     */
    public FallbackRealmUserPasswordChangeToken () {
        super();
    }


    /**
     * @param username
     * @param oldPassword
     * @param newPassword
     * @param rememberMe
     * @param host
     */
    public FallbackRealmUserPasswordChangeToken ( String username, String oldPassword, String newPassword, boolean rememberMe, String host ) {
        super(username, oldPassword, newPassword, rememberMe, host);
    }


    /**
     * @param username
     * @param oldPassword
     * @param newPassword
     * @param rememberMe
     */
    public FallbackRealmUserPasswordChangeToken ( String username, String oldPassword, String newPassword, boolean rememberMe ) {
        super(username, oldPassword, newPassword, rememberMe);
    }


    /**
     * @param username
     * @param oldPassword
     * @param newPassword
     * @param host
     */
    public FallbackRealmUserPasswordChangeToken ( String username, String oldPassword, String newPassword, String host ) {
        super(username, oldPassword, newPassword, host);
    }


    /**
     * @param username
     * @param oldPassword
     * @param newPassword
     */
    public FallbackRealmUserPasswordChangeToken ( String username, String oldPassword, String newPassword ) {
        super(username, oldPassword, newPassword);
    }

}
