/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config.internal;


import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

import eu.agno3.runtime.util.osgi.ResourceUtil;
import eu.agno3.runtime.logging.config.AbstractPropertiesConfigurationSource;
import eu.agno3.runtime.logging.config.LoggerConfigurationException;


/**
 * Configuration source from a properties file.
 * 
 * @author mbechler
 * 
 */
public class BundleConfigurationSource extends AbstractPropertiesConfigurationSource {

    private static final Logger log = Logger.getLogger(BundleConfigurationSource.class);

    private static final String BUNDLE_ROOT = "/"; //$NON-NLS-1$

    private URL url;


    /**
     * @param bnd
     *            The bundle which contains the config .properties file
     * @param path
     *            The path to the .properties file The properties file to use for configuration
     * @param prio
     *            The priority of this source
     * @throws LoggerConfigurationException
     */
    public BundleConfigurationSource ( Bundle bnd, String path, int prio ) throws LoggerConfigurationException {
        super(prio);
        this.url = ResourceUtil.safeFindEntry(bnd, BUNDLE_ROOT, path);

        if ( this.url == null ) {
            throw new LoggerConfigurationException(String.format("Failed to lookup properties file '%s' in bundle '%s'", path, bnd.getSymbolicName())); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof BundleConfigurationSource ) {
            BundleConfigurationSource other = (BundleConfigurationSource) obj;
            return this.url.equals(other.url);
        }
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.url.hashCode();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws LoggerConfigurationException
     * 
     * @see eu.agno3.runtime.logging.config.AbstractLoggerConfigurationSource#getConfig()
     */
    @Override
    public Map<String, ?> getConfig () throws LoggerConfigurationException {
        try ( InputStream in = this.url.openStream() ) {
            Properties props = new Properties();
            props.load(in);
            return super.getConfig(props);
        }
        catch ( Exception e ) {
            log.error("Failed to read properties file " + this.url, e); //$NON-NLS-1$
            throw new LoggerConfigurationException(String.format("Failed to read properties file '%s'", this.url.toString()), e); //$NON-NLS-1$
        }
    }
}
