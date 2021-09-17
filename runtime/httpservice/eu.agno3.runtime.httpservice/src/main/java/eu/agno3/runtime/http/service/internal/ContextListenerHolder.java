/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import eu.agno3.runtime.http.service.filter.FilterConfig;
import eu.agno3.runtime.http.service.handler.ContextHandlerConfig;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 */
class ContextListenerHolder implements ServletContextListener {

    private static final Logger log = Logger.getLogger(ContextListenerHolder.class);

    private Queue<ServiceReference<ServletContextListener>> deferContextListener = new LinkedList<>();
    private boolean contextAlreadyInitialized;
    private ComponentContext bundleContext;

    private ServiceRegistration<ServletContext> servletContextRegistration;

    private ContextClassLoaderHolder contextClassLoader;


    ContextListenerHolder ( ContextClassLoaderHolder contextClassLoader ) {
        this.contextClassLoader = contextClassLoader;
    }


    protected synchronized void activate ( ComponentContext ctx, DefaultServletContextHandler contextHandler ) {
        this.bundleContext = ctx;
        for ( ServiceReference<ServletContextListener> deferred : this.deferContextListener ) {
            ServletContextListener listener = this.bundleContext.getBundleContext().getService(deferred);
            if ( listener != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format(
                        "%s: Activating deferred ServletContextListener %s", //$NON-NLS-1$
                        contextHandler.getContextName(),
                        listener.getClass().getName()));
                }
                this.bindListener(deferred, contextHandler);
            }
        }
    }


    protected synchronized void bindListener ( ServiceReference<ServletContextListener> ref, DefaultServletContextHandler contextHandler ) {
        String contextAttr = (String) ref.getProperty(FilterConfig.CONTEXT_ATTR);

        if ( !contextHandler.isActive() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Deferring registration of ServletContextListener until handler has been set up"); //$NON-NLS-1$
            }
            this.deferContextListener.add(ref);
            return;
        }

        if ( !appliesToContext(contextHandler, contextAttr) ) {
            return;
        }

        bindListenerActive(ref, contextHandler);
    }


    /**
     * @param contextHandler
     * @param contextAttr
     */
    protected boolean appliesToContext ( DefaultServletContextHandler contextHandler, String contextAttr ) {
        if ( contextAttr != null && !contextAttr.equals(contextHandler.getContextName()) ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format(
                    "ServletContextListener is restricted to context %s, this is %s, skip registration.", //$NON-NLS-1$
                    contextAttr,
                    contextHandler.getContextName()));
            }
            return false;
        }

        return true;
    }


    /**
     * @param ref
     * @param contextHandler
     */
    private void bindListenerActive ( ServiceReference<ServletContextListener> ref, DefaultServletContextHandler contextHandler ) {
        ServletContextListener listener = this.bundleContext.getBundleContext().getService(ref);

        if ( listener != null ) {

            this.contextClassLoader.bindObject(listener);
            if ( log.isDebugEnabled() ) {
                log.debug("Registering ServletContextListener " + listener.getClass().getName()); //$NON-NLS-1$
            }
            contextHandler.addEventListener(listener);
            try {
                if ( this.contextAlreadyInitialized ) {
                    listener.contextInitialized(new ServletContextEvent(contextHandler.getServletContext()));
                }
            }
            catch ( Exception e ) {
                log.warn("Failure in contextInitialized listener:", e); //$NON-NLS-1$
            }
        }
    }


    protected synchronized void unbindListener ( ServiceReference<ServletContextListener> ref, DefaultServletContextHandler contextHandler,
            boolean shutdown ) {

        String contextAttr = (String) ref.getProperty(FilterConfig.CONTEXT_ATTR);

        if ( !appliesToContext(contextHandler, contextAttr) ) {
            return;
        }

        ServletContextListener listener = this.bundleContext.getBundleContext().getService(ref);
        if ( listener != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Unregistering ServletContextListener " + listener.getClass().getName()); //$NON-NLS-1$
            }
            contextHandler.removeEventListener(listener);

            try {
                if ( this.contextAlreadyInitialized ) {
                    listener.contextDestroyed(new ServletContextEvent(contextHandler.getServletContext()));
                }
            }
            catch ( Exception e ) {
                log.warn("Failure in contextDestroyed listener:", e); //$NON-NLS-1$
            }

            this.contextClassLoader.unbindObject(listener);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public synchronized void contextDestroyed ( ServletContextEvent ev ) {
        log.debug("contextDestroyed()"); //$NON-NLS-1$
        this.contextAlreadyInitialized = false;

        if ( this.servletContextRegistration != null ) {
            DsUtil.unregisterSafe(this.bundleContext, this.servletContextRegistration);
            this.servletContextRegistration = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized ( ServletContextEvent ev ) {

        if ( log.isDebugEnabled() ) {
            log.debug("contextInitialized() " + ev.getServletContext().getServletContextName()); //$NON-NLS-1$
        }

        ev.getServletContext().setAttribute("name", ev.getServletContext().getAttribute(ContextHandlerConfig.CONTEXT_NAME_ATTR)); //$NON-NLS-1$
        ev.getServletContext().setAttribute("displayName", ev.getServletContext().getServletContextName()); //$NON-NLS-1$
        this.contextAlreadyInitialized = true;

        Dictionary<String, Object> servletContextProperties = new Hashtable<>();

        Enumeration<String> keys = ev.getServletContext().getAttributeNames();

        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();
            servletContextProperties.put(key, ev.getServletContext().getAttribute(key));
        }

        this.servletContextRegistration = DsUtil
                .registerSafe(this.bundleContext, ServletContext.class, ev.getServletContext(), servletContextProperties);
    }

}