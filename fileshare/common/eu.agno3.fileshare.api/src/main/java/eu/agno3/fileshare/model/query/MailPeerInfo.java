/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.io.Serializable;
import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class MailPeerInfo implements PeerInfo, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1871505256243332145L;

    private UUID id = UUID.randomUUID();
    private String mailAddress;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.query.PeerInfo#getId()
     */
    @Override
    public UUID getId () {
        return this.id;
    }


    /**
     * @return the mailAddress
     */
    public String getMailAddress () {
        return this.mailAddress;
    }


    /**
     * @param mailAddress
     *            the mailAddress to set
     */
    public void setMailAddress ( String mailAddress ) {
        this.mailAddress = mailAddress;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.query.PeerInfo#haveSharedFrom()
     */
    @Override
    public boolean haveSharedFrom () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.query.PeerInfo#haveSharedTo()
     */
    @Override
    public boolean haveSharedTo () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.mailAddress == null ) ? 0 : this.mailAddress.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        MailPeerInfo other = (MailPeerInfo) obj;
        if ( this.mailAddress == null ) {
            if ( other.mailAddress != null )
                return false;
        }
        else if ( !this.mailAddress.equals(other.mailAddress) )
            return false;
        return true;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "mail-" + this.mailAddress; //$NON-NLS-1$
    }
}
