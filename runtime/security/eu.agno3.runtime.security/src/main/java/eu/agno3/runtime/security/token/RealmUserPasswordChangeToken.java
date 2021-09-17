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
public class RealmUserPasswordChangeToken extends RealmUserPasswordToken implements PasswordChangeToken {

    /**
     * 
     */
    private static final long serialVersionUID = 5459490702738848075L;
    private String newPassword;


    /**
     * 
     */
    public RealmUserPasswordChangeToken () {
        super();
    }


    /**
     * @param username
     * @param oldPassword
     * @param newPassword
     * @param rememberMe
     * @param host
     */
    public RealmUserPasswordChangeToken ( String username, String oldPassword, String newPassword, boolean rememberMe, String host ) {
        super(username, oldPassword, rememberMe, host);
        setNewPassword(newPassword);
    }


    /**
     * @param username
     * @param oldPassword
     * @param newPassword
     * @param rememberMe
     */
    public RealmUserPasswordChangeToken ( String username, String oldPassword, String newPassword, boolean rememberMe ) {
        super(username, oldPassword, rememberMe);
        setNewPassword(newPassword);
    }


    /**
     * @param username
     * @param oldPassword
     * @param newPassword
     * @param host
     */
    public RealmUserPasswordChangeToken ( String username, String oldPassword, String newPassword, String host ) {
        super(username, oldPassword, host);
        setNewPassword(newPassword);
    }


    /**
     * @param username
     * @param oldPassword
     * @param newPassword
     */
    public RealmUserPasswordChangeToken ( String username, String oldPassword, String newPassword ) {
        super(username, oldPassword);
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
