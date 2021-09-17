/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


/**
 * @author mbechler
 * 
 */
public enum JobState {

    /**
     * Job state is unknown right now
     * 
     * e.g. connection to delegated coordinator is unavailable
     */
    UNKNOWN,

    /**
     * Job has not yet been submitted to a coordinator
     */
    NEW,

    /**
     * Job is ready for execution but the target is not available
     */
    STALLED,

    /**
     * Job is queued by a coordinator
     */
    QUEUED,

    /**
     * Job is queued and prerequesites are met
     */
    RUNNABLE,

    /**
     * Job is currently executing
     */
    RUNNING,

    /**
     * Job finished executing
     */
    FINISHED,

    /**
     * An error occured while executing
     */
    FAILED,

    /**
     * The job was cancelled
     */
    CANCELLED,

    /**
     * The job reached it's execution deadline while queued
     */
    TIMEOUT,

    /**
     * The job is suspended, waiting for resumption
     */
    SUSPENDED,

    /**
     * The job is resumed
     */
    RESUMED

}
