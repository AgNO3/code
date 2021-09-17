/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 9, 2017 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.util.HttpDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.runtime.webdav.server.WriteReplaceProperty;


/**
 * @author mbechler
 *
 */
public class GrantDetailProperty extends DefaultDavProperty<Grant> implements DavProperty<Grant>, WriteReplaceProperty {

    private String creatorRef;
    private String targetRef;


    /**
     * @param value
     */
    public GrantDetailProperty ( Grant value ) {
        super(Constants.GRANT_DETAILS, value);
        this.creatorRef = SubjectsSubtreeProvider.getSubjectUrl(value.getCreator());
        if ( value instanceof SubjectGrant ) {
            this.targetRef = SubjectsSubtreeProvider.getSubjectUrl( ( (SubjectGrant) value ).getTarget());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.property.AbstractDavProperty#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml ( Document doc ) {
        String ns = getName().getNamespace().getURI();
        Element elem = doc.createElementNS(ns, getName().getName());
        Grant g = getValue();
        elem.setAttribute("id", g.getId().toString()); //$NON-NLS-1$
        elem.setAttribute("created", HttpDateFormat.creationDateFormat().format(g.getCreated().toDate())); //$NON-NLS-1$
        if ( g.getExpires() != null ) {
            elem.setAttribute("expires", HttpDateFormat.creationDateFormat().format(g.getExpires().toDate())); //$NON-NLS-1$
        }
        elem.setAttribute("permissions", String.valueOf(g.getPerms())); //$NON-NLS-1$
        elem.setAttribute("creator", this.creatorRef); //$NON-NLS-1$
        if ( this.targetRef != null ) {
            elem.setAttribute("target", this.targetRef); //$NON-NLS-1$
        }
        return elem;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.WriteReplaceProperty#writeReplaceProperty(org.apache.jackrabbit.webdav.DavResourceLocator,
     *      org.apache.jackrabbit.webdav.DavLocatorFactory)
     */
    @Override
    public DavProperty<?> writeReplaceProperty ( DavResourceLocator locator, DavLocatorFactory locatorFactory ) {
        this.creatorRef = locatorFactory.createResourceLocator(locator.getPrefix(), locator.getWorkspacePath(), this.creatorRef).getHref(false);
        if ( this.targetRef != null ) {
            this.targetRef = locatorFactory.createResourceLocator(locator.getPrefix(), locator.getWorkspacePath(), this.targetRef).getHref(false);
        }
        return this;
    }
}
