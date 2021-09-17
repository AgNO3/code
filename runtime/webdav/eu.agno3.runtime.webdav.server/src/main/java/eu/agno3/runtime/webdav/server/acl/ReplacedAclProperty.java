/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.acl;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.security.AclProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.w3c.dom.Element;

import eu.agno3.runtime.webdav.server.WriteReplaceProperty;


/**
 * @author mbechler
 *
 */
public class ReplacedAclProperty extends AbstractDavProperty<List<ReplacedAce>> implements WriteReplaceProperty {

    private List<ReplacedAce> aces;


    /**
     * @param aces
     */
    public ReplacedAclProperty ( List<ReplacedAce> aces ) {
        super(SecurityConstants.ACL, true);
        this.aces = aces;
    }


    /**
     * 
     * @param ace
     */
    public ReplacedAclProperty ( AclProperty ace ) {
        super(SecurityConstants.ACL, true);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.WriteReplaceProperty#writeReplaceProperty(org.apache.jackrabbit.webdav.DavResourceLocator,
     *      org.apache.jackrabbit.webdav.DavLocatorFactory)
     */
    @Override
    public DavProperty<?> writeReplaceProperty ( DavResourceLocator locator, DavLocatorFactory locatorFactory ) {
        List<ReplacedAce> a = new ArrayList<>();
        for ( ReplacedAce ace : getValue() ) {
            a.add(ace.toACE(locator, locatorFactory));
        }
        return new ReplacedAclProperty(a);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.property.DavProperty#getValue()
     */
    @Override
    public List<ReplacedAce> getValue () {
        return this.aces;
    }


    /**
     * @param aclElement
     * @return the parsed acl property
     * @throws DavException
     */
    public static ReplacedAclProperty createFromXml ( Element aclElement ) throws DavException {
        if ( !DomUtil.matches(aclElement, SecurityConstants.ACL.getName(), SecurityConstants.ACL.getNamespace()) ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "ACL request requires a DAV:acl body."); //$NON-NLS-1$
        }
        List<ReplacedAce> aces = new ArrayList<>();
        ElementIterator it = DomUtil.getChildren(aclElement, ReplacedAce.XML_ACE, SecurityConstants.NAMESPACE);
        while ( it.hasNext() ) {
            Element aceElem = it.next();
            aces.add(ReplacedAce.createFromXml(aceElem));
        }
        return new ReplacedAclProperty(aces);
    }

}
