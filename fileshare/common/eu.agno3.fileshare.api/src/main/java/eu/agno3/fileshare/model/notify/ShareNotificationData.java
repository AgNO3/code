/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import java.io.Serializable;
import java.util.Set;

import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.LinkShareData;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.runtime.util.format.ByteSizeFormatter;


/**
 * @author mbechler
 *
 */
public class ShareNotificationData implements Serializable, MailNotificationData, LinkShareData {

    /**
     * 
     */
    private static final long serialVersionUID = -5760882301380456504L;

    private VFSEntity entity;

    private SubjectGrant grant;

    private Set<MailRecipient> recipient;

    private MailSender sender;

    private boolean viewable;

    private String viewURL;

    private String downloadURL;

    private String message;

    private boolean hideSensitive;

    private String overrideSubject;


    /**
     * @return the entity
     */
    public VFSEntity getEntity () {
        return this.entity;
    }


    /**
     * @param entity
     *            the entity to set
     */
    public void setEntity ( VFSEntity entity ) {
        this.entity = entity;
    }


    /**
     * @return the grant
     */
    public SubjectGrant getGrant () {
        return this.grant;
    }


    /**
     * @param grant
     *            the grant to set
     */
    public void setGrant ( SubjectGrant grant ) {
        this.grant = grant;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.notify.MailNotificationData#getRecipients()
     */
    @Override
    public Set<MailRecipient> getRecipients () {
        return this.recipient;
    }


    /**
     * @param recipient
     *            the recipient to set
     */
    public void setRecipients ( Set<MailRecipient> recipient ) {
        this.recipient = recipient;
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
     * @return the viewable
     */
    @Override
    public boolean getViewable () {
        return this.viewable;
    }


    /**
     * @param viewable
     *            the viewable to set
     */
    @Override
    public void setViewable ( boolean viewable ) {
        this.viewable = viewable;
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
     * @return the formatted file size
     */
    public String getFormattedSize () {
        if ( this.getGrant() == null || ! ( this.getGrant().getEntity() instanceof FileEntity ) ) {
            return null;
        }
        FileEntity fe = (FileEntity) this.getGrant().getEntity();
        return ByteSizeFormatter.formatByteSize(fe.getFileSize());
    }


    /**
     * @param viewURL
     */
    @Override
    public void setViewURL ( String viewURL ) {
        this.viewURL = viewURL;
    }


    /**
     * @return the viewURL
     */
    @Override
    public String getViewURL () {
        return this.viewURL;
    }


    /**
     * @param downloadURL
     */
    @Override
    public void setDownloadURL ( String downloadURL ) {
        this.downloadURL = downloadURL;
    }


    /**
     * @return the downloadURL
     */
    @Override
    public String getDownloadURL () {
        return this.downloadURL;
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
}
