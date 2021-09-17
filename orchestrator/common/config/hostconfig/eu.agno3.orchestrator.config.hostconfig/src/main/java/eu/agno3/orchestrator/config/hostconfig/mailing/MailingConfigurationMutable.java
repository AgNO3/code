/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.mailing;


import eu.agno3.orchestrator.config.web.SMTPConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( MailingConfiguration.class )
public interface MailingConfigurationMutable extends MailingConfiguration {

    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.mailing.MailingConfiguration#getSmtpConfiguration()
     */
    @Override
    SMTPConfigurationMutable getSmtpConfiguration ();


    /**
     * @param smtpConfiguration
     */
    void setSmtpConfiguration ( SMTPConfigurationMutable smtpConfiguration );


    /**
     * @param mailingEnabled
     */
    void setMailingEnabled ( Boolean mailingEnabled );

}
