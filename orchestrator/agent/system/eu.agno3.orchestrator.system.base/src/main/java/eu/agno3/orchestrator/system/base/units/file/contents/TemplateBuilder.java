/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.12.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.contents;


import java.io.IOException;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.runtime.tpl.TemplateConfigurationBuilder;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;


/**
 * @author mbechler
 *
 */
@SystemServiceType ( TemplateBuilder.class )
public class TemplateBuilder implements SystemService {

    private TemplateConfigurationBuilder tplConfigBuilder;


    /**
     * @param tplConfigBuilder
     * 
     */
    public TemplateBuilder ( TemplateConfigurationBuilder tplConfigBuilder ) {
        this.tplConfigBuilder = tplConfigBuilder;
    }


    /**
     * @param callerClass
     * @param tplName
     * @return the template
     * @throws IOException
     */
    public Template buildTemplate ( Class<?> callerClass, String tplName ) throws IOException {
        Bundle bundle = FrameworkUtil.getBundle(callerClass);
        if ( bundle == null ) {
            throw new IOException("Failed to get caller bundle"); //$NON-NLS-1$
        }
        TemplateLoader baseLoader = this.tplConfigBuilder.makeBundleLoader(FrameworkUtil.getBundle(TemplateConfigurationBuilder.class));
        TemplateLoader bundleLoader = this.tplConfigBuilder.makeBundleLoader(bundle);
        Configuration config = this.tplConfigBuilder.create(baseLoader, bundleLoader);
        return config.getTemplate(tplName);
    }
}