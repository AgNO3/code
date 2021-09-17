/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.state;


import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;


/**
 * @author mbechler
 * 
 */
public interface JobStateListener {

    /**
     * 
     * @param ev
     */
    void jobUpdated ( JobStateUpdatedEvent ev );


    /**
     * @param job
     * @param ev
     */
    void jobKeepalive ( Job job, JobKeepAliveEvent ev );
}
