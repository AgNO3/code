/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.service.config.FrontendConfiguration;
import eu.agno3.runtime.http.service.HttpServiceInfo;
import eu.agno3.runtime.http.service.webapp.WebAppConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = FrontendConfiguration.class, configurationPid = "frontend" )
public class FrontendConfigurationImpl implements FrontendConfiguration {

    private static final Logger log = Logger.getLogger(FrontendConfigurationImpl.class);

    private URI webFrontendURI;
    private boolean webFrontendURIReliable;

    private Duration intentTimeout;

    private boolean allowUserModificationTimes;

    private Duration sessionIncompleteExpireDuration;
    private Duration userIncompleteExpireDuration;

    private Long perUserIncompleteSizeLimit;
    private Long perSessionIncompleteSizeLimit;

    private HttpServiceInfo httpServiceInfo;
    private WebAppConfiguration webAppConfig;

    private DateTimeZone defaultTimezone;

    private boolean enableCompression;
    private boolean auditEnabled;


    /**
     * @param ctx
     */
    private void parseConfig ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        String overrideUri = (String) cfg.get("overrideWebBaseUri"); //$NON-NLS-1$

        try {
            if ( !StringUtils.isBlank(overrideUri) ) {
                // make sure this always contains a slash
                if ( overrideUri.charAt(overrideUri.length() - 1) != '/' ) {
                    overrideUri += "/"; //$NON-NLS-1$
                }
                this.webFrontendURI = new URI(overrideUri);
                this.webFrontendURIReliable = true;
            }
            else {
                String contextPath = "/"; //$NON-NLS-1$
                String configured = this.webAppConfig.getProperties().get(WebAppConfiguration.CONTEXT_PATH_ATTR);
                if ( configured != null ) {
                    contextPath = configured.trim();
                }
                this.webFrontendURI = this.httpServiceInfo.getContextBaseUrl(this.webAppConfig.getConnector(), contextPath, null).toURI();
            }
        }
        catch (
            URISyntaxException |
            ServletException e ) {
            log.error("Failed to determine server canonical URL", e); //$NON-NLS-1$
        }

        if ( log.isInfoEnabled() ) {
            log.info("Frontend URI is " + this.webFrontendURI); //$NON-NLS-1$
        }

        this.intentTimeout = ConfigUtil.parseDuration(cfg, "intentTimeout", Duration.standardHours(6)); //$NON-NLS-1$

        this.allowUserModificationTimes = ConfigUtil.parseBoolean(cfg, "allowUserModificationTimes", false); //$NON-NLS-1$

        this.sessionIncompleteExpireDuration = ConfigUtil.parseDuration(cfg, "sessionIncompleteExpireDuration", Duration.standardMinutes(30)); //$NON-NLS-1$
        this.userIncompleteExpireDuration = ConfigUtil.parseDuration(cfg, "userIncompleteExpireDuration", Duration.standardHours(4)); //$NON-NLS-1$

        this.perUserIncompleteSizeLimit = ConfigUtil.parseByteSize(cfg, "perUserIncompleteSizeLimit", null); //$NON-NLS-1$
        this.perSessionIncompleteSizeLimit = ConfigUtil.parseByteSize(cfg, "perSessionIncompleteSizeLimit", this.perUserIncompleteSizeLimit); //$NON-NLS-1$

        String timezoneId = ConfigUtil.parseString(cfg, "defaultTimeZone", null); //$NON-NLS-1$
        if ( !StringUtils.isBlank(timezoneId) ) {
            try {
                this.defaultTimezone = DateTimeZone.forID(timezoneId);
            }
            catch ( IllegalArgumentException e ) {
                log.error("Invalid timzone " + timezoneId, e); //$NON-NLS-1$
                this.defaultTimezone = DateTimeZone.getDefault();
            }
        }
        else {
            this.defaultTimezone = DateTimeZone.getDefault();
        }

        this.enableCompression = ConfigUtil.parseBoolean(cfg, "enableCompression", false); //$NON-NLS-1$
        this.auditEnabled = ConfigUtil.parseBoolean(cfg, "auditEnabled", true); //$NON-NLS-1$
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    @Reference
    protected synchronized void setHttpServiceInfo ( HttpServiceInfo hsi ) {
        this.httpServiceInfo = hsi;
    }


    protected synchronized void unsetHttpServiceInfo ( HttpServiceInfo hsi ) {
        if ( this.httpServiceInfo == hsi ) {
            this.httpServiceInfo = null;
        }
    }


    @Reference
    protected synchronized void setWebAppConfig ( WebAppConfiguration wac ) {
        this.webAppConfig = wac;
    }


    protected synchronized void unsetWebAppConfig ( WebAppConfiguration wac ) {
        if ( this.webAppConfig == wac ) {
            this.webAppConfig = null;
        }
    }


    @Override
    public URI getWebFrontendURI () {
        return this.webFrontendURI;
    }


    @Override
    public boolean isWebFrontendURIReliable () {
        return this.webFrontendURIReliable;
    }


    @Override
    public boolean isEnableCompression () {
        return this.enableCompression;
    }


    @Override
    public Duration getIntentTimeout () {
        return this.intentTimeout;
    }


    @Override
    public boolean isAllowUserModificationTimes () {
        return this.allowUserModificationTimes;
    }


    @Override
    public Duration getTokenIncompleteExpireDuration () {
        return this.sessionIncompleteExpireDuration;
    }


    @Override
    public Duration getUserIncompleteExpireDuration () {
        return this.userIncompleteExpireDuration;
    }


    @Override
    public Long getPerSessionIncompleteSizeLimit () {
        return this.perSessionIncompleteSizeLimit;
    }


    @Override
    public Long getPerUserIncompleteSizeLimit () {
        return this.perUserIncompleteSizeLimit;
    }


    @Override
    public DateTimeZone getDefaultTimeZone () {
        return this.defaultTimezone;
    }


    @Override
    public boolean isAuditEnabled () {
        return this.auditEnabled;
    }
}
