/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.11.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.exec;


import java.io.InputStream;
import java.io.Serializable;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;


/**
 * @author mbechler
 *
 */
public interface JobResumptionHandler {

    /**
     * 
     * @param job
     * @param s
     * @throws JobQueueException
     */
    <T extends Job> void writeSuspendData ( T job, Serializable s ) throws JobQueueException;


    /**
     * @param job
     * @return a stream of suspended data
     * @throws JobQueueException
     */
    <T extends Job> InputStream getSuspendData ( T job ) throws JobQueueException;


    /**
     * @param job
     * @throws JobQueueException
     */
    <T extends Job> void resumed ( T job ) throws JobQueueException;

}
