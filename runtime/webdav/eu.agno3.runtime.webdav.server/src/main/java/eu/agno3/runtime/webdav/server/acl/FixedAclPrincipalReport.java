/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.acl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.security.report.AbstractSecurityReport;
import org.apache.jackrabbit.webdav.security.report.AclPrincipalReport;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;

import eu.agno3.runtime.webdav.server.ResolvedHrefProperty;


/**
 * @author mbechler
 *
 */
public class FixedAclPrincipalReport extends AbstractSecurityReport {

    /**
     * @see Report#init(DavResource, ReportInfo)
     */
    @Override
    public void init ( DavResource resource, ReportInfo info ) throws DavException {
        super.init(resource, info);
        // build the DAV:responses objects.
        DavProperty<?> acl = resource.getProperty(SecurityConstants.ACL);
        if ( ! ( acl instanceof ReplacedAclProperty ) ) {
            throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DAV:acl property expected."); //$NON-NLS-1$
        }

        if ( info.getPropertyNameSet().isEmpty() ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Missing properties"); //$NON-NLS-1$ ss
        }

        DavResourceLocator loc = resource.getLocator();
        Map<String, MultiStatusResponse> respMap = new HashMap<>();
        List<ReplacedAce> list = ( (ReplacedAclProperty) acl ).getValue();
        for ( ReplacedAce ace : list ) {
            String href = ace.getPrincipal().getHref();
            if ( href == null || respMap.containsKey(href) ) {

                if ( ace.getPrincipal().getPropertyName() != null ) {
                    DavProperty<?> refProperty = resource.getProperty(ace.getPrincipal().getPropertyName());

                    if ( refProperty instanceof HrefProperty && ( (HrefProperty) refProperty ).getHrefs().size() == 1 ) {
                        href = ( (HrefProperty) refProperty ).getHrefs().get(0);
                    }
                    else if ( refProperty instanceof ResolvedHrefProperty && ( (ResolvedHrefProperty) refProperty ).getHrefs().size() == 1 ) {
                        href = ( (ResolvedHrefProperty) refProperty ).getHrefs().get(0);
                    }
                    else {
                        continue;
                    }
                }
                else {
                    // ignore non-href principals and principals that have been listed before
                    continue;
                }
            }
            // href-principal that has not been found before
            DavResourceLocator princLocator = loc.getFactory().createResourceLocator(loc.getPrefix(), href);
            DavResource principalResource = resource.getFactory().createResource(princLocator, resource.getSession());
            if ( !principalResource.exists() ) {
                respMap.put(
                    href,
                    new MultiStatusResponse(href, principalResource.exists() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND));
            }
            else {
                respMap.put(href, new MultiStatusResponse(principalResource, info.getPropertyNameSet()));
            }
        }
        this.responses = respMap.values().toArray(new MultiStatusResponse[respMap.size()]);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.version.report.Report#getType()
     */
    @Override
    public ReportType getType () {
        return AclPrincipalReport.REPORT_TYPE;
    }

}
