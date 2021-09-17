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
public enum Status {
    /**
     * The execution unit has failed
     * 
     * This can be either caused by the execution unit returning this status or
     * by an exception.
     */
    FAIL,

    /**
     * The execution unit was skipped because of external conditions
     */
    SKIPPED,

    /**
     * The execution unit ran successfully
     */
    SUCCESS;

    /**
     * @param s
     * @return the combined status
     */
    public Status and ( Status s ) {
        if ( s == FAIL || this == FAIL ) {
            return FAIL;
        }

        if ( this == SUCCESS || s == SUCCESS ) {
            return SUCCESS;
        }

        if ( this == SKIPPED || s == SKIPPED ) {
            return SKIPPED;
        }

        return this;
    }
}
