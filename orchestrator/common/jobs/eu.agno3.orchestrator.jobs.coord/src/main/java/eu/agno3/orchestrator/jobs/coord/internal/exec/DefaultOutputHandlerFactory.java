/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.exec;


import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.coord.LoggingJobOutputHandler;
import eu.agno3.orchestrator.jobs.coord.OutputHandlerFactory;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;


/**
 * @author mbechler
 * 
 */
public class DefaultOutputHandlerFactory implements OutputHandlerFactory {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.OutputHandlerFactory#forJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobOutputHandler forJob ( Job j ) {
        return new LoggingJobOutputHandler(Logger.getLogger(j.getClass()));
    }

}
