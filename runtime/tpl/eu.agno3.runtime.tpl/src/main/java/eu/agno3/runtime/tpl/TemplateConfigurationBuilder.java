/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.tpl;


import org.osgi.framework.Bundle;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;


/**
 * @author mbechler
 *
 */
public interface TemplateConfigurationBuilder {

    /**
     * @param cfg
     */
    void setup ( Configuration cfg );


    /**
     * @param cfg
     */
    void destroy ( Configuration cfg );


    /**
     * @param bundle
     * @return a template loader loading from the bundle
     */
    TemplateLoader makeBundleLoader ( Bundle bundle );


    /**
     * @param bundle
     * @return a template loader loading from config files
     */
    TemplateLoader makeConfigFileLoader ( Bundle bundle );


    /**
     * @param loaders
     * @return a template configuration using the given loaders
     */
    Configuration create ( TemplateLoader... loaders );

}
