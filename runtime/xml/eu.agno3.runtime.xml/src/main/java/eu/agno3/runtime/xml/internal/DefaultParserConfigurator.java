/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.internal;


import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.ctc.wstx.stax.WstxInputFactory;

import eu.agno3.runtime.xml.XMLParserConfigurator;


/**
 * @author mbechler
 * 
 */
@Component ( service = XMLParserConfigurator.class )
public class DefaultParserConfigurator implements XMLParserConfigurator {

    private static final Logger log = Logger.getLogger(DefaultParserConfigurator.class);

    private static final String SAX_EXTERNAL_ENTITIES_FEATURE = "http://xml.org/sax/features/external-general-entities"; //$NON-NLS-1$
    private static final String SAX_DISALLOW_DOCTYPE_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.XMLParserConfigurator#configureXMLInputFactory()
     */
    @Override
    public XMLInputFactory configureXMLInputFactory () {
        XMLInputFactory xif = new WstxInputFactory();
        xif.setXMLResolver(new EmptyEntityResolver());
        xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xif.setProperty(XMLInputFactory.IS_VALIDATING, false);
        xif.setXMLReporter(new LoggingXMLReporter(Logger.getLogger("eu.agno3.runtime.xml.parser"))); //$NON-NLS-1$

        return xif;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.XMLParserConfigurator#configureDOMDocumentBuilderFactory()
     */
    @Override
    public DocumentBuilderFactory configureDOMDocumentBuilderFactory () {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);

        try {
            dbf.setFeature(SAX_EXTERNAL_ENTITIES_FEATURE, false);
        }
        catch ( ParserConfigurationException e ) {
            log.error("Failed to disable external entity usage for DOM", e); //$NON-NLS-1$
        }

        try {
            dbf.setFeature(SAX_DISALLOW_DOCTYPE_FEATURE, true);
        }
        catch ( ParserConfigurationException e ) {
            log.error("Failed to disable embedded doctype declaration for DOM", e); //$NON-NLS-1$
        }

        return dbf;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.xml.XMLParserConfigurator#configureSAXParserFactory()
     */
    @Override
    public SAXParserFactory configureSAXParserFactory () {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(false);

        try {
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        }
        catch (
            ParserConfigurationException |
            SAXNotRecognizedException |
            SAXNotSupportedException e ) {
            log.error("Failed to enable secure processing for SAX", e); //$NON-NLS-1$
        }

        try {
            spf.setFeature(SAX_EXTERNAL_ENTITIES_FEATURE, false);
        }
        catch (
            ParserConfigurationException |
            SAXNotRecognizedException |
            SAXNotSupportedException e ) {
            log.error("Failed to disable external entity usage for SAX", e); //$NON-NLS-1$
        }

        try {
            spf.setFeature(SAX_DISALLOW_DOCTYPE_FEATURE, true);
        }
        catch (
            ParserConfigurationException |
            SAXNotRecognizedException |
            SAXNotSupportedException e ) {
            log.error("Failed to disable embedded doctype declaration for SAX", e); //$NON-NLS-1$
        }

        return spf;
    }

}
