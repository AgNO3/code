/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2015 by mbechler
 */
package eu.agno3.fileshare.model.tokens;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class RegistrationToken extends SingleUseToken {

    /**
     * 
     */
    private static final long serialVersionUID = 7367200121434370636L;

    private String userName;
    private MailRecipient recipient;
    private UUID invitingUserId;

    private DateTime userExpires;

    private String invitingUserDisplayName;

    private UUID invitedUserId;


    /**
     * @return the userName
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
     * @return the recipient
     */
    public MailRecipient getRecipient () {
        return this.recipient;
    }


    /**
     * @param recipient
     *            the recipient to set
     */
    public void setRecipient ( MailRecipient recipient ) {
        this.recipient = recipient;
    }


    /**
     * @return the invitingUserId
     */
    public UUID getInvitingUserId () {
        return this.invitingUserId;
    }


    /**
     * @param id
     */
    public void setInvitingUserId ( UUID id ) {
        this.invitingUserId = id;
    }


    /**
     * @return the userExpires
     */
    public DateTime getUserExpires () {
        return this.userExpires;
    }


    /**
     * @param userExpires
     */
    public void setUserExpires ( DateTime userExpires ) {
        this.userExpires = userExpires;
    }


    /**
     * @return the display name of the inviting user
     */
    public String getInvitingUserDisplayName () {
        return this.invitingUserDisplayName;
    }


    /**
     * @param invitingUserDisplayName
     *            the invitingUserDisplayName to set
     */
    public void setInvitingUserDisplayName ( String invitingUserDisplayName ) {
        this.invitingUserDisplayName = invitingUserDisplayName;
    }


    /**
     * @param id
     */
    public void setInvitedUserId ( UUID id ) {
        this.invitedUserId = id;
    }


    /**
     * @return the invitedUserId
     */
    public UUID getInvitedUserId () {
        return this.invitedUserId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.tokens.SingleUseToken#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        super.writeExternal(out);

        out.writeBoolean(this.userName != null);
        if ( this.userName != null ) {
            out.writeUTF(this.userName);
        }
        out.writeBoolean(this.recipient != null);
        if ( this.recipient != null ) {
            this.recipient.writeExternal(out);
        }
        out.writeBoolean(this.invitingUserId != null);
        if ( this.invitingUserId != null ) {
            out.writeLong(this.invitingUserId.getMostSignificantBits());
            out.writeLong(this.invitingUserId.getLeastSignificantBits());
        }
        out.writeBoolean(this.userExpires != null);
        if ( this.userExpires != null ) {
            out.writeLong(this.userExpires.getMillis());
        }
        out.writeBoolean(this.invitingUserDisplayName != null);
        if ( this.invitingUserDisplayName != null ) {
            out.writeUTF(this.invitingUserDisplayName);
        }
        out.writeBoolean(this.invitedUserId != null);
        if ( this.invitedUserId != null ) {
            out.writeLong(this.invitedUserId.getMostSignificantBits());
            out.writeLong(this.invitedUserId.getLeastSignificantBits());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.tokens.SingleUseToken#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal ( ObjectInput in ) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        if ( in.readBoolean() ) {
            this.userName = in.readUTF();
        }

        if ( in.readBoolean() ) {
            this.recipient = new MailRecipient();
            this.recipient.readExternal(in);
        }
        if ( in.readBoolean() ) {
            long msb = in.readLong();
            long lsb = in.readLong();
            this.invitingUserId = new UUID(msb, lsb);
        }
        if ( in.readBoolean() ) {
            this.userExpires = new DateTime(in.readLong());
        }
        if ( in.readBoolean() ) {
            this.invitingUserDisplayName = in.readUTF();
        }
        if ( in.readBoolean() ) {
            long msb = in.readLong();
            long lsb = in.readLong();
            this.invitedUserId = new UUID(msb, lsb);
        }
    }
}
