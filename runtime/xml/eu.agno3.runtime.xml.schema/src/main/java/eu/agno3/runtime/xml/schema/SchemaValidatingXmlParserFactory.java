/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.validation.Schema;


/**
 * @author mbechler
 * 
 */
public interface SchemaValidatingXmlParserFactory {

    /**
     * @param s
     * @return a DocumentBuilder configured for the given schema
     * @throws ParserConfigurationException
     */
    DocumentBuilder createDocumentBuilder ( Schema s ) throws ParserConfigurationException;


    /**
     * @param s
     * @return a SAXParser configured for the given schema
     * @throws ParserConfigurationException
     */
    SAXParser createSAXParser ( Schema s ) throws ParserConfigurationException;

}
