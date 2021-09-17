/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2017 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.List;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author mbechler
 *
 */
public class AllowedSecurityLevelsProperty extends DefaultDavProperty<List<String>> implements DavProperty<List<String>> {

    /**
     * @param allowed
     */
    public AllowedSecurityLevelsProperty ( List<String> allowed ) {
        super(Constants.ALLOWED_SECURITY_LEVELS, allowed);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.property.AbstractDavProperty#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml ( Document doc ) {
        Element elem = getName().toXml(doc);
        String ns = Constants.AGNO3_NS.getURI();
        String prefix = elem.lookupPrefix(ns);
        for ( String level : getValue() ) {
            Element el = doc.createElementNS(ns, "level"); //$NON-NLS-1$
            el.setPrefix(prefix);
            el.setTextContent(level);
            elem.appendChild(el);
        }
        return elem;
    }
}
