/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security.token;


/**
 * @author mbechler
 *
 */
public class RealmOTPPasswordChangeToken extends RealmOTPToken implements PasswordChangeToken {

    /**
     * 
     */
    private static final long serialVersionUID = 5459490702738848075L;
    private String newPassword;


    /**
     * 
     */
    public RealmOTPPasswordChangeToken () {
        super();
    }


    /**
     * @param username
     * @param oldPassword
     * @param pin
     * @param newPassword
     * @param host
     */
    public RealmOTPPasswordChangeToken ( String username, String oldPassword, String pin, String newPassword, String host ) {
        super(username, oldPassword, host);
        setNewPassword(newPassword);
    }


    /**
     * @param username
     * @param oldPassword
     * @param pin
     * @param newPassword
     */
    public RealmOTPPasswordChangeToken ( String username, String oldPassword, String pin, String newPassword ) {
        super(username, pin, oldPassword);
        setNewPassword(newPassword);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.token.PasswordChangeToken#getNewPassword()
     */
    @Override
    public String getNewPassword () {
        return this.newPassword;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.token.PasswordChangeToken#setNewPassword(java.lang.String)
     */
    @Override
    public final void setNewPassword ( String newPassword ) {
        this.newPassword = newPassword;
    }

}
