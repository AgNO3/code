/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.jobs;


import eu.agno3.orchestrator.config.model.jobs.SystemJobGroup;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class RescanDrivesJob extends JobImpl {

    /**
     * 
     */
    public RescanDrivesJob () {
        super(new SystemJobGroup());
    }

}
