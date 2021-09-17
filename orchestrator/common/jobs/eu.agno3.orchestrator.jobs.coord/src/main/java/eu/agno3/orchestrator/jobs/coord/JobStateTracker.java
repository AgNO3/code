/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import java.util.Collection;
import java.util.UUID;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.orchestrator.jobs.state.JobStateListener;


/**
 * @author mbechler
 * 
 */
public interface JobStateTracker {

    /**
     * 
     * @param j
     * @return the job info for the given job
     * @throws JobUnknownException
     */
    JobInfo getJobState ( Job j ) throws JobUnknownException;


    /**
     * @param jobId
     * @return the info
     * @throws JobUnknownException
     */
    JobInfo getJobState ( UUID jobId ) throws JobUnknownException;


    /**
     * 
     * @param jobs
     * @return the job info for the given jobs
     * @throws JobUnknownException
     */
    Collection<JobInfo> getJobStates ( Collection<Job> jobs ) throws JobUnknownException;


    /**
     * 
     * @param jobInfo
     * @param s
     * @return the new job info
     * @throws JobQueueException
     */
    JobInfo updateJobState ( Job jobInfo, JobState s ) throws JobQueueException;


    /**
     * updateJobState variant not producing events
     * 
     * @param j
     * @param s
     * @return the new job info
     * @throws JobQueueException
     */
    JobInfo updateJobStateExternal ( Job j, JobState s ) throws JobQueueException;


    /**
     * @param l
     */
    void addListener ( JobStateListener l );


    /**
     * @param l
     */
    void removeListener ( JobStateListener l );


    /**
     * @param g
     * @return a known jobs in the group
     * @throws JobQueueException
     */
    Collection<JobInfo> getAllJobInfo ( JobGroup g ) throws JobQueueException;


    /**
     * @param ev
     */
    void handleEvent ( JobStateUpdatedEvent ev );


    /**
     * Triggers an update of the last keepalive time
     * 
     * @param jobId
     */
    void doKeepAlive ( UUID jobId );


    /**
     * @return the number of cleared jobs
     */
    int clearFinishedJobs ();


    /**
     * @param jobId
     * @return the job object for the id
     */
    Job getJobData ( UUID jobId );


    /**
     * @return jobs to load
     */
    Collection<Job> getLoadableJobs ();

}
