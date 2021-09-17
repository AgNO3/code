/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class ShareProperties implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8381581528191032729L;

    private Set<GrantPermission> permissions = EnumSet.of(GrantPermission.BROWSE, GrantPermission.READ);

    private DateTime expiry;

    private String message;

    private String subject;

    private String password;

    private String overrideBaseUri;


    /**
     * @return the permissions
     */
    public Set<GrantPermission> getPermissions () {
        return this.permissions;
    }


    /**
     * @param permissions
     *            the permissions to set
     */
    public void setPermissions ( Set<GrantPermission> permissions ) {
        this.permissions = permissions;
    }


    /**
     * @return the expiry
     */
    public DateTime getExpiry () {
        return this.expiry;
    }


    /**
     * @param expiry
     *            the expiry to set
     */
    public void setExpiry ( DateTime expiry ) {
        this.expiry = expiry;
    }


    /**
     * @return the message
     */
    public String getMessage () {
        return this.message;
    }


    /**
     * @param message
     *            the message to set
     */
    public void setMessage ( String message ) {
        this.message = message;
    }


    /**
     * @return the subject
     */
    public String getNotificationSubject () {
        return this.subject;
    }


    /**
     * @param subject
     *            the subject to set
     */
    public void setNotificationSubject ( String subject ) {
        this.subject = subject;
    }


    /**
     * @return the password
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
     * @return override the link share base uri with this uri
     */
    public String getOverrideBaseURI () {
        return this.overrideBaseUri;
    }


    /**
     * @param overrideBaseUri
     *            the overrideBaseUri to set
     */
    public void setOverrideBaseURI ( String overrideBaseUri ) {
        this.overrideBaseUri = overrideBaseUri;
    }
}
