/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml;


import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.xml.sax.SAXException;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractXmlParserFactoryImpl implements XmlParserFactory {

    private SAXParserFactory saxParserFactory;
    private DocumentBuilderFactory documentBuilderFactory;
    private XMLInputFactory xmlInputFactory;

    private XMLParserConfigurator config;


    protected AbstractXmlParserFactoryImpl ( XMLParserConfigurator config ) {
        this.config = config;
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        this.saxParserFactory = this.config.configureSAXParserFactory();
        this.documentBuilderFactory = this.config.configureDOMDocumentBuilderFactory();
        this.xmlInputFactory = this.config.configureXMLInputFactory();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws XMLParserConfigurationException
     * 
     * @see eu.agno3.runtime.xml.XmlParserFactory#createSAXParser()
     */
    @Override
    public SAXParser createSAXParser () throws XMLParserConfigurationException {
        try {
            return this.saxParserFactory.newSAXParser();
        }
        catch (
            SAXException |
            ParserConfigurationException e ) {
            throw new XMLParserConfigurationException("Failed to create SAX parser:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws XMLParserConfigurationException
     * 
     * @see eu.agno3.runtime.xml.XmlParserFactory#createDocumentBuilder()
     */
    @Override
    public DocumentBuilder createDocumentBuilder () throws XMLParserConfigurationException {
        try {
            DocumentBuilder db = this.documentBuilderFactory.newDocumentBuilder();
            db.setErrorHandler(new LoggingErrorHandler(Logger.getLogger("eu.agno3.runtime.xml.parser"))); //$NON-NLS-1$
            return db;
        }
        catch ( ParserConfigurationException e ) {
            throw new XMLParserConfigurationException("Failed to create DOM parser:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws XMLParserConfigurationException
     * 
     * @see eu.agno3.runtime.xml.XmlParserFactory#createStreamReader(java.io.InputStream)
     */
    @Override
    public XMLStreamReader createStreamReader ( InputStream s ) throws XMLParserConfigurationException {
        try {
            return this.xmlInputFactory.createXMLStreamReader(s);
        }
        catch ( XMLStreamException e ) {
            throw new XMLParserConfigurationException("Failed to create StAX parser:", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.xml.XmlParserFactory#createStreamReader(java.io.InputStream, java.lang.String)
     */
    @Override
    public XMLStreamReader createStreamReader ( InputStream s, String systemId ) throws XMLParserConfigurationException {
        try {
            return this.xmlInputFactory.createXMLStreamReader(systemId, s);
        }
        catch ( XMLStreamException e ) {
            throw new XMLParserConfigurationException("Failed to create StAX parser:", e); //$NON-NLS-1$
        }
    }
}
