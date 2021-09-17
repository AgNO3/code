/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.12.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class UserCreateData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -221521135196624161L;

    private String userName;
    private boolean disabled;
    private boolean forcePasswordChange;
    private boolean noSubjectRoot;
    private DateTime expires;
    private UserDetails userDetails = new UserDetails();

    private String password;

    private Long quota;
    private String securityLabel;

    private List<String> roles = new ArrayList<>();


    /**
     * @return the user name
     */
    public String getUserName () {
        return this.userName;
    }


    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName ( String userName ) {
        this.userName = userName;
    }


    /**
     * @return whether the user should be disabled
     */
    public boolean getDisabled () {
        return this.disabled;
    }


    /**
     * @param disabled
     *            the disabled to set
     */
    public void setDisabled ( boolean disabled ) {
        this.disabled = disabled;
    }


    /**
     * @return whether to force an immediate password change
     */
    public boolean getForcePasswordChange () {
        return this.forcePasswordChange;
    }


    /**
     * @param forcePasswordChange
     *            the forcePasswordChange to set
     */
    public void setForcePasswordChange ( boolean forcePasswordChange ) {
        this.forcePasswordChange = forcePasswordChange;
    }


    /**
     * @return whether the user should have a subject root
     */
    public boolean getNoSubjectRoot () {
        return this.noSubjectRoot;
    }


    /**
     * @param noSubjectRoot
     *            the noSubjectRoot to set
     */
    public void setNoSubjectRoot ( boolean noSubjectRoot ) {
        this.noSubjectRoot = noSubjectRoot;
    }


    /**
     * @return the user expiration date
     */
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
     * @return the user details
     */
    public UserDetails getUserDetails () {
        return this.userDetails;
    }


    /**
     * @param userDetails
     *            the userDetails to set
     */
    public void setUserDetails ( UserDetails userDetails ) {
        this.userDetails = userDetails;
    }


    /**
     * @return the user password
     */
    public String getPassword () {
        return this.password;
    }


    /**
     * @param password
     *            the password to set
     */
    public void setPassword ( String password ) {
        this.password = password;
    }


    /**
     * @return the user security label
     */
    public String getSecurityLabel () {
        return this.securityLabel;
    }


    /**
     * @param securityLabel
     *            the securityLabel to set
     */
    public void setSecurityLabel ( String securityLabel ) {
        this.securityLabel = securityLabel;
    }


    /**
     * @return the roles
     */
    public List<String> getRoles () {
        return this.roles;
    }


    /**
     * @param roles
     */
    public void setRoles ( List<String> roles ) {
        this.roles = roles;
    }


    /**
     * @return the quota
     */
    public Long getQuota () {
        return this.quota;
    }


    /**
     * @param quota
     */
    public void setQuota ( Long quota ) {
        this.quota = quota;
    }

}
