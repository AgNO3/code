/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.io.OutputContextImpl;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.SimpleLockManager;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.search.QueryGrammerSet;
import org.apache.jackrabbit.webdav.search.SearchInfo;
import org.apache.jackrabbit.webdav.security.report.AclPrincipalReport;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.security.report.PrincipalSearchReport;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.DeltaVResource;
import org.apache.jackrabbit.webdav.version.SupportedMethodSetProperty;
import org.apache.jackrabbit.webdav.version.report.ExpandPropertyReport;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.version.report.SupportedReportSetProperty;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;

import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.DAVTreeProvider;
import eu.agno3.runtime.webdav.server.DavWrapperFactory;
import eu.agno3.runtime.webdav.server.ExtendedDavSession;
import eu.agno3.runtime.webdav.server.ExtendedStatus;
import eu.agno3.runtime.webdav.server.StreamingWebdavResponse;
import eu.agno3.runtime.webdav.server.WriteReplaceProperty;
import eu.agno3.runtime.webdav.server.acl.FixedAclPrincipalReport;
import eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport;
import eu.agno3.runtime.webdav.server.acl.PrincipalPropertySearchPropertySetReport;
import eu.agno3.runtime.webdav.server.acl.ReplacedAce;
import eu.agno3.runtime.webdav.server.acl.ReplacedAclProperty;
import eu.agno3.runtime.webdav.server.colsync.ColSyncReport;


/**
 * @author mbechler
 * @param <T>
 *            node identifier type
 *
 */
public class DefaultDavResourceFactory <T> implements DavResourceFactory, DavWrapperFactory<T> {

    /**
     * 
     */
    private static final String DAV_REPORT_PREFIX = "{DAV:}"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String ROOT = "/"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DefaultDavResourceFactory.class);

    private LockManager lockManager = new SimpleLockManager();

    private AbstractWebDAVServletImpl servlet;
    private DAVTreeProvider<T> treeProvider;


    /**
     * @param treeProvider
     * @param servlet
     * 
     */
    public DefaultDavResourceFactory ( DAVTreeProvider<T> treeProvider, AbstractWebDAVServletImpl servlet ) {
        this.treeProvider = treeProvider;
        this.servlet = servlet;
    }


    /**
     * @return the treeProvider
     */
    public DAVTreeProvider<T> getTreeProvider () {
        return this.treeProvider;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResourceFactory#createResource(org.apache.jackrabbit.webdav.DavResourceLocator,
     *      org.apache.jackrabbit.webdav.DavSession)
     */
    @Override
    public DavResourceWrapper<T> createResource ( DavResourceLocator locator, DavSession session ) throws DavException {
        if ( ! ( session instanceof ExtendedDavSession ) ) {
            throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return new DavResourceWrapper<>(getNode(locator), locator, this, (ExtendedDavSession) session, this.lockManager, false, null);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResourceFactory#createResource(org.apache.jackrabbit.webdav.DavResourceLocator,
     *      org.apache.jackrabbit.webdav.DavServletRequest, org.apache.jackrabbit.webdav.DavServletResponse)
     */
    @Override
    public DavResourceWrapper<T> createResource ( DavResourceLocator locator, DavServletRequest request, DavServletResponse response )
            throws DavException {
        if ( ! ( request.getDavSession() instanceof ExtendedDavSession ) ) {
            throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        DAVTreeNode<T> node = getNode(locator);
        boolean overrideIsCollection = false;
        if ( node == null ) {
            overrideIsCollection = DavMethods.isCreateCollectionRequest(request);
        }
        return new DavResourceWrapper<>(
            node,
            locator,
            this,
            (ExtendedDavSession) request.getDavSession(),
            this.lockManager,
            overrideIsCollection,
            null);
    }


    /**
     * @param context
     * @param child
     * @return a wrapper for the given node
     */
    @Override
    public DavResourceWrapper<T> getWrapperFor ( DavResourceWrapper<T> context, DAVTreeNode<T> child ) {
        DavResourceLocator locator;
        try {
            locator = this.servlet.getLocatorFactory(context.getSession()).createResourceLocator(
                context.getLocator().getPrefix(),
                context.getLocator().getWorkspacePath(),
                this.treeProvider.getAbsolutePath(child));
        }
        catch ( DavException e ) {
            log.warn("Failed to get resource path", e); //$NON-NLS-1$
            return null;
        }
        if ( log.isTraceEnabled() ) {
            log.trace("Get wrapper for " + locator.getResourcePath()); //$NON-NLS-1$
        }
        return new DavResourceWrapper<>(child, locator, this, context.getSession(), this.lockManager, false, null);
    }


    /**
     * @param locator
     * @return
     * @throws DavException
     */
    protected DAVTreeNode<T> getNode ( DavResourceLocator locator ) throws DavException {

        if ( StringUtils.isEmpty(locator.getResourcePath()) || "/".equals(locator.getResourcePath()) ) { //$NON-NLS-1$
            log.trace("Fetch root node"); //$NON-NLS-1$
            return this.treeProvider.getRootNode();
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Fetch node at " + locator.getResourcePath()); //$NON-NLS-1$
        }
        return this.treeProvider.getResourceWithPath(locator.getRepositoryPath());
    }


    /**
     * @param parentLocator
     * @param davTreeNode
     * @return
     */
    protected Collection<DAVTreeNode<T>> getNodeChildren ( DavResourceLocator parentLocator, DAVTreeNode<T> davTreeNode ) throws DavException {
        if ( log.isTraceEnabled() ) {
            log.trace("Get children of " + davTreeNode); //$NON-NLS-1$
        }

        return this.treeProvider.getNodeChildren(davTreeNode);
    }


    /**
     * 
     * @param node
     * @return a children iterator
     * @throws DavException
     */
    public DavResourceIterator getChildren ( DavResourceWrapper<T> node ) throws DavException {
        return new ResourceIteratorWrapper<>(this, node, getNodeChildren(node.getLocator(), node.getWrapped()));
    }


    /**
     * @param node
     * @return the parent or null if root
     * @throws DavException
     */
    public DavResourceWrapper<T> fetchParent ( DavResourceWrapper<T> node ) throws DavException {
        if ( node.getResourcePath() != null && !node.getResourcePath().equals(ROOT) ) {
            String parentPath = Text.getRelativeParent(node.getResourcePath(), 1);
            if ( StringUtils.isBlank(parentPath) ) {
                parentPath = ROOT;
            }

            DavResourceLocator loc = this.servlet.getLocatorFactory(node.getSession())
                    .createResourceLocator(node.getLocator().getPrefix(), node.getLocator().getWorkspacePath(), parentPath);

            return createResource(loc, node.getSession());
        }

        return null;
    }


    /**
     * @param parent
     * @param member
     * @param context
     * @throws DavException
     */
    public void create ( DavResourceWrapper<T> parent, DavResourceWrapper<T> member, InputContext context ) throws DavException {
        String createName = getLastPathSegment(member);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("create \"%s\" in %s", createName, parent.getHref())); //$NON-NLS-1$
        }

        DAVTreeNode<T> created;
        if ( member.isCollection() ) {
            created = this.treeProvider.createCollection(parent.getWrapped(), createName, context);
        }
        else {
            created = this.treeProvider.create(parent.getWrapped(), member.getWrapped(), createName, context);
        }

        if ( created != null ) {
            OutputContextImpl oc = new OutputContextImpl(WebUtils.getHttpResponse(SecurityUtils.getSubject()), null);
            setResourceHeaders(oc, created, false);
            this.treeProvider.addDynamicHeaders(created, oc);
        }
    }


    /**
     * @param member
     * @return
     * @throws DavException
     */
    private static <T> String getLastPathSegment ( DavResourceWrapper<T> member ) throws DavException {
        String createName = member.getResourcePath().substring(member.getResourcePath().lastIndexOf('/') + 1);
        try {
            createName = URLDecoder.decode(createName, UTF_8);
        }
        catch ( UnsupportedEncodingException e ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Illegal URI", e, null); //$NON-NLS-1$
        }
        return createName;
    }


    /**
     * @param parent
     * @param member
     * @throws DavException
     */
    public void remove ( DavResourceWrapper<T> parent, DavResourceWrapper<T> member ) throws DavException {
        if ( log.isDebugEnabled() ) {
            log.debug("remove " + member.getHref()); //$NON-NLS-1$
        }
        this.treeProvider.delete(member.getWrapped());
    }


    /**
     * @param item
     * @param context
     * @throws DavException
     */
    public void update ( DavResourceWrapper<T> item, InputContext context ) throws DavException {
        if ( log.isDebugEnabled() ) {
            log.debug("update " + item.getHref()); //$NON-NLS-1$
        }

        DAVTreeNode<T> updated = this.treeProvider.update(item.getWrapped(), context);
        if ( updated != null ) {
            OutputContextImpl oc = new OutputContextImpl(WebUtils.getHttpResponse(SecurityUtils.getSubject()), null);
            setResourceHeaders(oc, updated, false);
            this.treeProvider.addDynamicHeaders(updated, oc);
        }
    }


    /**
     * @param item
     * @param destination
     * @throws DavException
     */
    @SuppressWarnings ( "unchecked" )
    public void move ( DavResourceWrapper<T> item, DavResourceWrapper<T> destination ) throws DavException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("move %s to %s", item.getHref(), destination.getHref())); //$NON-NLS-1$
        }

        if ( destination.exists() && destination.isCollection() ) {
            log.debug("move to collection"); //$NON-NLS-1$
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        else if ( !destination.exists() ) {
            DavResourceWrapper<T> oldParent = (DavResourceWrapper<T>) item.getCollection();
            DavResourceWrapper<T> newParent = (DavResourceWrapper<T>) destination.getCollection();

            if ( oldParent != null && newParent != null ) {
                String targetName = getLastPathSegment(destination);
                if ( newParent.equals(oldParent) ) {
                    log.debug("Rename item"); //$NON-NLS-1$
                    this.treeProvider.rename(item.getWrapped(), targetName);
                }
                else {
                    this.treeProvider.move(item.getWrapped(), newParent.getWrapped(), targetName);
                }
                return;
            }
        }

        throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }


    /**
     * @param item
     * @param destination
     * @param shallow
     * @throws DavException
     */
    public void copy ( DavResourceWrapper<T> item, DavResourceWrapper<T> destination, boolean shallow ) throws DavException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "copy%s %s to %s", //$NON-NLS-1$
                shallow ? " shallow" : StringUtils.EMPTY, //$NON-NLS-1$
                item.getHref(),
                destination.getHref()));
        }

        throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }


    /**
     * @param davResourceWrapper
     * @param output
     * @throws IOException
     */
    public void spoolContents ( DavResourceWrapper<T> davResourceWrapper, OutputContext output ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("spoolContents " + davResourceWrapper.getHref()); //$NON-NLS-1$
        }

        if ( !davResourceWrapper.exists() ) {
            return;
        }

        DAVTreeNode<T> n = davResourceWrapper.getWrapped();
        setResourceHeaders(output, n, true);
        this.treeProvider.addDynamicHeaders(n, output);

        if ( output.getOutputStream() != null ) {
            this.treeProvider.writeOutput(n, output.getOutputStream());
        }

        log.debug("DONE spooling"); //$NON-NLS-1$
    }


    /**
     * @param output
     * @param n
     * @param sendContentLength
     */
    public static <T> void setResourceHeaders ( OutputContext output, DAVTreeNode<T> n, boolean sendContentLength ) {
        if ( n.getModificationTime() == null ) {
            output.setModificationTime(System.currentTimeMillis());
        }
        else {
            output.setModificationTime(n.getModificationTime().getMillis());
        }

        if ( sendContentLength ) {
            output.setContentType(n.getContentType());
            if ( n.getContentLength() != null ) {
                output.setContentLength(n.getContentLength());
            }
        }

        String eTag = makeETag(n);
        if ( eTag != null ) {
            output.setETag(eTag);
        }

        Map<String, String> hdrs = n.getCustomHeaders();
        if ( hdrs != null ) {
            for ( Entry<String, String> h : hdrs.entrySet() ) {
                output.setProperty(h.getKey(), h.getValue());
            }
        }

    }


    /**
     * @param davResourceWrapper
     * @param n
     * @return
     */
    private static <T> String makeETag ( DAVTreeNode<T> n ) {
        String eTag = n.getETag();
        if ( eTag == null && n.getId() != null ) {
            eTag = String.format(
                "\"%s-%s\"", //$NON-NLS-1$
                n.getId().toString(),
                n.getModificationTime() != null ? n.getModificationTime().getMillis() : System.currentTimeMillis());
        }
        return eTag;
    }


    /**
     * @param req
     * @param resource
     * @return whether the preconditions are met
     */
    public boolean checkPrecondition ( WebdavRequest req, DavResource resource ) {
        log.trace("isPreconditionValid"); //$NON-NLS-1$

        String ifMatch = req.getHeader("If-Match"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(ifMatch) ) {
            return checkIfMatch(req, resource, ifMatch);
        }

        String ifNoneMatch = req.getHeader("If-None-Match"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(ifNoneMatch) ) {
            return checkIfNoneMatch(req, resource, ifNoneMatch);
        }

        String ifModifiedSince = req.getHeader("If-Modified-Since"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(ifModifiedSince) ) {
            log.debug("Found If modified since"); //$NON-NLS-1$
        }

        String ifUnmodifiedSince = req.getHeader("If-Unmodified-Since"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(ifUnmodifiedSince) ) {
            log.debug("Found If unmodified since"); //$NON-NLS-1$
        }

        return true;
    }


    /**
     * @param req
     * @param resource
     * @param ifNoneMatch
     * @return
     */
    private static boolean checkIfNoneMatch ( WebdavRequest req, DavResource resource, String ifNoneMatch ) {
        log.debug("Found If none match " + ifNoneMatch); //$NON-NLS-1$

        if ( !resource.exists() ) {
            return true;
        }

        if ( "*".equals(ifNoneMatch.trim()) ) { //$NON-NLS-1$
            return false;
        }

        if ( ! ( resource instanceof DavResourceWrapper ) ) {
            return false;
        }
        String etag = ( (DavResourceWrapper<?>) resource ).getWrapped().getETag();

        if ( etag == null ) {
            return false;
        }

        Enumeration<String> toMatch = req.getHeaders("If-None-Match"); //$NON-NLS-1$
        while ( toMatch.hasMoreElements() ) {
            String[] matches = StringUtils.split(toMatch.nextElement(), ',');

            for ( String match : matches ) {
                match = match.trim();
                if ( etag.equals(match) ) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * @param req
     * @param resource
     * @param ifMatch
     * @return
     */
    private static boolean checkIfMatch ( WebdavRequest req, DavResource resource, String ifMatch ) {
        log.debug("Found If-Match header" + ifMatch); //$NON-NLS-1$
        if ( !resource.exists() ) {
            return false;
        }

        if ( "*".equals(ifMatch.trim()) ) { //$NON-NLS-1$
            return true;
        }

        if ( ! ( resource instanceof DavResourceWrapper ) ) {
            return false;
        }
        String etag = ( (DavResourceWrapper<?>) resource ).getWrapped().getETag();

        if ( etag == null ) {
            return false;
        }

        Enumeration<String> toMatch = req.getHeaders("If-Match"); //$NON-NLS-1$
        while ( toMatch.hasMoreElements() ) {
            String[] matches = StringUtils.split(toMatch.nextElement(), ',');

            for ( String match : matches ) {
                match = match.trim();
                if ( etag.equals(match) ) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * @param item
     * @param changeList
     * @return multi status
     * @throws DavException
     */
    public MultiStatusResponse alterProperties ( DavResourceWrapper<T> item, List<? extends PropEntry> changeList ) throws DavException {
        if ( log.isDebugEnabled() ) {
            log.debug("alterProperties " + item.getHref()); //$NON-NLS-1$

            for ( PropEntry e : changeList ) {
                if ( e instanceof DavProperty ) {
                    DavProperty<?> prop = (DavProperty<?>) e;
                    log.debug(String.format("Set property %s: %s", prop.getName(), prop.getValue())); //$NON-NLS-1$
                }
                else {
                    log.debug("Remove property " + ( (DavPropertyName) e ).getName()); //$NON-NLS-1$
                }
            }
        }
        ExtendedMultiStatusResponse msr = new ExtendedMultiStatusResponse(item.getHref(), null);
        Map<? extends PropEntry, ExtendedStatus> status = this.treeProvider.alterProperties(item.getWrapped(), changeList);
        for ( PropEntry propEntry : changeList ) {
            ExtendedStatus st = status.get(propEntry);
            if ( st == null ) {
                st = ExtendedMultiStatusResponse.OK_STATUS;
            }

            if ( propEntry instanceof DavProperty ) {
                msr.add( ( (DavProperty<?>) propEntry ).getName(), st);
            }
            else {
                msr.add((DavPropertyName) propEntry, st);

            }
        }
        return msr;
    }


    /**
     * @param item
     * @param acl
     * @throws DavException
     */
    public void alterAcl ( DavResourceWrapper<T> item, ReplacedAclProperty acl ) throws DavException {
        if ( log.isDebugEnabled() ) {
            log.debug("alterAcl " + item.getHref()); //$NON-NLS-1$
        }

        List<ReplacedAce> aces = new ArrayList<>();
        for ( ReplacedAce ace : acl.getValue() ) {
            aces.add(ace.relativize(item.getLocator()));
        }
        this.treeProvider.alterAcl(item.getWrapped(), new ReplacedAclProperty(aces));
    }


    /**
     * @param item
     * @param report
     * @return the acl report
     * @throws DavException
     */
    public Report getReport ( DavResourceWrapper<T> item, ReportInfo report ) throws DavException {
        String reportName = report.getReportName();
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("getReport %s on %s", reportName, item.getHref())); //$NON-NLS-1$
        }

        Report res = null;
        if ( item instanceof DeltaVResource ) {
            res = ReportType.getType(report).createReport((DeltaVResource) item, report);
        }
        if ( res == null && reportName.startsWith(DAV_REPORT_PREFIX) ) { // $NON-NLS-1$
            switch ( reportName.substring(6) ) {
            case AclPrincipalReport.REPORT_NAME:
                res = new FixedAclPrincipalReport();
                break;
            case PrincipalMatchReport.REPORT_NAME:
                PrincipalMatchReport pm = new PrincipalMatchReport();
                pm.init(item, report);
                pm.setResponses(wrapMultistatus(item, this.treeProvider.findPrincipalMatch(item.getWrapped(), pm), report));
                return pm;
            case PrincipalSearchReport.REPORT_NAME:
                FixedPrincipalSearchReport ps = new FixedPrincipalSearchReport();
                ps.init(item, report);
                ps.setResponses(wrapMultistatus(item, this.treeProvider.findPrincipalSearch(item.getWrapped(), ps), report));
                return ps;
            case PrincipalPropertySearchPropertySetReport.REPORT_NAME:
                PrincipalPropertySearchPropertySetReport pps = new PrincipalPropertySearchPropertySetReport();
                pps.init(item, report);
                pps.setSupportedProperties(this.treeProvider.getSupportedPrincipalSearchProperties(item.getWrapped()));
                return pps;
            case DeltaVConstants.XML_EXPAND_PROPERTY:
                res = new ExpandPropertyReport();
                break;
            }
        }
        if ( res != null ) {
            res.init(item, report);
        }
        else {
            throw new DavException(DavServletResponse.SC_UNPROCESSABLE_ENTITY);
        }
        return res;
    }


    /**
     * @param davResourceWrapper
     * @param info
     * @return whether the given report can be streamed
     */
    public boolean canStream ( DavResourceWrapper<T> davResourceWrapper, ReportInfo info ) {
        String reportName = info.getReportName();
        if ( reportName.startsWith(DAV_REPORT_PREFIX) ) { // $NON-NLS-1$
            switch ( reportName.substring(6) ) {
            case ColSyncReport.REPORT_NAME:
                return true;
            }
        }
        return false;
    }


    /**
     * @param item
     * @param info
     * @param response
     * @throws DavException
     * @throws IOException
     */
    public void streamReport ( DavResourceWrapper<T> item, ReportInfo info, StreamingWebdavResponse response ) throws DavException, IOException {
        String reportName = info.getReportName();
        if ( reportName.startsWith(DAV_REPORT_PREFIX) ) { // $NON-NLS-1$
            switch ( reportName.substring(6) ) {
            case ColSyncReport.REPORT_NAME:
                try ( ColSyncReport<T> cs = new ColSyncReport<>(item, response, this) ) {
                    try {
                        cs.init(item, info);
                        this.treeProvider.syncCollectionStream(item.getWrapped(), cs);
                        return;
                    }
                    catch ( DavException e ) {
                        cs.setFailed();
                        response.sendError(e);
                    }
                }
            }
        }
        throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
    }


    /**
     * @param result
     * @param report
     * @return
     * @throws DavException
     */
    private MultiStatusResponse[] wrapMultistatus ( DavResourceWrapper<T> context, Map<DAVTreeNode<T>, Status> result, ReportInfo report )
            throws DavException {
        MultiStatusResponse[] responses = new MultiStatusResponse[result.size()];
        int i = 0;
        for ( Entry<DAVTreeNode<T>, Status> node : result.entrySet() ) {
            responses[ i++ ] = wrapMultistatusResponse(context, report, node);
        }
        return responses;
    }


    /**
     * @param context
     * @param report
     * @param node
     * @return
     * @throws DavException
     */
    MultiStatusResponse wrapMultistatusResponse ( DavResourceWrapper<T> context, ReportInfo report, Entry<DAVTreeNode<T>, Status> node )
            throws DavException {
        MultiStatusResponse msr;
        if ( node.getValue().getStatusCode() == 200 && !report.getPropertyNameSet().isEmpty() ) {
            DavResource resource = getWrapperFor(context, node.getKey());
            msr = new MultiStatusResponse(resource, report.getPropertyNameSet());
        }
        else {
            DavResourceLocator locator = this.servlet.getLocatorFactory(context.getSession()).createResourceLocator(
                context.getLocator().getPrefix(),
                context.getLocator().getWorkspacePath(),
                this.treeProvider.getAbsolutePath(node.getKey()));
            msr = new MultiStatusResponse(locator.getHref(node.getKey().isCollection()), node.getValue(), null);
        }
        return msr;
    }


    /**
     * @param item
     * @param search
     * @return search result
     * @throws DavException
     */
    public MultiStatus search ( DavResourceWrapper<T> item, SearchInfo search ) throws DavException {
        if ( log.isDebugEnabled() ) {
            log.debug("search " + item.getHref()); //$NON-NLS-1$
        }
        return this.treeProvider.search(item.getWrapped(), search);
    }


    /**
     * 
     * @param item
     * @return the supported query grammars
     */
    public QueryGrammerSet getQueryGrammarSet ( DavResourceWrapper<T> item ) {
        if ( log.isDebugEnabled() ) {
            log.debug("getQueryGrammarSet " + item.getHref()); //$NON-NLS-1$
        }
        return this.treeProvider.getQueryGrammarSet(item.getWrapped());
    }


    /**
     * @param item
     * @param inputContext
     * @throws IOException
     * @throws DavException
     */
    public void patch ( DavResourceWrapper<T> item, InputContext inputContext ) throws DavException, IOException {
        this.treeProvider.patch(item.getWrapped(), inputContext);
    }


    /**
     * @param item
     * @return the acceptable patch formats
     */
    public Collection<String> getAcceptablePatchFormats ( DavResourceWrapper<T> item ) {
        return this.treeProvider.getAcceptablePatchFormats(item.getWrapped());
    }


    /**
     * @param item
     * @param name
     * @return the, potentially computed, property value
     */
    public DavProperty<?> getProperty ( DavResourceWrapper<T> item, DavPropertyName name ) {

        if ( DeltaVConstants.SUPPORTED_REPORT_SET.equals(name) ) {
            Set<ReportType> reports = new HashSet<>(item.getWrapped().getSupportedReports());
            reports.add(AclPrincipalReport.REPORT_TYPE);
            return new SupportedReportSetProperty(reports.toArray(new ReportType[reports.size()]));
        }

        if ( DeltaVConstants.SUPPORTED_METHOD_SET.equals(name) ) {
            Set<String> methods = new HashSet<>(item.getWrapped().getSupportedMethods());
            return new SupportedMethodSetProperty(methods.toArray(new String[methods.size()]));
        }

        DavProperty<?> prop = this.treeProvider.getDynamicProperty(item.getWrapped(), name);
        if ( prop == null ) {
            prop = item.getProperties().get(name);
        }
        if ( prop instanceof WriteReplaceProperty ) {
            return ( (WriteReplaceProperty) prop ).writeReplaceProperty(item.getLocator(), this.servlet.getLocatorFactory(item.getSession()));
        }
        return prop;
    }


    /**
     * @param item
     * @return the available property names
     */
    public DavPropertyName[] getPropertyNames ( DavResourceWrapper<T> item ) {
        Set<DavPropertyName> dynProperties = this.treeProvider.getSupportedDynamicProperties(item.getWrapped());
        if ( dynProperties == null ) {
            return item.getProperties().getPropertyNames();
        }
        dynProperties = new HashSet<>(dynProperties);
        dynProperties.addAll(Arrays.asList(item.getProperties().getPropertyNames()));
        return dynProperties.toArray(new DavPropertyName[dynProperties.size()]);
    }

}
