/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * @author mbechler
 *
 */
public interface XMLStreamable {

    /**
     * @param streamWriter
     * @throws XMLStreamException
     */
    void writeTo ( XMLStreamWriter streamWriter ) throws XMLStreamException;

}
