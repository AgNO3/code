/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author mbechler
 * 
 */
public class LoggingErrorHandler extends DefaultHandler {

    private final Logger log;
    private String element;


    /**
     * @param log
     * 
     */
    public LoggingErrorHandler ( Logger log ) {
        this.log = log;
    }


    /**
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     * @throws SAXException
     */
    @Override
    public void startElement ( String uri, String localName, String qName, Attributes attributes ) throws SAXException {

        if ( localName != null && !localName.isEmpty() ) {
            this.element = localName;
        }
        else {
            this.element = qName;
        }

    }


    private String format ( SAXParseException e ) {

        return String.format("%s: %s on%s%s%s", //$NON-NLS-1$
            e.getClass().getName(),
            e.getMessage(),
            formatLineNumber(e),
            formatColumnNumber(e),
            formatElement(this.element));
    }


    /**
     * @return
     */
    private static String formatElement ( String elem ) {
        return elem != null ? " element " + elem : StringUtils.EMPTY; //$NON-NLS-1$ 
    }


    /**
     * @param e
     * @return
     */
    private static String formatColumnNumber ( SAXParseException e ) {
        return e.getColumnNumber() >= 0 ? " row " + e.getColumnNumber() : StringUtils.EMPTY; //$NON-NLS-1$
    }


    /**
     * @param e
     * @return
     */
    private static String formatLineNumber ( SAXParseException e ) {
        return e.getLineNumber() >= 0 ? " line " + e.getLineNumber() : StringUtils.EMPTY; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error ( SAXParseException exception ) throws SAXException {
        this.log.warn(this.format(exception));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError ( SAXParseException exception ) throws SAXException {
        this.log.error(this.format(exception));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning ( SAXParseException exception ) throws SAXException {
        this.log.warn(this.format(exception));
    }

}
