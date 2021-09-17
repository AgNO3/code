/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.02.2015 by mbechler
 */
package eu.agno3.runtime.mail.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.mail.SMTPConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = SMTPConfiguration.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = SMTPConfiguration.PID )
public class SMTPConfigurationImpl implements SMTPConfiguration {

    private static final String EXTRA_PREFIX = "extra."; //$NON-NLS-1$

    private String smtpHost;
    private Integer smtpPort;

    private Duration connTimeout;
    private Duration readTimeout;
    private Duration writeTimeout;

    private String ehloHostName;
    private String defaultFromAddress;
    private String defaultFromName;

    private boolean ssl;
    private boolean startTLS;
    private boolean startTLSRequired;

    private boolean authEnabled;
    private String smtpUser;
    private String smtpPassword;
    private List<String> authMechanisms;

    private Map<String, String> extraProperties = new HashMap<>();

    private String instanceId;

    private boolean useSendmail;
    private boolean setSendmailSender;
    private List<String> sendmailExtraArgs;
    private String sendmailPath;


    @Activate
    @Modified
    protected synchronized void activate ( ComponentContext ctx ) throws IOException {
        this.instanceId = (String) ctx.getProperties().get("instanceId"); //$NON-NLS-1$
        this.useSendmail = ConfigUtil.parseBoolean(ctx.getProperties(), "useSendmail", false); //$NON-NLS-1$
        this.setSendmailSender = ConfigUtil.parseBoolean(ctx.getProperties(), "setSendmailSender", true); //$NON-NLS-1$
        this.sendmailExtraArgs = new ArrayList<>(ConfigUtil.parseStringCollection(ctx.getProperties(), "sendmailExtraArgs", Collections.EMPTY_LIST)); //$NON-NLS-1$
        this.sendmailPath = ConfigUtil.parseString(ctx.getProperties(), "sendmailPath", null); //$NON-NLS-1$

        configureSenderDefaults(ctx);
        configureTimeouts(ctx);
        configureSMTPAddress(ctx);
        configureTLS(ctx);
        configureAuth(ctx);
        configureExtraProperties(ctx);
    }


    /**
     * @param ctx
     */
    private void configureTimeouts ( ComponentContext ctx ) {
        this.readTimeout = ConfigUtil.parseDuration(ctx.getProperties(), "readTimeout", Duration.standardSeconds(5)); //$NON-NLS-1$
        this.writeTimeout = ConfigUtil.parseDuration(ctx.getProperties(), "writeTimeout", Duration.standardSeconds(2)); //$NON-NLS-1$
        this.connTimeout = ConfigUtil.parseDuration(ctx.getProperties(), "connTimeout", Duration.standardSeconds(3)); //$NON-NLS-1$
    }


    /**
     * @param ctx
     */
    private void configureExtraProperties ( ComponentContext ctx ) {
        Enumeration<String> keys = ctx.getProperties().keys();
        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();

            if ( key != null && key.startsWith(EXTRA_PREFIX) ) {
                String realKey = key.substring(EXTRA_PREFIX.length());
                this.extraProperties.put(realKey, ctx.getProperties().get(key).toString());
            }
        }
    }


    /**
     * @param ctx
     * @throws IOException
     */
    private void configureAuth ( ComponentContext ctx ) throws IOException {
        String authEnableAttr = (String) ctx.getProperties().get("enableAuth"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(authEnableAttr) && Boolean.parseBoolean(authEnableAttr.trim()) ) {
            this.authEnabled = true;
        }

        String userAttr = (String) ctx.getProperties().get("smtpUser"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(userAttr) ) {
            this.smtpUser = userAttr.trim();
        }

        this.smtpPassword = ConfigUtil.parseSecret(ctx.getProperties(), "smtpPassword", null); //$NON-NLS-1$

        String mechsAttr = (String) ctx.getProperties().get("authMechs"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(mechsAttr) ) {
            List<String> mechs = new ArrayList<>();

            for ( String mech : StringUtils.split(mechsAttr, ',') ) {
                mechs.add(mech.trim());
            }
            this.authMechanisms = mechs;
        }
    }


    /**
     * @param ctx
     */
    private void configureTLS ( ComponentContext ctx ) {
        String sslAttr = (String) ctx.getProperties().get("enableSSL"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(sslAttr) && Boolean.parseBoolean(sslAttr.trim()) ) {
            this.ssl = true;
        }

        String startTLSAttr = (String) ctx.getProperties().get("enableStartTLS"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(startTLSAttr) && Boolean.parseBoolean(startTLSAttr.trim()) ) {
            this.startTLS = true;
        }

        String startTLSRequiredAttr = (String) ctx.getProperties().get("requireStartTLS"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(startTLSRequiredAttr) && Boolean.parseBoolean(startTLSRequiredAttr.trim()) ) {
            this.startTLSRequired = true;
        }
    }


    /**
     * @param ctx
     */
    private void configureSMTPAddress ( ComponentContext ctx ) {
        String smtpHostAttr = (String) ctx.getProperties().get("smtpHost"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(smtpHostAttr) ) {
            this.smtpHost = smtpHostAttr.trim();
        }
        else {
            this.smtpHost = "localhost"; //$NON-NLS-1$
        }

        String smtpPortAttr = (String) ctx.getProperties().get("smtpPort"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(smtpPortAttr) ) {
            this.smtpPort = Integer.valueOf(smtpPortAttr.trim());
        }
    }


    /**
     * @param ctx
     */
    private void configureSenderDefaults ( ComponentContext ctx ) {
        String ehloOverrideAttr = (String) ctx.getProperties().get("overrideHostname"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(ehloOverrideAttr) ) {
            this.ehloHostName = ehloOverrideAttr.trim();
        }
        else {
            this.ehloHostName = LocalHostUtil.guessPrimaryHostName();
        }

        String defaultFromAddressAttr = (String) ctx.getProperties().get("defaultFromAddress"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(defaultFromAddressAttr) ) {
            this.defaultFromAddress = defaultFromAddressAttr.trim();
        }
        else {
            String userName = System.getProperty("user.name"); //$NON-NLS-1$
            if ( Character.isDigit(this.ehloHostName.charAt(0)) ) {
                this.defaultFromAddress = String.format("%s@[%s]", userName, this.ehloHostName); //$NON-NLS-1$
            }
            else {
                this.defaultFromAddress = String.format("%s@%s", userName, this.ehloHostName); //$NON-NLS-1$
            }
        }

        String defaultFromNameAttr = (String) ctx.getProperties().get("defaultFromName"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(defaultFromNameAttr) ) {
            this.defaultFromName = defaultFromNameAttr.trim();
        }
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
        return this.useSendmail;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSendmailPath()
     */
    @Override
    public String getSendmailPath () {
        return this.sendmailPath;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSendmailExtraArgs()
     */
    @Override
    public List<String> getSendmailExtraArgs () {
        return this.sendmailExtraArgs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isSetSendmailSender()
     */
    @Override
    public boolean isSetSendmailSender () {
        return this.setSendmailSender;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isSSL()
     */
    @Override
    public boolean isSSL () {
        return this.ssl;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isStartTLS()
     */
    @Override
    public boolean isStartTLS () {
        return this.startTLS;
    }


    /**
     * @return the ehloHostName
     */
    @Override
    public String getEhloHostName () {
        return this.ehloHostName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isStartTLSRequired()
     */
    @Override
    public boolean isStartTLSRequired () {
        return this.startTLSRequired;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSMTPHost()
     */
    @Override
    public String getSMTPHost () {
        return this.smtpHost;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSMTPPort()
     */
    @Override
    public int getSMTPPort () {
        if ( this.smtpPort == null ) {
            return this.isSSL() ? 465 : 25;
        }
        return this.smtpPort;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSMTPUser()
     */
    @Override
    public String getSMTPUser () {
        return this.smtpUser;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getSMTPPassword()
     */
    @Override
    public String getSMTPPassword () {
        return this.smtpPassword;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#isAuthEnabled()
     */
    @Override
    public boolean isAuthEnabled () {
        return this.authEnabled;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getAuthMechanisms()
     */
    @Override
    public List<String> getAuthMechanisms () {
        return this.authMechanisms;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getDefaultFromAddress()
     */
    @Override
    public String getDefaultFromAddress () {
        return this.defaultFromAddress;
    }


    /**
     * @return the defaultFromName
     */
    @Override
    public String getDefaultFromName () {
        return this.defaultFromName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPConfiguration#getExtraProperties()
     */
    @Override
    public Map<String, String> getExtraProperties () {
        return this.extraProperties;
    }


    /**
     * @return the readTimeout in millis
     */
    @Override
    public int getReadTimeout () {
        return (int) this.readTimeout.getMillis();
    }


    /**
     * @return the writeTimeout in millis
     */
    @Override
    public int getWriteTimeout () {
        return (int) this.writeTimeout.getMillis();
    }


    /**
     * @return the connTimeout in millis
     */
    @Override
    public int getConnTimeout () {
        return (int) this.connTimeout.getMillis();
    }

}
