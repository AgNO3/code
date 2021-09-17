/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.jackrabbit.commons.xml.SerializingContentHandler;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import eu.agno3.runtime.webdav.server.StreamingContext;
import eu.agno3.runtime.webdav.server.StreamingWebdavResponse;


/**
 * @author mbechler
 *
 */
public class DefaultWebdavResponseImpl extends WebdavResponseImpl implements StreamingWebdavResponse {

    private static final Logger log = Logger.getLogger(DefaultWebdavResponseImpl.class);
    private static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private HttpServletResponse httpResponse;
    private boolean streaming;


    /**
     * @param httpResponse
     * @param noCache
     */
    public DefaultWebdavResponseImpl ( HttpServletResponse httpResponse, boolean noCache ) {
        super(httpResponse, noCache);
        this.httpResponse = httpResponse;
    }


    /**
     * @param httpResponse
     */
    public DefaultWebdavResponseImpl ( HttpServletResponse httpResponse ) {
        super(httpResponse);
        this.httpResponse = httpResponse;
    }


    /**
     * 
     * @return the content type to use in xml responses
     */
    protected String getXmlResponseContentType () {
        return "text/xml; charset=UTF-8"; //$NON-NLS-1$
    }


    /**
     * @return a streaming output context
     */
    @Override
    public StreamingContext getStreamingContext () {
        this.streaming = true;
        return new StreamingContextImpl(this.httpResponse, getXmlResponseContentType());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.WebdavResponseImpl#sendXmlResponse(org.apache.jackrabbit.webdav.xml.XmlSerializable,
     *      int)
     */
    @Override
    public void sendXmlResponse ( XmlSerializable serializable, int status ) throws IOException {
        if ( this.streaming && status < 300 ) {
            return;
        }
        this.httpResponse.setStatus(status);

        if ( serializable != null ) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                Document doc = BUILDER_FACTORY.newDocumentBuilder().newDocument();
                doc.appendChild(serializable.toXml(doc));

                fixupDOM(doc.getDocumentElement());

                ContentHandler handler = SerializingContentHandler.getSerializer(out);
                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();
                transformer.transform(new DOMSource(doc), new SAXResult(handler));

                this.httpResponse.setContentType(getXmlResponseContentType());
                this.httpResponse.setContentLength(out.size());

                log.debug("Sending response"); //$NON-NLS-1$
                if ( log.isTraceEnabled() ) {
                    log.trace(new String(out.toByteArray(), Charset.defaultCharset()));
                }
                out.writeTo(this.httpResponse.getOutputStream());

            }
            catch ( ParserConfigurationException e ) {
                log.error(e.getMessage());
                throw new IOException(e.getMessage(), e);
            }
            catch ( TransformerException e ) {
                log.error(e.getMessage());
                throw new IOException(e.getMessage(), e);
            }
            catch ( SAXException e ) {
                log.error(e.getMessage());
                throw new IOException(e.getMessage(), e);
            }
        }
    }


    /**
     * @param doc
     */
    private void fixupDOM ( Element root ) {
        fixupElement(root);
        NodeList nodeList = root.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); i++ ) {
            Node currentNode = nodeList.item(i);
            if ( currentNode instanceof Element ) {
                fixupDOM((Element) currentNode);
            }
        }
    }


    /**
     * @param root
     */
    private static void fixupElement ( Element root ) {
        if ( "DAV:".equals(root.getNamespaceURI()) ) { //$NON-NLS-1$
            root.setPrefix("d"); //$NON-NLS-1$
        }
    }
}
