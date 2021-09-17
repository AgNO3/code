/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
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
public interface JobInfo extends Serializable {

    /**
     * 
     * @return the job id
     */
    UUID getJobId ();


    /**
     * @return the job type (class name)
     */
    String getType ();


    /**
     * 
     * @return the user that started this job, null if none
     */
    UserPrincipal getOwner ();


    /**
     * 
     * @return the job's state
     */
    JobState getState ();


    /**
     * 
     * @return the time this job was queued
     */
    DateTime getQueuedTime ();


    /**
     * 
     * @return the time this job's execution was started
     */
    DateTime getStartedTime ();


    /**
     * 
     * @return the time this job's execution was finished
     */
    DateTime getFinishedTime ();


    /**
     * 
     * @return the last time this job was known to be active
     */
    DateTime getLastKeepAliveTime ();

}
