/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.config.util;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.runtime.security.web.config.FilterChainBuilder;
import eu.agno3.runtime.security.web.config.FilterChainEntry;
import eu.agno3.runtime.security.web.config.FilterChainPriorities;
import eu.agno3.runtime.security.web.config.FilterChainSpecBuilder;


/**
 * @author mbechler
 * 
 */
public final class FilterChainBuilderImpl implements FilterChainBuilder {

    private Set<FilterChainEntry> entries = new HashSet<>();


    private FilterChainBuilderImpl () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainBuilder#withEntry(eu.agno3.runtime.security.web.config.FilterChainEntry)
     */
    @Override
    public FilterChainBuilder withEntry ( FilterChainEntry e ) {
        this.entries.add(e);
        return this;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainBuilder#withChain(int, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public FilterChainBuilder withChain ( int priority, String path, String filterSpec ) {
        return this.withEntry(new FilterChainEntryImpl(priority, path, filterSpec));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainBuilder#withChain(eu.agno3.runtime.security.web.config.FilterChainPriorities,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public FilterChainBuilder withChain ( FilterChainPriorities prio, String path, String filterSpec ) {
        return this.withChain(prio.getPriority(), path, filterSpec);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainBuilder#forPath(int, java.lang.String)
     */
    @Override
    public FilterChainSpecBuilder forPath ( int priority, String path ) {
        return new FilterChainSpecBuilderImpl(this, priority, path);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainBuilder#forPath(eu.agno3.runtime.security.web.config.FilterChainPriorities,
     *      java.lang.String)
     */
    @Override
    public FilterChainSpecBuilder forPath ( FilterChainPriorities prio, String path ) {
        return this.forPath(prio.getPriority(), path);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainBuilder#build()
     */
    @Override
    public Collection<FilterChainEntry> build () {
        return Collections.unmodifiableSet(this.entries);
    }


    /**
     * @return a new FilterChainBuilder instance
     */
    public static FilterChainBuilder create () {
        return new FilterChainBuilderImpl();
    }
}
