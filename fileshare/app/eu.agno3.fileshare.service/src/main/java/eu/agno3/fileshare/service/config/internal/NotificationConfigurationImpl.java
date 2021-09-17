/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.net.IDN;
import java.util.Dictionary;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.fileshare.service.config.NotificationConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = NotificationConfiguration.class, configurationPid = "notify" )
public class NotificationConfigurationImpl implements NotificationConfiguration {

    private static final Logger log = Logger.getLogger(NotificationConfigurationImpl.class);

    private boolean disabled;
    private boolean alwaysSendText;
    private boolean sendNotificationsAsUser;
    private Locale defaultLocale;
    private String defaultSenderName;
    private String defaultSenderAddress;
    private String footer;
    private Set<String> asUserNotificationDomains;

    private String adminContact;

    private boolean hideSensitiveInformation;

    private Duration expirationNotificationPeriod;


    /**
     * @param ctx
     */
    private void parseConfig ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        this.disabled = ConfigUtil.parseBoolean(cfg, "disabled", true); //$NON-NLS-1$
        this.alwaysSendText = ConfigUtil.parseBoolean(cfg, "alwaysSendText", false); //$NON-NLS-1$
        this.sendNotificationsAsUser = ConfigUtil.parseBoolean(cfg, "sendAsUsers", false); //$NON-NLS-1$
        this.asUserNotificationDomains = ConfigUtil.parseStringSet(cfg, "asUserNotificationDomains", null); //$NON-NLS-1$

        String defaultLocaleName = ConfigUtil.parseString(cfg, "defaultLocale", null); //$NON-NLS-1$

        if ( defaultLocaleName != null ) {
            this.defaultLocale = Locale.forLanguageTag(defaultLocaleName);
        }
        else {
            this.defaultLocale = Locale.getDefault();
        }
        this.defaultSenderName = ConfigUtil.parseString(
            cfg,
            "defaultSenderName", //$NON-NLS-1$
            "AgNO3 FileShield"); //$NON-NLS-1$

        String guessAddr = LocalHostUtil.guessPrimaryHostName();
        String defAddress;
        if ( Character.isDigit(guessAddr.charAt(0)) ) {
            defAddress = "fileshield@[" + guessAddr + ']'; //$NON-NLS-1$
        }
        else {
            defAddress = "fileshield@" + guessAddr; //$NON-NLS-1$
        }
        this.defaultSenderAddress = ConfigUtil.parseString(
            cfg,
            "defaultSenderAddress", //$NON-NLS-1$
            defAddress); // $NON-NLS-1$

        this.footer = ConfigUtil.parseString(cfg, "mailFooterText", null); //$NON-NLS-1$

        this.adminContact = ConfigUtil.parseString(cfg, "adminContact", null); //$NON-NLS-1$
        this.hideSensitiveInformation = ConfigUtil.parseBoolean(cfg, "hideSensitiveInformation", false); //$NON-NLS-1$
        this.expirationNotificationPeriod = ConfigUtil.parseDuration(cfg, "expirationNotificationPeriod", Duration.standardDays(2 * 7)); //$NON-NLS-1$
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx);

    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#isMailingDisabled()
     */
    @Override
    public boolean isMailingDisabled () {
        return this.disabled;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#isAlwaysSendText()
     */
    @Override
    public boolean isAlwaysSendText () {
        return this.alwaysSendText;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#isSendNotificationsAsUser(java.lang.String)
     */
    @Override
    public boolean isSendNotificationsAsUser ( String address ) {
        if ( !this.sendNotificationsAsUser ) {
            return false;
        }

        if ( this.asUserNotificationDomains == null ) {
            return true;
        }
        try {
            int lastIndexOf = address.lastIndexOf('@');
            if ( lastIndexOf < 0 ) {
                return false;
            }
            String domain = IDN.toASCII(address.substring(lastIndexOf + 1));
            return this.asUserNotificationDomains.contains(domain);
        }
        catch ( IllegalArgumentException e ) {
            log.warn("Failed to parse domain name", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#getDefaultLocale()
     */
    @Override
    public Locale getDefaultLocale () {
        return this.defaultLocale;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#getDefaultSenderAddress()
     */
    @Override
    public String getDefaultSenderAddress () {
        return this.defaultSenderAddress;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#getDefaultSenderName()
     */
    @Override
    public String getDefaultSenderName () {
        return this.defaultSenderName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#getFooter()
     */
    @Override
    public String getFooter () {
        return this.footer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#getAdminContact()
     */
    @Override
    public String getAdminContact () {
        return this.adminContact;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#isHideSensitiveInformation()
     */
    @Override
    public boolean isHideSensitiveInformation () {
        return this.hideSensitiveInformation;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.NotificationConfiguration#getExpirationNotificationPeriod()
     */
    @Override
    public Duration getExpirationNotificationPeriod () {
        return this.expirationNotificationPeriod;
    }

}
