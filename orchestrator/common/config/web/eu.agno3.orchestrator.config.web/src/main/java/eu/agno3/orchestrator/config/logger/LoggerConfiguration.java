/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.logger;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:logger" )
public interface LoggerConfiguration extends ConfigurationObject {

    /**
     * @return the in days for which to retain log data
     */
    Integer getRetentionDays ();


    /**
     * @return anonymization scheme to apply to IPs logged
     */
    IPLogAnonymizationType getIpAnonymizationType ();

}
