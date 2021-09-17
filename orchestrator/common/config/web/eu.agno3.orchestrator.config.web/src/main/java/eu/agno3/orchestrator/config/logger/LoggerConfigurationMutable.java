/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.logger;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LoggerConfiguration.class )
public interface LoggerConfigurationMutable extends LoggerConfiguration {

    /**
     * @param retentionDays
     */
    void setRetentionDays ( Integer retentionDays );


    /**
     * @param ipAnonymizationType
     */
    void setIpAnonymizationType ( IPLogAnonymizationType ipAnonymizationType );

}
