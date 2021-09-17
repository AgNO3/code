/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author mbechler
 *
 */
public class VerifiedString implements XmlSerializable {

    private String value;
    private boolean verified;


    /**
     * @param value
     * @param verified
     */
    public VerifiedString ( String value, boolean verified ) {
        this.value = value;
        this.verified = verified;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml ( Document doc ) {
        Element elem;
        if ( this.verified ) {
            elem = doc.createElementNS(Constants.AGNO3_NS.getURI(), "verified"); //$NON-NLS-1$
        }
        else {
            elem = doc.createElementNS(Constants.AGNO3_NS.getURI(), "unverified"); //$NON-NLS-1$
        }
        elem.setTextContent(this.value);
        return elem;
    }

}
