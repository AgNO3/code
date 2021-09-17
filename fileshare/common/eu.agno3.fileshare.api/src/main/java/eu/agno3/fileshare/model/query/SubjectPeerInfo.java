/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.io.Serializable;
import java.util.UUID;

import eu.agno3.fileshare.model.SubjectInfo;


/**
 * @author mbechler
 *
 */
public class SubjectPeerInfo implements PeerInfo, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1871505256243332145L;
    private boolean haveSharedFrom;
    private boolean haveSharedTo;

    private SubjectInfo subject;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.query.PeerInfo#getId()
     */
    @Override
    public UUID getId () {
        return this.subject.getId();
    }


    /**
     * @return the subject
     */
    public SubjectInfo getSubject () {
        return this.subject;
    }


    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject ( SubjectInfo subject ) {
        this.subject = subject;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.query.PeerInfo#haveSharedFrom()
     */
    @Override
    public boolean haveSharedFrom () {
        return this.haveSharedFrom;
    }


    /**
     * @param haveSharedFrom
     *            the haveSharedFrom to set
     */
    public void setHaveSharedFrom ( boolean haveSharedFrom ) {
        this.haveSharedFrom = haveSharedFrom;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.query.PeerInfo#haveSharedTo()
     */
    @Override
    public boolean haveSharedTo () {
        return this.haveSharedTo;
    }


    /**
     * @param haveSharedTo
     *            the haveSharedTo to set
     */
    public void setHaveSharedTo ( boolean haveSharedTo ) {
        this.haveSharedTo = haveSharedTo;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.subject == null ) ? 0 : this.subject.hashCode() );
        return result;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        SubjectPeerInfo other = (SubjectPeerInfo) obj;
        if ( this.subject == null ) {
            if ( other.subject != null )
                return false;
        }
        else if ( !this.subject.equals(other.subject) )
            return false;
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "subject-" + this.getSubject().getId(); //$NON-NLS-1$
    }

}
