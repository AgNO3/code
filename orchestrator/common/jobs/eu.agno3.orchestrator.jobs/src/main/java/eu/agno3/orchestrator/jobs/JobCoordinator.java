/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;


/**
 * 
 * 
 * 
 * @author mbechler
 * 
 */
public interface JobCoordinator {

    /**
     * @param j
     * @return initial job info
     * @throws JobQueueException
     */
    JobInfo queueJob ( Job j ) throws JobQueueException;


    /**
     * 
     * @return the job groups known to this coordinator
     * @throws JobQueueException
     */
    Set<JobGroup> getKnownGroups () throws JobQueueException;


    /**
     * @param g
     * @return all jobs currently known to this coordinator
     * @throws JobQueueException
     */
    Collection<JobInfo> getAllJobs ( JobGroup g ) throws JobQueueException;


    /**
     * 
     * @param g
     * @return the jobs currently queued by this coordinator
     * @throws JobQueueException
     */
    Collection<JobInfo> getQueuedJobs ( JobGroup g ) throws JobQueueException;


    /**
     * 
     * @param g
     * @return the jobs currently executing under this coordinator
     * @throws JobQueueException
     */
    Collection<JobInfo> getActiveJobs ( JobGroup g ) throws JobQueueException;


    /**
     * @param uuid
     * @return updated job info
     * @throws JobQueueException
     */
    JobInfo cancelJob ( UUID uuid ) throws JobQueueException;


    /**
     * @param job
     * @return the job info
     * @throws JobQueueException
     */
    JobInfo getJobInfo ( Job job ) throws JobQueueException;


    /**
     * @param jobId
     * @return the job info
     * @throws JobQueueException
     */
    JobInfo getJobInfo ( UUID jobId ) throws JobQueueException;


    /**
     * Remove all jobs that are finished, failed or cancelled
     * 
     * @return the removed jobs
     */
    int clearFinishedJobs ();


    /**
     * Run regular tasks
     * 
     * @throws JobQueueException
     */
    void run () throws JobQueueException;


    /**
     * Disable execution of jobs
     */
    void disableLocalExecution ();


    /**
     * Enable execution of jobs
     */
    void enableLocalExecution ();


    /**
     * Called by other components to notify of job activity
     * 
     * @param jobId
     * @throws JobUnknownException
     */
    void notifyExternalKeepAlive ( UUID jobId ) throws JobUnknownException;


    /**
     * @param j
     * @return job info
     * @throws JobQueueException
     */
    JobInfo cancelJob ( Job j ) throws JobQueueException;


    /**
     * @param jobId
     * @return the job object
     * @throws JobUnknownException
     */
    Job getJobData ( UUID jobId ) throws JobUnknownException;


    /**
     * @return whether the maintenance task should run
     */
    boolean needsMaintenance ();

}
