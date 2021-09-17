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
public class RealmOTPToken extends UsernamePasswordToken {

    /**
     * 
     */
    private static final long serialVersionUID = -3925620377726277661L;

    private String realmName;
    private String pin;


    /**
     * 
     */
    public RealmOTPToken () {
        super();
    }


    /**
     * @param username
     * @param password
     * @param pin
     * @param host
     */
    public RealmOTPToken ( String username, String password, String pin, String host ) {
        super(username, password, host);
        this.pin = pin;
    }


    /**
     * @param username
     * @param password
     * @param pin
     */
    public RealmOTPToken ( String username, String password, String pin ) {
        super(username, password);
        this.pin = pin;
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


    /**
     * @return the pin
     */
    public String getPin () {
        return this.pin;
    }


    /**
     * @param pin
     *            the pin to set
     */
    public void setPin ( String pin ) {
        this.pin = pin;
    }
}
