/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;


/**
 * @author mbechler
 * 
 */
public interface OutputHandlerFactory {

    /**
     * @param j
     * @return an output handler for the job
     */
    JobOutputHandler forJob ( Job j );

}
