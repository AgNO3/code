/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.impl;


import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfo;


/**
 * @author mbechler
 * 
 */
public interface RemoteQueueClient {

    /**
     * 
     * @param j
     * @return whether the job has been queues
     */
    boolean tryQueueJob ( Job j );


    /**
     * 
     * @param j
     * @return whether the job has been cancelled
     */
    boolean tryCancelJob ( Job j );


    /**
     * @param j
     * @return the job info retrieved from the remote queue
     */
    JobInfo tryGetJobInfo ( Job j );
}
