/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2014 by mbechler
 */
package eu.agno3.runtime.xml;


import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * @author mbechler
 * 
 */
public class DuplicatingXmlStreamWriter implements XMLStreamWriter {

    private List<XMLStreamWriter> delegates;
    private XMLStreamWriter primary;


    /**
     * @param primary
     * @param writers
     */
    public DuplicatingXmlStreamWriter ( XMLStreamWriter primary, XMLStreamWriter... writers ) {
        this.primary = primary;
        this.delegates = Arrays.asList(writers);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.stream.XMLStreamWriter#close()
     */
    @Override
    public void close () throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.close();
        }
        this.primary.close();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.xml.stream.XMLStreamWriter#flush()
     */
    @Override
    public void flush () throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.flush();
        }
        this.primary.flush();
    }


    @Override
    public NamespaceContext getNamespaceContext () {
        return this.primary.getNamespaceContext();
    }


    @Override
    public String getPrefix ( String uri ) throws XMLStreamException {
        return this.primary.getPrefix(uri);
    }


    @Override
    public Object getProperty ( String name ) {
        return this.primary.getProperty(name);
    }


    @Override
    public void setDefaultNamespace ( String uri ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.setDefaultNamespace(uri);
        }
        this.primary.setDefaultNamespace(uri);
    }


    @Override
    public void setNamespaceContext ( NamespaceContext context ) throws XMLStreamException {
        this.primary.setNamespaceContext(context);
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.setNamespaceContext(context);
        }
    }


    @Override
    public void setPrefix ( String prefix, String uri ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.setPrefix(prefix, uri);
        }
        this.primary.setPrefix(prefix, uri);
    }


    @Override
    public void writeAttribute ( String prefix, String namespaceURI, String localName, String value ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeAttribute(prefix, namespaceURI, localName, value);
        }
        this.primary.writeAttribute(prefix, namespaceURI, localName, value);
    }


    @Override
    public void writeAttribute ( String namespaceURI, String localName, String value ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeAttribute(namespaceURI, localName, value);
        }
        this.primary.writeAttribute(namespaceURI, localName, value);
    }


    @Override
    public void writeAttribute ( String localName, String value ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeAttribute(localName, value);
        }
        this.primary.writeAttribute(localName, value);
    }


    @Override
    public void writeCData ( String data ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeCData(data);
        }
        this.primary.writeCData(data);
    }


    @Override
    public void writeCharacters ( char[] text, int start, int len ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeCharacters(text, start, len);
        }
        this.primary.writeCharacters(text, start, len);
    }


    @Override
    public void writeCharacters ( String text ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeCharacters(text);
        }
        this.primary.writeCharacters(text);
    }


    @Override
    public void writeComment ( String data ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeComment(data);
        }
        this.primary.writeComment(data);
    }


    @Override
    public void writeDTD ( String dtd ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeDTD(dtd);
        }
        this.primary.writeDTD(dtd);
    }


    @Override
    public void writeDefaultNamespace ( String namespaceURI ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeDefaultNamespace(namespaceURI);
        }
        this.primary.writeDefaultNamespace(namespaceURI);
    }


    @Override
    public void writeEmptyElement ( String prefix, String localName, String namespaceURI ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeEmptyElement(prefix, localName, namespaceURI);
        }
        this.primary.writeEmptyElement(prefix, localName, namespaceURI);
    }


    @Override
    public void writeEmptyElement ( String namespaceURI, String localName ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeEmptyElement(namespaceURI, localName);
        }
        this.primary.writeEmptyElement(namespaceURI, localName);
    }


    @Override
    public void writeEmptyElement ( String localName ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeEmptyElement(localName);
        }
        this.primary.writeEmptyElement(localName);
    }


    @Override
    public void writeEndDocument () throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeEndDocument();
        }
        this.primary.writeEndDocument();
    }


    @Override
    public void writeEndElement () throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeEndElement();
        }
        this.primary.writeEndElement();
    }


    @Override
    public void writeEntityRef ( String name ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeEntityRef(name);
        }
        this.primary.writeEntityRef(name);
    }


    @Override
    public void writeNamespace ( String prefix, String namespaceURI ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeNamespace(prefix, namespaceURI);
        }
        this.primary.writeNamespace(prefix, namespaceURI);
    }


    @Override
    public void writeProcessingInstruction ( String target, String data ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeProcessingInstruction(target, data);
        }
        this.primary.writeProcessingInstruction(target, data);
    }


    @Override
    public void writeProcessingInstruction ( String target ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeProcessingInstruction(target);
        }
        this.primary.writeProcessingInstruction(target);
    }


    @Override
    public void writeStartDocument () throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeStartDocument();
        }
        this.primary.writeStartDocument();
    }


    @Override
    public void writeStartDocument ( String encoding, String version ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeStartDocument(encoding, version);
        }
        this.primary.writeStartDocument(encoding, version);
    }


    @Override
    public void writeStartDocument ( String version ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeStartDocument(version);
        }
        this.primary.writeStartDocument(version);
    }


    @Override
    public void writeStartElement ( String prefix, String localName, String namespaceURI ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeStartElement(prefix, localName, namespaceURI);
        }
        this.primary.writeStartElement(prefix, localName, namespaceURI);
    }


    @Override
    public void writeStartElement ( String namespaceURI, String localName ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeStartElement(namespaceURI, localName);
        }
        this.primary.writeStartElement(namespaceURI, localName);
    }


    @Override
    public void writeStartElement ( String localName ) throws XMLStreamException {
        for ( XMLStreamWriter delegate : this.delegates ) {
            delegate.writeStartElement(localName);
        }
        this.primary.writeStartElement(localName);
    }

}
