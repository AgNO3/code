/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.exec;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.JobState;


/**
 * @author mbechler
 * 
 */
public interface JobRunnable {

    /**
     * 
     * @param outHandler
     * @throws Exception
     * @return the target job state
     */
    JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception;
}
