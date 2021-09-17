/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.exec;


import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface ResumableJobRunnableFactory <T extends Job> extends JobRunnableFactory<T> {

    /**
     * @param j
     * @param h
     * @return a runnable for the given job
     * @throws JobRunnableException
     */
    JobRunnable getRunnableForJob ( T j, JobResumptionHandler h ) throws JobRunnableException;


    /**
     * @param job
     * @param h
     * @return a runnable for the resumed job
     * @throws JobRunnableException
     */
    JobRunnable getJobResumption ( T job, JobResumptionHandler h ) throws JobRunnableException;

}
