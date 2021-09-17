/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config.internal;


import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.agno3.runtime.logging.DynamicVerbosityLayout;
import eu.agno3.runtime.logging.LogConfigurationService;
import eu.agno3.runtime.logging.TracingVerbosity;


/**
 * @author mbechler
 * 
 */
public class LogConfigurationServiceImpl implements LogConfigurationService {

    private DynamicLoggerConfigurationSource dynamicSource;
    private DynamicVerbosityLayout tracingConsoleLayout;

    private static final Level[] LOG_LEVELS = {
        Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL
    };


    /**
     * @param tracingConsoleLayout
     * @param dynamicSource
     */
    public LogConfigurationServiceImpl ( DynamicLoggerConfigurationSource dynamicSource, DynamicVerbosityLayout tracingConsoleLayout ) {
        super();
        this.tracingConsoleLayout = tracingConsoleLayout;
        this.dynamicSource = dynamicSource;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.LogConfigurationService#getEffectiveLogLevel(java.lang.String)
     */
    @Override
    public Level getEffectiveLogLevel ( String scope ) {
        return getLoggerLevel(Logger.getLogger(scope));
    }


    /**
     * Retrieve the lowest active logger level from a LOG4J Logger
     * 
     * @param l
     * @return lowest active logger level
     */
    private static Level getLoggerLevel ( Logger l ) {
        for ( Level level : LOG_LEVELS ) {
            if ( l.isEnabledFor(level) ) {
                return level;
            }
        }

        return Level.OFF;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.LogConfigurationService#getOverrideLogLevel(java.lang.String)
     */
    @Override
    public Level getOverrideLogLevel ( String scope ) {
        return this.dynamicSource.getLevel(scope);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.LogConfigurationService#setOverrideLogLevel(java.lang.String,
     *      org.apache.log4j.Level)
     */
    @Override
    public void setOverrideLogLevel ( String scope, Level level ) {
        this.dynamicSource.setLevel(scope, level);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.LogConfigurationService#listOverrides()
     */
    @Override
    public Map<String, Level> listOverrides () {
        return this.dynamicSource.getLoggerLevels();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.LogConfigurationService#resetOverrides(java.lang.String)
     */
    @Override
    public void resetOverrides ( String scope ) {
        this.dynamicSource.reset(scope);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.LogConfigurationService#setTraceVerbosity(eu.agno3.runtime.logging.TracingVerbosity)
     */
    @Override
    public void setTraceVerbosity ( TracingVerbosity verbosity ) {
        this.tracingConsoleLayout.setVerbosity(verbosity);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.LogConfigurationService#getTracingVerbosity()
     */
    @Override
    public TracingVerbosity getTracingVerbosity () {
        return this.tracingConsoleLayout.getVerbosity();
    }

}
