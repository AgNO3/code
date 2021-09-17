/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config.internal;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.agno3.runtime.logging.config.AbstractLoggerConfigurationSource;
import eu.agno3.runtime.logging.config.LoggerConfigurationException;


/**
 * @author mbechler
 * 
 */
public class DynamicLoggerConfigurationSource extends AbstractLoggerConfigurationSource {

    private static final String LOGGER_CONFIG_PREFIX = "log4j.logger."; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DynamicLoggerConfigurationSource.class);

    private Map<String, Level> loggerLevels = new HashMap<>();


    /**
     * @param prio
     *            Priority of this dynamic logger
     */
    public DynamicLoggerConfigurationSource ( int prio ) {
        super(prio);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.config.LoggerConfigurationSource#getConfig()
     */
    @Override
    public Map<String, ?> getConfig () throws LoggerConfigurationException {
        Map<String, Object> res = new HashMap<>();
        log.debug("Setting dynamic logger configuration"); //$NON-NLS-1$
        synchronized ( this.loggerLevels ) {
            for ( Entry<String, Level> entry : this.loggerLevels.entrySet() ) {
                String loggerKey = LOGGER_CONFIG_PREFIX + entry.getKey();
                res.put(loggerKey, entry.getValue().toString());
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("%s=%s", loggerKey, entry.getValue().toString())); //$NON-NLS-1$
                }
            }

        }
        return res;
    }


    /**
     * Get all loggers and their levels configured through this config source
     * 
     * @return Map of loggers and their assigned levels
     */
    public Map<String, Level> getLoggerLevels () {
        return new HashMap<>(this.loggerLevels);
    }


    /**
     * Reset a logging scope to its default value.
     * 
     * Effectivly removes all logger config below this scope from this configuration source.
     * 
     * @param scope
     *            Logger scope to remove
     */
    public void reset ( String scope ) {
        List<String> toRemove = new LinkedList<>();

        synchronized ( this.loggerLevels ) {
            for ( String logger : this.loggerLevels.keySet() ) {
                if ( logger.equals(scope) || logger.startsWith(scope + ".") ) { //$NON-NLS-1$
                    toRemove.add(logger);
                }
            }

            for ( String logger : toRemove ) {
                this.loggerLevels.remove(logger);
            }
        }
        this.setChanged();
        this.notifyObservers();
    }


    /**
     * Set a logger to a specific level
     * 
     * @param logger
     * @param level
     */
    public void setLevel ( String logger, Level level ) {
        log.debug(String.format("Set threshold of logger '%s' to %s", logger, level.toString())); //$NON-NLS-1$
        synchronized ( this.loggerLevels ) {
            this.loggerLevels.put(logger, level);
        }
        this.setChanged();
        this.notifyObservers();
    }


    /**
     * Get this configuration source's log level of a specific logger
     * 
     * @param logger
     * @return the logger's log level
     */
    public Level getLevel ( String logger ) {
        if ( this.loggerLevels.containsKey(logger) ) {
            return this.loggerLevels.get(logger);
        }
        return null;
    }
}
