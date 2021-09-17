/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.util.serialization.SafeSerialization;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( JobInfo.class )
@SafeSerialization
public class JobInfoImpl implements JobInfo, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = -2401927734402557649L;

    private UUID jobId;

    private JobState state;

    private UserPrincipal owner;

    private DateTime queuedTime;

    private DateTime startedTime;

    private DateTime finishedTime;

    private DateTime lastKeepAliveTime;

    private String type;


    /**
     * 
     */
    public JobInfoImpl () {}


    /**
     * @param j
     */
    public JobInfoImpl ( JobInfo j ) {
        this.jobId = j.getJobId();
        this.owner = j.getOwner();
        this.state = j.getState();
        this.type = j.getType();
        this.queuedTime = j.getQueuedTime();
        this.startedTime = j.getStartedTime();
        this.finishedTime = j.getFinishedTime();
        this.lastKeepAliveTime = j.getLastKeepAliveTime();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobInfo#getJobId()
     */
    @Override
    public UUID getJobId () {
        return this.jobId;
    }


    /**
     * @param jobId
     */
    public void setJobId ( UUID jobId ) {
        this.jobId = jobId;
    }


    /**
     * @return the type
     */
    @Override
    public String getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( String type ) {
        this.type = type;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfo#getOwner()
     */
    @Override
    public UserPrincipal getOwner () {
        return this.owner;
    }


    /**
     * @param owner
     *            the owner to set
     */
    public void setOwner ( UserPrincipal owner ) {
        this.owner = owner;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobInfo#getState()
     */
    @Override
    public JobState getState () {
        return this.state;
    }


    /**
     * @param state
     *            the state to set
     */
    public void setState ( JobState state ) {
        this.state = state;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobInfo#getQueuedTime()
     */
    @Override
    public DateTime getQueuedTime () {
        return this.queuedTime;
    }


    /**
     * @param queuedTime
     *            the queuedTime to set
     */
    public void setQueuedTime ( DateTime queuedTime ) {
        this.queuedTime = queuedTime;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobInfo#getStartedTime()
     */
    @Override
    public DateTime getStartedTime () {
        return this.startedTime;
    }


    /**
     * @param startedTime
     *            the startedTime to set
     */
    public void setStartedTime ( DateTime startedTime ) {
        this.startedTime = startedTime;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobInfo#getFinishedTime()
     */
    @Override
    public DateTime getFinishedTime () {
        return this.finishedTime;
    }


    /**
     * @param finishedTime
     *            the finishedTime to set
     */
    public void setFinishedTime ( DateTime finishedTime ) {
        this.finishedTime = finishedTime;
    }


    /**
     * @return the lastKeepAliveTime
     */
    @Override
    public DateTime getLastKeepAliveTime () {
        return this.lastKeepAliveTime;
    }


    /**
     * @param lastKeepAliveTime
     *            the lastKeepAliveTime to set
     */
    public void setLastKeepAliveTime ( DateTime lastKeepAliveTime ) {
        this.lastKeepAliveTime = lastKeepAliveTime;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "%s: state=%s queued=%s started=%s finished=%s owner=%s", //$NON-NLS-1$
            this.jobId,
            this.state,
            this.queuedTime,
            this.startedTime,
            this.finishedTime,
            this.owner);
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
        result = prime * result + ( ( this.finishedTime == null ) ? 0 : this.finishedTime.hashCode() );
        result = prime * result + ( ( this.jobId == null ) ? 0 : this.jobId.hashCode() );
        result = prime * result + ( ( this.lastKeepAliveTime == null ) ? 0 : this.lastKeepAliveTime.hashCode() );
        result = prime * result + ( ( this.owner == null ) ? 0 : this.owner.hashCode() );
        result = prime * result + ( ( this.queuedTime == null ) ? 0 : this.queuedTime.hashCode() );
        result = prime * result + ( ( this.startedTime == null ) ? 0 : this.startedTime.hashCode() );
        result = prime * result + ( ( this.state == null ) ? 0 : this.state.hashCode() );
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
        JobInfoImpl other = (JobInfoImpl) obj;
        if ( this.finishedTime == null ) {
            if ( other.finishedTime != null )
                return false;
        }
        else if ( !this.finishedTime.equals(other.finishedTime) )
            return false;
        if ( this.jobId == null ) {
            if ( other.jobId != null )
                return false;
        }
        else if ( !this.jobId.equals(other.jobId) )
            return false;
        if ( this.lastKeepAliveTime == null ) {
            if ( other.lastKeepAliveTime != null )
                return false;
        }
        else if ( !this.lastKeepAliveTime.equals(other.lastKeepAliveTime) )
            return false;
        if ( this.owner == null ) {
            if ( other.owner != null )
                return false;
        }
        else if ( !this.owner.equals(other.owner) )
            return false;
        if ( this.queuedTime == null ) {
            if ( other.queuedTime != null )
                return false;
        }
        else if ( !this.queuedTime.equals(other.queuedTime) )
            return false;
        if ( this.startedTime == null ) {
            if ( other.startedTime != null )
                return false;
        }
        else if ( !this.startedTime.equals(other.startedTime) )
            return false;
        if ( this.state != other.state )
            return false;
        return true;
    }


    // -GENERATED

    @Override
    public JobInfoImpl clone () {
        return new JobInfoImpl(this);
    }


    /**
     * @param info
     */
    public void update ( JobInfo info ) {
        this.state = info.getState();
        this.queuedTime = info.getQueuedTime();
        this.startedTime = info.getStartedTime();
        this.finishedTime = info.getFinishedTime();
    }

}
