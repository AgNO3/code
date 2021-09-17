/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.model.UserCreateData;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.fileshare.webgui.admin.quota.QuotaFormatter;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.validation.email.ValidEmail;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "app_fs_adm_userCreateContext" )
public class UserCreateContext implements Serializable {

    /**
     * 
     */
    private static final String ADMIN_CREATED_USER = "ADMIN_CREATED_USER"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -7807597353902394431L;

    private UserCreateData data = new UserCreateData();

    private boolean quotaEnabled;
    private long quotaSize;
    private int quotaExponent = 2;

    @Inject
    private FileshareAdminServiceProvider fsp;


    @PostConstruct
    protected void init () {
        this.data.setRoles(new ArrayList<>(this.fsp.getConfigurationProvider().getUserDefaultRoles()));
        Long defaultQuota = this.fsp.getConfigurationProvider().getGlobalDefaultQuota();

        if ( defaultQuota != null ) {
            this.quotaEnabled = true;
            this.quotaExponent = QuotaFormatter.getBaseExponent(defaultQuota);
            this.quotaSize = (long) ( defaultQuota / Math.pow(1000, this.quotaExponent) );
        }
    }


    /**
     * 
     */
    public void reset () {
        this.data = new UserCreateData();
        this.data.setRoles(new ArrayList<>(this.fsp.getConfigurationProvider().getUserDefaultRoles()));

        this.quotaEnabled = false;
        this.quotaExponent = 2;
        this.quotaSize = 0;
    }


    /**
     * @return the userName
     */
    public String getUserName () {
        return this.data.getUserName();
    }


    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName ( String userName ) {
        this.data.setUserName(userName);
    }


    /**
     * @return the fullName
     */
    public String getPreferredName () {
        return this.data.getUserDetails().getPreferredName();
    }


    /**
     * @param fullName
     *            the fullName to set
     */
    public void setPreferredName ( String fullName ) {
        this.data.getUserDetails().setPreferredName(fullName);
    }


    /**
     * @return the mailAddress
     */
    @ValidEmail
    public String getMailAddress () {
        return this.data.getUserDetails().getMailAddress();
    }


    /**
     * @param mailAddress
     *            the mailAddress to set
     */
    public void setMailAddress ( String mailAddress ) {
        this.data.getUserDetails().setMailAddress(mailAddress);
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.data.getPassword();
    }


    /**
     * @param password
     *            the password to set
     */
    public void setPassword ( String password ) {
        this.data.setPassword(password);
    }


    /**
     * @return the securityLabel
     */
    public String getSecurityLabel () {
        return this.data.getSecurityLabel();
    }


    /**
     * @param securityLabel
     *            the securityLabel to set
     */
    public void setSecurityLabel ( String securityLabel ) {
        this.data.setSecurityLabel(securityLabel);
    }


    /**
     * @return the forcePasswordChange
     */
    public boolean getForcePasswordChange () {
        return this.data.getForcePasswordChange();
    }


    /**
     * @param forcePasswordChange
     *            the forcePasswordChange to set
     */
    public void setForcePasswordChange ( boolean forcePasswordChange ) {
        this.data.setForcePasswordChange(forcePasswordChange);
    }


    /**
     * @return the disabled
     */
    public boolean getDisabled () {
        return this.data.getDisabled();
    }


    /**
     * @param disabled
     *            the disabled to set
     */
    public void setDisabled ( boolean disabled ) {
        this.data.setDisabled(disabled);
    }


    /**
     * @return the permissions
     */
    public List<String> getRoles () {
        return this.data.getRoles();
    }


    /**
     * @param roles
     *            the roles to set
     */
    public void setRoles ( List<String> roles ) {
        this.data.setRoles(roles);
    }


    /**
     * @return the quotaEnabled
     */
    public boolean getQuotaEnabled () {
        return this.quotaEnabled;
    }


    /**
     * @param quotaEnabled
     *            the quotaEnabled to set
     */
    public void setQuotaEnabled ( boolean quotaEnabled ) {
        this.quotaEnabled = quotaEnabled;
    }


    /**
     * @return the quotaExponent
     */
    public int getQuotaExponent () {
        return this.quotaExponent;
    }


    /**
     * @param quotaExponent
     *            the quotaExponent to set
     */
    public void setQuotaExponent ( int quotaExponent ) {
        this.quotaExponent = quotaExponent;
    }


    /**
     * @return the quotaSize
     */
    public long getQuotaSize () {
        return this.quotaSize;
    }


    /**
     * @param quotaSize
     *            the quotaSize to set
     */
    public void setQuotaSize ( long quotaSize ) {
        this.quotaSize = quotaSize;
    }


    /**
     * 
     * @return the password policy
     */
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.fsp.getPasswordPolicy();
    }


    /**
     * @return noRoot
     */
    public boolean getNoRoot () {
        return this.data.getNoSubjectRoot();
    }


    /**
     * @param noRoot
     *            the noRoot to set
     */
    public void setNoRoot ( boolean noRoot ) {
        this.data.setNoSubjectRoot(noRoot);
    }


    /**
     * @return the data
     */
    public UserCreateData getData () {
        if ( this.getQuotaEnabled() ) {
            this.data.setQuota(this.getQuotaSize() * (long) Math.pow(1000, this.getQuotaExponent()));
        }
        else {
            this.data.setQuota(null);
        }

        if ( !this.data.getRoles().contains(ADMIN_CREATED_USER) ) {
            this.data.getRoles().add(ADMIN_CREATED_USER); // $NON-NLS-1$
        }

        return this.data;
    }

}
