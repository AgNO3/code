/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.mailing;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.web.SMTPConfigurationImpl;
import eu.agno3.orchestrator.config.web.SMTPConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( MailingConfiguration.class )
@Entity
@Table ( name = "config_hostconfig_mailing" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_mail" )
public class MailingConfigurationImpl extends AbstractConfigurationObject<MailingConfiguration> implements MailingConfiguration,
        MailingConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -2321523169593411188L;

    private Boolean mailingEnabled;

    private SMTPConfigurationImpl smtpConfiguration;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<MailingConfiguration> getType () {
        return MailingConfiguration.class;
    }


    /**
     * @return the mailingEnabled
     */
    @Override
    public Boolean getMailingEnabled () {
        return this.mailingEnabled;
    }


    /**
     * @param mailingEnabled
     *            the mailingEnabled to set
     */
    @Override
    public void setMailingEnabled ( Boolean mailingEnabled ) {
        this.mailingEnabled = mailingEnabled;
    }


    /**
     * @return the smtpConfiguration
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = SMTPConfigurationImpl.class )
    public SMTPConfigurationMutable getSmtpConfiguration () {
        return this.smtpConfiguration;
    }


    /**
     * @param smtpConfiguration
     *            the smtpConfiguration to set
     */
    @Override
    public void setSmtpConfiguration ( SMTPConfigurationMutable smtpConfiguration ) {
        this.smtpConfiguration = (SMTPConfigurationImpl) smtpConfiguration;
    }
}
