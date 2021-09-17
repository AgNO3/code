/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.02.2017 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.EnumSet;
import java.util.Set;

import org.apache.jackrabbit.webdav.security.Privilege;
import org.joda.time.DateTime;

import eu.agno3.fileshare.model.GrantType;


/**
 * @author mbechler
 *
 */
public class GrantSettings {

    private Set<GrantType> allowedTypes = EnumSet.noneOf(GrantType.class);
    private DateTime maxExpire;
    private DateTime defaultExpire;
    private int minTokenPasswordEntropy;
    private boolean noUserTokenPasswords;
    private boolean requireTokenPassword;

    private boolean notificationsAllowed;

    private String defaultMailSubject;

    private Privilege defaultPermissions;


    /**
     * @return the allowedTypes
     */
    public Set<GrantType> getAllowedTypes () {
        return this.allowedTypes;
    }


    /**
     * @param allowedTypes
     */
    public void setAllowedTypes ( Set<GrantType> allowedTypes ) {
        this.allowedTypes = allowedTypes;
    }


    /**
     * @return the maxExpire
     */
    public DateTime getMaxExpire () {
        return this.maxExpire;
    }


    /**
     * @param maxExpire
     */
    public void setMaxExpire ( DateTime maxExpire ) {
        this.maxExpire = maxExpire;
    }


    /**
     * @return the defaultExpire
     */
    public DateTime getDefaultExpire () {
        return this.defaultExpire;
    }


    /**
     * @param defaultExpire
     */
    public void setDefaultExpire ( DateTime defaultExpire ) {
        this.defaultExpire = defaultExpire;
    }


    /**
     * @return the minTokenPasswordEntropy
     */
    public int getMinTokenPasswordEntropy () {
        return this.minTokenPasswordEntropy;
    }


    /**
     * @param minTokenPasswordEntropy
     */
    public void setMinTokenPasswordEntropy ( int minTokenPasswordEntropy ) {
        this.minTokenPasswordEntropy = minTokenPasswordEntropy;
    }


    /**
     * @return the noUserTokenPasswords
     */
    public boolean isNoUserTokenPasswords () {
        return this.noUserTokenPasswords;
    }


    /**
     * @param noUserTokenPasswords
     */
    public void setNoUserTokenPassword ( boolean noUserTokenPasswords ) {
        this.noUserTokenPasswords = noUserTokenPasswords;
    }


    /**
     * @return the requireTokenPassword
     */
    public boolean isRequireTokenPassword () {
        return this.requireTokenPassword;
    }


    /**
     * @param requireTokenPassword
     */
    public void setRequireTokenPassword ( boolean requireTokenPassword ) {
        this.requireTokenPassword = requireTokenPassword;
    }


    /**
     * @return the defaultPermissions
     */
    public Privilege getDefaultPermissions () {
        return this.defaultPermissions;
    }


    /**
     * @param defaultPermissions
     *            the defaultPermissions to set
     */
    public void setDefaultPermissions ( Privilege defaultPermissions ) {
        this.defaultPermissions = defaultPermissions;
    }


    /**
     * @return the defaultMailSubject
     */
    public String getDefaultMailSubject () {
        return this.defaultMailSubject;
    }


    /**
     * @param defaultMailSubject
     *            the defaultMailSubject to set
     */
    public void setDefaultMailSubject ( String defaultMailSubject ) {
        this.defaultMailSubject = defaultMailSubject;
    }


    /**
     * @return the notificationsAllowed
     */
    public boolean isNotificationsAllowed () {
        return this.notificationsAllowed;
    }


    /**
     * @param notificationsAllowed
     *            the notificationsAllowed to set
     */
    public void setNotificationsAllowed ( boolean notificationsAllowed ) {
        this.notificationsAllowed = notificationsAllowed;
    }

}
