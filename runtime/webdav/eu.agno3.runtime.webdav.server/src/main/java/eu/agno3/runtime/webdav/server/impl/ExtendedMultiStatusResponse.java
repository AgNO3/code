/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2017 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.PropContainer;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.agno3.runtime.webdav.server.ExtendedStatus;


/**
 * 
 * Output only variant of MultiStatusResponse that supports additional status information
 * 
 * @author mbechler
 *
 */
public class ExtendedMultiStatusResponse extends MultiStatusResponse {

    /**
     * 
     */
    public static final ExtendedStatus OK_STATUS = new ExtendedStatus(HttpServletResponse.SC_OK);
    private static final String XML_ERROR = "error"; //$NON-NLS-1$
    private final Map<ExtendedStatus, PropContainer> statusMap = new HashMap<>();


    /**
     * @param href
     * @param responseDescription
     */
    public ExtendedMultiStatusResponse ( String href, String responseDescription ) {
        super(href, responseDescription);
    }


    /**
     * @param document
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml ( Document document ) {
        Element response = DomUtil.createElement(document, XML_RESPONSE, NAMESPACE);
        // add '<href>'
        response.appendChild(DomUtil.hrefToXml(getHref(), document));

        // add '<propstat>' elements
        Iterator<ExtendedStatus> iter = this.statusMap.keySet().iterator();
        while ( iter.hasNext() ) {
            ExtendedStatus st = iter.next();
            PropContainer propCont = this.statusMap.get(st);

            Element propstat = DomUtil.createElement(document, XML_PROPSTAT, NAMESPACE);
            propstat.appendChild(propCont.toXml(document));
            propstat.appendChild(st.toXml(document));

            if ( st.getError() != null ) {
                Element error;
                if ( DomUtil.matches(st.getError(), XML_ERROR, DavConstants.NAMESPACE) ) {
                    error = (Element) document.importNode(st.getError(), true);
                }
                else {
                    error = DomUtil.createElement(document, XML_ERROR, DavConstants.NAMESPACE);
                    error.appendChild(document.importNode(st.getError(), true));
                }
                propstat.appendChild(error);
            }

            response.appendChild(propstat);
        }

        // add the optional '<responsedescription>' element
        String description = getResponseDescription();
        if ( description != null ) {
            Element desc = DomUtil.createElement(document, XML_RESPONSEDESCRIPTION, NAMESPACE);
            DomUtil.setText(desc, description);
            response.appendChild(desc);
        }
        return response;
    }


    /**
     * Adds a property to this response '200' propstat set.
     *
     * @param property
     *            the property to add
     */
    @Override
    public void add ( DavProperty<?> property ) {
        PropContainer status200 = getPropContainer(OK_STATUS, false);
        status200.addContent(property);
    }


    /**
     * Adds a property name to this response '200' propstat set.
     *
     * @param propertyName
     *            the property name to add
     */
    @Override
    public void add ( DavPropertyName propertyName ) {
        PropContainer status200 = getPropContainer(OK_STATUS, true);
        status200.addContent(propertyName);
    }


    /**
     * Adds a property to this response
     *
     * @param property
     *            the property to add
     * @param status
     *            the status of the response set to select
     */
    @Override
    public void add ( DavProperty<?> property, int status ) {
        PropContainer propCont = getPropContainer(new ExtendedStatus(status), false);
        propCont.addContent(property);
    }


    /**
     * Adds a property name to this response
     *
     * @param propertyName
     *            the property name to add
     * @param status
     *            the status of the response set to select
     */
    @Override
    public void add ( DavPropertyName propertyName, int status ) {
        PropContainer propCont = getPropContainer(new ExtendedStatus(status), true);
        propCont.addContent(propertyName);
    }


    /**
     * Adds a property to this response
     * 
     * @param property
     * @param status
     */
    public void add ( DavProperty<?> property, ExtendedStatus status ) {
        PropContainer propCont = getPropContainer(status, false);
        propCont.addContent(property);
    }


    /**
     * Adds a property name to this response
     *
     * @param propertyName
     *            the property name to add
     * @param status
     *            the status of the response set to select
     */
    public void add ( DavPropertyName propertyName, ExtendedStatus status ) {
        PropContainer propCont = getPropContainer(status, true);
        propCont.addContent(propertyName);
    }


    /**
     * @param status
     * @return
     */
    private PropContainer getPropContainer ( ExtendedStatus status, boolean forNames ) {
        PropContainer propContainer;
        Object entry = this.statusMap.get(status);
        if ( entry == null ) {
            if ( forNames ) {
                propContainer = new DavPropertyNameSet();
            }
            else {
                propContainer = new DavPropertySet();
            }
            this.statusMap.put(status, propContainer);
        }
        else {
            propContainer = (PropContainer) entry;
        }
        return propContainer;
    }

}
