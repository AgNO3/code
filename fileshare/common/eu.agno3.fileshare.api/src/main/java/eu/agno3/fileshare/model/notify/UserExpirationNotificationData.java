/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import java.util.HashSet;
import java.util.Set;

import eu.agno3.fileshare.model.User;


/**
 * @author mbechler
 *
 */
public class UserExpirationNotificationData implements MailNotificationData {

    private Set<MailRecipient> recpients = new HashSet<>();

    private String extensionLink;

    private User expiringUser;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.notify.MailNotificationData#getRecipients()
     */
    @Override
    public Set<MailRecipient> getRecipients () {
        return this.recpients;
    }


    /**
     * @param recpients
     *            the recpients to set
     */
    public void setRecpients ( Set<MailRecipient> recpients ) {
        this.recpients = recpients;
    }


    /**
     * @return the extensionLink
     */
    public String getExtensionLink () {
        return this.extensionLink;
    }


    /**
     * @param extensionLink
     *            the extensionLink to set
     */
    public void setExtensionLink ( String extensionLink ) {
        this.extensionLink = extensionLink;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.notify.MailNotificationData#getSender()
     */
    @Override
    public MailSender getSender () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.notify.MailNotificationData#getHideSensitive()
     */
    @Override
    public boolean getHideSensitive () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.notify.MailNotificationData#getOverrideSubject()
     */
    @Override
    public String getOverrideSubject () {
        return null;
    }


    /**
     * @return the expiringUser
     */
    public User getExpiringUser () {
        return this.expiringUser;
    }


    /**
     * @param u
     */
    public void setExpiringUser ( User u ) {
        this.expiringUser = u;
    }

}
