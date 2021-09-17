/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.http.service.HttpConfigurationException;
import eu.agno3.runtime.http.service.filter.FilterInfo;
import eu.agno3.runtime.http.service.handler.BaseContextHandler;
import eu.agno3.runtime.http.service.handler.ContextHandlerConfig;
import eu.agno3.runtime.http.service.handler.ExtendedHandler;
import eu.agno3.runtime.http.service.logging.LoggingRequestLog;
import eu.agno3.runtime.http.service.servlet.ServletInfo;
import eu.agno3.runtime.http.service.session.SessionManagerFactory;
import eu.agno3.runtime.update.PlatformState;
import eu.agno3.runtime.update.PlatformStateListener;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    Handler.class, PlatformStateListener.class
}, property = {
    ContextHandlerConfig.DISPLAY_NAME_ATTR + "=Default Servlet Handler"
}, configurationPid = DefaultServletContextHandler.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class DefaultServletContextHandler extends ServletContextHandler implements ExtendedHandler, PlatformStateListener {

    /**
     * Configuration PID
     */
    public static final String PID = "httpservice.handler.servlet"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DefaultServletContextHandler.class);
    private ErrorHandler errorHandler;
    private SessionManagerFactory sessionManagerFactory;

    private String contextName;
    private boolean active = false;

    private final ContextClassLoaderHolder contextClassLoader = new ContextClassLoaderHolder();
    private ContextFilterHolder filterHolder = new ContextFilterHolder(this.contextClassLoader);
    private ServiceRegistration<FilterInfo> filterInfoRegistration;

    private ContextServletHolder servletHolder = new ContextServletHolder(this.contextClassLoader);
    private ServiceRegistration<ServletInfo> servletInfoRegistration;

    private ContextListenerHolder listenerHolder = new ContextListenerHolder(this.contextClassLoader);

    private boolean shutdown;
    private boolean updating;


    /**
     * 
     */
    public DefaultServletContextHandler () {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.handler.ExtendedHandler#getPriority()
     */
    @Override
    public float getPriority () {
        return 10f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.PlatformStateListener#stateChanged(eu.agno3.runtime.update.PlatformState)
     */
    @Override
    public void stateChanged ( PlatformState state ) {
        if ( state == PlatformState.STOPPING ) {
            this.shutdown = true;
        }
        else if ( state == PlatformState.UPDATING ) {
            this.updating = true;
        }
        else if ( state == PlatformState.STARTED ) {
            this.updating = false;
        }

    }


    /**
     * @return the shutdown
     */
    public boolean isShuttingDown () {
        return this.shutdown;
    }


    /**
     * @return the updating
     */
    public boolean isUpdating () {
        return this.updating;
    }


    @Activate
    @Modified
    protected synchronized void activate ( ComponentContext context ) throws HttpConfigurationException {
        this.contextName = (String) context.getProperties().get(ContextHandlerConfig.CONTEXT_NAME_ATTR);

        if ( this.contextName == null ) {
            throw new HttpConfigurationException("DefaultServletContextHandler has no name set"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Creating DefaultServletContextHandler for context " + this.contextName); //$NON-NLS-1$
        }
        this.setServletHandler(new ServletHandler());

        if ( this.sessionManagerFactory != null ) {
            log.debug("Setting up SessionManager"); //$NON-NLS-1$
            this.setSessionHandler(this.sessionManagerFactory.createSessionHandler(this.contextName));
        }
        else {
            log.info("No SessionManager available"); //$NON-NLS-1$
        }

        Dictionary<String, Object> infoProperties = new Hashtable<>();
        infoProperties.put(ContextHandlerConfig.CONTEXT_NAME_ATTR, this.contextName);

        try {
            this.setAttribute(ContextHandlerConfig.CONTEXT_NAME_ATTR, this.contextName);
            BaseContextHandler.configureContextHandler(this, context.getProperties(), this.contextClassLoader);
            RequestLogHandler reqLog = new RequestLogHandler();
            reqLog.setRequestLog(new LoggingRequestLog());
            this.insertHandler(reqLog);
            this.active = true;

            this.addEventListener(this.listenerHolder);
            this.listenerHolder.activate(context, this);

            this.filterHolder.activate(context, this);
            this.filterInfoRegistration = DsUtil
                    .registerSafe(context, FilterInfo.class, new FilterInfoAdapter(this, this.filterHolder), infoProperties);

            this.servletHolder.activate(context, this);
            this.servletInfoRegistration = DsUtil
                    .registerSafe(context, ServletInfo.class, new ServletInfoAdapter(this, this.servletHolder), infoProperties);
        }
        catch ( Exception e ) {
            log.error("Failed to configure DefaultServletContextHandler:", e); //$NON-NLS-1$
        }

    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        try {
            DsUtil.unregisterSafe(context, this.servletInfoRegistration);
            DsUtil.unregisterSafe(context, this.filterInfoRegistration);
            this.active = false;
            this.stop();
        }
        catch ( Exception e ) {
            log.warn("Failed to stop DefaultServletContextHandler:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.server.handler.ContextHandler#setServer(org.eclipse.jetty.server.Server)
     */
    @Override
    public void setServer ( Server server ) {
        super.setServer(server);

        if ( this.errorHandler != null ) {
            this.setErrorHandler(this.errorHandler);
        }
    }


    @Reference
    protected synchronized void bindErrorHandler ( ErrorHandler handler ) {
        this.errorHandler = handler;

        if ( this.getServer() != null ) {
            this.setErrorHandler(this.errorHandler);
        }
    }


    protected synchronized void unbindErrorHandler ( ErrorHandler handler ) {
        if ( this.errorHandler == handler ) {
            this.errorHandler = null;
            this.setErrorHandler(null);
        }
    }


    @Reference
    protected synchronized void setSessionManagerFactory ( SessionManagerFactory factory ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Binding SessionManagerFactory " + factory.getClass().getName()); //$NON-NLS-1$
        }
        this.sessionManagerFactory = factory;
    }


    protected synchronized void unsetSessionManagerFactory ( SessionManagerFactory factory ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding SessionManagerFactory " + factory.getClass().getName()); //$NON-NLS-1$
        }
        if ( this.sessionManagerFactory == factory ) {
            this.sessionManagerFactory = null;
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, service = Servlet.class, updated = "updatedServlet" )
    protected synchronized void bindServlet ( ServiceReference<Servlet> ref ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Binding servlet " + ref); //$NON-NLS-1$
        }
        this.servletHolder.bindServlet(ref, this);
    }


    protected synchronized void unbindServlet ( ServiceReference<Servlet> ref ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Binding servlet " + ref); //$NON-NLS-1$
        }
        this.servletHolder.unbindServlet(ref, this, this.shutdown);
    }


    protected synchronized void updatedServlet ( ServiceReference<Servlet> ref ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Updated servlet " + ref); //$NON-NLS-1$
        }
        this.servletHolder.updatedServlet(ref, this);
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, service = ServletContextListener.class )
    protected synchronized void bindListener ( ServiceReference<ServletContextListener> ref ) {
        this.listenerHolder.bindListener(ref, this);
    }


    protected synchronized void unbindListener ( ServiceReference<ServletContextListener> ref ) {
        this.listenerHolder.unbindListener(ref, this, this.shutdown);
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, service = Filter.class, updated = "updatedFilter" )
    protected synchronized void bindFilter ( ServiceReference<Filter> ref ) {
        this.filterHolder.bindFilter(ref, this);
    }


    protected synchronized void unbindFilter ( ServiceReference<Filter> ref ) {
        this.filterHolder.unbindFilter(ref, this, this.shutdown);
    }


    protected synchronized void updatedFilter ( ServiceReference<Filter> ref ) {
        this.filterHolder.updatedFilter(ref, this);

    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.server.handler.ContextHandler#doStart()
     */
    @Override
    protected void doStart () {
        try {
            getServletHandler().setServer(getServer());
            super.doStart();
        }
        catch ( Exception e ) {
            log.error("Context startup failed: ", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.servlet.ServletContextHandler#doStop()
     */
    @Override
    protected void doStop () throws Exception {
        try {
            super.doStop();
        }
        catch ( Exception e ) {
            log.error("Context shutdown failed: ", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the active
     */
    protected boolean isActive () {
        return this.active;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.handler.ExtendedHandler#getContextName()
     */
    @Override
    public String getContextName () {
        return this.contextName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.server.handler.ContextHandler#doHandle(java.lang.String, org.eclipse.jetty.server.Request,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doHandle ( String path, Request req, HttpServletRequest httpReq, HttpServletResponse httpResp ) throws IOException, ServletException {
        if ( !req.isHandled() ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Trying to handle request to %s using context %s", path, this.contextName)); //$NON-NLS-1$
            }

            super.doHandle(path, req, httpReq, httpResp);

            if ( req.isHandled() && log.isTraceEnabled() ) {
                log.trace("Request was handled by " + this.contextName); //$NON-NLS-1$
            }
        }
        else if ( log.isTraceEnabled() ) {
            log.trace("Request already handled"); //$NON-NLS-1$
        }

    }

}
