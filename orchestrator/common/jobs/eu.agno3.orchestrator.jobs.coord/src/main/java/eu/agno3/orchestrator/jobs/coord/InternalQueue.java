/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import java.util.Collection;
import java.util.List;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.state.JobStateListener;


/**
 * @author mbechler
 * 
 */
public interface InternalQueue extends JobStateListener {

    /**
     * Adds a job to the pre queue
     * 
     * @param j
     * @throws JobQueueException
     */
    void queueJob ( Job j ) throws JobQueueException;


    /**
     * Triggers cancellation
     * 
     * @param j
     * @throws JobQueueException
     */
    void cancelJob ( Job j ) throws JobQueueException;


    /**
     * 
     * @param j
     * @return the queue that directly handles this job
     */
    InternalQueue getQueueForJob ( Job j );


    /**
     * 
     * @return the jobs currently queued by this coordinator
     */
    List<Job> getQueuedJobs ();


    /**
     * @return a job ready for execution
     */
    Job getJobForExecution ();


    /**
     * @return the currently active jobs
     */
    Collection<Job> getActiveJobs ();


    /**
     * Run regular queue maintenance tasks
     */
    void doMaintenance ();


    /**
     * @return whether the queue is empty
     */
    boolean isEmpty ();


    /**
     * @param j
     * @param js
     */
    void loadJob ( Job j, JobInfo js );

}