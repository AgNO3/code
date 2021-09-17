/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.config;


/**
 * @author mbechler
 * 
 */
public enum FilterChainPriorities {

    /**
     * Priority to use for global fallbacks (default deny)
     */
    FALLBACK(1000),

    /**
     * Priority to use for global defaults
     */
    GLOBAL_DEFAULT(0),

    /**
     * Priority to use for overriding global defaults
     */
    GLOBAL_OVERRIDE(-10),

    /**
     * Priority to use for module defaults
     */
    MODULE_DEFAULT(-100),

    /**
     * Priority to use for module level overrides
     */
    MODULE_OVERRIDE(-1000);

    private int priority;


    FilterChainPriorities ( int priority ) {
        this.priority = priority;
    }


    /**
     * @return the priority value for this entry
     */
    public int getPriority () {
        return this.priority;
    }
}
