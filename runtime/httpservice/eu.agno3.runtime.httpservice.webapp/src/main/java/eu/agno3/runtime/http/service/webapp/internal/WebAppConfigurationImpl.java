/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.02.2014 by mbechler
 */
package eu.agno3.runtime.http.service.webapp.internal;


import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.http.service.webapp.WebAppConfiguration;


/**
 * @author mbechler
 * 
 */
@Component ( service = WebAppConfiguration.class, configurationPid = WebAppConfigurationImpl.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class WebAppConfigurationImpl implements WebAppConfiguration {

    private static final Logger log = Logger.getLogger(WebAppConfigurationImpl.class);

    /**
     * 
     */
    public static final String PID = "httpservice.webapp"; //$NON-NLS-1$

    private static final Set<String> BLACKLIST_PROPERTIES = new HashSet<>();


    static {
        BLACKLIST_PROPERTIES.add(TARGET_BUNDLE_ATTR);
        BLACKLIST_PROPERTIES.add("component.name"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("component.id"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("service.id"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("service.pid"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("service.factoryPid"); //$NON-NLS-1$
    }

    private Map<String, String> properties = new HashMap<>();
    private String bundleSymbolicName;
    private String dependencies;


    @Activate
    protected void activate ( ComponentContext context ) {

        String bundle = (String) context.getProperties().get(TARGET_BUNDLE_ATTR);
        if ( bundle == null ) {
            log.warn("Target bundle not set in webapp configuration, ignore"); //$NON-NLS-1$
            return;
        }

        String depends = (String) context.getProperties().get("depends"); //$NON-NLS-1$
        if ( StringUtils.isBlank(depends) ) {
            depends = "none"; //$NON-NLS-1$
        }
        this.dependencies = depends;
        this.bundleSymbolicName = bundle;
        configure(context);
    }


    @Modified
    protected void modified ( ComponentContext context ) {
        configure(context);
    }


    /**
     * @param context
     */
    private void configure ( ComponentContext context ) {
        this.properties.clear();

        Enumeration<String> keys = context.getProperties().keys();
        if ( log.isDebugEnabled() ) {
            log.debug("Configuring webapp context for bundle " + this.bundleSymbolicName); //$NON-NLS-1$
        }

        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();
            Object value = context.getProperties().get(key);

            if ( ! ( value instanceof String ) ) {
                continue;
            }
            if ( BLACKLIST_PROPERTIES.contains(key) ) {
                continue;
            }

            if ( log.isDebugEnabled() ) {
                log.trace(String.format("Setting property %s = %s", key, value)); //$NON-NLS-1$
            }

            this.properties.put(key, (String) value);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.webapp.WebAppConfiguration#getConnector()
     */
    @Override
    public String getConnector () {
        String vhosts = getProperties().get("virtualHosts"); //$NON-NLS-1$

        int sepPos = vhosts.indexOf(',');
        if ( sepPos >= 0 ) {
            vhosts = vhosts.substring(sepPos + 1);
        }

        vhosts = vhosts.trim();
        if ( vhosts.charAt(0) != '@' ) {
            return null;
        }
        return vhosts.substring(1);
    }


    /**
     * @return the bundleSymbolicName
     */
    @Override
    public String getBundleSymbolicName () {
        return this.bundleSymbolicName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.webapp.WebAppConfiguration#getDependencies()
     */
    @Override
    public String getDependencies () {
        return this.dependencies;
    }


    /**
     * @return the properties
     */
    @Override
    public Map<String, String> getProperties () {
        return Collections.unmodifiableMap(this.properties);
    }
}
