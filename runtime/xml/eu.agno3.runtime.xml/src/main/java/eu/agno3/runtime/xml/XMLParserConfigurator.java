/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;


/**
 * @author mbechler
 * 
 */
public interface XMLParserConfigurator {

    /**
     * @return A preconfigured XMLInputFactory
     * 
     */
    XMLInputFactory configureXMLInputFactory ();


    /**
     * @return A preconfigured DocumentBuildFactory
     */
    DocumentBuilderFactory configureDOMDocumentBuilderFactory ();


    /**
     * @return A preconfigured SAXParserFactory
     * 
     */
    SAXParserFactory configureSAXParserFactory ();

}