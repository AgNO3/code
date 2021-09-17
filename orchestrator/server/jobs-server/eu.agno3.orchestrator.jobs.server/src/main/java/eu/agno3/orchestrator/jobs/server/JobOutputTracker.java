/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server;


import java.util.UUID;

import eu.agno3.orchestrator.jobs.msg.JobOutputEvent;


/**
 * @author mbechler
 *
 */
public interface JobOutputTracker {

    /**
     * @param ev
     */
    void handleOutputEvent ( JobOutputEvent ev );


    /**
     * @param jobId
     * @return the output buffer
     */
    JobOutputBuffer getOutput ( UUID jobId );

}
