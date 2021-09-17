/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.notify;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
@Named ( "mailPreviewBean" )
@ApplicationScoped
public class MailPreviewBean {

    private static final Logger log = Logger.getLogger(MailPreviewBean.class);


    /**
     * 
     * @param msg
     * @return the from address as a string
     * @throws MessagingException
     */
    public String getFromAddrs ( MimeMessage msg ) throws MessagingException {
        List<String> res = new LinkedList<>();

        if ( msg == null || msg.getFrom() == null ) {
            return StringUtils.EMPTY;
        }

        for ( Address addr : msg.getFrom() ) {
            res.add(addr.toString());
        }

        return StringUtils.join(res, ", "); //$NON-NLS-1$
    }


    /**
     * 
     * @param msg
     * @return the from address as a string
     * @throws MessagingException
     */
    public String getToAddrs ( MimeMessage msg ) throws MessagingException {
        List<String> res = new LinkedList<>();

        if ( msg == null ) {
            return StringUtils.EMPTY;
        }

        Address[] recpts = msg.getRecipients(javax.mail.Message.RecipientType.TO);
        if ( recpts == null ) {
            return StringUtils.EMPTY;
        }

        for ( Address addr : recpts ) {
            res.add(addr.toString());
        }

        return StringUtils.join(res, ", "); //$NON-NLS-1$
    }


    /**
     * 
     * @param msg
     * @return the body (preferred html)
     * @throws IOException
     * @throws MessagingException
     * @throws MimeTypeParseException
     */
    public String getBody ( MimeMessage msg ) throws IOException, MessagingException, MimeTypeParseException {
        if ( msg == null || msg.getContent() == null ) {
            return StringUtils.EMPTY;
        }

        Object content = msg.getContent();

        if ( content instanceof MimeMultipart ) {

            MimeMultipart mp = (MimeMultipart) content;

            if ( mp.getCount() == 1 ) {
                return returnMimePart(mp.getBodyPart(0));
            }

            BodyPart htmlPart = findFirstPart(mp, "text/html"); //$NON-NLS-1$
            if ( htmlPart != null ) {
                return returnMimePart(htmlPart);
            }

            log.debug("Did not find HTML part"); //$NON-NLS-1$

            BodyPart textPart = findFirstPart(mp, "text/plain"); //$NON-NLS-1$
            if ( textPart != null ) {
                return returnMimePart(textPart);
            }

        }
        else if ( msg.isMimeType("text/plain") ) { //$NON-NLS-1$
            return wrapInPre(content);
        }

        return StringUtils.EMPTY;
    }


    /**
     * @param mp
     * @return
     * @throws MessagingException
     * @throws IOException
     * @throws MimeTypeParseException
     */
    private static BodyPart findFirstPart ( MimeMultipart mp, String type ) throws MessagingException, IOException, MimeTypeParseException {
        for ( int idx = 0; idx < mp.getCount(); idx++ ) {
            BodyPart part = mp.getBodyPart(idx);
            MimeType partType = new MimeType(part.getDataHandler().getContentType());
            if ( partType.match(type) ) {
                return part;
            }
        }

        return null;
    }


    /**
     * @param mp
     * @return
     * @throws MessagingException
     * @throws IOException
     * @throws MimeTypeParseException
     */
    private static String returnMimePart ( BodyPart bodyPart ) throws MessagingException, IOException, MimeTypeParseException {
        MimeType partType = new MimeType(bodyPart.getDataHandler().getContentType());
        if ( partType.match("text/html") ) { //$NON-NLS-1$
            return bodyPart.getContent().toString();
        }
        else if ( partType.match("text/plain") ) { //$NON-NLS-1$
            return wrapInPre(bodyPart.getContent().toString());
        }
        return StringUtils.EMPTY;
    }


    /**
     * @param content
     * @return
     */
    private static String wrapInPre ( Object content ) {
        return "<html><head></head><body><pre>" + //$NON-NLS-1$
                content + "</pre></body></html>"; //$NON-NLS-1$
    }
}
