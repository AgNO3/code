/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 18, 2016 by mbechler
 */
package eu.agno3.runtime.security.web.filter;


import java.util.Dictionary;

import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.security.web.SecurityHeadersFilterConfig;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = SecurityHeadersFilterConfig.class, configurationPid = "secheaders" )
public class SecurityHeadersFilterConfigImpl implements SecurityHeadersFilterConfig {

    /**
     * Default CSP
     * 
     * unsafe-inline, unsafe-eval required for JSF/Primefaces :(
     * 
     * websockets need an explicit base, as the protocols do not match :|
     */
    public static final String DEFAULT_CSP = "default-src 'self';" //$NON-NLS-1$
            + " img-src 'self' data: blob: ;" //$NON-NLS-1$
            + " script-src 'self' 'unsafe-inline' 'unsafe-eval';" //$NON-NLS-1$
            + " style-src 'self' 'unsafe-inline';" //$NON-NLS-1$
            + " font-src 'self' data: ;" //$NON-NLS-1$
            + " frame-src 'self' https:{base};" //$NON-NLS-1$
            + " connect-src 'self' wss:{base};" //$NON-NLS-1$
            + " strict-mixed-content-checking;" //$NON-NLS-1$
            + " referrer origin-when-cross-origin;" //$NON-NLS-1$
            + " frame-ancestors 'self';"; //$NON-NLS-1$

    private boolean denyIndex = true;
    private String defaultCspHeader = DEFAULT_CSP;

    private boolean hstsEnabled;
    private long hstsMaxAge;
    private boolean hstsIncludeSubdomains;
    private boolean hstsPreload;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @param properties
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.defaultCspHeader = ConfigUtil.parseString(cfg, "defaultCspHeader", DEFAULT_CSP); //$NON-NLS-1$
        this.denyIndex = ConfigUtil.parseBoolean(cfg, "denyIndex", true); //$NON-NLS-1$
        this.hstsEnabled = ConfigUtil.parseBoolean(cfg, "hstsEnabled", true); //$NON-NLS-1$
        this.hstsMaxAge = ConfigUtil.parseDuration(cfg, "hstsMaxAge", Duration.standardDays(180)).getStandardSeconds(); //$NON-NLS-1$
        this.hstsIncludeSubdomains = ConfigUtil.parseBoolean(cfg, "hstsIncludeSubdomains", false); //$NON-NLS-1$
        this.hstsPreload = ConfigUtil.parseBoolean(cfg, "hstsPreload", false); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.SecurityHeadersFilterConfig#isDenyIndex()
     */
    @Override
    public boolean isDenyIndex () {
        return this.denyIndex;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.SecurityHeadersFilterConfig#getDefaultCSPHeader()
     */
    @Override
    public String getDefaultCSPHeader () {
        return this.defaultCspHeader;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.SecurityHeadersFilterConfig#getHSTSMaxAge()
     */
    @Override
    public long getHSTSMaxAge () {
        return this.hstsMaxAge;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.SecurityHeadersFilterConfig#isHSTSEnabled()
     */
    @Override
    public boolean isHSTSEnabled () {
        return this.hstsEnabled;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.SecurityHeadersFilterConfig#isHSTSIncludeSubdomains()
     */
    @Override
    public boolean isHSTSIncludeSubdomains () {
        return this.hstsIncludeSubdomains;
    }


    /**
     * @return the hstsPreload
     */
    @Override
    public boolean isHSTSPreload () {
        return this.hstsPreload;
    }

}
