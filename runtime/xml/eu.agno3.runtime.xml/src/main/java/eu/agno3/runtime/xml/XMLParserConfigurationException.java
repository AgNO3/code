/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2014 by mbechler
 */
package eu.agno3.runtime.xml;


/**
 * @author mbechler
 * 
 */
public class XMLParserConfigurationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8148190203043055612L;


    /**
     * 
     */
    public XMLParserConfigurationException () {}


    /**
     * @param message
     */
    public XMLParserConfigurationException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public XMLParserConfigurationException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public XMLParserConfigurationException ( String message, Throwable cause ) {
        super(message, cause);
    }

}
