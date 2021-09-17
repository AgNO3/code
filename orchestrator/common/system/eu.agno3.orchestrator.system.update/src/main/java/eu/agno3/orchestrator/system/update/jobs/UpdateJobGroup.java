/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.12.2014 by mbechler
 */
package eu.agno3.orchestrator.system.update.jobs;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobGroup;


/**
 * @author mbechler
 *
 */
@Component ( service = JobGroup.class )
public class UpdateJobGroup implements JobGroup {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobGroup#getId()
     */
    @Override
    public String getId () {
        return "update"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobGroup#isCheckGlobalConflicts()
     */
    @Override
    public boolean isCheckGlobalConflicts () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobGroup#conflicts(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public boolean conflicts ( Job j1, Job j2 ) {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.getClass().hashCode();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object other ) {
        return other instanceof UpdateJobGroup;
    }
}
