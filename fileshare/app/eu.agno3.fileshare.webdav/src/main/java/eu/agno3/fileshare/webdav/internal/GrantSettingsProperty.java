/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.02.2017 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.util.HttpDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.agno3.fileshare.model.GrantType;


/**
 * @author mbechler
 *
 */
public class GrantSettingsProperty extends DefaultDavProperty<GrantSettings> {

    /**
     * @param defl
     * 
     */
    public GrantSettingsProperty ( GrantSettings defl ) {
        super(Constants.GRANT_SETTINGS, defl);
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

        GrantSettings settings = this.getValue();

        Element allowedElem = doc.createElementNS(ns, "a"); //$NON-NLS-1$
        allowedElem.setPrefix(prefix);
        for ( GrantType t : settings.getAllowedTypes() ) {
            allowedElem.appendChild(doc.createElementNS(ns, t.name()));
        }
        elem.appendChild(allowedElem);

        Element expireElem = doc.createElementNS(ns, "exp"); //$NON-NLS-1$
        expireElem.setPrefix(prefix);
        if ( settings.getDefaultExpire() != null ) {
            expireElem.setAttribute("default", HttpDateFormat.creationDateFormat().format(settings.getDefaultExpire().toDate())); //$NON-NLS-1$
        }
        if ( settings.getMaxExpire() != null ) {
            expireElem.setAttribute("max", HttpDateFormat.creationDateFormat().format(settings.getMaxExpire().toDate())); //$NON-NLS-1$
        }
        elem.appendChild(expireElem);

        Element pwElem = doc.createElementNS(ns, "pw"); //$NON-NLS-1$
        pwElem.setPrefix(prefix);
        if ( settings.isNoUserTokenPasswords() ) {
            pwElem.setAttribute("nochoice", Boolean.TRUE.toString()); //$NON-NLS-1$
        }
        if ( settings.isRequireTokenPassword() ) {
            pwElem.setAttribute("require", Boolean.TRUE.toString()); //$NON-NLS-1$
        }
        if ( settings.getMinTokenPasswordEntropy() > 0 ) {
            pwElem.setAttribute("minEntropy", String.valueOf(settings.getMinTokenPasswordEntropy())); //$NON-NLS-1$
        }

        elem.appendChild(pwElem);

        Element defPriv = doc.createElementNS(ns, "defPriv"); //$NON-NLS-1$
        defPriv.setPrefix(prefix);
        defPriv.appendChild(
            doc.createElementNS(settings.getDefaultPermissions().getNamespace().getURI(), settings.getDefaultPermissions().getName()));

        elem.appendChild(defPriv);

        if ( settings.isNotificationsAllowed() ) {
            Element allowNotify = doc.createElementNS(ns, "notifications"); //$NON-NLS-1$
            allowNotify.setPrefix(prefix);
            allowNotify.setTextContent(Boolean.toString(true));
            elem.appendChild(allowNotify);
        }

        if ( settings.getDefaultMailSubject() != null ) {
            Element defSubj = doc.createElementNS(ns, "defMailSubj"); //$NON-NLS-1$
            defSubj.setPrefix(prefix);
            defSubj.setTextContent(settings.getDefaultMailSubject());
            elem.appendChild(defSubj);
        }

        return elem;
    }
}
