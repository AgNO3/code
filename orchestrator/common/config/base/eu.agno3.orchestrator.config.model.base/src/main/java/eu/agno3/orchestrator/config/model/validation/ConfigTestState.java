/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.validation;


/**
 * @author mbechler
 *
 */
public enum ConfigTestState {

    /**
     * 
     */
    UNKNOWN,

    /**
     * Validation has failed
     */
    VALIDATION,

    /**
     * Test has been queued
     */
    QUEUED,

    /**
     * Test is executing
     */
    RUNNING,

    /**
     * No test plugin found
     */
    NO_TEST,

    /**
     * 
     */
    WARNING,

    /**
     * 
     */
    FAILURE,

    /**
     * 
     */
    SUCCESS,

}
