/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.versioning;


import java.io.Serializable;
import java.util.Date;


/**
 * @author mbechler
 * 
 */
public class VersionInfoImpl implements VersionInfo, Serializable, Comparable<VersionInfo> {

    private static final long serialVersionUID = 8765199878937655358L;
    private long revisionNumber;
    private Date revisionTime;
    private RevisionType revisionType;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.versioning.VersionInfo#getRevisionNumber()
     */
    @Override
    public long getRevisionNumber () {
        return this.revisionNumber;
    }


    /**
     * @param revisionNumber
     *            the revisionNumber to set
     */
    public void setRevisionNumber ( long revisionNumber ) {
        this.revisionNumber = revisionNumber;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.versioning.VersionInfo#getRevisionTime()
     */
    @Override
    public Date getRevisionTime () {
        if ( this.revisionTime != null ) {
            return (Date) this.revisionTime.clone();
        }

        return null;

    }


    /**
     * @param revisionTime
     *            the revisionTime to set
     */
    public void setRevisionTime ( Date revisionTime ) {
        if ( revisionTime == null ) {
            this.revisionTime = null;
            return;
        }
        this.revisionTime = (Date) revisionTime.clone();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.versioning.VersionInfo#getRevisionType()
     */
    @Override
    public RevisionType getRevisionType () {
        return this.revisionType;
    }


    /**
     * @param revisionType
     *            the revisionType to set
     */
    public void setRevisionType ( RevisionType revisionType ) {
        this.revisionType = revisionType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( VersionInfo o ) {
        return this.compareInternalRevNum(o);
    }


    /**
     * @param o
     * @return
     */
    private int compareInternalRevNum ( VersionInfo o ) {
        int res = Long.compare(this.revisionNumber, o.getRevisionNumber());

        if ( res != 0 ) {
            return res;
        }

        return this.compareInternalRevDate(o);
    }


    /**
     * @param o
     * @return
     */
    private int compareInternalRevDate ( VersionInfo o ) {
        if ( this.revisionTime == null && o.getRevisionTime() == null ) {
            return this.compareInternalRevType(o);
        }
        else if ( this.revisionTime == null ) {
            return -1;
        }
        else if ( o.getRevisionTime() == null ) {
            return 1;
        }
        return this.compareInternalRevType(o);
    }


    /**
     * @param o
     * @return
     */
    private int compareInternalRevType ( VersionInfo o ) {
        if ( this.revisionType == null && o.getRevisionType() == null ) {
            return 0;
        }
        else if ( this.revisionType == null ) {
            return -1;
        }
        else if ( o.getRevisionType() == null ) {
            return 1;
        }

        return this.revisionType.compareTo(o.getRevisionType());
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) ( this.revisionNumber ^ ( this.revisionNumber >>> 32 ) );
        result = prime * result + ( ( this.revisionTime == null ) ? 0 : this.revisionTime.hashCode() );
        result = prime * result + ( ( this.revisionType == null ) ? 0 : this.revisionType.hashCode() );
        return result;
    }


    // -GENERATED

    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( ! ( obj instanceof VersionInfoImpl ) )
            return false;
        VersionInfoImpl other = (VersionInfoImpl) obj;
        if ( this.revisionNumber != other.revisionNumber )
            return false;
        if ( this.revisionTime == null ) {
            if ( other.revisionTime != null )
                return false;
        }
        else if ( !this.revisionTime.equals(other.revisionTime) )
            return false;
        if ( this.revisionType != other.revisionType )
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
        return String.format("Version %d (type: %s, time: %s)", //$NON-NLS-1$
            this.revisionNumber,
            this.revisionType,
            this.revisionTime);
    }
}
