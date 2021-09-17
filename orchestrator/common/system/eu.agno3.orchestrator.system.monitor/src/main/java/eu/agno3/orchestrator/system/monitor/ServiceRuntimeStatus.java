/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.monitor;


/**
 * @author mbechler
 *
 */
public enum ServiceRuntimeStatus {

    /**
     * Service status is unknown, internal error
     */
    UNKNOWN,

    /**
     * Service is disabled
     */
    DISABLED,

    /**
     * Service is not currently running properly, critiacal error
     */
    ERROR,

    /**
     * Service is not currently running properly
     */
    WARNING,

    /**
     * Service is currently running properly
     */
    ACTIVE,

    /**
     * Service is currently transient (stopping, starting, reconfiguring)
     */
    TRANSIENT

}
