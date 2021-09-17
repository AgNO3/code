/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2014 by mbechler
 */
package eu.agno3.runtime.logging.bridge.internal;


import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.logging.config.AbstractLoggerConfigurationSource;
import eu.agno3.runtime.logging.config.LoggerConfigurationException;
import eu.agno3.runtime.logging.config.PrioritizedLoggerConfigurationSource;


/**
 * @author mbechler
 *
 */
@Component (
    service = PrioritizedLoggerConfigurationSource.class,
    configurationPid = ConfigConfigurationSource.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ConfigConfigurationSource extends AbstractLoggerConfigurationSource {

    private static final Logger log = Logger.getLogger(ConfigConfigurationSource.class);
    Map<String, ?> config = new HashMap<>();

    /**
     * 
     */
    public static final String PID = "log"; //$NON-NLS-1$
    private static final Set<String> FILTER_PROPERTIES = new HashSet<>();
    private static final String PREFIX = "log4j.logger."; //$NON-NLS-1$

    static {
        FILTER_PROPERTIES.add(Constants.SERVICE_ID);
        FILTER_PROPERTIES.add(Constants.SERVICE_PID);
        FILTER_PROPERTIES.add(Constants.SERVICE_BUNDLEID);
        FILTER_PROPERTIES.add(Constants.SERVICE_SCOPE);
        FILTER_PROPERTIES.add(Constants.OBJECTCLASS);
        FILTER_PROPERTIES.add(ComponentConstants.COMPONENT_ID);
        FILTER_PROPERTIES.add(ComponentConstants.COMPONENT_NAME);
    }


    /**
     * 
     */
    public ConfigConfigurationSource () {
        super(0);
    }


    @Activate
    protected synchronized void activate ( ComponentContext context ) {

        Dictionary<String, Object> props = context.getProperties();

        if ( props == null ) {
            log.debug("No logger configuration"); //$NON-NLS-1$
            this.config = new HashMap<>();
            return;
        }

        log.debug("Setting logger configuration"); //$NON-NLS-1$
        this.config = makeConfig(props);
    }


    @Modified
    protected synchronized void modified ( ComponentContext context ) {

        Dictionary<String, Object> props = context.getProperties();

        if ( props == null ) {
            log.debug("No logger configuration"); //$NON-NLS-1$
            this.config = new HashMap<>();

        }
        else {
            log.debug("Updating logger configuration"); //$NON-NLS-1$
            this.config = makeConfig(props);
        }

        this.setChanged();
        this.notifyObservers();
    }


    /**
     * @param props
     * @return
     */
    protected Map<String, Object> makeConfig ( Dictionary<String, Object> props ) {
        final Map<String, Object> newConfig = new HashMap<>();

        Enumeration<String> keys = props.keys();

        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();

            if ( FILTER_PROPERTIES.contains(key) ) {
                continue;
            }

            Object val = props.get(key);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Setting %s = %s", key, val)); //$NON-NLS-1$
            }
            newConfig.put(PREFIX + key, val);
        }
        return newConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.logging.config.AbstractLoggerConfigurationSource#getConfig()
     */
    @Override
    public Map<String, ?> getConfig () throws LoggerConfigurationException {
        return this.config;
    }

}
