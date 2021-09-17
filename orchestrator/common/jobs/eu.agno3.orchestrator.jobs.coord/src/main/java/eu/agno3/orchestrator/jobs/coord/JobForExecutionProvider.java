/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import java.util.Collection;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobGroup;


/**
 * @author mbechler
 * 
 */
public interface JobForExecutionProvider {

    /**
     * @param g
     * @return a job that is ready for execution
     */
    Job getJobForExecution ( JobGroup g );


    /**
     * @return the groups for which jobs might be available
     */
    Collection<JobGroup> getKnownGroups ();

}