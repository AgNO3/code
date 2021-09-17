/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.07.2015 by mbechler
 */
package eu.agno3.runtime.http.service.handler;


import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = Handler.class, configurationPid = "http.defaultRedirect", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class WelcomeRedirectHandler extends ContextHandler implements ExtendedHandler {

    private static final Logger log = Logger.getLogger(WelcomeRedirectHandler.class);

    /**
     * 
     */
    private static final String ROOT = "/"; //$NON-NLS-1$
    private String[] virtualHosts;
    private String redirectTo;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.handler.ExtendedHandler#getPriority()
     */
    @Override
    public float getPriority () {
        return -10f;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.redirectTo = ConfigUtil.parseString(ctx.getProperties(), "redirectTo", null); //$NON-NLS-1$

        Set<String> vhosts = ConfigUtil.parseStringSet(ctx.getProperties(), "virtualHosts", null); //$NON-NLS-1$
        if ( vhosts == null ) {
            return;
        }
        this.virtualHosts = vhosts.toArray(new String[vhosts.size()]);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.handler.ExtendedHandler#getContextName()
     */
    @Override
    public String getContextName () {
        return "defaultRedirect"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.handler.ContextHandler#getContextPath()
     */
    @Override
    public String getContextPath () {
        return ROOT;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.handler.ContextHandler#getVirtualHosts()
     */
    @Override
    public String[] getVirtualHosts () {
        return this.virtualHosts;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.handler.ContextHandler#doHandle(java.lang.String, org.eclipse.jetty.server.Request,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doHandle ( String target, Request req, HttpServletRequest httpReq, HttpServletResponse httpResp )
            throws IOException, ServletException {
        if ( log.isTraceEnabled() ) {
            log.trace("Handling " + target); //$NON-NLS-1$
        }
        if ( ROOT.equals(target) && this.redirectTo != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Redirect to " + this.redirectTo); //$NON-NLS-1$
            }
            httpResp.sendRedirect(this.redirectTo);
            return;
        }
        super.doHandle(target, req, httpReq, httpResp);
    }
}
