/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.validation.Valid;

import eu.agno3.orchestrator.config.logger.LoggerConfiguration;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:logger" )
public interface FileshareLoggerConfig extends ConfigurationObject {

    /**
     * @return logger configuration for unauthenticated/non-terms accepted purposes
     */
    @ReferencedObject
    @Valid
    LoggerConfiguration getUnauthLoggerConfig ();


    /**
     * @return default logger configuration
     */
    @ReferencedObject
    @Valid
    LoggerConfiguration getDefaultLoggerConfig ();

}
