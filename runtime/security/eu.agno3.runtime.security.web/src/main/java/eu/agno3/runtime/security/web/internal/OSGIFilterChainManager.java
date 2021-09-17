/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.internal;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.security.web.SecurityFilter;
import eu.agno3.runtime.security.web.config.FilterChainContributor;
import eu.agno3.runtime.security.web.config.FilterChainEntry;


/**
 * @author mbechler
 * 
 */
@Component ( service = FilterChainManager.class, factory = OSGIFilterChainManager.FACTORY )
public class OSGIFilterChainManager extends DefaultFilterChainManager {

    private static final Logger log = Logger.getLogger(OSGIFilterChainManager.class);

    protected static final String FACTORY = "eu.agno3.runtime.security.web.internal.OSGIFilterChainManager"; //$NON-NLS-1$

    private SortedSet<FilterChainEntry> filterChains = new TreeSet<>();


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindFilter ( SecurityFilter filter ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Binding filter %s with name %s", filter.getClass().getName(), filter.getFilterName())); //$NON-NLS-1$
        }
        if ( this.getFilter(filter.getFilterName()) != null ) {
            log.error("Duplicate filter definition for name " + filter.getFilterName()); //$NON-NLS-1$
            return;
        }

        this.addFilter(filter.getFilterName(), filter, false, true);

    }


    protected synchronized void unbindFilter ( SecurityFilter filter ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unbinding filter %s with name %s", filter.getClass().getName(), filter.getFilterName())); //$NON-NLS-1$
        }
        this.getFilters().remove(filter.getFilterName());
        this.updateFilterChains();
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindFilterChainContributor ( FilterChainContributor contrib ) {
        this.filterChains.addAll(contrib.getChainContributions());
        this.updateFilterChains();
    }


    protected synchronized void unbindFilterChainContributor ( FilterChainContributor contrib ) {
        this.filterChains.removeAll(contrib.getChainContributions());
        this.updateFilterChains();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.shiro.web.filter.mgt.DefaultFilterChainManager#getChainNames()
     */
    @Override
    public synchronized Set<String> getChainNames () {
        return new ChainNameSet(Collections.unmodifiableSortedSet(this.filterChains));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.shiro.web.filter.mgt.DefaultFilterChainManager#getChain(java.lang.String)
     */
    @Override
    public NamedFilterList getChain ( String chainName ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Get chain " + chainName); //$NON-NLS-1$
        }
        NamedFilterList l = super.getChain(chainName);

        if ( l != null ) {
            for ( javax.servlet.Filter f : l ) {
                log.debug(f.toString());
            }
        }

        return l;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.shiro.web.filter.mgt.DefaultFilterChainManager#getFilterChains()
     */
    @Override
    public synchronized Map<String, NamedFilterList> getFilterChains () {
        return super.getFilterChains();
    }


    /**
     * 
     */
    private synchronized void updateFilterChains () {
        this.setFilterChains(new HashMap<String, NamedFilterList>());

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found %d filter chains:", this.filterChains.size())); //$NON-NLS-1$
        }

        Set<String> matchedPaths = new HashSet<>();

        for ( FilterChainEntry e : this.filterChains ) {
            try {
                setupFilterChain(matchedPaths, e);
            }
            catch ( Exception ex ) {
                log.error(String.format("Failed to setup security filter chain %s: %s", e.getPath(), e.getChainDefinition()), ex); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param matchedPaths
     * @param e
     */
    private void setupFilterChain ( Set<String> matchedPaths, FilterChainEntry e ) {
        if ( !matchedPaths.contains(e.getPath()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Adding filter chain on path %s with priority %d: %s", //$NON-NLS-1$ 
                    e.getPath(),
                    e.getPriority(),
                    e.getChainDefinition()));
            }
            this.createChain(e.getPath(), e.getChainDefinition());
            matchedPaths.add(e.getPath());
        }
    }

}
