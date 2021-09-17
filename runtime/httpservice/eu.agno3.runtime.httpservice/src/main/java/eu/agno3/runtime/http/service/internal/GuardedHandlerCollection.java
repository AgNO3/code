/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.ArrayUtil;

import eu.agno3.runtime.http.service.ReverseProxyConfig;


/**
 * @author mbechler
 *
 */
public class GuardedHandlerCollection extends ContextHandlerCollection {

    private static final String CACHE_CONTROL = "Cache-Control"; //$NON-NLS-1$
    private static final String PUBLIC = "public"; //$NON-NLS-1$

    private static final String CROSSDOMAIN_XML = "/crossdomain.xml"; //$NON-NLS-1$
    private static final String CLIENTACCESSPOLICY_XML = "/clientaccesspolicy.xml"; //$NON-NLS-1$
    private static final String ROBOTS_TXT = "/robots.txt"; //$NON-NLS-1$

    private static final String[] X_ROBOT_SUPPORT_BOTS = {
        "User-Agent: googlebot\n", //$NON-NLS-1$
        "User-Agent: googlebot-image\n", //$NON-NLS-1$
        "User-Agent: yahoo-slurp\n", //$NON-NLS-1$
        "User-Agent: yahoo-mmcrawler\n", //$NON-NLS-1$
        "User-Agent: bingbot\n" //$NON-NLS-1$
    };

    private static final Logger log = Logger.getLogger(GuardedHandlerCollection.class);

    private GuardHandlerImpl guardHandler;


    /**
     * @param guardHandler
     *            the guardHandler to set
     */
    public void setGuardHandler ( GuardHandlerImpl guardHandler ) {
        this.guardHandler = guardHandler;
    }


    /**
     * @return the guardHandler
     */
    public GuardHandlerImpl getGuardHandler () {
        return this.guardHandler;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.handler.ContextHandlerCollection#mapContexts()
     */
    @Override
    public void mapContexts () {
        super.mapContexts();
    }


    @Override
    public void addHandler ( Handler handler ) {
        Handler[] ordered = ArrayUtil.addToArray(getHandlers(), handler, Handler.class);
        Arrays.sort(ordered, new HandlerComparator());
        if ( log.isDebugEnabled() ) {
            log.debug("Handlers are " + Arrays.toString(ordered)); //$NON-NLS-1$
        }
        setHandlers(ordered);
    }


    /* ------------------------------------------------------------ */
    @Override
    public void removeHandler ( Handler handler ) {
        Handler[] handlers = getHandlers();

        if ( handlers != null && handlers.length > 0 ) {
            Handler[] ordered = ArrayUtil.removeFromArray(handlers, handler);
            Arrays.sort(ordered, new HandlerComparator());
            if ( log.isDebugEnabled() ) {
                log.debug("Handlers are " + Arrays.toString(ordered)); //$NON-NLS-1$
            }
            setHandlers(ordered);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.handler.HandlerCollection#handle(java.lang.String,
     *      org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void handle ( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response )
            throws IOException, ServletException {

        if ( "TRACE".equals(request.getMethod()) ) { //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        if ( !HostUtil.validateHostHeader(request.getServerName()) ) {
            // host header is dangerous as it's reflected in request.getServerName
            if ( log.isDebugEnabled() ) {
                log.debug("Rejecting host header " + request.getServerName()); //$NON-NLS-1$
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        HttpServletRequest wrappedReq = request;
        HttpServletResponse wrappedResp = response;
        HttpConfiguration hc = baseRequest.getHttpChannel().getHttpConfiguration();
        if ( hc instanceof ExtendedHttpConfiguration ) {
            ExtendedHttpConfiguration eh = (ExtendedHttpConfiguration) hc;
            HttpConnectorFactory hcf = eh.getHttpConnectorFactory();
            ReverseProxyConfig reverseProxyConfig = hcf.getReverseProxyConfig();

            if ( reverseProxyConfig != null ) {
                log.trace("Behind reverse proxy, wrapping request"); //$NON-NLS-1$
                wrappedReq = reverseProxyConfig.wrapRequest(wrappedReq);
                wrappedResp = reverseProxyConfig.wrapResponse(wrappedResp, wrappedReq);
            }
        }

        GuardHandlerImpl handler = getGuardHandler();

        if ( handler != null ) {
            handler.handle(target, baseRequest, wrappedReq, wrappedResp);
            if ( baseRequest.isHandled() ) {
                return;
            }
        }

        // this doesn't allow the other handlers to override these, is that needed?
        if ( customHandling(target, baseRequest, wrappedReq, wrappedResp) ) {
            return;
        }

        super.handle(target, baseRequest, wrappedReq, wrappedResp);

    }


    private static boolean customHandling ( String target, Request req, HttpServletRequest httpReq, HttpServletResponse httpResp )
            throws IOException {

        // this is quite a mess:
        // - some bots can only prevented from crawling (maybe not even from indexing at all) using the robots.txt
        // - other bots (e.g. google) can only prevented from indexing if they are allowed to crawl, and they will index
        // if they find an external link

        if ( ROBOTS_TXT.equals(target) ) { // $NON-NLS-1$
            handleRobots(httpResp);
            req.setHandled(true);
            return true;
        }

        if ( CROSSDOMAIN_XML.equals(target) ) {
            handleCrossdomain(httpReq, httpResp);
            req.setHandled(true);
            return true;
        }

        if ( CLIENTACCESSPOLICY_XML.equals(target) ) {
            handleClientPolicy(httpReq, httpResp);
            req.setHandled(true);
            return true;
        }

        return false;
    }


    /**
     * @param response
     * @throws IOException
     */
    private static void handleRobots ( HttpServletResponse response ) throws IOException {
        response.setContentType("text/plain; charset=utf-8"); //$NON-NLS-1$
        response.setHeader(
            CACHE_CONTROL, // $NON-NLS-1$
            "public"); //$NON-NLS-1$
        response.setStatus(200);
        try ( Writer w = response.getWriter() ) {

            if ( X_ROBOT_SUPPORT_BOTS.length > 0 ) {
                for ( String robotSupportUA : X_ROBOT_SUPPORT_BOTS ) {
                    w.write(robotSupportUA);
                }
                w.write("Disallow: \n"); //$NON-NLS-1$
                w.write('\n');
            }

            w.write("User-agent: *\n"); //$NON-NLS-1$
            w.write("Disallow: /\n"); //$NON-NLS-1$
        }
    }


    /**
     * @param req
     * @param resp
     * @throws IOException
     */
    private static void handleClientPolicy ( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
        resp.setHeader(CACHE_CONTROL, PUBLIC);
        resp.setContentType("application/xml; charset=utf-8"); //$NON-NLS-1$
        resp.setStatus(200);
        try ( Writer w = resp.getWriter() ) {
            w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"); //$NON-NLS-1$
            w.write("<access-policy>\n"); //$NON-NLS-1$
            w.write("<cross-domain-access>\n"); //$NON-NLS-1$
            w.write("</cross-domain-access>\n"); //$NON-NLS-1$
            w.write("</access-policy>\n"); //$NON-NLS-1$
        }

    }


    /**
     * @param req
     * @param resp
     * @throws IOException
     */
    private static void handleCrossdomain ( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
        resp.setHeader(CACHE_CONTROL, PUBLIC);
        resp.setContentType("text/x-cross-domain-policy; charset=utf-8"); //$NON-NLS-1$
        resp.setStatus(200);
        try ( Writer w = resp.getWriter() ) {
            w.write("<?xml version=\"1.0\"?>\n"); //$NON-NLS-1$
            w.write("<!DOCTYPE cross-domain-policy SYSTEM \"http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd\">\n"); //$NON-NLS-1$
            w.write("<cross-domain-policy>\n"); //$NON-NLS-1$
            w.write("<site-control permitted-cross-domain-policies=\"none\"/>\n"); //$NON-NLS-1$
            w.write("</cross-domain-policy>"); //$NON-NLS-1$
        }

    }

}
