/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.smtp.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.runtime.mail.SMTPConfiguration;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 *
 */
public class SMTPConfigurationAdapter implements SMTPConfiguration {

    private eu.agno3.orchestrator.config.web.SMTPConfiguration config;
    private String instanceId;
    private String hostname;


    /**
     * @param config
     */
    public SMTPConfigurationAdapter ( eu.agno3.orchestrator.config.web.SMTPConfiguration config ) {
        this.config = config;
        this.instanceId = "test-" + config.getId(); //$NON-NLS-1$
        this.hostname = LocalHostUtil.guessPrimaryHostName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getInstanceId()
     */
    @Override
    public String getInstanceId () {
        return this.instanceId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isUseSendmail()
     */
    @Override
    public boolean isUseSendmail () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSendmailExtraArgs()
     */
    @Override
    public List<String> getSendmailExtraArgs () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSendmailPath()
     */
    @Override
    public String getSendmailPath () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isSetSendmailSender()
     */
    @Override
    public boolean isSetSendmailSender () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isSSL()
     */
    @Override
    public boolean isSSL () {
        return this.config.getSslClientMode() == SSLClientMode.SSL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isStartTLS()
     */
    @Override
    public boolean isStartTLS () {
        return this.config.getSslClientMode() == SSLClientMode.REQUIRE_STARTTLS || this.config.getSslClientMode() == SSLClientMode.TRY_STARTTLS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isStartTLSRequired()
     */
    @Override
    public boolean isStartTLSRequired () {
        return this.config.getSslClientMode() == SSLClientMode.REQUIRE_STARTTLS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSMTPHost()
     */
    @Override
    public String getSMTPHost () {
        return this.config.getServerUri().getHost();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSMTPPort()
     */
    @Override
    public int getSMTPPort () {
        return this.config.getServerUri().getPort();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSMTPUser()
     */
    @Override
    public String getSMTPUser () {
        return this.config.getSmtpUser();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSMTPPassword()
     */
    @Override
    public String getSMTPPassword () {
        return this.config.getSmtpPassword();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isAuthEnabled()
     */
    @Override
    public boolean isAuthEnabled () {
        return this.config.getAuthEnabled();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getAuthMechanisms()
     */
    @Override
    public List<String> getAuthMechanisms () {
        return new ArrayList<>(this.config.getAuthMechanisms());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getDefaultFromAddress()
     */
    @Override
    public String getDefaultFromAddress () {
        if ( this.config.getOverrideDefaultFromAddress() != null ) {
            return this.config.getOverrideDefaultFromAddress();
        }

        String userName = System.getProperty("user.name"); //$NON-NLS-1$
        String hn = getEhloHostName();
        if ( Character.isDigit(hn.charAt(0)) ) {
            return String.format("%s@[%s]", userName, hn); //$NON-NLS-1$
        }

        return String.format("%s@%s", userName, hn); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getDefaultFromName()
     */
    @Override
    public String getDefaultFromName () {
        return this.config.getOverrideDefaultFromName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getExtraProperties()
     */
    @Override
    public Map<String, String> getExtraProperties () {
        return Collections.EMPTY_MAP;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getEhloHostName()
     */
    @Override
    public String getEhloHostName () {
        if ( this.config.getOverrideEhloHostName() != null ) {
            return this.config.getOverrideEhloHostName();
        }
        return this.hostname;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getReadTimeout()
     */
    @Override
    public int getReadTimeout () {
        return (int) this.config.getSocketTimeout().getMillis();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getWriteTimeout()
     */
    @Override
    public int getWriteTimeout () {
        return (int) this.config.getSocketTimeout().getMillis();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getConnTimeout()
     */
    @Override
    public int getConnTimeout () {
        return (int) this.config.getSocketTimeout().getMillis();
    }

}
