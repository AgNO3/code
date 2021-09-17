/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server;


import java.util.UUID;

import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.msg.JobProgressEvent;


/**
 * @author mbechler
 * 
 */
public interface JobProgressTracker {

    /**
     * @param jobId
     * @return the currently available progress info
     */
    JobProgressInfo getProgressInfo ( UUID jobId );


    /**
     * @param ev
     */
    void handleEvent ( JobProgressEvent ev );

}