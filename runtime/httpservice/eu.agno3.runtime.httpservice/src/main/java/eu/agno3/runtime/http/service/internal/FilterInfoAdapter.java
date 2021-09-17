/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.Arrays;
import java.util.Collection;

import javax.servlet.Filter;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;

import eu.agno3.runtime.http.service.filter.FilterInfo;


/**
 * @author mbechler
 * 
 */
class FilterInfoAdapter implements FilterInfo {

    private DefaultServletContextHandler contextHandler;
    private ContextFilterHolder filterHolder;


    /**
     * 
     */
    FilterInfoAdapter ( DefaultServletContextHandler contextHandler, ContextFilterHolder holder ) {
        this.contextHandler = contextHandler;
        this.filterHolder = holder;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.filter.FilterInfo#getContextName()
     */
    @Override
    public String getContextName () {
        return this.contextHandler.getContextName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.filter.FilterInfo#getFilters()
     */
    @Override
    public Collection<FilterHolder> getFilters () {
        return this.filterHolder.getFilters();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.filter.FilterInfo#getFilterMappings()
     */
    @Override
    public Collection<FilterMapping> getFilterMappings () {
        return Arrays.asList(this.contextHandler.getServletHandler().getFilterMappings());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.filter.FilterInfo#getFilterMapping(javax.servlet.Filter)
     */
    @Override
    public FilterMapping getFilterMapping ( Filter f ) {
        return this.filterHolder.getFilterMapping(f);
    }

}
