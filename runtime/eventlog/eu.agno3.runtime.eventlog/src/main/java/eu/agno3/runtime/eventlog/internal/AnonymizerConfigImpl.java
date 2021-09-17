/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 9, 2016 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.eventlog.AnonymizerConfig;
import eu.agno3.runtime.eventlog.AnonymizerRule;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */

@Component ( service = AnonymizerConfig.class, configurationPid = "event.anonymize", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class AnonymizerConfigImpl implements AnonymizerConfig {

    /**
     * 
     */
    private static final String FIELD_PREFIX = "field."; //$NON-NLS-1$
    private static final String OPT_PREFIX = "opts."; //$NON-NLS-1$
    private String matchStream;
    private Map<String, AnonymizerRule> rules = new HashMap<>();


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.matchStream = ConfigUtil.parseString(ctx.getProperties(), "matchStream", null); //$NON-NLS-1$
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
        Enumeration<String> keys = cfg.keys();
        Map<String, AnonymizerRule> newRules = new HashMap<>();
        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();
            if ( !key.startsWith(FIELD_PREFIX) || ! ( cfg.get(key) instanceof String ) ) {
                continue;
            }
            String field = key.substring(FIELD_PREFIX.length());
            String type = (String) cfg.get(key);
            Map<String, String> opts = ConfigUtil.parseStringMap(cfg, OPT_PREFIX + field, Collections.EMPTY_MAP);
            newRules.put(field, new AnonymizerRuleImpl(field, type, opts));
        }
        this.rules = newRules;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AnonymizerConfig#getMatchStream()
     */
    @Override
    public String getMatchStream () {
        return this.matchStream;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AnonymizerConfig#getRules()
     */
    @Override
    public Map<String, AnonymizerRule> getRules () {
        return this.rules;
    }
}
