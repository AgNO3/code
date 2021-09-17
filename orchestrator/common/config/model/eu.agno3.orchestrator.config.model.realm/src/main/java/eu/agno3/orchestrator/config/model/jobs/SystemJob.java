/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.jobs;


import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 * 
 */
public class SystemJob extends JobImpl {

    /**
     * 
     */
    public SystemJob () {
        super(new SystemJobGroup());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobImpl#setJobGroup(eu.agno3.orchestrator.jobs.JobGroup)
     */
    @Override
    public void setJobGroup ( JobGroup jobGroup ) {
        // ignore
    }

}
