/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


/**
 * @author mbechler
 *
 */
public enum StructuralObjectState {

    /**
     * object state is unknown
     */
    UNKNOWN,

    /**
     * object is in error
     */
    ERROR,

    /**
     * warnings exist for this object
     */
    WARNING,

    /**
     * a interaction is required
     */
    NEEDSACTION,

    /**
     * everything looks ok
     */
    OK,

    /**
     * Bootstrap process required
     */
    BOOTSTRAPPING

}
