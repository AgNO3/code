/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update.jobs;


import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class UpdateCheckJob extends JobImpl {

    private Set<String> updateStreams;
    private Set<String> updateImageTypes;
    private DateTime retainAfterTime;
    private DateTime updateBeforeTime;


    /**
     * 
     */
    public UpdateCheckJob () {
        super(new UpdateJobGroup());
    }


    /**
     * @return the streams to update
     */
    public Set<String> getUpdateStreams () {
        return this.updateStreams;
    }


    /**
     * @return the image types to update
     */
    public Set<String> getUpdateImageTypes () {
        return this.updateImageTypes;
    }


    /**
     * @return time after which no usable descriptors will be retained
     */
    public DateTime getRetainAfterTime () {
        return this.retainAfterTime;
    }


    /**
     * @return do not update descriptors for which the last update is before the given time
     */
    public DateTime getUpdateBeforeTime () {
        return this.updateBeforeTime;
    }


    /**
     * 
     * @param updateStreams
     */
    public void setUpdateStreams ( Set<String> updateStreams ) {
        this.updateStreams = updateStreams;
    }


    /**
     * 
     * @param updateImageTypes
     */
    public void setUpdateImageTypes ( Set<String> updateImageTypes ) {
        this.updateImageTypes = updateImageTypes;
    }


    /**
     * 
     * @param retainAfterTime
     */
    public void setRetainAfterTime ( DateTime retainAfterTime ) {
        this.retainAfterTime = retainAfterTime;
    }


    /**
     * 
     * @param updateBeforeTime
     */
    public void setUpdateBeforeTime ( DateTime updateBeforeTime ) {
        this.updateBeforeTime = updateBeforeTime;
    }
}
