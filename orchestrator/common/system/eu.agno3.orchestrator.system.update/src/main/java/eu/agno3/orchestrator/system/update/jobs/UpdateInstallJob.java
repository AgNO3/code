/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update.jobs;


import eu.agno3.orchestrator.jobs.JobImpl;
import eu.agno3.orchestrator.system.update.UpdateDescriptor;


/**
 * @author mbechler
 *
 */
public class UpdateInstallJob extends JobImpl {

    private UpdateDescriptor descriptor;
    private String descriptorStream;
    private boolean allowReboot;


    /**
     * 
     */
    public UpdateInstallJob () {
        super(new UpdateJobGroup());
    }


    /**
     * @return the descriptor
     */
    public UpdateDescriptor getDescriptor () {
        return this.descriptor;
    }


    /**
     * @param descriptor
     *            the descriptor to set
     */
    public void setDescriptor ( UpdateDescriptor descriptor ) {
        this.descriptor = descriptor;
    }


    /**
     * @return the descriptors source stream
     */
    public String getDescriptorStream () {
        return this.descriptorStream;
    }


    /**
     * @param descriptorStream
     *            the descriptorStream to set
     */
    public void setDescriptorStream ( String descriptorStream ) {
        this.descriptorStream = descriptorStream;
    }


    /**
     * @return whether to allow automated reboots
     */
    public boolean getAllowReboot () {
        return this.allowReboot;
    }


    /**
     * @param allowReboot
     *            the allowReboot to set
     */
    public void setAllowReboot ( boolean allowReboot ) {
        this.allowReboot = allowReboot;
    }
}
