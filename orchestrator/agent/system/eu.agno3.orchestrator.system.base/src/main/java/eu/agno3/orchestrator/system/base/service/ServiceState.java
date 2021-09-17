/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.service;


/**
 * @author mbechler
 * 
 */
public enum ServiceState {

    /**
     * Service state is unknown
     */
    UNKNOWN,

    /**
     * Service is inactive (not started)
     */
    INACTIVE,

    /**
     * Service is inactive by failure
     */
    FAILED,

    /**
     * Currently activating
     */
    ACTIVATING,

    /**
     * Currently deactivating
     */
    DEACTIVATING,

    /**
     * Service is active
     */
    ACTIVE

}
