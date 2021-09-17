/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.properties;


import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.PropContainer;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.agno3.runtime.webdav.server.XMLStreamable;


/**
 * @author mbechler
 *
 */
public class StreamableMultiStatusResponse extends MultiStatusResponse implements XMLStreamable {

    private static final Logger log = Logger.getLogger(StreamableMultiStatusResponse.class);
    private static final String DAV_NS = DavConstants.NAMESPACE.getURI();
    private static final int TYPE_PROPSTAT = 0;
    private static final int TYPE_HREFSTATUS = 1;

    private int type;
    private List<QName> error;


    /**
     * 
     * @param resource
     * @param props
     * @param type
     */
    public StreamableMultiStatusResponse ( DavResource resource, DavPropertyNameSet props, int type ) {
        super(resource, props, type);
        this.type = type;
    }


    /**
     * 
     * @param resource
     * @param propNameSet
     */
    public StreamableMultiStatusResponse ( DavResource resource, DavPropertyNameSet propNameSet ) {
        super(resource, propNameSet);
        this.type = TYPE_PROPSTAT;
    }


    /**
     * 
     * @param href
     * @param statusCode
     * @param responseDescription
     */
    public StreamableMultiStatusResponse ( String href, int statusCode, String responseDescription ) {
        super(href, statusCode, responseDescription);
        this.type = TYPE_HREFSTATUS;
    }


    /**
     * 
     * @param href
     * @param statusCode
     */
    public StreamableMultiStatusResponse ( String href, int statusCode ) {
        super(href, statusCode);
        this.type = TYPE_HREFSTATUS;
    }


    /**
     * 
     * @param href
     * @param status
     * @param responseDescription
     */
    public StreamableMultiStatusResponse ( String href, Status status, String responseDescription ) {
        super(href, status, responseDescription);
        this.type = TYPE_HREFSTATUS;
    }


    /**
     * 
     * @param href
     * @param status
     * @param responseDescription
     * @param error
     */
    public StreamableMultiStatusResponse ( String href, Status status, String responseDescription, List<QName> error ) {
        super(href, status, responseDescription);
        this.type = TYPE_HREFSTATUS;
        this.error = error;
    }


    /**
     * 
     * @param href
     * @param responseDescription
     */
    public StreamableMultiStatusResponse ( String href, String responseDescription ) {
        super(href, responseDescription);
        this.type = TYPE_PROPSTAT;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws XMLStreamException
     *
     * @see eu.agno3.runtime.webdav.server.XMLStreamable#writeTo(javax.xml.stream.XMLStreamWriter)
     */
    @Override
    public void writeTo ( XMLStreamWriter sw ) throws XMLStreamException {

        sw.writeStartElement(DAV_NS, DavConstants.XML_RESPONSE);

        sw.writeStartElement(DAV_NS, DavConstants.XML_HREF);
        sw.writeCharacters(getHref());
        sw.writeEndElement();

        Status[] status = getStatus();

        for ( Status st : status ) {
            if ( this.type == TYPE_PROPSTAT ) {
                PropContainer propCont = getProperties(st.getStatusCode());
                if ( !propCont.isEmpty() ) {
                    sw.writeStartElement(DAV_NS, DavConstants.XML_PROPSTAT);
                    writeProperties(sw, propCont);
                    writeStatus(sw, st);
                    sw.writeEndElement();
                }
            }
            else {
                writeStatus(sw, st);
                break;
            }
        }

        if ( this.error != null ) {
            sw.writeStartElement(DAV_NS, "error"); //$NON-NLS-1$
            for ( QName e : this.error ) {
                sw.writeStartElement(e.getNamespaceURI(), e.getLocalPart());
                // ignoring contents?!?
                sw.writeEndElement();
            }
            sw.writeEndElement();
        }

        String desc = this.getResponseDescription();
        if ( !StringUtils.isBlank(desc) ) {
            sw.writeStartElement(DAV_NS, DavConstants.XML_RESPONSEDESCRIPTION);
            sw.writeCharacters(desc);
            sw.writeEndElement();
        }

        // end response
        sw.writeEndElement();
    }


    /**
     * @param sw
     * @param st
     * @throws XMLStreamException
     */
    private static void writeStatus ( XMLStreamWriter sw, Status st ) throws XMLStreamException {
        if ( st instanceof XMLStreamable ) {
            ( (XMLStreamable) st ).writeTo(sw);
        }
        else {
            String statusLine = "HTTP/1.1 " + st.getStatusCode() + StringUtils.SPACE + DavException.getStatusPhrase(st.getStatusCode()); //$NON-NLS-1$
            sw.writeStartElement(DAV_NS, DavConstants.XML_STATUS);
            sw.writeCharacters(statusLine);
            sw.writeEndElement();
        }
    }


    /**
     * @param sw
     * @param propCont
     * @throws XMLStreamException
     */
    private static void writeProperties ( XMLStreamWriter sw, PropContainer propCont ) throws XMLStreamException {
        Document doc = null;
        sw.writeStartElement(DAV_NS, DavConstants.XML_PROP);
        for ( Object data : propCont.getContent() ) {
            if ( data instanceof XMLStreamable ) {
                ( (XMLStreamable) data ).writeTo(sw);
            }
            else if ( data instanceof XmlSerializable ) {
                try {
                    if ( doc == null ) {
                        doc = DomUtil.createDocument();
                    }
                    Element elem = ( (XmlSerializable) data ).toXml(doc);
                    fixupDOM(sw, elem);
                    TransformerFactory inst = TransformerFactory.newInstance();
                    Transformer trans = inst.newTransformer();
                    StAXResult out = new StAXResult(new FragmentXmlStreamWriter(sw));
                    DOMSource in = new DOMSource(elem);
                    trans.transform(in, out);
                }
                catch (
                    ParserConfigurationException |
                    TransformerException e ) {
                    throw new XMLStreamException("Failed to transform property", e); //$NON-NLS-1$
                }
            }
            else {
                log.debug("Unexpected content in PropContainer: should be XmlSerializable."); //$NON-NLS-1$
            }
        }
        sw.writeEndElement();
    }


    /**
     * @param sw
     * @param doc
     * @throws XMLStreamException
     * @throws DOMException
     */
    private static void fixupDOM ( XMLStreamWriter sw, Element root ) throws DOMException, XMLStreamException {
        fixupElement(sw, root);
        NodeList nodeList = root.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); i++ ) {
            Node currentNode = nodeList.item(i);
            if ( currentNode instanceof Element ) {
                fixupDOM(sw, (Element) currentNode);
            }
        }
    }


    /**
     * @param sw
     * @param root
     * @throws XMLStreamException
     * @throws DOMException
     */
    private static void fixupElement ( XMLStreamWriter sw, Element root ) throws DOMException, XMLStreamException {
        root.setPrefix(sw.getPrefix(root.getNamespaceURI()));
    }

}
