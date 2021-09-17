/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;


/**
 * @author mbechler
 * 
 */
public interface Executor {

    /**
     * Shutdown the executor
     * 
     * First, disables execution of new jobs and waits for shutdownTimeout seconds for short lived jobs to finished,
     * then cancels the remaining jobs
     */
    void shutdown ();


    /**
     * Submit new jobs for execution
     * 
     * Pools for new executable jobs on the coordinator and submits them
     * to the executor backend.
     */
    void run ();


    /**
     * Wait until a certain job has finished
     * 
     * @param j
     * @throws InterruptedException
     */
    void waitFor ( Job j ) throws InterruptedException;


    /**
     * Cancel a potentially running job
     * 
     * @param j
     * @throws JobQueueException
     */
    void cancel ( Job j ) throws JobQueueException;


    /**
     * 
     */
    void disableExecution ();


    /**
     * 
     */
    void enableExecution ();


    /**
     * @param j
     * @return whether the job can be run
     */
    boolean canRun ( Job j );

}