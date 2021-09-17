/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.exec;


import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;


/**
 * @author mbechler
 * @param <T>
 *            job class
 * 
 */
public interface JobRunnableFactory <T extends Job> {

    /**
     * @param j
     * @return a runnable for the given job
     * @throws JobRunnableException
     */
    JobRunnable getRunnableForJob ( T j ) throws JobRunnableException;
}
