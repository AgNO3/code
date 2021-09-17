/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 * 
 */
@Component ( service = JobGroup.class )
public class DefaultGroup implements JobGroup {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobGroup#getId()
     */
    @Override
    public String getId () {
        return "default"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobGroup#conflicts(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public boolean conflicts ( Job j1, Job j2 ) {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof DefaultGroup ) {
            return true;
        }
        return super.equals(obj);
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
     * @see eu.agno3.orchestrator.jobs.JobGroup#isCheckGlobalConflicts()
     */
    @Override
    public boolean isCheckGlobalConflicts () {
        return false;
    }
}
