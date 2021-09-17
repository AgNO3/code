/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.mail.tpl;


import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import freemarker.template.TemplateException;


/**
 * @author mbechler
 *
 */
public interface MailTemplateBuilder {

    /**
     * 
     * @param templateFile
     * @param locale
     * @param tplId
     * @return the template source
     * @throws IOException
     */
    String getTemplateSource ( String templateFile, Locale locale ) throws IOException;


    /**
     * @param msg
     * @param tplId
     * @param data
     * @param noHtml
     * @param l
     * @param encoding
     * @throws IOException
     * @throws MessagingException
     * @throws TemplateException
     */
    void makeMessage ( MimeMessage msg, String tplId, Map<String, Object> data, boolean noHtml, Locale l ) throws IOException, MessagingException,
            TemplateException;


    /**
     * @param tplId
     * @param l
     * @param data
     * @return the generated subject
     * @throws TemplateException
     * @throws IOException
     */
    String makeSubject ( String tplId, Locale l, Object data ) throws TemplateException, IOException;

}
