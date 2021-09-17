/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.properties;


import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eu.agno3.runtime.xml.DuplicatingXmlStreamWriter;


/**
 * @author mbechler
 *
 */
public final class FragmentXmlStreamWriter extends DuplicatingXmlStreamWriter {

    /**
     * @param primary
     * @param writers
     */
    public FragmentXmlStreamWriter ( XMLStreamWriter primary, XMLStreamWriter... writers ) {
        super(primary, writers);
    }


    @Override
    public void writeStartDocument () throws XMLStreamException {}


    @Override
    public void writeStartDocument ( String encoding, String version ) throws XMLStreamException {}


    @Override
    public void writeStartDocument ( String version ) throws XMLStreamException {}


    @Override
    public void writeEndDocument () throws XMLStreamException {}


    @Override
    public void close () throws XMLStreamException {}
}