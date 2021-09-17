/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.configloader.jmx.internal;


import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.configloader.ConfigLoader;
import eu.agno3.runtime.configloader.jmx.ConfigLoaderMXBean;
import eu.agno3.runtime.jmx.MBean;


/**
 * @author mbechler
 *
 */
@Component ( service = MBean.class, property = {
    "objectName=eu.agno3.runtime.configloader:type=ConfigLoader"
} )
public class ConfigLoaderMXBeanImpl implements ConfigLoaderMXBean, MBean {

    private static final Logger log = Logger.getLogger(ConfigLoaderMXBean.class);

    private ConfigLoader configLoader;


    @Reference
    protected synchronized void setConfigLoader ( ConfigLoader cl ) {
        this.configLoader = cl;
    }


    protected synchronized void unsetConfigLoader ( ConfigLoader cl ) {
        if ( this.configLoader == cl ) {
            this.configLoader = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.jmx.ConfigLoaderMXBean#reloadConfig(java.lang.String)
     */
    @Override
    public boolean reloadConfig ( String spec ) {

        ConfigLoader cl = this.configLoader;
        if ( cl == null ) {
            return false;
        }

        try {
            cl.reload(spec);
            return true;
        }
        catch ( IOException e ) {
            log.error("Failed to reload configuration", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.jmx.ConfigLoaderMXBean#forceReloadConfig(java.lang.String)
     */
    @Override
    public boolean forceReloadConfig ( String spec ) {
        ConfigLoader cl = this.configLoader;
        if ( cl == null ) {
            return false;
        }

        try {
            cl.forceReload(spec);
            return true;
        }
        catch ( IOException e ) {
            log.error("Failed to reload configuration", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.jmx.ConfigLoaderMXBean#reloadConfig(java.util.Set)
     */
    @Override
    public boolean reloadConfig ( Set<String> pids ) {
        ConfigLoader cl = this.configLoader;
        if ( cl == null ) {
            return false;
        }

        try {
            cl.reload(pids);
            return true;
        }
        catch ( IOException e ) {
            log.error("Failed to reload configuration", e); //$NON-NLS-1$
            return false;
        }
    }
}
