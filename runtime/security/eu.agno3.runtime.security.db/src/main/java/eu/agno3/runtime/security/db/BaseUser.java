/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.10.2014 by mbechler
 */
package eu.agno3.runtime.security.db;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Entity
@PersistenceUnit ( unitName = "auth" )
@Table ( name = "users", indexes = @Index ( columnList = "username", unique = true ) )
public class BaseUser extends BaseAuthObject implements UserInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 5049344270191639260L;

    private static final String LOCAL_REALM = "LOCAL"; //$NON-NLS-1$

    private String passwordHash;
    private String userName;
    private String salt;
    private DateTime created;

    private DateTime lastPwChange;
    private DateTime lastSuccessfulLogin;
    private DateTime lastFailedLogin;

    private DateTime expires;
    private DateTime pwExpiry;

    private int failedLoginAttempts;
    private boolean disabled;


    /**
     * @return the associated principal
     */
    @Override
    @Transient
    public UserPrincipal getUserPrincipal () {
        return new UserPrincipal(LOCAL_REALM, this.getId(), this.userName);
    }


    /**
     * @return the username
     */
    @Basic
    @NotNull
    @Column ( nullable = false, name = "username", length = 100 )
    public String getUserName () {
        return this.userName;
    }


    /**
     * @param username
     *            the username to set
     */
    public void setUserName ( String username ) {
        this.userName = username;
    }


    /**
     * @return the salt
     */
    @Basic
    @NotNull
    @Column ( nullable = true, name = "password_salt", length = 100 )
    public String getSalt () {
        return this.salt;
    }


    /**
     * @param salt
     *            the salt to set
     */
    public void setSalt ( String salt ) {
        this.salt = salt;
    }


    /**
     * @return the passwordHash
     */
    @Basic
    @NotNull
    @Column ( nullable = true, name = "password", length = 100 )
    public String getPasswordHash () {
        return this.passwordHash;
    }


    /**
     * @param passwordHash
     *            the passwordHash to set
     */
    public void setPasswordHash ( String passwordHash ) {
        this.passwordHash = passwordHash;
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
     * @return the expires
     */
    @Override
    @Column ( name = "expires", nullable = true )
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
     * @return the lastPwChange
     */
    @Override
    @Column ( name = "last_pw_change", nullable = true )
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
     * @return the lastFailedLogin
     */
    @Override
    @Column ( name = "last_failed", nullable = true )
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
     * @return the lastSuccessfulLogin
     */
    @Override
    @Column ( name = "last_success", nullable = true )
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
     * @return the failedLoginAttempts
     */
    @Override
    @Column ( name = "fail_attempts" )
    public Integer getFailedLoginAttempts () {
        return this.failedLoginAttempts;
    }


    /**
     * @param failedLoginAttempts
     *            the failedLoginAttempts to set
     */
    public void setFailedLoginAttempts ( int failedLoginAttempts ) {
        this.failedLoginAttempts = failedLoginAttempts;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserInfo#getPwExpiry()
     */
    @Override
    @Column ( name = "pw_expires", nullable = true )
    public DateTime getPwExpiry () {
        return this.pwExpiry;
    }


    /**
     * @param pwExpiry
     *            the pwExpires to set
     */
    public void setPwExpiry ( DateTime pwExpiry ) {
        this.pwExpiry = pwExpiry;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.UserInfo#getDisabled()
     */
    @Override
    @Column ( name = "disabled", nullable = false )
    public Boolean getDisabled () {
        return this.disabled;
    }


    /**
     * @param disabled
     *            the disabled to set
     */
    public void setDisabled ( boolean disabled ) {
        this.disabled = disabled;
    }

}
