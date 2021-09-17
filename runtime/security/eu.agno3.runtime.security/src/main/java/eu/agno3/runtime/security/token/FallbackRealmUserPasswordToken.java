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
public class FallbackRealmUserPasswordToken extends RealmUserPasswordToken {

    /**
     * 
     */
    public FallbackRealmUserPasswordToken () {
        super();
    }


    /**
     * @param username
     * @param password
     * @param rememberMe
     * @param host
     */
    public FallbackRealmUserPasswordToken ( String username, String password, boolean rememberMe, String host ) {
        super(username, password, rememberMe, host);
    }


    /**
     * @param username
     * @param password
     * @param rememberMe
     */
    public FallbackRealmUserPasswordToken ( String username, String password, boolean rememberMe ) {
        super(username, password, rememberMe);
    }


    /**
     * @param username
     * @param password
     * @param host
     */
    public FallbackRealmUserPasswordToken ( String username, String password, String host ) {
        super(username, password, host);
    }


    /**
     * @param username
     * @param password
     */
    public FallbackRealmUserPasswordToken ( String username, String password ) {
        super(username, password);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8585944746913150348L;

}
