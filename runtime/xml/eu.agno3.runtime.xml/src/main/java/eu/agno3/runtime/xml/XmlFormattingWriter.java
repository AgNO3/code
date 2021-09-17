/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.08.2013 by mbechler
 */
package eu.agno3.runtime.xml;


import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 * 
 */
public class XmlFormattingWriter implements XMLStreamWriter {

    private XMLStreamWriter delegate;
    private int indent = 0;
    boolean lastIsCloseElement;


    /**
     * @param delegate
     */
    public XmlFormattingWriter ( XMLStreamWriter delegate ) {
        this.delegate = delegate;
    }


    @Override
    public void close () throws XMLStreamException {
        this.delegate.close();
    }


    @Override
    public void flush () throws XMLStreamException {
        this.delegate.flush();
    }


    @Override
    public NamespaceContext getNamespaceContext () {
        return this.delegate.getNamespaceContext();
    }


    @Override
    public String getPrefix ( String uri ) throws XMLStreamException {
        return this.delegate.getPrefix(uri);
    }


    @Override
    public Object getProperty ( String name ) {
        return this.delegate.getProperty(name);
    }


    @Override
    public void setDefaultNamespace ( String uri ) throws XMLStreamException {
        this.delegate.setDefaultNamespace(uri);
    }


    @Override
    public void setNamespaceContext ( NamespaceContext context ) throws XMLStreamException {
        this.delegate.setNamespaceContext(context);
    }


    @Override
    public void setPrefix ( String prefix, String uri ) throws XMLStreamException {
        this.delegate.setPrefix(prefix, uri);
    }


    @Override
    public void writeAttribute ( String prefix, String namespaceURI, String localName, String value ) throws XMLStreamException {
        this.delegate.writeAttribute(prefix, namespaceURI, localName, value);
    }


    @Override
    public void writeAttribute ( String namespaceURI, String localName, String value ) throws XMLStreamException {
        this.delegate.writeAttribute(namespaceURI, localName, value);
    }


    @Override
    public void writeAttribute ( String localName, String value ) throws XMLStreamException {
        this.delegate.writeAttribute(localName, value);
    }


    @Override
    public void writeCData ( String data ) throws XMLStreamException {
        this.delegate.writeCData(data);
        this.lastIsCloseElement = false;
    }


    @Override
    public void writeCharacters ( char[] text, int start, int len ) throws XMLStreamException {
        this.delegate.writeCharacters(text, start, len);
        this.lastIsCloseElement = false;
    }


    @Override
    public void writeCharacters ( String text ) throws XMLStreamException {
        this.delegate.writeCharacters(text);
        this.lastIsCloseElement = false;
    }


    @Override
    public void writeComment ( String data ) throws XMLStreamException {
        this.delegate.writeComment(data);
    }


    @Override
    public void writeDTD ( String dtd ) throws XMLStreamException {
        this.delegate.writeDTD(dtd);
    }


    @Override
    public void writeDefaultNamespace ( String namespaceURI ) throws XMLStreamException {
        this.delegate.writeDefaultNamespace(namespaceURI);
    }


    @Override
    public void writeEmptyElement ( String prefix, String localName, String namespaceURI ) throws XMLStreamException {
        this.delegate.writeEmptyElement(prefix, localName, namespaceURI);
        this.lastIsCloseElement = true;
    }


    @Override
    public void writeEmptyElement ( String namespaceURI, String localName ) throws XMLStreamException {
        this.delegate.writeEmptyElement(namespaceURI, localName);
        this.lastIsCloseElement = true;
    }


    @Override
    public void writeEmptyElement ( String localName ) throws XMLStreamException {
        this.delegate.writeCharacters(System.lineSeparator());
        this.delegate.writeCharacters(StringUtils.repeat(" ", 2 * this.indent)); //$NON-NLS-1$
        this.delegate.writeEmptyElement(localName);
        this.lastIsCloseElement = true;
    }


    @Override
    public void writeEndDocument () throws XMLStreamException {
        this.delegate.writeEndDocument();
        this.delegate.writeCharacters(System.lineSeparator());
    }


    @Override
    public void writeEndElement () throws XMLStreamException {
        this.indent--;

        if ( this.lastIsCloseElement ) {
            this.delegate.writeCharacters(System.lineSeparator());
            this.delegate.writeCharacters(StringUtils.repeat(" ", 2 * this.indent)); //$NON-NLS-1$
        }

        this.delegate.writeEndElement();

        this.lastIsCloseElement = true;
    }


    @Override
    public void writeEntityRef ( String name ) throws XMLStreamException {
        this.delegate.writeEntityRef(name);
    }


    @Override
    public void writeNamespace ( String prefix, String namespaceURI ) throws XMLStreamException {
        this.delegate.writeNamespace(prefix, namespaceURI);
    }


    @Override
    public void writeProcessingInstruction ( String target, String data ) throws XMLStreamException {
        this.delegate.writeProcessingInstruction(target, data);
    }


    @Override
    public void writeProcessingInstruction ( String target ) throws XMLStreamException {
        this.delegate.writeProcessingInstruction(target);
    }


    @Override
    public void writeStartDocument () throws XMLStreamException {
        this.delegate.writeStartDocument();
        this.delegate.writeCharacters(System.lineSeparator());
    }


    @Override
    public void writeStartDocument ( String encoding, String version ) throws XMLStreamException {
        this.delegate.writeStartDocument(encoding, version);
        this.delegate.writeCharacters(System.lineSeparator());
    }


    @Override
    public void writeStartDocument ( String version ) throws XMLStreamException {
        this.delegate.writeStartDocument(version);
        this.delegate.writeCharacters(System.lineSeparator());
    }


    @Override
    public void writeStartElement ( String prefix, String localName, String namespaceURI ) throws XMLStreamException {
        this.delegate.writeCharacters(System.lineSeparator());
        this.delegate.writeCharacters(StringUtils.repeat(" ", 2 * this.indent)); //$NON-NLS-1$
        this.delegate.writeStartElement(prefix, localName, namespaceURI);
        this.indent++;
        this.lastIsCloseElement = false;
    }


    @Override
    public void writeStartElement ( String namespaceURI, String localName ) throws XMLStreamException {
        this.delegate.writeCharacters(System.lineSeparator());
        this.delegate.writeCharacters(StringUtils.repeat(" ", 2 * this.indent)); //$NON-NLS-1$
        this.delegate.writeStartElement(namespaceURI, localName);
        this.indent++;
        this.lastIsCloseElement = false;
    }


    @Override
    public void writeStartElement ( String localName ) throws XMLStreamException {
        this.delegate.writeCharacters(System.lineSeparator());
        this.delegate.writeCharacters(StringUtils.repeat(" ", 2 * this.indent)); //$NON-NLS-1$
        this.delegate.writeStartElement(localName);
        this.indent++;
        this.lastIsCloseElement = false;
    }

}
