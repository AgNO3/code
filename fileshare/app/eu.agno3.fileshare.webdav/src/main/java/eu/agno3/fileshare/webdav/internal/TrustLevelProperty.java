/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.05.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Locale;

import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.agno3.fileshare.model.TrustLevel;


/**
 * @author mbechler
 *
 */
public class TrustLevelProperty extends DefaultDavProperty<TrustLevel> {

    private Locale locale;


    /**
     * @param tl
     * @param l
     * 
     */
    public TrustLevelProperty ( TrustLevel tl, Locale l ) {
        super(Constants.TRUST_LEVEL, tl);
        this.locale = l;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml ( Document doc ) {
        String ns = this.getName().getNamespace().getURI();
        Element root = doc.createElementNS(ns, this.getName().getName());
        if ( !Locale.ROOT.equals(this.locale) ) {
            root.setAttributeNS(
                "http://www.w3.org/XML/1998/namespace", //$NON-NLS-1$
                "lang", //$NON-NLS-1$
                this.locale.getLanguage());
        }

        Element id = doc.createElementNS(ns, "id"); //$NON-NLS-1$
        id.setTextContent(this.getValue().getId());
        root.appendChild(id);

        Element color = doc.createElementNS(ns, "color"); //$NON-NLS-1$
        color.setTextContent(this.getValue().getColor());
        root.appendChild(color);

        Element title = doc.createElementNS(ns, "title"); //$NON-NLS-1$
        title.setTextContent(this.getValue().getTitle(this.locale));
        root.appendChild(title);

        Element order = doc.createElementNS(ns, "order"); //$NON-NLS-1$
        order.setTextContent(String.valueOf(this.getValue().getPriority()));
        root.appendChild(title);

        Element message = doc.createElementNS(ns, "message"); //$NON-NLS-1$
        message.setTextContent(this.getValue().getMessage(this.locale));
        root.appendChild(message);

        return root;
    }

}
