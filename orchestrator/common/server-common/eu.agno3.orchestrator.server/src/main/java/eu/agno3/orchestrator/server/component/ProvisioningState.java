/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2015 by mbechler
 */
package eu.agno3.orchestrator.server.component;


/**
 * @author mbechler
 *
 */
public enum ProvisioningState {

    /**
     * 
     */
    UNKNOWN,

    /**
     * 
     */
    BOOTSTRAP,

    /**
     * Agent is not attached to an instance
     */
    DETACHED,

    /**
     * Agent is attached to an instance
     */
    ATTACHED
}
