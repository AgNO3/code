/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.acl;


import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author mbechler
 *
 */
public class PrincipalSearchProperty implements XmlSerializable {

    private DavPropertyName prop;
    private String description = StringUtils.EMPTY;


    /**
     * @param prop
     * @param desc
     * 
     */
    public PrincipalSearchProperty ( DavPropertyName prop, String desc ) {
        this.prop = prop;
        this.description = desc;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml ( Document doc ) {
        Element root = doc.createElementNS(DavConstants.NAMESPACE.getURI(), "principal-search-property"); //$NON-NLS-1$
        Element propElem = doc.createElementNS(DavConstants.NAMESPACE.getURI(), DavConstants.XML_PROP);
        propElem.appendChild(this.prop.toXml(doc));
        root.appendChild(propElem);
        Element descElem = doc.createElementNS(DavConstants.NAMESPACE.getURI(), "description"); //$NON-NLS-1$
        descElem.setTextContent(this.description);
        root.appendChild(descElem);
        return root;
    }

}
