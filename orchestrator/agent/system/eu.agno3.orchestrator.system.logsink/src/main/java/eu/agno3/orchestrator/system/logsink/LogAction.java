/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink;


/**
 * @author mbechler
 *
 */
public enum LogAction {

    /**
     * 
     */
    UNKNOWN,

    /**
     * 
     */
    IGNORE,

    /**
     * 
     */
    EMIT,

    /**
     * 
     */
    DROP;

    /**
     * Combines two log actions
     * 
     * @param other
     * @return the combined action
     */
    public LogAction and ( LogAction other ) {
        if ( this == UNKNOWN || other == UNKNOWN ) {
            return UNKNOWN;
        }
        else if ( this == IGNORE ) {
            return other;
        }
        else if ( other == IGNORE ) {
            return this;
        }
        else if ( this == DROP ) {
            return DROP;
        }
        else if ( other == DROP ) {
            return DROP;
        }
        return this;
    }
}
