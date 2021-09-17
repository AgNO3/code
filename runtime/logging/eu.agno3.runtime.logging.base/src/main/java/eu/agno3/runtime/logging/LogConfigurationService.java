/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.logging;


import java.util.Map;

import org.apache.log4j.Level;


/**
 * Service to dynamically configure the logging facilities
 * 
 * @author mbechler
 * 
 */
public interface LogConfigurationService {

    /**
     * Get the log level for a specific scope
     * 
     * @param scope
     *            Logger scope
     * @return Log level threshold for this scope
     */
    Level getEffectiveLogLevel ( String scope );


    /**
     * Get the override level for a specific scope
     * 
     * @param scope
     *            Logger scope
     * @return The override Level or null if none is set
     */
    Level getOverrideLogLevel ( String scope );


    /**
     * Set an Level override for a specific scope
     * 
     * @param scope
     *            Scope to set override
     * @param level
     *            Level to set
     */
    void setOverrideLogLevel ( String scope, Level level );


    /**
     * Get all active Level overrides
     * 
     * @return A map of all scopes with explicit overrides
     */
    Map<String, Level> listOverrides ();


    /**
     * Resets all overrides on a scope and its children
     * 
     * @param scope
     *            Scope to reset
     */
    void resetOverrides ( String scope );


    /**
     * Set the verbosity of tracing (console) output
     * 
     * @param verbosity
     */
    void setTraceVerbosity ( TracingVerbosity verbosity );


    /**
     * Get the current tracing verbosity
     * 
     * @return current tracing verbosity
     */
    TracingVerbosity getTracingVerbosity ();
}
