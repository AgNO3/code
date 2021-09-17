/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.io.Serializable;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class InstanceUpdateStatus implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2219670256795364730L;

    private UpdateState state;
    private String currentStream;
    private String imageType;
    private Long currentSequence;
    private DateTime currentInstallDate;

    private boolean rebootIndicated;

    private UpdateDescriptor latestDescriptor;
    private String descriptorStream;

    private Long revertSequence;
    private DateTime revertTimestamp;
    private String revertStream;


    /**
     * @return the state
     */
    public UpdateState getState () {
        return this.state;
    }


    /**
     * @param state
     *            the state to set
     */
    public void setState ( UpdateState state ) {
        this.state = state;
    }


    /**
     * @return the rebootIndicated
     */
    public boolean getRebootIndicated () {
        return this.rebootIndicated;
    }


    /**
     * @param rebootIndicated
     *            the rebootIndicated to set
     */
    public void setRebootIndicated ( boolean rebootIndicated ) {
        this.rebootIndicated = rebootIndicated;
    }


    /**
     * @return the imageType
     */
    public String getImageType () {
        return this.imageType;
    }


    /**
     * @param imageType
     *            the imageType to set
     */
    public void setImageType ( String imageType ) {
        this.imageType = imageType;
    }


    /**
     * @return the currentSequence
     */
    public Long getCurrentSequence () {
        return this.currentSequence;
    }


    /**
     * @param currentSequence
     *            the currentSequence to set
     */
    public void setCurrentSequence ( Long currentSequence ) {
        this.currentSequence = currentSequence;
    }


    /**
     * @return the currentStream
     */
    public String getCurrentStream () {
        return this.currentStream;
    }


    /**
     * @param currentStream
     *            the currentStream to set
     */
    public void setCurrentStream ( String currentStream ) {
        this.currentStream = currentStream;
    }


    /**
     * @return the currentInstallDate
     */
    public DateTime getCurrentInstallDate () {
        return this.currentInstallDate;
    }


    /**
     * @param currentInstallDate
     *            the currentInstallDate to set
     */
    public void setCurrentInstallDate ( DateTime currentInstallDate ) {
        this.currentInstallDate = currentInstallDate;
    }


    /**
     * @return the latestDescriptor
     */
    public UpdateDescriptor getLatestDescriptor () {
        return this.latestDescriptor;
    }


    /**
     * @param latestDescriptor
     *            the latestDescriptor to set
     */
    public void setLatestDescriptor ( UpdateDescriptor latestDescriptor ) {
        this.latestDescriptor = latestDescriptor;
    }


    /**
     * @return the descriptorStream
     */
    public String getDescriptorStream () {
        return this.descriptorStream;
    }


    /**
     * @param stream
     */
    public void setDescriptorStream ( String stream ) {
        this.descriptorStream = stream;
    }


    /**
     * @return the revertSequence
     */
    public Long getRevertSequence () {
        return this.revertSequence;
    }


    /**
     * @param revertSequence
     *            the revertSequence to set
     */
    public void setRevertSequence ( Long revertSequence ) {
        this.revertSequence = revertSequence;
    }


    /**
     * @return the revertTimestamp
     */
    public DateTime getRevertTimestamp () {
        return this.revertTimestamp;
    }


    /**
     * @param revertTimestamp
     *            the revertTimestamp to set
     */
    public void setRevertTimestamp ( DateTime revertTimestamp ) {
        this.revertTimestamp = revertTimestamp;
    }


    /**
     * @return the revertStream
     */
    public String getRevertStream () {
        return this.revertStream;
    }


    /**
     * @param revertStream
     */
    public void setRevertStream ( String revertStream ) {
        this.revertStream = revertStream;
    }
}
