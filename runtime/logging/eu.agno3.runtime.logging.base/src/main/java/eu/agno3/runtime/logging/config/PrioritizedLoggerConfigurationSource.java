/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config;


/**
 * A source for logging configuration
 * 
 * When merging these sources, a source having a higher priority value will be preferred over the ones having lower
 * values.
 * 
 * 
 * @author mbechler
 * 
 */
public interface PrioritizedLoggerConfigurationSource extends LoggerConfigurationSource, Comparable<PrioritizedLoggerConfigurationSource> {

    /**
     * Get source priority
     * 
     * @return the priority of the config source, higher is favored
     */
    int getPriority ();
}
