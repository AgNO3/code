/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.09.2016 by mbechler
 */
package eu.agno3.runtime.eventlog.elasticsearch.internal;


import java.util.Dictionary;
import java.util.Locale;
import java.util.Set;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.eventlog.IndexType;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = ElasticsearchLoggerConfig.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "event.elastic" )
public class ElasticsearchLoggerConfig {

    private String logName;

    private int retainOpenDays;
    private int retainDays;

    private boolean ignorePostdated;
    private IndexType indexType;

    private Set<String> excludeStreams;
    private Set<String> includeStreams;


    /**
     * @return the logName
     */
    public String getIndexName () {
        return this.logName;
    }


    /**
     * @return the retainIndexedDays
     */
    public int getRetainDays () {
        return this.retainDays;
    }


    /**
     * @return the retainOpenIndexedDays
     */
    public int getRetainOpenDays () {
        return this.retainOpenDays;
    }


    /**
     * @return the ignorePostdated
     */
    public boolean isIgnorePostdated () {
        return this.ignorePostdated;
    }


    /**
     * @return the indexType
     */
    public IndexType getIndexType () {
        return this.indexType;
    }


    /**
     * @return the excludeStreams
     */
    public Set<String> getExcludeStreams () {
        return this.excludeStreams;
    }


    /**
     * @return the includeStreams
     */
    public Set<String> getIncludeStreams () {
        return this.includeStreams;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @param cfg
     */
    void parseConfig ( Dictionary<String, Object> cfg ) {
        this.logName = ConfigUtil.parseString(
            cfg,
            "logName", //$NON-NLS-1$
            "eventlog").toLowerCase(Locale.ROOT); //$NON-NLS-1$

        this.includeStreams = ConfigUtil.parseStringSet(cfg, "includeStreams", null); //$NON-NLS-1$
        this.excludeStreams = ConfigUtil.parseStringSet(cfg, "excludeStreams", null); //$NON-NLS-1$

        this.retainDays = ConfigUtil.parseInt(cfg, "retainDays", 30); //$NON-NLS-1$
        this.retainOpenDays = ConfigUtil.parseInt(cfg, "retainOpenDays", this.retainDays); //$NON-NLS-1$
        this.ignorePostdated = ConfigUtil.parseBoolean(cfg, "ignorePostdated", true); //$NON-NLS-1$

        this.indexType = IndexType.valueOf(ConfigUtil.parseString(
            cfg,
            "indexType", //$NON-NLS-1$
            "MONTHLY")); //$NON-NLS-1$
    }
}
