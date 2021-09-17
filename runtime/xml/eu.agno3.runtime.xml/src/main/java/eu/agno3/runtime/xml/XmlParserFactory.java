/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml;


import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParser;
import javax.xml.stream.XMLStreamReader;


/**
 * @author mbechler
 * 
 */
public interface XmlParserFactory {

    /**
     * @return a SAX parser for the given input
     * @throws XMLParserConfigurationException
     */
    SAXParser createSAXParser () throws XMLParserConfigurationException;


    /**
     * @return a DOM DocumentBuilder for the given input
     * @throws XMLParserConfigurationException
     */
    DocumentBuilder createDocumentBuilder () throws XMLParserConfigurationException;


    /**
     * @param s
     * @return a StaX StreamReader for the given input
     * @throws XMLParserConfigurationException
     */
    XMLStreamReader createStreamReader ( InputStream s ) throws XMLParserConfigurationException;


    /**
     * @param s
     * @param systemId
     * @return a StaX StreamReader for the given input
     * @throws XMLParserConfigurationException
     */
    XMLStreamReader createStreamReader ( InputStream s, String systemId ) throws XMLParserConfigurationException;
}
