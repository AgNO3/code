/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.apache.jackrabbit.webdav.security.AclResource;
import org.apache.jackrabbit.webdav.server.AbstractWebdavServlet;
import org.apache.jackrabbit.webdav.util.CSRFUtil;
import org.apache.jackrabbit.webdav.version.DeltaVResource;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import eu.agno3.runtime.webdav.server.ExtendedDavSession;
import eu.agno3.runtime.webdav.server.PatchableResource;
import eu.agno3.runtime.webdav.server.StreamingReportResource;
import eu.agno3.runtime.webdav.server.StreamingWebdavResponse;
import eu.agno3.runtime.webdav.server.WebDAVServlet;
import eu.agno3.runtime.webdav.server.acl.ReplacedAclProperty;
import eu.agno3.runtime.webdav.server.acl.ReplacedAclResource;


/**
 * @author mbechler
 *
 */
public abstract class AbstractWebDAVServletImpl extends AbstractWebdavServlet implements WebDAVServlet {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(AbstractWebDAVServletImpl.class);
    private static final long serialVersionUID = 5115727625459640672L;
    private static final String PATCH = "PATCH"; //$NON-NLS-1$
    private CSRFUtil csrfUtil = new CSRFUtil(null);


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#setDavSessionProvider(org.apache.jackrabbit.webdav.DavSessionProvider)
     */
    @Override
    public void setDavSessionProvider ( DavSessionProvider sessionProvider ) {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#setLocatorFactory(org.apache.jackrabbit.webdav.DavLocatorFactory)
     */
    @Override
    public void setLocatorFactory ( DavLocatorFactory locatorFactory ) {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#setResourceFactory(org.apache.jackrabbit.webdav.DavResourceFactory)
     */
    @Override
    public void setResourceFactory ( DavResourceFactory resourceFactory ) {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#isPreconditionValid(org.apache.jackrabbit.webdav.WebdavRequest,
     *      org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    protected boolean isPreconditionValid ( WebdavRequest req, DavResource resource ) {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#doPropFind(org.apache.jackrabbit.webdav.WebdavRequest,
     *      org.apache.jackrabbit.webdav.WebdavResponse, org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    protected void doPropFind ( WebdavRequest request, WebdavResponse response, DavResource resource ) throws IOException, DavException {
        int depth = request.getDepth(DEPTH_INFINITY);

        if ( log.isTraceEnabled() ) {
            log.debug("Depth is " + depth); //$NON-NLS-1$
        }

        if ( depth > this.getMaxDepth() ) {
            throw new DavException(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }

        super.doPropFind(request, response, resource);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#execute(org.apache.jackrabbit.webdav.WebdavRequest,
     *      org.apache.jackrabbit.webdav.WebdavResponse, int, org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    protected boolean execute ( WebdavRequest request, WebdavResponse response, int method, DavResource resource )
            throws ServletException, IOException, DavException {

        if ( PATCH.equals(request.getMethod()) && resource instanceof PatchableResource ) {
            doPatch(request, response, (PatchableResource) resource);
            return true;
        }

        return super.execute(wrapRequest(request, response), wrapResponse(response, request), method, resource);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#doOptions(org.apache.jackrabbit.webdav.WebdavRequest,
     *      org.apache.jackrabbit.webdav.WebdavResponse, org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    protected void doOptions ( WebdavRequest req, WebdavResponse resp, DavResource res ) throws IOException, DavException {
        if ( res instanceof PatchableResource ) {
            PatchableResource patchable = (PatchableResource) res;
            if ( patchable.getAcceptablePatchFormats() != null ) {
                for ( String format : patchable.getAcceptablePatchFormats() ) {
                    resp.addHeader("Accept-Patch", format); //$NON-NLS-1$
                }
            }
        }
        super.doOptions(req, resp, res);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#doAcl(org.apache.jackrabbit.webdav.WebdavRequest,
     *      org.apache.jackrabbit.webdav.WebdavResponse, org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    protected void doAcl ( WebdavRequest request, WebdavResponse response, DavResource resource ) throws DavException, IOException {
        if ( ! ( resource instanceof ReplacedAclResource ) ) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        Document doc = request.getRequestDocument();
        if ( doc == null ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "ACL request requires a DAV:acl body."); //$NON-NLS-1$
        }
        ReplacedAclProperty acl = ReplacedAclProperty.createFromXml(doc.getDocumentElement());
        ( (ReplacedAclResource) resource ).alterAcl(acl);
    }


    /**
     * @param request
     * @param response
     * @param resource
     * @throws DavException
     */
    protected void doPatch ( WebdavRequest request, WebdavResponse response, PatchableResource resource ) throws DavException, IOException {
        resource.patch(getInputContext(request, request.getInputStream()));
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#doReport(org.apache.jackrabbit.webdav.WebdavRequest,
     *      org.apache.jackrabbit.webdav.WebdavResponse, org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    protected void doReport ( WebdavRequest request, WebdavResponse response, DavResource resource ) throws DavException, IOException {
        ReportInfo info = request.getReportInfo();
        Report report;
        if ( resource instanceof StreamingReportResource && response instanceof StreamingWebdavResponse ) {
            StreamingReportResource sr = (StreamingReportResource) resource;
            if ( sr.canStream(info) ) {
                sr.streamReport(info, (StreamingWebdavResponse) response);
                return;
            }
        }

        if ( resource instanceof DeltaVResource ) {
            report = ( (DeltaVResource) resource ).getReport(info);
        }
        else if ( resource instanceof AclResource ) {
            report = ( (AclResource) resource ).getReport(info);
        }
        else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        int statusCode = ( report.isMultiStatusReport() ) ? DavServletResponse.SC_MULTI_STATUS : HttpServletResponse.SC_OK;
        response.sendXmlResponse(report, statusCode);
    }


    @Override
    protected void service ( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        WebdavRequest webdavRequest = createRequest(request);
        // DeltaV requires 'Cache-Control' header for all methods except 'VERSION-CONTROL' and 'REPORT'.
        int methodCode = DavMethods.getMethodCode(request.getMethod());
        boolean noCache = DavMethods.isDeltaVMethod(webdavRequest)
                && ! ( DavMethods.DAV_VERSION_CONTROL == methodCode || DavMethods.DAV_REPORT == methodCode );
        WebdavResponse webdavResponse = createResponse(webdavRequest, response, noCache);
        try {
            // make sure there is a authenticated user
            if ( !getDavSessionProvider().attachSession(webdavRequest) ) {
                return;
            }

            // perform referrer host checks if CSRF protection is enabled
            if ( !this.csrfUtil.isValidRequest(webdavRequest) ) {
                log.warn("CSRF check failed for " + //$NON-NLS-1$
                        request.getHeader("Referer")); //$NON-NLS-1$
                webdavResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // check matching if=header for lock-token relevant operations
            DavResource resource = getResourceFactory().createResource(webdavRequest.getRequestLocator(), webdavRequest, webdavResponse);
            if ( !isPreconditionValid(webdavRequest, resource) ) {
                webdavResponse.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }

            if ( !execute(webdavRequest, webdavResponse, methodCode, resource) ) {
                super.service(request, response);
            }

        }
        catch ( DavException e ) {
            if ( e.getErrorCode() == HttpServletResponse.SC_UNAUTHORIZED ) {
                sendUnauthorized(webdavRequest, webdavResponse, e);
            }
            else {
                webdavResponse.sendError(e);
            }
        }
        finally {
            getDavSessionProvider().releaseSession(webdavRequest);
        }
    }


    /**
     * @param response
     * @param noCache
     * @return
     */
    protected WebdavResponseImpl createResponse ( WebdavRequest request, HttpServletResponse response, boolean noCache ) {
        return new DefaultWebdavResponseImpl(response, noCache);
    }


    /**
     * @param request
     * @return
     */
    protected WebdavRequestImpl createRequest ( HttpServletRequest request ) {
        return new DefaultWebdavRequestImpl(request, getLocatorFactory(request), isCreateAbsoluteURI());
    }


    /**
     * @param request
     * @return a locator factory for the request
     */
    protected DavLocatorFactory getLocatorFactory ( HttpServletRequest request ) {
        return this.getLocatorFactory();
    }


    /**
     * @param session
     * @return a locator factory
     */
    public DavLocatorFactory getLocatorFactory ( ExtendedDavSession session ) {
        return this.getLocatorFactory();
    }


    /**
     * @param request
     * @return wrapped request
     */
    protected WebdavRequest wrapRequest ( WebdavRequest request, WebdavResponse resp ) {
        return request;
    }


    /**
     * @param response
     * @return wrapped response
     */
    protected WebdavResponse wrapResponse ( WebdavResponse response, WebdavRequest req ) {
        return response;
    }


    /**
     * @return
     */
    protected int getMaxDepth () {
        return DEPTH_INFINITY;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#sendUnauthorized(org.apache.jackrabbit.webdav.WebdavRequest,
     *      org.apache.jackrabbit.webdav.WebdavResponse, org.apache.jackrabbit.webdav.DavException)
     */
    @Override
    protected void sendUnauthorized ( WebdavRequest request, WebdavResponse response, DavException error ) throws IOException {
        response.sendError(error);
    }

}
