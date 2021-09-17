/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.runtime.tpl.internal;


import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.tpl.TemplateConfigurationBuilder;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;


/**
 * @author mbechler
 * 
 */
@Component ( service = Configuration.class, servicefactory = true )
public class ConfigurationFactory extends Configuration {

    private TemplateConfigurationBuilder builder;


    /**
     * 
     */
    public ConfigurationFactory () {
        super(Configuration.VERSION_2_3_21);
    }


    @Reference
    protected synchronized void setConfigurationBuilder ( TemplateConfigurationBuilder b ) {
        this.builder = b;
    }


    protected synchronized void unsetConfigurationBuilder ( TemplateConfigurationBuilder b ) {
        if ( this.builder == b ) {
            this.builder = null;
        }
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        this.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[] {
            this.builder.makeBundleLoader(context.getUsingBundle()),
            this.builder.makeBundleLoader(FrameworkUtil.getBundle(ConfigurationFactory.class))
        }));
        this.builder.setup(this);
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.builder.destroy(this);
    }

}
