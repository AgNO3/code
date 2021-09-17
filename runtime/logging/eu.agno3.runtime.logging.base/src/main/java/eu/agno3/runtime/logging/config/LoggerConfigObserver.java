/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config;


/**
 * @author mbechler
 * 
 */
public interface LoggerConfigObserver {

    /**
     * Called when a logger configuration source changed
     * 
     * @param s
     *            The configuration source that has changed
     */
    void configurationUpdated ( LoggerConfigurationSource s );

}
