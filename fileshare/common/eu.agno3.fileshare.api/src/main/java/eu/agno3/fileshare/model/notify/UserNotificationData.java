/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.07.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import java.util.HashSet;
import java.util.Set;

import eu.agno3.fileshare.model.User;


/**
 * @author mbechler
 *
 */
public class UserNotificationData implements MailNotificationData {

    private Set<MailRecipient> recpients = new HashSet<>();

    private User user;


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
    public User getUser () {
        return this.user;
    }


    /**
     * @param u
     */
    public void setUser ( User u ) {
        this.user = u;
    }

}
