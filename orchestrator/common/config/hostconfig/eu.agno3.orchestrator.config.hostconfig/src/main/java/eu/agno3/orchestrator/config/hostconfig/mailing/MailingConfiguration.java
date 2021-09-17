/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.mailing;


import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.web.SMTPConfiguration;
import eu.agno3.runtime.validation.ValidConditional;


/**
 * Basic system configuration
 * 
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:mailing" )

public interface MailingConfiguration extends ConfigurationObject {

    /**
     * @return the smtp client configuration
     */
    @ReferencedObject
    @ValidConditional ( when = "#{mailingEnabled}" )
    SMTPConfiguration getSmtpConfiguration ();


    /**
     * @return whether mailing is generally enabled
     */
    Boolean getMailingEnabled ();

}
