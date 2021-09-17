/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.jobs;


import eu.agno3.orchestrator.config.model.jobs.SystemJobGroup;
import eu.agno3.orchestrator.jobs.JobImpl;
import eu.agno3.orchestrator.system.info.storage.VolumeCreationInformation;


/**
 * @author mbechler
 *
 */
public class InitializeDriveJob extends JobImpl {

    private VolumeCreationInformation creationInfo;


    /**
     * 
     */
    public InitializeDriveJob () {
        super(new SystemJobGroup());
    }


    /**
     * @return the creationInfo
     */
    public VolumeCreationInformation getCreationInfo () {
        return this.creationInfo;
    }


    /**
     * @param creationInfo
     *            the creationInfo to set
     */
    public void setCreationInfo ( VolumeCreationInformation creationInfo ) {
        this.creationInfo = creationInfo;
    }
}
