/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.db;


import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "job_output", indexes = @Index ( columnList = "jobId,level,offset", unique = true ) )
@PersistenceUnit ( unitName = "jobs" )
public class JobOutputSegment implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2226057234275753676L;
    private long offset;
    private Long combinedOffset;
    private JobOutputLevel level;
    private String content;
    private UUID jobId;
    private boolean eof;


    /**
     * 
     */
    public JobOutputSegment () {}


    /**
     * @param jobId
     * @param offset
     * @param combinedOffset
     * @param level
     * @param content
     */
    public JobOutputSegment ( UUID jobId, long offset, long combinedOffset, JobOutputLevel level, String content ) {
        super();
        this.jobId = jobId;
        this.offset = offset;
        this.combinedOffset = combinedOffset;
        this.level = level;
        this.content = content;
    }


    /**
     * @return the jobId
     */
    @Basic
    @Column ( length = 16 )
    @Id
    public UUID getJobId () {
        return this.jobId;
    }


    /**
     * @param jobId
     *            the jobId to set
     */
    protected void setJobId ( UUID jobId ) {
        this.jobId = jobId;
    }


    /**
     * @return whether EOF was found
     */
    public boolean getEof () {
        return this.eof;
    }


    /**
     * @param eof
     *            the eof to set
     */
    public void setEof ( boolean eof ) {
        this.eof = eof;
    }


    /**
     * @return the offset
     */
    @Basic
    @Id
    public long getOffset () {
        return this.offset;
    }


    /**
     * @param offset
     *            the offset to set
     */
    protected void setOffset ( long offset ) {
        this.offset = offset;
    }


    /**
     * @return the combinedOffset
     */
    public long getCombinedOffset () {
        if ( this.combinedOffset < 0 ) {
            return getOffset();
        }
        return this.combinedOffset;
    }


    /**
     * @param combinedOffset
     *            the combinedOffset to set
     */
    public void setCombinedOffset ( long combinedOffset ) {
        this.combinedOffset = combinedOffset;
    }


    /**
     * @return the level
     */
    @Enumerated ( EnumType.ORDINAL )
    @Id
    public JobOutputLevel getLevel () {
        return this.level;
    }


    /**
     * @param level
     *            the level to set
     */
    protected void setLevel ( JobOutputLevel level ) {
        this.level = level;
    }


    /**
     * @return the content
     */
    @Lob
    @Column ( length = 65535 )
    public String getContent () {
        return this.content;
    }


    /**
     * @param content
     *            the content to set
     */
    public void setContent ( String content ) {
        this.content = content;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s output buffer @%d: %s", this.getLevel(), this.getOffset(), this.getContent()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.jobId == null ) ? 0 : this.jobId.hashCode() );
        result = prime * result + ( ( this.level == null ) ? 0 : this.level.hashCode() );
        result = prime * result + (int) ( this.offset ^ ( this.offset >>> 32 ) );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    // +GENERATED
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        JobOutputSegment other = (JobOutputSegment) obj;
        if ( this.jobId == null ) {
            if ( other.jobId != null )
                return false;
        }
        else if ( !this.jobId.equals(other.jobId) )
            return false;
        if ( this.level != other.level )
            return false;
        if ( this.offset != other.offset )
            return false;
        return true;
    }
    // -GENERATED

}
