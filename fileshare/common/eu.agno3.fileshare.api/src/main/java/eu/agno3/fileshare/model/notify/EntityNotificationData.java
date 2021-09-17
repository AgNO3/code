/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.runtime.util.format.ByteSizeFormatter;


/**
 * @author mbechler
 *
 */
public class EntityNotificationData implements MailNotificationData {

    private Set<MailRecipient> recipient;

    private MailSender sender;

    private VFSEntity entity;

    private String fullPath;

    private boolean ownerIsGroup;

    private boolean hideSensitive;

    private String overrideSubject;


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
     * @return the fullPath
     */
    public String getFullPath () {
        return this.fullPath;
    }


    /**
     * @param fullPath
     *            the fullPath to set
     */
    public void setFullPath ( String fullPath ) {
        this.fullPath = fullPath;
    }


    /**
     * @param b
     */
    public void setOwnerIsGroup ( boolean b ) {
        this.ownerIsGroup = b;
    }


    /**
     * @return the ownerIsGroup
     */
    public boolean getOwnerIsGroup () {
        return this.ownerIsGroup;
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
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.notify.MailNotificationData#getOverrideSubject()
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
     * @return the formatted entity size
     */
    public String getFormattedSize () {
        if ( this.entity instanceof VFSFileEntity ) {
            return ByteSizeFormatter.formatByteSizeSI( ( (VFSFileEntity) this.entity ).getFileSize());
        }
        else if ( this.entity instanceof VFSContainerEntity ) {
            Long childrenSize = ( (VFSContainerEntity) this.entity ).getChildrenSize();
            if ( childrenSize != null ) {
                return ByteSizeFormatter.formatByteSizeSI(childrenSize);
            }
        }
        return StringUtils.EMPTY;
    }
}
