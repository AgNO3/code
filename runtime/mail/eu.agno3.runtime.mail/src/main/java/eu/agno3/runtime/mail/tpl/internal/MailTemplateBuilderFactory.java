/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.mail.tpl.internal;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.mail.tpl.MailTemplateBuilder;
import eu.agno3.runtime.tpl.TemplateConfigurationBuilder;

import freemarker.template.Configuration;


/**
 * @author mbechler
 *
 */
@Component ( service = MailTemplateBuilder.class, servicefactory = true )
public class MailTemplateBuilderFactory extends MailTemplateBuilderImpl {

    private TemplateConfigurationBuilder tplCfgBuilder;


    @Reference
    protected synchronized void setTemplateConfigurationBuilder ( TemplateConfigurationBuilder tcb ) {
        this.tplCfgBuilder = tcb;
    }


    protected synchronized void unsetTemplateConfigurationBuilder ( TemplateConfigurationBuilder tcb ) {
        if ( this.tplCfgBuilder == tcb ) {
            this.tplCfgBuilder = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        Configuration cfg = this.tplCfgBuilder.create(this.tplCfgBuilder.makeConfigFileLoader(ctx.getUsingBundle()));
        cfg.setCustomAttribute("language", null); //$NON-NLS-1$
        this.setTemplateConfiguration(cfg);
    }

}
