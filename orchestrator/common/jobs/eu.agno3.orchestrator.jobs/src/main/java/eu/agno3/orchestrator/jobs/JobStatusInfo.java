/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


import java.io.Serializable;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 * 
 */
public interface JobStatusInfo extends Serializable {

    /**
     * 
     * @return the job id
     */
    UUID getJobId ();


    /**
     * 
     * @return the job target
     */
    JobTarget getTarget ();


    /**
     * 
     * @return the job type
     */
    String getJobType ();


    /**
     * 
     * @return the job's state
     */
    JobState getState ();


    /**
     * 
     * @return the progress
     */
    JobProgressInfo getProgress ();


    /**
     * @return the display name for the target
     */
    String getTargetDisplayName ();


    /**
     * @return the job owner
     */
    UserPrincipal getOwner ();


    /**
     * @return the time the job was queued
     */
    DateTime getQueuedTime ();


    /**
     * @return the time the job was started
     */
    DateTime getStartedTime ();


    /**
     * @return the time the job was finished
     */
    DateTime getFinishedTime ();

}
