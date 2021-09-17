/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.runtime.security.principal;


import java.io.Serializable;

import org.joda.time.DateTime;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class UserInfoImpl implements UserInfo, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6995912591947541818L;

    private UserPrincipal userPrincipal;
    private DateTime created;
    private DateTime lastPwChange;
    private DateTime expires;
    private DateTime pwExpiry;
    private DateTime lastSuccessfulLogin;
    private DateTime lastFailedLogin;
    private Integer failedLoginAttempts;
    private Boolean disabled;


    /**
     * 
     */
    public UserInfoImpl () {}


    /**
     * @param ui
     */
    public UserInfoImpl ( UserInfo ui ) {
        this.userPrincipal = ui.getUserPrincipal();
        this.created = ui.getCreated();
        this.lastPwChange = ui.getLastPwChange();
        this.pwExpiry = ui.getPwExpiry();
        this.expires = ui.getExpires();
        this.lastSuccessfulLogin = ui.getLastSuccessfulLogin();
        this.lastFailedLogin = ui.getLastFailedLogin();
        this.failedLoginAttempts = ui.getFailedLoginAttempts();
        this.disabled = ui.getDisabled();
    }


    /**
     * @return the userPrincipal
     */
    @Override
    public UserPrincipal getUserPrincipal () {
        return this.userPrincipal;
    }


    /**
     * @param userPrincipal
     *            the userPrincipal to set
     */
    public void setUserPrincipal ( UserPrincipal userPrincipal ) {
        this.userPrincipal = userPrincipal;
    }


    /**
     * @return the created
     */
    @Override
    public DateTime getCreated () {
        return this.created;
    }


    /**
     * @param created
     *            the created to set
     */
    public void setCreated ( DateTime created ) {
        this.created = created;
    }


    /**
     * @return the lastPwChange
     */
    @Override
    public DateTime getLastPwChange () {
        return this.lastPwChange;
    }


    /**
     * @param lastPwChange
     *            the lastPwChange to set
     */
    public void setLastPwChange ( DateTime lastPwChange ) {
        this.lastPwChange = lastPwChange;
    }


    /**
     * @return the pwExpiry
     */
    @Override
    public DateTime getPwExpiry () {
        return this.pwExpiry;
    }


    /**
     * @param pwExpiry
     *            the pwExpiry to set
     */
    public void setPwExpiry ( DateTime pwExpiry ) {
        this.pwExpiry = pwExpiry;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserInfo#getExpires()
     */
    @Override
    public DateTime getExpires () {
        return this.expires;
    }


    /**
     * @param expires
     *            the expires to set
     */
    public void setExpires ( DateTime expires ) {
        this.expires = expires;
    }


    /**
     * @return the lastSuccessfulLogin
     */
    @Override
    public DateTime getLastSuccessfulLogin () {
        return this.lastSuccessfulLogin;
    }


    /**
     * @param lastSuccessfulLogin
     *            the lastSuccessfulLogin to set
     */
    public void setLastSuccessfulLogin ( DateTime lastSuccessfulLogin ) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
    }


    /**
     * @return the lastFailedLogin
     */
    @Override
    public DateTime getLastFailedLogin () {
        return this.lastFailedLogin;
    }


    /**
     * @param lastFailedLogin
     *            the lastFailedLogin to set
     */
    public void setLastFailedLogin ( DateTime lastFailedLogin ) {
        this.lastFailedLogin = lastFailedLogin;
    }


    /**
     * @return the failedLoginAttempts
     */
    @Override
    public Integer getFailedLoginAttempts () {
        return this.failedLoginAttempts;
    }


    /**
     * @param failedLoginAttempts
     *            the failedLoginAttempts to set
     */
    public void setFailedLoginAttempts ( Integer failedLoginAttempts ) {
        this.failedLoginAttempts = failedLoginAttempts;
    }


    /**
     * @return the disabled
     */
    @Override
    public Boolean getDisabled () {
        return this.disabled;
    }


    /**
     * @param disabled
     *            the disabled to set
     */
    public void setDisabled ( Boolean disabled ) {
        this.disabled = disabled;
    }

}
