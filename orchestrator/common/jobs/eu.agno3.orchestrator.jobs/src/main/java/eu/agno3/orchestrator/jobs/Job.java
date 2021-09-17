/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 * 
 */
public interface Job {

    /**
     * 
     * @return a unique identifier for a job instance
     */
    UUID getJobId ();


    /**
     * 
     * @return the job owner, null if none
     */
    UserPrincipal getOwner ();


    /**
     * @return the system component on which this job should be executed
     * 
     */
    JobTarget getTarget ();


    /**
     * 
     * @return the job group this job belongs to, used for serialization
     */
    JobGroup getJobGroup ();


    /**
     * 
     * @return the deadline until which the job may be queued
     */
    DateTime getDeadline ();

}
