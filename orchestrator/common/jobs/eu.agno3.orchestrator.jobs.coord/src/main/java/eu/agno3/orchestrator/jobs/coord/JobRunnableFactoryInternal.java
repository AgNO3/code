/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobResumptionHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;


/**
 * @author mbechler
 * 
 */
public interface JobRunnableFactoryInternal {

    /**
     * @param j
     * @param state
     * @param h
     * @return a runnable for the given job
     * @throws JobRunnableException
     */
    <T extends Job> JobRunnable getRunnableForJob ( T j, JobState state ) throws JobRunnableException;


    /**
     * @param j
     * @param state
     * @param h
     * @return a runnable for the given job
     * @throws JobRunnableException
     */
    <T extends Job> JobRunnable getRunnableForJob ( T j, JobState state, JobResumptionHandler h ) throws JobRunnableException;


    /**
     * @param j
     * @return whether a runnable is available
     */
    boolean hasRunnable ( Job j );

}
