/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.mail.tpl.internal;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.mail.tpl.MailTemplateBuilder;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * @author mbechler
 *
 */
public class MailTemplateBuilderImpl implements MailTemplateBuilder {

    private static final Logger log = Logger.getLogger(MailTemplateBuilderImpl.class);
    private Configuration tplConfig;


    /**
     * 
     */
    protected MailTemplateBuilderImpl () {
        super();
    }


    /**
     * @param tplConfig
     */
    public MailTemplateBuilderImpl ( Configuration tplConfig ) {
        this.tplConfig = tplConfig;
    }


    /**
     * @param create
     */
    protected void setTemplateConfiguration ( Configuration cfg ) {
        this.tplConfig = cfg;
    }


    @Override
    public void makeMessage ( MimeMessage msg, String tplId, Map<String, Object> data, boolean preferNoHtml, Locale l ) throws IOException,
            MessagingException, TemplateException {
        makeSubject(msg, tplId, l, data);
        Template plainTemplate = getPlainTextTemplate(tplId, l);
        Template htmlTemplate = getHtmlTextTemplate(tplId, l);

        boolean haveAttachments = false;

        MimeMultipart mimeMultipart = new MimeMultipart();
        if ( !preferNoHtml && plainTemplate != null && htmlTemplate != null ) {
            mimeMultipart.setSubType("alternative"); //$NON-NLS-1$
            makeAlternativesMessage(msg, mimeMultipart, data, plainTemplate, htmlTemplate, l);
            return;
        }
        else if ( !haveAttachments && plainTemplate != null ) {
            makePlainTextBody(plainTemplate, data, l, msg);
            return;
        }
        else if ( plainTemplate != null ) {
            makePlainTextPart(plainTemplate, data, l, mimeMultipart);
        }
        else if ( !haveAttachments && htmlTemplate != null ) {
            makeHTMLBody(htmlTemplate, data, l, msg);
            return;
        }
        else if ( htmlTemplate != null ) {
            makeHTMLPart(htmlTemplate, data, l, mimeMultipart);
        }
        else {
            throw new IOException("No template found for id " + tplId); //$NON-NLS-1$
        }

        msg.setContent(mimeMultipart);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.tpl.MailTemplateBuilder#getTemplateSource(java.lang.String, java.util.Locale)
     */
    @SuppressWarnings ( "deprecation" )
    @Override
    public String getTemplateSource ( String tplId, Locale l ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Trying to load %s in locale %s", tplId, l != null ? l.toLanguageTag() : null)); //$NON-NLS-1$
        }
        Template tpl = this.tplConfig.getTemplate(String.format("mail/%s.ftl", tplId), l); //$NON-NLS-1$
        return tpl.getRootTreeNode().toString();
    }


    /**
     * @param msg
     * @param tplId
     * @param l
     * @param data
     * @throws IOException
     * @throws TemplateException
     * @throws MessagingException
     */
    private void makeSubject ( MimeMessage msg, String tplId, Locale l, Object data ) throws IOException, TemplateException, MessagingException {
        Template subjectTemplate = getSubjectTemplate(tplId, l);
        String subject = makeSubjectString(data, subjectTemplate);
        String encoding = subjectTemplate.getEncoding();
        msg.setSubject(subject, encoding);
    }


    /**
     * 
     * @param tplId
     * @param l
     * @param data
     * @return the generated subject
     * @throws TemplateException
     * @throws IOException
     */
    @Override
    public String makeSubject ( String tplId, Locale l, Object data ) throws TemplateException, IOException {
        Template subjectTemplate = getSubjectTemplate(tplId, l);
        return makeSubjectString(data, subjectTemplate);
    }


    /**
     * @param data
     * @param subjectTemplate
     * @return
     * @throws TemplateException
     * @throws IOException
     */
    private static String makeSubjectString ( Object data, Template subjectTemplate ) throws TemplateException, IOException {
        StringWriter sw = new StringWriter();
        subjectTemplate.process(data, sw);
        String subject = sw.toString();
        // Javamail: "The application must ensure that the subject does not contain any line breaks. "
        subject = StringUtils.replaceChars(subject, "\r\n", StringUtils.EMPTY); //$NON-NLS-1$
        return subject;
    }


    /**
     * @param msg
     * @param data2
     * @param plainTemplate
     * @param htmlTemplate
     * @param l
     * @param encoding
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    private static void makeAlternativesMessage ( MimeMessage msg, MimeMultipart multiPart, Object data, Template plainTemplate,
            Template htmlTemplate, Locale l ) throws MessagingException, TemplateException, IOException {
        makePlainTextPart(plainTemplate, data, l, multiPart);
        makeHTMLPart(htmlTemplate, data, l, multiPart);
        msg.setContent(multiPart);
    }


    /**
     * @param plainTemplate
     * @param l
     * @param encoding
     * @param multiPart
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    private static void makePlainTextPart ( Template plainTemplate, Object data, Locale l, Multipart multiPart ) throws MessagingException,
            TemplateException, IOException {
        MimeBodyPart textPart = new MimeBodyPart();
        String[] language = makeLanguage(plainTemplate);
        if ( language != null ) {
            textPart.setContentLanguage(language);
        }
        textPart.setText(renderPlainText(plainTemplate, data), plainTemplate.getEncoding());
        multiPart.addBodyPart(textPart);
    }


    /**
     * @param plainTemplate
     * @param data
     * @param l
     * @param msg
     * @throws IOException
     * @throws TemplateException
     * @throws MessagingException
     */
    private static void makePlainTextBody ( Template plainTemplate, Map<String, Object> data, Locale l, MimeMessage msg ) throws MessagingException,
            TemplateException, IOException {
        String[] language = makeLanguage(plainTemplate);
        if ( language != null ) {
            msg.setContentLanguage(language);
        }
        msg.setText(renderPlainText(plainTemplate, data), plainTemplate.getEncoding());
    }


    /**
     * @param plainTemplate
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    private static String renderPlainText ( Template plainTemplate, Object data ) throws TemplateException, IOException {
        StringWriter sw = new StringWriter();
        plainTemplate.process(data, sw);
        return sw.toString();
    }


    /**
     * @param htmlTemplate
     * @param l
     * @param encoding
     * @param multiPart
     * @param textPart
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    private static void makeHTMLPart ( Template htmlTemplate, Object data, Locale l, Multipart multiPart ) throws MessagingException,
            TemplateException, IOException {
        MimeBodyPart htmlPart = new MimeBodyPart();
        String[] language = makeLanguage(htmlTemplate);
        if ( language != null ) {
            htmlPart.setContentLanguage(language);
        }
        String contentType = makeHTMLContentType(htmlTemplate.getEncoding());

        htmlPart.setContent(renderHTML(htmlTemplate, data), contentType);
        multiPart.addBodyPart(htmlPart);
    }


    /**
     * @param htmlTemplate
     * @param data
     * @param l
     * @param msg
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    private static void makeHTMLBody ( Template htmlTemplate, Map<String, Object> data, Locale l, MimeMessage msg ) throws MessagingException,
            TemplateException, IOException {
        String[] language = makeLanguage(htmlTemplate);
        if ( language != null ) {
            msg.setContentLanguage(language);
        }
        String contentType = makeHTMLContentType(htmlTemplate.getEncoding());
        msg.setContent(renderHTML(htmlTemplate, data), contentType);
    }


    /**
     * @param htmlTemplate
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    private static String renderHTML ( Template htmlTemplate, Object data ) throws TemplateException, IOException {
        StringWriter sw = new StringWriter();
        htmlTemplate.process(data, sw);
        return sw.toString();
    }


    /**
     * @param encoding
     * @return
     */
    private static String makeHTMLContentType ( String encoding ) {
        return "text/html; charset=" + encoding; //$NON-NLS-1$
    }


    /**
     * @param tplId
     * @param l
     * @param encoding
     * @return
     * @throws IOException
     */
    private Template getHtmlTextTemplate ( String tplId, Locale l ) throws IOException {
        try {
            return this.tplConfig.getTemplate(String.format("mail/%s.html.ftl", tplId), l, this.tplConfig.getEncoding(l)); //$NON-NLS-1$
        }
        catch ( FileNotFoundException e ) {
            log.trace("No plain text template found", e); //$NON-NLS-1$
            return null;
        }

    }


    private Template getPlainTextTemplate ( String tplId, Locale l ) throws IOException {
        try {
            return this.tplConfig.getTemplate(String.format("mail/%s.plain.ftl", tplId), l, this.tplConfig.getEncoding(l), true); //$NON-NLS-1$
        }
        catch ( FileNotFoundException e ) {
            log.trace("No plain text template found", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param tplId
     * @param l
     * @return
     * @throws IOException
     */
    private Template getSubjectTemplate ( String tplId, Locale l ) throws IOException {
        return this.tplConfig.getTemplate(String.format("mail/%s.subject.ftl", tplId), l, this.tplConfig.getEncoding(l), true); //$NON-NLS-1$
    }


    /**
     * @param l
     * @return
     */
    private static String[] makeLanguage ( Template tpl ) {
        String langAttribute = (String) tpl.getCustomAttribute("language"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(langAttribute) ) {
            return new String[] {
                langAttribute
            };
        }

        return null;
    }

}