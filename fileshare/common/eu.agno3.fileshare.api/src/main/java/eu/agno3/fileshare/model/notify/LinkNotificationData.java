/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.fileshare.model.User;


/**
 * @author mbechler
 *
 */
public class LinkNotificationData implements MailNotificationData {

    private Set<MailRecipient> recipients;

    private String link;

    private MailSender sender;

    private String extraMessage;

    private String overrideSubject;

    private DateTime expirationDate;

    private User sendingUser;

    private boolean hideSensitive;


    /**
     * @return the link
     */
    public String getLink () {
        return this.link;
    }


    /**
     * @param link
     *            the link to set
     */
    public void setLink ( String link ) {
        this.link = link;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.notify.MailNotificationData#getRecipients()
     */
    @Override
    public Set<MailRecipient> getRecipients () {
        return this.recipients;
    }


    /**
     * @param recipients
     *            the recipients to set
     */
    public void setRecipients ( Set<MailRecipient> recipients ) {
        this.recipients = recipients;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.notify.MailNotificationData#getSender()
     */
    @Override
    public MailSender getSender () {
        return this.sender;
    }


    /**
     * @param sender
     *            the sender to set
     */
    public void setSender ( MailSender sender ) {
        this.sender = sender;
    }


    /**
     * @return the message
     */
    public String getExtraMessage () {
        return this.extraMessage;
    }


    /**
     * @param message
     */
    public void setExtraMessage ( String message ) {
        this.extraMessage = message;
    }


    /**
     * @return the expirationDate
     */
    public DateTime getExpirationDate () {
        return this.expirationDate;
    }


    /**
     * @param expires
     */
    public void setExpirationDate ( DateTime expires ) {
        this.expirationDate = expires;
    }


    /**
     * @return the sendingUser
     */
    public User getSendingUser () {
        return this.sendingUser;
    }


    /**
     * @param sendingUser
     *            the sendingUser to set
     */
    public void setSendingUser ( User sendingUser ) {
        this.sendingUser = sendingUser;
    }


    /**
     * @return the hideSensitive
     */
    @Override
    public boolean getHideSensitive () {
        return this.hideSensitive;
    }


    /**
     * @param hideSensitive
     *            the hideSensitive to set
     */
    public void setHideSensitive ( boolean hideSensitive ) {
        this.hideSensitive = hideSensitive;
    }


    /**
     * @return the overrideSubject
     */
    @Override
    public String getOverrideSubject () {
        return this.overrideSubject;
    }


    /**
     * @param overrideSubject
     *            the overrideSubject to set
     */
    public void setOverrideSubject ( String overrideSubject ) {
        this.overrideSubject = overrideSubject;
    }


    /**
     * @param userExpires
     */
    public void setUserExpires ( DateTime userExpires ) {
        // TODO Auto-generated method stub

    }
}
