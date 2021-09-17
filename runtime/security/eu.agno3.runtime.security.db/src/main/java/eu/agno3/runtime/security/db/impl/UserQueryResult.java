/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security.db.impl;


import java.io.Serializable;
import java.util.UUID;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public final class UserQueryResult implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8260737208332801392L;

    private String salt;
    private String hash;
    private int failedLoginAttempts;
    private boolean disabled;
    private DateTime expires;
    private DateTime pwExpiry;
    private UUID userId;
    private DateTime lastPwChange;


    /**
     * @param userId
     * @param salt
     * @param hash
     * @param failedLoginAttempts
     * @param disabled
     * @param expires
     * @param pwExpiry
     * @param lastPwChange
     */
    public UserQueryResult ( UUID userId, String hash, String salt, int failedLoginAttempts, boolean disabled, DateTime expires, DateTime pwExpiry,
            DateTime lastPwChange ) {
        super();
        this.userId = userId;
        this.salt = salt;
        this.hash = hash;
        this.failedLoginAttempts = failedLoginAttempts;
        this.disabled = disabled;
        this.expires = expires;
        this.pwExpiry = pwExpiry;
        this.lastPwChange = lastPwChange;
    }


    /**
     * @return the salt
     */
    public String getSalt () {
        return this.salt;
    }


    /**
     * @return the hash
     */
    public String getHash () {
        return this.hash;
    }


    /**
     * @return the failedLoginAttempts
     */
    public int getFailedLoginAttempts () {
        return this.failedLoginAttempts;
    }


    /**
     * @return the disabled
     */
    public boolean isDisabled () {
        return this.disabled;
    }


    /**
     * @return the expires
     */
    public DateTime getExpires () {
        return this.expires;
    }


    /**
     * @return the pwExpiry
     */
    public DateTime getPwExpiry () {
        return this.pwExpiry;
    }


    /**
     * @return the user id
     */
    public UUID getUserId () {
        return this.userId;
    }


    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId ( UUID userId ) {
        this.userId = userId;
    }


    /**
     * @return the last password change time
     */
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

}