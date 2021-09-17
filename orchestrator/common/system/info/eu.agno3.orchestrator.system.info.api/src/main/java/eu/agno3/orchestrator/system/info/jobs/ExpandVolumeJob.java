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
public class ExpandVolumeJob extends JobImpl {

    private String driveId;
    private String volume;


    /**
     * 
     */
    public ExpandVolumeJob () {
        super(new SystemJobGroup());
    }


    /**
     * @return the driveId
     */
    public String getDriveId () {
        return this.driveId;
    }


    /**
     * @param driveId
     *            the driveId to set
     */
    public void setDriveId ( String driveId ) {
        this.driveId = driveId;
    }


    /**
     * @return the volume
     */
    public String getVolume () {
        return this.volume;
    }


    /**
     * @param volume
     *            the volume to set
     */
    public void setVolume ( String volume ) {
        this.volume = volume;
    }
}
