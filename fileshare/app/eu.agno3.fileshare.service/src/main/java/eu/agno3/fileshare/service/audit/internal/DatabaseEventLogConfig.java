/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 19, 2017 by mbechler
 */
package eu.agno3.fileshare.service.audit.internal;


import java.util.Dictionary;
import java.util.Set;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = DatabaseEventLogConfig.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "audit.db" )
public class DatabaseEventLogConfig {

    private Set<String> excludeStreams;
    private Set<String> includeStreams;
    private int retainDays;
    private boolean ignorePostdated;


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
        this.includeStreams = ConfigUtil.parseStringSet(cfg, "includeStreams", null); //$NON-NLS-1$
        this.excludeStreams = ConfigUtil.parseStringSet(cfg, "excludeStreams", null); //$NON-NLS-1$
        this.retainDays = ConfigUtil.parseInt(cfg, "retainDays", 30); //$NON-NLS-1$
        this.ignorePostdated = ConfigUtil.parseBoolean(cfg, "ignorePostdated", true); //$NON-NLS-1$
    }


    /**
     * @return the ignorePostdated
     */
    public boolean isIgnorePostdated () {
        return this.ignorePostdated;
    }


    /**
     * @return the retainDays
     */
    public int getRetainDays () {
        return this.retainDays;
    }


    /**
     * @return excluded log streams
     */
    public Set<String> getExcludeStreams () {
        return this.excludeStreams;
    }


    /**
     * @return included log streams
     */
    public Set<String> getIncludeStreams () {
        return this.includeStreams;
    }

}
