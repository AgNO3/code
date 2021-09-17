/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config;


import org.osgi.service.cm.Configuration;


/**
 * @author mbechler
 * 
 */
public interface LoggerConfigApplier {

    /**
     * @param config
     *            CM object to apply configuration to
     * @throws LoggerConfigurationException
     *             if configuration cannot be applied
     */
    void applyTo ( Configuration config ) throws LoggerConfigurationException;
}
