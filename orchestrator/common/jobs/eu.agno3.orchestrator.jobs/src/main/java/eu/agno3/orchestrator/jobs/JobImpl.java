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
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( Job.class )
public class JobImpl implements Job {

    private UUID jobId;
    private UserPrincipal owner;
    private JobTarget target;
    private JobGroup jobGroup;
    private DateTime deadline;


    /**
     * 
     */
    public JobImpl () {
        this.jobId = UUID.randomUUID();
    }


    /**
     * @param jobGroup
     */
    public JobImpl ( JobGroup jobGroup ) {
        this();
        this.jobGroup = jobGroup;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.Job#getJobId()
     */
    @Override
    public UUID getJobId () {
        return this.jobId;
    }


    /**
     * @param jobId
     *            the jobId to set
     */
    public void setJobId ( UUID jobId ) {
        this.jobId = jobId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.Job#getOwner()
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
     * @see eu.agno3.orchestrator.jobs.Job#getTarget()
     */
    @Override
    public JobTarget getTarget () {
        return this.target;
    }


    /**
     * @param target
     *            the target to set
     */
    public void setTarget ( JobTarget target ) {
        this.target = target;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.Job#getJobGroup()
     */
    @Override
    public JobGroup getJobGroup () {
        return this.jobGroup;
    }


    /**
     * @param jobGroup
     *            the jobGroup to set
     */
    public void setJobGroup ( JobGroup jobGroup ) {
        this.jobGroup = jobGroup;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.Job#getDeadline()
     */
    @Override
    public DateTime getDeadline () {
        return this.deadline;
    }


    /**
     * @param deadline
     *            the deadline to set
     */
    public void setDeadline ( DateTime deadline ) {
        this.deadline = deadline;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s (id=%s target=%s group=%s deadline=%s owner=%s",//$NON-NLS-1$
            this.getClass().getName(),
            this.jobId,
            this.target,
            this.jobGroup,
            this.deadline,
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
        result = prime * result + ( ( this.deadline == null ) ? 0 : this.deadline.hashCode() );
        result = prime * result + ( ( this.jobGroup == null ) ? 0 : this.jobGroup.hashCode() );
        result = prime * result + ( ( this.jobId == null ) ? 0 : this.jobId.hashCode() );
        result = prime * result + ( ( this.owner == null ) ? 0 : this.owner.hashCode() );
        result = prime * result + ( ( this.target == null ) ? 0 : this.target.hashCode() );
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
        JobImpl other = (JobImpl) obj;
        if ( this.deadline == null ) {
            if ( other.deadline != null )
                return false;
        }
        else if ( !this.deadline.equals(other.deadline) )
            return false;
        if ( this.jobGroup == null ) {
            if ( other.jobGroup != null )
                return false;
        }
        else if ( !this.jobGroup.equals(other.jobGroup) )
            return false;
        if ( this.jobId == null ) {
            if ( other.jobId != null )
                return false;
        }
        else if ( !this.jobId.equals(other.jobId) )
            return false;
        if ( this.owner == null ) {
            if ( other.owner != null )
                return false;
        }
        else if ( !this.owner.equals(other.owner) )
            return false;
        if ( this.target == null ) {
            if ( other.target != null )
                return false;
        }
        else if ( !this.target.equals(other.target) )
            return false;
        return true;
    }
    // -GENERATED

}
