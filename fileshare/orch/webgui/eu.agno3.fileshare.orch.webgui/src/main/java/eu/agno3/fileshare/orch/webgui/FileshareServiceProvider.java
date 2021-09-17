/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.fileshare.mail.tpl.FileshareMailTemplateBuilder;
import eu.agno3.runtime.tpl.TemplateConfigurationBuilder;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FileshareServiceProvider {

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private FileshareMailTemplateBuilder mailTemplateBuilder;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    TemplateConfigurationBuilder templateConfigBuilder;


    /**
     * @return the mailTemplateBuilder
     */
    public FileshareMailTemplateBuilder getMailTemplateBuilder () {
        return this.mailTemplateBuilder;
    }


    /**
     * @return the templateConfig
     */
    public TemplateConfigurationBuilder getTemplateConfig () {
        return this.templateConfigBuilder;
    }

}
