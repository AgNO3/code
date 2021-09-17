/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update;


import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobResumptionHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.system.update.jobs.UpdateInstallJob;


/**
 * @author mbechler
 *
 */
public interface UpdateInstallRunnableFactory {

    /**
     * 
     * @param j
     * @param resumeHandler
     * @return the job runnable
     * @throws JobRunnableException
     */
    JobRunnable getRunnableForJob ( UpdateInstallJob j, JobResumptionHandler resumeHandler ) throws JobRunnableException;
}
