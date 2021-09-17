/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config.internal;


import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import eu.agno3.runtime.logging.config.AbstractPropertiesConfigurationSource;
import eu.agno3.runtime.logging.config.LoggerConfigurationException;


/**
 * Configuration source from a properties file.
 * 
 * @author mbechler
 * 
 */
public class FileConfigurationSource extends AbstractPropertiesConfigurationSource {

    File file;


    /**
     * @param f
     *            The properties file to use for configuration
     * @param prio
     *            The priority of this source
     */
    public FileConfigurationSource ( File f, int prio ) {
        super(prio);
        this.file = f;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof FileConfigurationSource ) {
            FileConfigurationSource other = (FileConfigurationSource) obj;
            return this.file.equals(other.file);
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
        return this.file.hashCode();
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
        try ( FileInputStream in = new FileInputStream(this.file) ) {
            Properties props = new Properties();
            props.load(in);
            return super.getConfig(props);
        }
        catch ( Exception e ) {
            throw new LoggerConfigurationException(String.format("Failed to read properties file '%s'", this.file.getAbsolutePath()), e); //$NON-NLS-1$
        }
    }
}
