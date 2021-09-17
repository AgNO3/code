/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.06.2015 by mbechler
 */
package eu.agno3.fileshare.mail.tpl.internal;


import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.mail.tpl.FileshareMailTemplateBuilder;
import eu.agno3.runtime.mail.tpl.MailTemplateBuilder;

import freemarker.template.TemplateException;


/**
 * @author mbechler
 *
 */
@Component ( service = FileshareMailTemplateBuilder.class )
public class FileshareMailTemplateBuilderImpl implements FileshareMailTemplateBuilder {

    private MailTemplateBuilder tplBuilder;


    @Reference
    protected synchronized void setMailTemplateBuilder ( MailTemplateBuilder mtb ) {
        this.tplBuilder = mtb;
    }


    protected synchronized void unsetMailTemplateBuilder ( MailTemplateBuilder mtb ) {
        if ( this.tplBuilder == mtb ) {
            this.tplBuilder = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.tpl.MailTemplateBuilder#makeMessage(javax.mail.internet.MimeMessage, java.lang.String,
     *      java.util.Map, boolean, java.util.Locale)
     */
    @Override
    public void makeMessage ( MimeMessage msg, String tplId, Map<String, Object> data, boolean noHtml, Locale l ) throws IOException,
            MessagingException, TemplateException {
        this.tplBuilder.makeMessage(msg, tplId, data, noHtml, l);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.tpl.MailTemplateBuilder#makeSubject(java.lang.String, java.util.Locale,
     *      java.lang.Object)
     */
    @Override
    public String makeSubject ( String tplId, Locale l, Object data ) throws TemplateException, IOException {
        return this.tplBuilder.makeSubject(tplId, l, data);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.tpl.MailTemplateBuilder#getTemplateSource(java.lang.String, java.util.Locale)
     */
    @Override
    public String getTemplateSource ( String templateFile, Locale locale ) throws IOException {
        return this.tplBuilder.getTemplateSource(templateFile, locale);
    }
}
