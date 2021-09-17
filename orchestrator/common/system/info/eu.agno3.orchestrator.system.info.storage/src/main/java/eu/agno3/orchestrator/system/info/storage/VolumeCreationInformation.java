/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage;


/**
 * @author mbechler
 *
 */
public class VolumeCreationInformation {

    private String driveId;
    private String volume;
    private String label;
    private boolean force;


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


    /**
     * @return the force
     */
    public boolean getForce () {
        return this.force;
    }


    /**
     * @param force
     *            the force to set
     */
    public void setForce ( boolean force ) {
        this.force = force;
    }


    /**
     * @return the label
     */
    public String getLabel () {
        return this.label;
    }


    /**
     * @param label
     *            the label to set
     */
    public void setLabel ( String label ) {
        this.label = label;
    }
}
