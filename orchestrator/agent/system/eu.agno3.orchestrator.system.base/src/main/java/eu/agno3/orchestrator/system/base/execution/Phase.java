/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


/**
 * @author mbechler
 * 
 */
public enum Phase implements Comparable<Phase> {
    /**
     * Early validation of requirements for execution
     */
    VALIDATE,

    /**
     * Prepare for execution (should e.g. generate resources but not modify anything critical)
     */
    PREPARE,

    /**
     * Actually make changes to the system
     */
    EXECUTE,

    /**
     * Undo changes done in the EXECUTE phase
     */
    ROLLBACK,

    /**
     * Remove e.g. temporary files
     */
    CLEANUP,

    /**
     * Prepare units for suspending
     */
    SUSPEND,

    /**
     * Prepare units for restoration
     */
    RESUME;
}
