/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.group;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.fileshare.webgui.admin.quota.QuotaFormatter;
import eu.agno3.runtime.validation.email.ValidEmail;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "app_fs_adm_groupCreateContext" )
public class GroupCreateContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7807597353902394431L;
    private String groupName;

    private boolean createRoot = true;

    private boolean disableNotifications;

    private Locale groupLocale;

    private String notificationOverrideAddress;

    private boolean quotaEnabled;
    private long quotaSize;
    private int quotaExponent = 2;

    private Set<String> roles = new HashSet<>();

    @Inject
    private FileshareAdminServiceProvider fsp;


    @PostConstruct
    protected void init () {
        Long defaultQuota = this.fsp.getConfigurationProvider().getGlobalDefaultQuota();
        if ( defaultQuota != null ) {
            this.quotaEnabled = true;
            this.quotaExponent = QuotaFormatter.getBaseExponent(defaultQuota);
            this.quotaSize = (long) ( defaultQuota / Math.pow(1000, this.quotaExponent) );
        }
    }


    /**
     * @return the groupName
     */
    public String getGroupName () {
        return this.groupName;
    }


    /**
     * @param groupName
     *            the groupName to set
     */
    public void setGroupName ( String groupName ) {
        this.groupName = groupName;
    }


    /**
     * @return the roles for the group
     */
    public Set<String> getRoles () {
        return this.roles;
    }


    /**
     * @param roles
     *            the roles to set
     */
    public void setRoles ( Set<String> roles ) {
        this.roles = roles;
    }


    /**
     * @return the createRoot
     */
    public boolean getCreateRoot () {
        return this.createRoot;
    }


    /**
     * @param createRoot
     *            the createRoot to set
     */
    public void setCreateRoot ( boolean createRoot ) {
        this.createRoot = createRoot;
    }


    /**
     * @return the disableNotifications
     */
    public boolean getDisableNotifications () {
        return this.disableNotifications;
    }


    /**
     * @param disableNotifications
     *            the disableNotifications to set
     */
    public void setDisableNotifications ( boolean disableNotifications ) {
        this.disableNotifications = disableNotifications;
    }


    /**
     * @return the groupLocale
     */
    public Locale getGroupLocale () {
        return this.groupLocale;
    }


    /**
     * @param groupLocale
     *            the groupLocale to set
     */
    public void setGroupLocale ( Locale groupLocale ) {
        this.groupLocale = groupLocale;
    }


    /**
     * @return the notificationOverrideAddress
     */
    @ValidEmail
    public String getNotificationOverrideAddress () {
        return this.notificationOverrideAddress;
    }


    /**
     * @param notificationOverrideAddress
     *            the notificationOverrideAddress to set
     */
    public void setNotificationOverrideAddress ( String notificationOverrideAddress ) {
        this.notificationOverrideAddress = notificationOverrideAddress;
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

}
