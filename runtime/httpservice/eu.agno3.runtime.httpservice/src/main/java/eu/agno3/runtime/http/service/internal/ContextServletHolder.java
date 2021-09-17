/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.servlet.Servlet;

import org.apache.log4j.Logger;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import eu.agno3.runtime.http.service.HttpConfigurationException;
import eu.agno3.runtime.http.service.servlet.ActiveServlet;
import eu.agno3.runtime.http.service.servlet.ServletConfig;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 */
class ContextServletHolder {

    private static final String SERVLET_CLASS = "servletClass"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ContextServletHolder.class);

    private Map<Servlet, ServletHolder> servletHolderMap = new HashMap<>();
    private Map<Servlet, ServletMapping> servletMap = new HashMap<>();
    private Map<Servlet, ServiceRegistration<ActiveServlet>> servletRegMap = new HashMap<>();
    private Queue<ServiceReference<Servlet>> deferServlets = new LinkedList<>();

    private ComponentContext componentContext;

    private ContextClassLoaderHolder contextClassLoader;


    /**
     * @param contextClassLoader
     * 
     */
    ContextServletHolder ( ContextClassLoaderHolder contextClassLoader ) {
        this.contextClassLoader = contextClassLoader;

    }


    protected synchronized void activate ( ComponentContext context, DefaultServletContextHandler contextHandler ) {
        this.componentContext = context;
        for ( ServiceReference<Servlet> deferred : this.deferServlets ) {
            Servlet servlet = this.componentContext.getBundleContext().getService(deferred);
            if ( servlet != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format(
                        "%s: Activating deferred Servlet %s", //$NON-NLS-1$
                        contextHandler.getContextName(),
                        servlet.getClass().getName()));
                }
                this.bindServlet(deferred, contextHandler);
            }
        }
    }


    protected synchronized void bindServlet ( ServiceReference<Servlet> ref, DefaultServletContextHandler contextHandler ) {

        String contextAttr = (String) ref.getProperty(ServletConfig.CONTEXT_ATTR);

        if ( !contextHandler.isActive() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Deferring registration of servlet until handler has been set up"); //$NON-NLS-1$
            }
            this.deferServlets.add(ref);
            return;
        }

        if ( contextAttr != null && !contextAttr.equals(contextHandler.getContextName()) ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format(
                    "ServletContextListener is restricted to context %s, this is %s, skip registration.", //$NON-NLS-1$
                    contextAttr,
                    contextHandler.getContextName()));
            }
            return;
        }

        bindServletActive(ref, contextHandler);
    }


    /**
     * @param ref
     */
    private void bindServletActive ( ServiceReference<Servlet> ref, DefaultServletContextHandler contextHandler ) {
        Servlet s = this.componentContext.getBundleContext().getService(ref);

        if ( s == null ) {
            log.warn("Failed to get servlet, already destroyed?"); //$NON-NLS-1$
            return;
        }

        if ( !ServletUtil.hasURLPattern(s, ref) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Servlet has no URL patterns configured, ignoring " + s.getClass().getName()); //$NON-NLS-1$
            }
            return;
        }

        ServletHolder holder = new ServletHolder(s);

        holder.setServletHandler(contextHandler.getServletHandler());

        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Registering servlet " + s.getClass().getName()); //$NON-NLS-1$
            }
            this.contextClassLoader.bindObject(s);
            s.init(new DefaultServletConfig(null, contextHandler));
            ServletMapping mapping = setupServletMapping(ref, s, holder);
            contextHandler.getServletHandler().addServlet(holder);
            this.servletMap.put(s, mapping);

            contextHandler.getServletHandler().addServletMapping(mapping);

            holder.start();
            this.servletHolderMap.put(s, holder);

            registerServlet(ref, s);
        }
        catch ( Exception e ) {
            log.error("Failed to start servlet", e); //$NON-NLS-1$
        }
    }


    /**
     * @param ref
     * @param s
     */
    private void registerServlet ( ServiceReference<Servlet> ref, Servlet s ) {
        Dictionary<String, Object> servletProperties = ServletUtil.cloneProperties(ref);
        servletProperties.put(SERVLET_CLASS, s.getClass().getName());
        this.servletRegMap.put(s, DsUtil.registerSafe(this.componentContext, ActiveServlet.class, new ActiveServlet(), servletProperties));
    }


    protected synchronized void unbindServlet ( ServiceReference<Servlet> ref, DefaultServletContextHandler contextHandler, boolean shutdown ) {
        Servlet s = this.componentContext.getBundleContext().getService(ref);
        ServletHolder holder = this.servletHolderMap.get(s);

        try {
            if ( holder != null ) {
                if ( this.servletRegMap.containsKey(s) ) {
                    ServiceRegistration<ActiveServlet> reg = this.servletRegMap.remove(s);
                    DsUtil.unregisterSafe(this.componentContext, reg);
                }
                List<ServletMapping> mappings = new ArrayList<>(Arrays.asList(contextHandler.getServletHandler().getServletMappings()));
                mappings.remove(this.servletMap.get(s));
                this.servletMap.remove(s);
                if ( !shutdown ) {
                    contextHandler.getServletHandler().setServletMappings(mappings.toArray(new ServletMapping[] {}));
                }
                holder.stop();
                this.servletHolderMap.remove(s);
                this.contextClassLoader.unbindObject(s);
                s.destroy();
            }
        }
        catch ( Exception e ) {
            log.error("Failed to stop servlet", e); //$NON-NLS-1$
        }
    }


    protected synchronized void updatedServlet ( ServiceReference<Servlet> ref, DefaultServletContextHandler contextHandler ) {

        Servlet s = this.componentContext.getBundleContext().getService(ref);
        ServletHolder holder = this.servletHolderMap.get(s);
        try {
            if ( this.servletRegMap.containsKey(s) ) {
                DsUtil.unregisterSafe(this.componentContext, this.servletRegMap.remove(s));
            }
            holder.stop();

            ServletMapping mapping = setupServletMapping(ref, s, holder);
            this.servletMap.put(s, mapping);
            List<ServletMapping> mappings = new ArrayList<>(Arrays.asList(contextHandler.getServletHandler().getServletMappings()));
            mappings.remove(this.servletMap.get(s));
            mappings.add(mapping);
            contextHandler.getServletHandler().setServletMappings(mappings.toArray(new ServletMapping[] {}));
            holder.start();
            this.registerServlet(ref, s);

        }
        catch ( Exception e ) {
            log.error("Failed to restart servlet", e); //$NON-NLS-1$
        }

    }


    /**
     * @param ref
     * @param s
     * @param holder
     * @return
     * @throws HttpConfigurationException
     */
    private static ServletMapping setupServletMapping ( ServiceReference<Servlet> ref, Servlet s, ServletHolder holder )
            throws HttpConfigurationException {
        String[] pathSpecs = ServletUtil.applyServletConfiguration(s, holder, ref);

        ServletMapping mapping = new ServletMapping();
        mapping.setPathSpecs(pathSpecs);
        mapping.setServletName(holder.getName());
        return mapping;
    }


    synchronized Collection<ServletHolder> getServlets () {
        return this.servletHolderMap.values();
    }


    synchronized ServletMapping getServletMapping ( Servlet s ) {
        return this.servletMap.get(s);
    }


    /**
     * @throws Exception
     * 
     */
    public void initialize () throws Exception {
        for ( ServletHolder h : this.servletHolderMap.values() ) {
            h.initialize();
        }
    }

}