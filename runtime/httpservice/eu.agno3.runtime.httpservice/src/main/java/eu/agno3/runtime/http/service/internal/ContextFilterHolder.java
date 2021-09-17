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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;

import org.apache.log4j.Logger;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import eu.agno3.runtime.http.service.HttpConfigurationException;
import eu.agno3.runtime.http.service.filter.ActiveFilter;
import eu.agno3.runtime.http.service.filter.FilterConfig;
import eu.agno3.runtime.util.log.LogFormatter;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 */
class ContextFilterHolder {

    /**
     * 
     */
    private static final String FILTER_CLASS_PROPERTY = "filterClass"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ContextFilterHolder.class);

    private Map<Filter, FilterHolder> filterHolderMap = new HashMap<>();
    private SortedMap<Filter, FilterMapping> filterMap = new TreeMap<>(new FilterComparator());
    private Map<Filter, ServiceRegistration<ActiveFilter>> filterRegMap = new HashMap<>();
    private Queue<ServiceReference<Filter>> deferFilters = new LinkedList<>();
    private ComponentContext componentContext;

    private ContextClassLoaderHolder contextClassLoader;


    ContextFilterHolder ( ContextClassLoaderHolder contextClassLoader ) {
        this.contextClassLoader = contextClassLoader;
    }


    protected synchronized void activate ( ComponentContext context, DefaultServletContextHandler contextHandler ) {
        this.componentContext = context;

        for ( ServiceReference<Filter> deferred : this.deferFilters ) {
            Filter filter = this.componentContext.getBundleContext().getService(deferred);
            if ( filter != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format(
                        "%s: Activating deferred ServletFilter %s", //$NON-NLS-1$
                        contextHandler.getContextName(),
                        filter.getClass().getName()));
                }
                this.bindFilter(deferred, contextHandler);
            }
        }
    }


    protected synchronized void bindFilter ( ServiceReference<Filter> ref, DefaultServletContextHandler contextHandler ) {
        String contextAttr = (String) ref.getProperty(FilterConfig.CONTEXT_ATTR);

        if ( !contextHandler.isActive() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Deferring registration of filter until handler has been set up"); //$NON-NLS-1$
            }
            this.deferFilters.add(ref);
            return;
        }

        if ( contextAttr != null && !contextAttr.equals(contextHandler.getContextName()) ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format(
                    "ServletFilter is restricted to context %s, this is %s, skip registration.", //$NON-NLS-1$
                    contextAttr,
                    contextHandler.getContextName()));
            }
            return;
        }

        bindFilterActive(ref, contextHandler);

    }


    /**
     * @param ref
     * @param contextHandler
     */
    private void bindFilterActive ( ServiceReference<Filter> ref, DefaultServletContextHandler contextHandler ) {
        Filter f = this.componentContext.getBundleContext().getService(ref);
        FilterHolder holder = new FilterHolder(f);

        holder.setServletHandler(contextHandler.getServletHandler());

        try {
            this.contextClassLoader.bindObject(f);
            FilterMapping mapping = makeMapping(ref, f, holder);
            f.init(new DefaultFilterConfig(null, contextHandler));
            contextHandler.getServletHandler().addFilter(holder);

            this.filterMap.put(f, mapping);
            this.filterHolderMap.put(f, holder);

            FilterMapping[] mappings = this.filterMap.values().toArray(new FilterMapping[] {});

            if ( log.isDebugEnabled() ) {
                log.debug("Filter mappings: " + LogFormatter.format(mappings)); //$NON-NLS-1$
            }

            contextHandler.getServletHandler().setFilterMappings(mappings);

            holder.start();
            registerFilter(ref, contextHandler, f);
        }
        catch ( Exception e ) {
            log.error("Failed to enable filter", e); //$NON-NLS-1$
        }
    }


    /**
     * @param ref
     * @param f
     * @param holder
     * @return
     * @throws HttpConfigurationException
     */
    private static FilterMapping makeMapping ( ServiceReference<Filter> ref, Filter f, FilterHolder holder ) throws HttpConfigurationException {
        FilterMapping mapping = new FilterMapping();

        String[] pathSpecs = ServletUtil.applyFilterConfiguration(f, holder, ref);
        mapping.setPathSpecs(pathSpecs);
        mapping.setFilterName(holder.getName());

        configureMappingFromAnnotation(f, mapping);
        return mapping;
    }


    /**
     * @param ref
     * @param contextHandler
     * @param f
     */
    private void registerFilter ( ServiceReference<Filter> ref, DefaultServletContextHandler contextHandler, Filter f ) {
        Dictionary<String, Object> filterProperties = ServletUtil.cloneProperties(ref);
        filterProperties.put(FILTER_CLASS_PROPERTY, f.getClass().getName());
        filterProperties.put(FilterConfig.CONTEXT_ATTR, contextHandler.getContextName());
        this.filterRegMap.put(f, DsUtil.registerSafe(this.componentContext, ActiveFilter.class, new ActiveFilter(), filterProperties));
    }


    /**
     * @param f
     * @param mapping
     */
    private static void configureMappingFromAnnotation ( Filter f, FilterMapping mapping ) {
        if ( f.getClass().isAnnotationPresent(WebFilter.class) ) {
            WebFilter filterAnnot = f.getClass().getAnnotation(WebFilter.class);
            if ( filterAnnot.dispatcherTypes() != null ) {
                mapping.setDispatcherTypes(EnumSet.copyOf(Arrays.asList(filterAnnot.dispatcherTypes())));
            }

            if ( filterAnnot.servletNames() != null ) {
                mapping.setServletNames(filterAnnot.servletNames());
            }
        }
    }


    protected synchronized void unbindFilter ( ServiceReference<Filter> ref, DefaultServletContextHandler contextHandler, boolean shutdown ) {
        Filter f = this.componentContext.getBundleContext().getService(ref);
        FilterHolder holder = this.filterHolderMap.get(f);
        try {
            if ( holder != null ) {
                if ( this.filterRegMap.containsKey(f) ) {
                    ServiceRegistration<ActiveFilter> reg = this.filterRegMap.remove(f);
                    DsUtil.unregisterSafe(this.componentContext, reg);
                }
                List<FilterMapping> mappings = new ArrayList<>(Arrays.asList(contextHandler.getServletHandler().getFilterMappings()));
                mappings.remove(this.filterMap.get(f));
                this.filterMap.remove(f);
                if ( !shutdown ) {
                    try {
                        contextHandler.getServletHandler().setFilterMappings(mappings.toArray(new FilterMapping[] {}));
                    }
                    catch ( Exception e ) {
                        log.warn("Failed to reset filters", e); //$NON-NLS-1$
                    }
                }
                holder.stop();
                this.filterHolderMap.remove(f);
                this.contextClassLoader.unbindObject(f);
            }
        }
        catch ( Exception e ) {
            log.error("Failed to disable filter", e); //$NON-NLS-1$
        }
    }


    protected synchronized void updatedFilter ( ServiceReference<Filter> ref, DefaultServletContextHandler contextHandler ) {
        Filter f = this.componentContext.getBundleContext().getService(ref);
        FilterHolder holder = this.filterHolderMap.get(f);
        try {
            if ( this.filterRegMap.containsKey(f) ) {
                DsUtil.unregisterSafe(this.componentContext, this.filterRegMap.remove(f));
            }
            holder.stop();

            FilterMapping mapping = makeMapping(ref, f, holder);

            this.filterMap.put(f, mapping);
            List<FilterMapping> mappings = new ArrayList<>(Arrays.asList(contextHandler.getServletHandler().getFilterMappings()));
            mappings.remove(this.filterMap.get(f));
            mappings.add(mapping);
            contextHandler.getServletHandler().setFilterMappings(mappings.toArray(new FilterMapping[] {}));

            holder.start();
            registerFilter(ref, contextHandler, f);
        }
        catch ( Exception e ) {
            log.error("Failed to update filter", e); //$NON-NLS-1$
        }

    }


    Collection<FilterHolder> getFilters () {
        return this.filterHolderMap.values();
    }


    FilterMapping getFilterMapping ( Filter f ) {
        return this.filterMap.get(f);
    }


    /**
     * @throws Exception
     * 
     */
    public void initialize () throws Exception {
        for ( FilterHolder f : this.filterHolderMap.values() ) {
            f.initialize();
        }
    }

}
