/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.IOException;
import java.util.Dictionary;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl;
import org.apache.log4j.Logger;
import org.apache.shiro.web.util.WebUtils;
import org.eclipse.jetty.io.EofException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.webdav.FileshareDAVTreeProvider;
import eu.agno3.fileshare.webdav.FileshareWebDAVServlet;
import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.webdav.server.ExtendedDavSession;
import eu.agno3.runtime.webdav.server.impl.AbstractWebDAVServletImpl;
import eu.agno3.runtime.webdav.server.impl.DefaultDavResourceFactory;
import eu.agno3.runtime.webdav.server.impl.DefaultWebdavRequestImpl;


/**
 * @author mbechler
 *
 */

@Component ( service = FileshareWebDAVServlet.class, configurationPid = "webdav", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class FileshareWebDAVServletImpl extends AbstractWebDAVServletImpl implements FileshareWebDAVServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 6483766493194458527L;

    private static final Logger log = Logger.getLogger(FileshareWebDAVServletImpl.class);

    private DavSessionProvider sessionProvider;
    private DavLocatorFactory locatorFactory = new LocatorFactoryImpl("/dav"); //$NON-NLS-1$
    private DavLocatorFactory ocLocatorFactory = new LocatorFactoryImpl("/remote.php/webdav"); //$NON-NLS-1$

    private FileshareDAVTreeProvider treeProvider;
    private DefaultDavResourceFactory<EntityKey> resourceFactory;

    private Boolean enabled;

    private UADetector uaDetector;


    @Reference
    protected synchronized void setTreeProvider ( FileshareDAVTreeProvider tp ) {
        this.treeProvider = tp;
    }


    protected synchronized void unsetTreeProvider ( FileshareDAVTreeProvider tp ) {
        if ( this.treeProvider == tp ) {
            this.treeProvider = null;
        }
    }


    @Reference
    protected synchronized void setUADetector ( UADetector uad ) {
        this.uaDetector = uad;
    }


    protected synchronized void unsetUADetector ( UADetector uad ) {
        if ( this.uaDetector == uad ) {
            this.uaDetector = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.resourceFactory = new DefaultDavResourceFactory<>(this.treeProvider, this);
        this.sessionProvider = new FileshareDavSessionProvider(this.uaDetector);
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.resourceFactory = null;
    }


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.enabled = ConfigUtil.parseBoolean(cfg, "enabled", false); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void service ( ServletRequest req, ServletResponse res ) throws ServletException, IOException {
        // forwarding to the error page here is not possible
        req.setAttribute("bypass-error-handler", true); //$NON-NLS-1$
        req.setAttribute("webdav.request", true); //$NON-NLS-1$

        if ( !this.enabled ) {
            ( (HttpServletResponse) res ).sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if ( this.resourceFactory == null ) {
            throw new ServletException("Not available"); //$NON-NLS-1$
        }

        HttpServletRequest httpReq = (HttpServletRequest) req;

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("%s %s", httpReq.getMethod(), httpReq.getRequestURI())); //$NON-NLS-1$
        }

        String ua = httpReq.getHeader("User-Agent"); //$NON-NLS-1$
        // TODO: change this back to REGULAR
        // DAVLayout layout = DAVLayout.NATIVE;
        DAVLayout layout = DAVLayout.REGULAR;
        if ( !StringUtils.isBlank(ua) && ua.startsWith(Constants.FS_UA_PREFIX) ) {
            log.trace("Native client"); //$NON-NLS-1$
            layout = DAVLayout.NATIVE;
        }
        else if ( WebUtils.getPathWithinApplication(httpReq).startsWith("/remote.php/webdav/") ) { //$NON-NLS-1$
            log.trace("Setting OC compatibility flag"); //$NON-NLS-1$
            layout = DAVLayout.OWNCLOUD;
        }
        httpReq.setAttribute(Constants.LAYOUT, layout.name());

        try {
            super.service(req, res);
        }
        catch ( EofException e ) {
            log.debug("Unexpected eof", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.impl.AbstractWebDAVServletImpl#createResponse(org.apache.jackrabbit.webdav.WebdavRequest,
     *      javax.servlet.http.HttpServletResponse, boolean)
     */
    @Override
    protected WebdavResponseImpl createResponse ( WebdavRequest request, HttpServletResponse response, boolean noCache ) {
        if ( getLayout(request) == DAVLayout.OWNCLOUD ) {
            return new OCCompatWebDAVResponse(response, noCache);
        }
        return super.createResponse(request, response, noCache);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.impl.AbstractWebDAVServletImpl#createRequest(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected WebdavRequestImpl createRequest ( HttpServletRequest request ) {
        if ( getLayout(request) == DAVLayout.OWNCLOUD ) {
            return new DefaultWebdavRequestImpl(request, getLocatorFactory(request), false);
        }
        return super.createRequest(request);
    }


    private static DAVLayout getLayout ( HttpServletRequest request ) {
        String layout = (String) request.getAttribute(Constants.LAYOUT);
        if ( layout == null ) {
            return DAVLayout.REGULAR;
        }
        return DAVLayout.valueOf(layout);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#getLocatorFactory()
     */
    @Override
    public DavLocatorFactory getLocatorFactory () {
        return this.locatorFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.impl.AbstractWebDAVServletImpl#getLocatorFactory(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected DavLocatorFactory getLocatorFactory ( HttpServletRequest request ) {
        if ( request != null && getLayout(request) == DAVLayout.OWNCLOUD ) {
            return this.ocLocatorFactory;
        }

        return super.getLocatorFactory(request);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.impl.AbstractWebDAVServletImpl#getLocatorFactory(eu.agno3.runtime.webdav.server.ExtendedDavSession)
     */
    @Override
    public DavLocatorFactory getLocatorFactory ( ExtendedDavSession session ) {
        if ( session != null && DAVLayout.OWNCLOUD.name().equals(session.getAttribute(Constants.LAYOUT)) ) {
            return this.ocLocatorFactory;
        }

        return super.getLocatorFactory(session);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#getDavSessionProvider()
     */
    @Override
    public DavSessionProvider getDavSessionProvider () {
        return this.sessionProvider;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#getResourceFactory()
     */
    @Override
    public DavResourceFactory getResourceFactory () {
        return this.resourceFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#isPreconditionValid(org.apache.jackrabbit.webdav.WebdavRequest,
     *      org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    protected boolean isPreconditionValid ( WebdavRequest req, DavResource resource ) {
        return this.resourceFactory.checkPrecondition(req, resource);
    }

}
