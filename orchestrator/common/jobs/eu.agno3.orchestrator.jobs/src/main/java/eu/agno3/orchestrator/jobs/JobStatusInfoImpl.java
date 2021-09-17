/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
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
@MapAs ( JobStatusInfo.class )
public class JobStatusInfoImpl implements JobStatusInfo {

    private static final long serialVersionUID = -666058827382803759L;
    private UUID jobId;
    private UserPrincipal owner;
    private JobTarget jobTarget;
    private String targetDisplayName;
    private String jobType;
    private JobState state;
    private JobProgressInfo progress;
    private DateTime finishedTime;
    private DateTime startedTime;
    private DateTime queuedTime;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobStatusInfo#getJobId()
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
     * @see eu.agno3.orchestrator.jobs.JobStatusInfo#getTarget()
     */
    @Override
    public JobTarget getTarget () {
        return this.jobTarget;
    }


    /**
     * @param jobTarget
     *            the jobTarget to set
     */
    public void setTarget ( JobTarget jobTarget ) {
        this.jobTarget = jobTarget;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobStatusInfo#getJobType()
     */
    @Override
    public String getJobType () {
        return this.jobType;
    }


    /**
     * @param jobType
     *            the jobType to set
     */
    public void setJobType ( String jobType ) {
        this.jobType = jobType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobStatusInfo#getProgress()
     */
    @Override
    public JobProgressInfo getProgress () {
        return this.progress;
    }


    /**
     * @param progress
     *            the progress to set
     */
    public void setProgress ( JobProgressInfo progress ) {
        this.progress = progress;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobStatusInfo#getState()
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
     * @return the owner
     */
    @Override
    public UserPrincipal getOwner () {
        return this.owner;
    }


    /**
     * @param owner
     */
    public void setOwner ( UserPrincipal owner ) {
        this.owner = owner;
    }


    /**
     * @param info
     * @param j
     * @return a status info object filled with the job info
     */
    public static JobStatusInfoImpl fromJobInfo ( JobInfo info, Job j ) {
        JobStatusInfoImpl s = new JobStatusInfoImpl();
        s.setJobId(info.getJobId());
        s.setOwner(info.getOwner());
        s.setTarget(j.getTarget());
        s.setJobType(j.getClass().getName());
        s.setState(info.getState());
        if ( j.getTarget() != null ) {
            s.setTargetDisplayName(j.getTarget().toString());
        }
        s.setQueuedTime(info.getQueuedTime());
        s.setStartedTime(info.getStartedTime());
        s.setFinishedTime(info.getFinishedTime());
        return s;
    }


    /**
     * @return the finishedTime
     */
    @Override
    public DateTime getFinishedTime () {
        return this.finishedTime;
    }


    /**
     * @param finishedTime
     */
    public void setFinishedTime ( DateTime finishedTime ) {
        this.finishedTime = finishedTime;
    }


    /**
     * @return the startedTime
     */
    @Override
    public DateTime getStartedTime () {
        return this.startedTime;
    }


    /**
     * @param startedTime
     */
    public void setStartedTime ( DateTime startedTime ) {
        this.startedTime = startedTime;
    }


    /**
     * @return the queuedTime
     */
    @Override
    public DateTime getQueuedTime () {
        return this.queuedTime;
    }


    /**
     * @param queuedTime
     */
    public void setQueuedTime ( DateTime queuedTime ) {
        this.queuedTime = queuedTime;
    }


    /**
     * @param displayName
     */
    public void setTargetDisplayName ( String displayName ) {
        this.targetDisplayName = displayName;
    }


    /**
     * @return the targetDisplayName
     */
    @Override
    public String getTargetDisplayName () {
        return this.targetDisplayName;
    }

}
