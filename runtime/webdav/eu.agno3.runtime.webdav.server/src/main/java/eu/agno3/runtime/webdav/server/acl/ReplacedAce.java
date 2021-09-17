/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.acl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.security.Principal;
import org.apache.jackrabbit.webdav.security.Privilege;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public class ReplacedAce implements XmlSerializable {

    private final Principal principal;
    private final boolean invert;
    private final Privilege[] privileges;
    private final boolean grant;
    private final boolean isProtected;
    private final String inheritedHref;
    private boolean princIsCollection;
    private List<Element> extraElements = new ArrayList<>();


    /**
     * @param principal
     * @param invert
     * @param privileges
     * @param grant
     * @param isProtected
     * @param inheritedHref
     */
    public ReplacedAce ( Principal principal, boolean princIsCollection, boolean invert, Privilege[] privileges, boolean grant, boolean isProtected,
            String inheritedHref ) {
        if ( principal == null ) {
            throw new IllegalArgumentException("Cannot create a new ACE with 'null' principal."); //$NON-NLS-1$
        }
        if ( privileges == null || privileges.length == 0 ) {
            throw new IllegalArgumentException("Cannot create a new ACE: at least a single privilege must be specified."); //$NON-NLS-1$
        }
        this.princIsCollection = princIsCollection;
        this.principal = principal;
        this.invert = invert;
        this.privileges = privileges;
        this.grant = grant;
        this.isProtected = isProtected;
        this.inheritedHref = inheritedHref;
    }


    public ReplacedAce relativize ( DavResourceLocator loc ) {
        Principal princ = this.getPrincipal();
        if ( princ.getHref() != null ) {
            DavResourceLocator locator = loc.getFactory().createResourceLocator(loc.getPrefix(), princ.getHref());
            princ = Principal.getHrefPrincipal(locator.getResourcePath());
        }

        String inhHref = this.getInheritedHref();
        if ( !StringUtils.isBlank(inhHref) ) {
            DavResourceLocator locator = loc.getFactory().createResourceLocator(loc.getPrefix(), inhHref);
            inhHref = locator.getResourcePath();
        }
        ReplacedAce r = new ReplacedAce(
            princ,
            this.princIsCollection,
            this.isInvert(),
            this.getPrivileges(),
            this.isGrant(),
            this.isProtected(),
            inhHref);
        r.extraElements = new ArrayList<>(this.extraElements);
        return r;
    }


    /**
     * @return the principal
     */
    public Principal getPrincipal () {
        return this.principal;
    }


    public boolean isInvert () {
        return this.invert;
    }


    public Privilege[] getPrivileges () {
        return this.privileges;
    }


    public boolean isGrant () {
        return this.grant;
    }


    public boolean isDeny () {
        return !this.grant;
    }


    public boolean isProtected () {
        return this.isProtected;
    }


    public String getInheritedHref () {
        return this.inheritedHref;
    }


    public void addExtraContentElement ( Element elem ) {
        this.extraElements.add(elem);
    }


    /**
     * @return the extraElements
     */
    public Map<QName, Element> getExtraElements () {
        Map<QName, Element> elements = new HashMap<>();

        for ( Element element : this.extraElements ) {
            elements.put(new QName(element.getNamespaceURI(), element.getLocalName()), element);
        }

        return elements;
    }


    public ReplacedAce toACE ( DavResourceLocator loc, DavLocatorFactory lf ) {
        Principal replacePrinc = getPrincipal();
        if ( !StringUtils.isBlank(replacePrinc.getHref()) ) {
            String replacePrincHref = lf.createResourceLocator(loc.getPrefix(), loc.getWorkspacePath(), replacePrinc.getHref())
                    .getHref(this.princIsCollection);
            replacePrinc = Principal.getHrefPrincipal(replacePrincHref);
        }
        String inheritedFrom = this.inheritedHref;
        if ( inheritedFrom != null ) {
            inheritedFrom = lf.createResourceLocator(loc.getPrefix(), loc.getWorkspacePath(), inheritedFrom).getHref(true);
        }

        ReplacedAce r = new ReplacedAce(
            replacePrinc,
            this.princIsCollection,
            this.invert,
            this.privileges,
            this.isGrant(),
            this.isProtected,
            inheritedFrom);

        for ( Element element : this.extraElements ) {
            r.addExtraContentElement(element);
        }
        return r;
    }

    static final String XML_ACE = "ace"; //$NON-NLS-1$
    static final String XML_INVERT = "invert"; //$NON-NLS-1$
    static final String XML_GRANT = "grant"; //$NON-NLS-1$
    static final String XML_DENY = "deny"; //$NON-NLS-1$
    static final String XML_PROTECTED = "protected"; //$NON-NLS-1$
    static final String XML_INHERITED = "inherited"; //$NON-NLS-1$


    /**
     * @see XmlSerializable#toXml(Document)
     */
    @Override
    public Element toXml ( Document document ) {
        Element ace = DomUtil.createElement(document, XML_ACE, SecurityConstants.NAMESPACE);
        if ( this.invert ) {
            Element inv = DomUtil.addChildElement(ace, XML_INVERT, SecurityConstants.NAMESPACE);
            inv.appendChild(this.principal.toXml(document));
        }
        else {
            ace.appendChild(this.principal.toXml(document));
        }
        Element gd = DomUtil.addChildElement(ace, ( ( this.grant ) ? XML_GRANT : XML_DENY ), SecurityConstants.NAMESPACE);
        for ( Privilege privilege : this.privileges ) {
            gd.appendChild(privilege.toXml(document));
        }
        if ( this.isProtected ) {
            DomUtil.addChildElement(ace, XML_PROTECTED, SecurityConstants.NAMESPACE);
        }
        if ( this.inheritedHref != null ) {
            Element inh = DomUtil.addChildElement(ace, XML_INHERITED, SecurityConstants.NAMESPACE);
            inh.appendChild(DomUtil.hrefToXml(this.inheritedHref, document));
        }
        for ( Element element : this.extraElements ) {
            Element imported = (Element) document.importNode(element, true);
            ace.appendChild(imported);
        }
        return ace;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "%s%s%s: %s%s", //$NON-NLS-1$
            !this.grant ? "deny " : StringUtils.EMPTY, //$NON-NLS-1$
            this.invert ? "invert " : StringUtils.EMPTY, //$NON-NLS-1$
            this.principal.getHref() != null ? this.principal.getHref() : this.principal.getPropertyName(),
            Arrays.toString(this.privileges),
            this.isProtected ? " (protected)" : StringUtils.EMPTY); //$NON-NLS-1$
    }


    /**
     * @param aceElem
     * @return
     * @throws DavException
     */
    public static ReplacedAce createFromXml ( Element aceElement ) throws DavException {
        boolean invert = DomUtil.hasChildElement(aceElement, XML_INVERT, DavConstants.NAMESPACE);
        Element pe;
        if ( invert ) {
            Element invertE = DomUtil.getChildElement(aceElement, XML_INVERT, DavConstants.NAMESPACE);
            pe = DomUtil.getChildElement(invertE, Principal.XML_PRINCIPAL, DavConstants.NAMESPACE);
        }
        else {
            pe = DomUtil.getChildElement(aceElement, Principal.XML_PRINCIPAL, SecurityConstants.NAMESPACE);
        }
        Principal principal = Principal.createFromXml(pe);

        boolean grant = DomUtil.hasChildElement(aceElement, XML_GRANT, SecurityConstants.NAMESPACE);
        Element gdElem;
        if ( grant ) {
            gdElem = DomUtil.getChildElement(aceElement, XML_GRANT, DavConstants.NAMESPACE);
        }
        else {
            gdElem = DomUtil.getChildElement(aceElement, XML_DENY, DavConstants.NAMESPACE);
        }
        List<Privilege> privilegeList = new ArrayList<>();
        ElementIterator privIt = DomUtil.getChildren(gdElem, Privilege.XML_PRIVILEGE, DavConstants.NAMESPACE);
        while ( privIt.hasNext() ) {
            Privilege pv = Privilege.getPrivilege(privIt.nextElement());
            privilegeList.add(pv);
        }
        Privilege[] privileges = privilegeList.toArray(new Privilege[privilegeList.size()]);

        boolean isProtected = DomUtil.hasChildElement(aceElement, XML_PROTECTED, DavConstants.NAMESPACE);
        String inheritedHref = null;
        if ( DomUtil.hasChildElement(aceElement, XML_INHERITED, DavConstants.NAMESPACE) ) {
            Element inhE = DomUtil.getChildElement(aceElement, XML_INHERITED, DavConstants.NAMESPACE);
            inheritedHref = DomUtil.getChildText(inhE, DavConstants.XML_HREF, DavConstants.NAMESPACE);
        }

        ReplacedAce ace = new ReplacedAce(
            principal,
            principal.getHref() != null && principal.getHref().endsWith("/"), //$NON-NLS-1$
            invert,
            privileges,
            grant,
            isProtected,
            inheritedHref);

        ElementIterator otherIt = DomUtil.getChildren(aceElement);
        while ( otherIt.hasNext() ) {
            Element elem = otherIt.nextElement();
            if ( DavConstants.NAMESPACE.isSame(elem.getNamespaceURI()) ) {
                continue;
            }
            ace.addExtraContentElement(elem);
        }

        return ace;
    }
}
