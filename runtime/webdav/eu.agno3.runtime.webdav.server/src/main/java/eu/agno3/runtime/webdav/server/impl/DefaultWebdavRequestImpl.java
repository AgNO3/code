/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author mbechler
 *
 */
public class DefaultWebdavRequestImpl extends WebdavRequestImpl {

    private static final Logger log = Logger.getLogger(DefaultWebdavRequestImpl.class);

    private int propfindType = PROPFIND_ALL_PROP;
    private HttpServletRequest httpRequest;
    private DavPropertyNameSet propfindProps;


    /**
     * @param httpRequest
     * @param factory
     * @param createAbsoluteURI
     */
    public DefaultWebdavRequestImpl ( HttpServletRequest httpRequest, DavLocatorFactory factory, boolean createAbsoluteURI ) {
        super(httpRequest, factory, createAbsoluteURI);
        this.httpRequest = httpRequest;
    }


    /**
     * @param httpRequest
     * @param factory
     */
    public DefaultWebdavRequestImpl ( HttpServletRequest httpRequest, DavLocatorFactory factory ) {
        super(httpRequest, factory);
        this.httpRequest = httpRequest;
    }


    /**
     * @return the httpRequest
     */
    public HttpServletRequest getHttpRequest () {
        return this.httpRequest;
    }


    /**
     * Returns the type of PROPFIND as indicated by the request body.
     *
     * @return type of the PROPFIND request. Default value is {@link #PROPFIND_ALL_PROP allprops}
     * @see DavServletRequest#getPropFindType()
     */
    @Override
    public int getPropFindType () throws DavException {
        if ( this.propfindProps == null ) {
            parsePropFindRequest();
        }
        return this.propfindType;
    }


    /**
     * Returns the set of properties requested by the PROPFIND body or an
     * empty set if the {@link #getPropFindType type} is either 'allprop' or
     * 'propname'.
     *
     * @return set of properties requested by the PROPFIND body or an empty set.
     * @see DavServletRequest#getPropFindProperties()
     */
    @Override
    public DavPropertyNameSet getPropFindProperties () throws DavException {
        if ( this.propfindProps == null ) {
            parsePropFindRequest();
        }
        return this.propfindProps;
    }


    /**
     * Parse the propfind request body in order to determine the type of the propfind
     * and the set of requested property.
     * NOTE: An empty 'propfind' request body will be treated as request for all
     * property according to the specification.
     */
    private void parsePropFindRequest () throws DavException {
        this.propfindProps = new DavPropertyNameSet();
        Document requestDocument = getRequestDocument();
        // propfind httpRequest with empty body >> retrieve all property
        if ( requestDocument == null ) {
            return;
        }

        // propfind httpRequest with invalid body
        Element root = requestDocument.getDocumentElement();
        if ( !XML_PROPFIND.equals(root.getLocalName()) ) {
            log.info("PropFind-Request has no <propfind> tag."); //$NON-NLS-1$
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "PropFind-Request has no <propfind> tag."); //$NON-NLS-1$
        }

        if ( log.isTraceEnabled() ) {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer;
            try {
                transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
                transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(requestDocument), new StreamResult(writer));
                log.trace(writer.getBuffer().toString());
            }
            catch ( TransformerException e ) {
                log.warn("Failed to create XML trace output", e); //$NON-NLS-1$
            }

        }

        DavPropertyNameSet include = null;

        ElementIterator it = DomUtil.getChildren(root);
        int propfindTypeFound = 0;

        while ( it.hasNext() ) {
            Element child = it.nextElement();
            String nodeName = child.getLocalName();
            if ( NAMESPACE.getURI().equals(child.getNamespaceURI()) ) {
                if ( XML_PROP.equals(nodeName) ) {
                    if ( propfindTypeFound > 0 && this.propfindType == PROPFIND_BY_PROPERTY && this.propfindProps != null ) {
                        log.debug("Client with broken PROPFIND request"); //$NON-NLS-1$
                        this.propfindProps.addAll(new DavPropertyNameSet(child));
                    }
                    else {
                        this.propfindType = PROPFIND_BY_PROPERTY;
                        this.propfindProps = new DavPropertyNameSet(child);
                        propfindTypeFound += 1;
                    }
                }
                else if ( XML_PROPNAME.equals(nodeName) ) {
                    this.propfindType = PROPFIND_PROPERTY_NAMES;
                    propfindTypeFound += 1;
                }
                else if ( XML_ALLPROP.equals(nodeName) ) {
                    this.propfindType = PROPFIND_ALL_PROP;
                    propfindTypeFound += 1;
                }
                else if ( XML_INCLUDE.equals(nodeName) ) {
                    include = new DavPropertyNameSet();
                    ElementIterator pit = DomUtil.getChildren(child);
                    while ( pit.hasNext() ) {
                        include.add(DavPropertyName.createFromXml(pit.nextElement()));
                    }
                }
            }
        }

        if ( propfindTypeFound > 1 ) {
            log.info("Multiple top-level propfind instructions"); //$NON-NLS-1$
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Multiple top-level propfind instructions"); //$NON-NLS-1$
        }

        if ( include != null ) {
            if ( this.propfindType == PROPFIND_ALL_PROP ) {
                // special case: allprop with include extension
                this.propfindType = PROPFIND_ALL_PROP_INCLUDE;
                this.propfindProps = include;
            }
            else {
                throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "<include> goes only with <allprop>"); //$NON-NLS-1$

            }
        }
    }
}
