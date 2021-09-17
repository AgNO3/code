/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config;


import java.util.Map;


/**
 * A source for logging configuration
 * 
 * @author mbechler
 * 
 */
public interface LoggerConfigurationSource {

    /**
     * Get the configuration described by this source
     * 
     * @return the current configuration provided by this source
     * @throws LoggerConfigurationException
     *             if the configuration cannot be constructed
     */
    Map<String, ?> getConfig () throws LoggerConfigurationException;
}
