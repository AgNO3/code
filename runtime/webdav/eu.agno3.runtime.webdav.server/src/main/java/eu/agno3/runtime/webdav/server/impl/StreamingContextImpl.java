/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.agno3.runtime.util.log.LogWriter;
import eu.agno3.runtime.webdav.server.StreamingContext;
import eu.agno3.runtime.webdav.server.XMLStreamable;
import eu.agno3.runtime.xml.DuplicatingXmlStreamWriter;
import eu.agno3.runtime.xml.XmlFormattingWriter;


/**
 * @author mbechler
 *
 */
public class StreamingContextImpl implements StreamingContext {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(StreamingContextImpl.class);
    private static final String DAV_NS = DavConstants.NAMESPACE.getURI();
    private HttpServletResponse response;
    private XMLStreamWriter streamWriter;
    private String contentType;
    private ServletOutputStream outputStream;

    private String responseDescription;

    private boolean wroteStart;


    /**
     * @param response
     * @param contentType
     * 
     */
    public StreamingContextImpl ( HttpServletResponse response, String contentType ) {
        this.response = response;
        this.contentType = contentType;
    }


    /**
     * @param responseDescription
     *            the responseDescription to set
     */
    public void setResponseDescription ( String responseDescription ) {
        this.responseDescription = responseDescription;
    }


    /**
     * @throws IOException
     * 
     */
    @Override
    public synchronized void startMultiStatus () throws IOException {
        try {
            this.response.setStatus(DavServletResponse.SC_MULTI_STATUS);
            this.response.setContentType(this.contentType);
            XMLStreamWriter sw = getStreamWriter();
            sw.writeStartDocument();
            sw.writeStartElement("d", DavConstants.XML_MULTISTATUS, DAV_NS); //$NON-NLS-1$
            sw.setPrefix("d", DAV_NS); //$NON-NLS-1$
            sw.writeNamespace("d", DAV_NS); //$NON-NLS-1$
            this.wroteStart = true;
        }
        catch (
            XMLStreamException |
            FactoryConfigurationError e ) {
            throw new IOException("Failed to create XML output", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the streamWriter
     * @throws IOException
     */
    @SuppressWarnings ( "resource" )
    @Override
    public XMLStreamWriter getStreamWriter () throws IOException {
        if ( this.streamWriter == null ) {
            this.outputStream = this.response.getOutputStream();
            try {
                XMLStreamWriter sw = XMLOutputFactory.newInstance().createXMLStreamWriter(this.outputStream);
                if ( log.isTraceEnabled() ) {
                    XMLStreamWriter debug = XMLOutputFactory.newInstance().createXMLStreamWriter(new LogWriter(log, Level.TRACE));
                    sw = new DuplicatingXmlStreamWriter(sw, new XmlFormattingWriter(debug));
                }
                this.streamWriter = sw;
            }
            catch (
                XMLStreamException |
                FactoryConfigurationError e ) {
                throw new IOException("Failed to create XML output", e); //$NON-NLS-1$
            }
        }
        return this.streamWriter;
    }


    /**
     * @throws XMLStreamException
     * 
     */
    private void writeEnd () throws XMLStreamException {
        if ( this.wroteStart ) {
            if ( this.responseDescription != null ) {
                this.streamWriter.writeStartElement(DAV_NS, DavConstants.XML_RESPONSEDESCRIPTION);
                this.streamWriter.writeCData(this.responseDescription);
                this.streamWriter.writeEndElement();
            }
            this.streamWriter.writeEndElement();
            this.streamWriter.writeEndDocument();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.webdav.server.StreamingContext#write(eu.agno3.runtime.webdav.server.XMLStreamable)
     */
    @Override
    public void write ( XMLStreamable xs ) throws IOException {
        try {
            xs.writeTo(getStreamWriter());
        }
        catch ( XMLStreamException e ) {
            throw new IOException("Failed to write element", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.StreamingContext#close()
     */
    @Override
    public synchronized void close () throws IOException {
        try {
            if ( this.streamWriter != null ) {
                writeEnd();
                this.streamWriter.flush();
                this.streamWriter.close();
                this.streamWriter = null;
            }
        }
        catch ( XMLStreamException e ) {
            throw new IOException("Failed to close XML writer", e); //$NON-NLS-1$
        }
        finally {
            if ( this.outputStream != null ) {
                this.outputStream.close();
            }
        }
    }

}
