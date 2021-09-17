/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.tpl.internal;


import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;

import eu.agno3.runtime.configloader.file.ConfigFileLoader;

import freemarker.cache.URLTemplateLoader;


/**
 * @author mbechler
 *
 */
public class ConfigTemplateLoader extends URLTemplateLoader {

    private static final Logger log = Logger.getLogger(ConfigTemplateLoader.class);

    private ConfigFileLoader cfLoader;


    /**
     * @param cfLoader
     */
    public ConfigTemplateLoader ( ConfigFileLoader cfLoader ) {
        this.cfLoader = cfLoader;
    }


    /**
     * {@inheritDoc}
     *
     * @see freemarker.cache.URLTemplateLoader#getURL(java.lang.String)
     */
    @Override
    protected URL getURL ( String path ) {
        try {
            return this.cfLoader.getURL(path);
        }
        catch ( IOException e ) {
            log.warn("Failed to load config template", e); //$NON-NLS-1$
            return null;
        }
    }

}
