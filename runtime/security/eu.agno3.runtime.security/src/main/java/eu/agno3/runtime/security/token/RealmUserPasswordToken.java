/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security.token;


import org.apache.shiro.authc.UsernamePasswordToken;


/**
 * @author mbechler
 *
 */
public class RealmUserPasswordToken extends UsernamePasswordToken {

    /**
     * 
     */
    private static final long serialVersionUID = -3925620377726277661L;

    private String realmName;


    /**
     * 
     */
    public RealmUserPasswordToken () {
        super();
    }


    /**
     * @param username
     * @param password
     * @param rememberMe
     * @param host
     */
    public RealmUserPasswordToken ( String username, String password, boolean rememberMe, String host ) {
        super(username, password, rememberMe, host);
    }


    /**
     * @param username
     * @param password
     * @param rememberMe
     */
    public RealmUserPasswordToken ( String username, String password, boolean rememberMe ) {
        super(username, password, rememberMe);
    }


    /**
     * @param username
     * @param password
     * @param host
     */
    public RealmUserPasswordToken ( String username, String password, String host ) {
        super(username, password, host);
    }


    /**
     * @param username
     * @param password
     */
    public RealmUserPasswordToken ( String username, String password ) {
        super(username, password);
    }


    /**
     * @return the realmName
     */
    public String getRealmName () {
        return this.realmName;
    }


    /**
     * @param realmName
     *            the realmName to set
     */
    public void setRealmName ( String realmName ) {
        this.realmName = realmName;
    }
}
