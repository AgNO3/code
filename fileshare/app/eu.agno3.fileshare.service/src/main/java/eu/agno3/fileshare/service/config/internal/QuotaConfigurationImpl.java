/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.shiro.subject.Subject;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.fileshare.service.config.QuotaConfiguration;
import eu.agno3.runtime.util.config.BytesUtil;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = QuotaConfiguration.class, configurationPid = "quotas" )
public class QuotaConfigurationImpl implements QuotaConfiguration {

    private LinkedHashMap<String, Long> defaultQuotaRules;

    private Long globalDefaultQuota;
    private boolean trackCombinedSizesWithoutQuota;
    private Duration directoryUpdateInterval;
    private Duration quotaPersistenceInterval;


    /**
     * @param ctx
     */
    private void parseConfig ( ComponentContext ctx ) {
        this.trackCombinedSizesWithoutQuota = ConfigUtil.parseBoolean(ctx.getProperties(), "trackCombinedSizesWithoutQuota", true); //$NON-NLS-1$
        this.globalDefaultQuota = ConfigUtil.parseByteSize(ctx.getProperties(), "defaultQuota", null); //$NON-NLS-1$

        Map<String, String> defaultQuotaRulesSpec = ConfigUtil.parseStringMap(ctx.getProperties(), "defaultQuotaRules", new HashMap<>()); //$NON-NLS-1$
        this.defaultQuotaRules = new LinkedHashMap<>();

        for ( Entry<String, String> e : defaultQuotaRulesSpec.entrySet() ) {
            this.defaultQuotaRules.put(e.getKey(), BytesUtil.parseByteSizeCompat(e.getValue()));
        }

        this.directoryUpdateInterval = ConfigUtil.parseDuration(ctx.getProperties(), "directoryUpdateInterval", Duration.standardHours(1)); //$NON-NLS-1$
        this.quotaPersistenceInterval = ConfigUtil.parseDuration(ctx.getProperties(), "quotaPersistenceInterval", Duration.standardSeconds(10)); //$NON-NLS-1$
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
     * @return the default quota when creating users
     */
    @Override
    public Long getGlobalDefaultQuota () {
        return this.globalDefaultQuota;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.QuotaConfiguration#getDefaultQuotaForRoles(java.util.Set)
     */
    @Override
    public Long getDefaultQuotaForRoles ( Set<String> roles ) {
        for ( Entry<String, Long> e : this.defaultQuotaRules.entrySet() ) {
            if ( roles.contains(e.getKey()) ) {
                return e.getValue();
            }
        }
        return getGlobalDefaultQuota();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.QuotaConfiguration#getDefaultQuotaForSubject(org.apache.shiro.subject.Subject)
     */
    @Override
    public Long getDefaultQuotaForSubject ( Subject s ) {
        for ( Entry<String, Long> e : this.defaultQuotaRules.entrySet() ) {
            if ( s.hasRole(e.getKey()) ) {
                return e.getValue();
            }
        }
        return getGlobalDefaultQuota();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.QuotaConfiguration#isTrackCombinedSizesWithoutQuota()
     */
    @Override
    public boolean isTrackCombinedSizesWithoutQuota () {
        return this.trackCombinedSizesWithoutQuota;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.QuotaConfiguration#getDirectoryUpdateInterval()
     */
    @Override
    public Duration getDirectoryUpdateInterval () {
        return this.directoryUpdateInterval;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.QuotaConfiguration#getQuotaPersistenceInterval()
     */
    @Override
    public Duration getQuotaPersistenceInterval () {
        return this.quotaPersistenceInterval;
    }
}
