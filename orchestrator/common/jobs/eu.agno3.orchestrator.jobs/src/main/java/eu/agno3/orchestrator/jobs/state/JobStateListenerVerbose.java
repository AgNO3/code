/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.state;


import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobIllegalStateException;


/**
 * @author mbechler
 * 
 */
public interface JobStateListenerVerbose {

    /**
     * 
     * @param j
     * @return QUEUED
     * @throws JobIllegalStateException
     */
    JobInfo queued ( JobInfo j ) throws JobIllegalStateException;


    /**
     * 
     * @param j
     * @return RUNNABLE
     * @throws JobIllegalStateException
     */
    JobInfo runnable ( JobInfo j ) throws JobIllegalStateException;


    /**
     * 
     * @param j
     * @return STALLED
     * @throws JobIllegalStateException
     */
    JobInfo stalled ( JobInfo j ) throws JobIllegalStateException;


    /**
     * 
     * @param j
     * @return TIMEOUT
     * @throws JobIllegalStateException
     */
    JobInfo timeout ( JobInfo j ) throws JobIllegalStateException;


    /**
     * 
     * @param j
     * @return CANCELLED
     * @throws JobIllegalStateException
     */
    JobInfo cancel ( JobInfo j ) throws JobIllegalStateException;


    /**
     * 
     * @param j
     * @return RUNNING
     * @throws JobIllegalStateException
     */
    JobInfo running ( JobInfo j ) throws JobIllegalStateException;


    /**
     * 
     * @param j
     * @return FINISHED
     * @throws JobIllegalStateException
     */
    JobInfo finished ( JobInfo j ) throws JobIllegalStateException;


    /**
     * 
     * @param j
     * @return FAILED
     * @throws JobIllegalStateException
     */
    JobInfo failed ( JobInfo j ) throws JobIllegalStateException;


    /**
     * 
     * @param j
     * @return UNKNOWN
     * @throws JobIllegalStateException
     */
    JobInfo updateTimeout ( JobInfo j ) throws JobIllegalStateException;

}