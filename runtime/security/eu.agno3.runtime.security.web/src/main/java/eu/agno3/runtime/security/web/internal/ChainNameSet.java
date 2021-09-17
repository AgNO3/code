/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.internal;


import java.util.AbstractSet;
import java.util.Iterator;
import java.util.SortedSet;

import eu.agno3.runtime.security.web.config.FilterChainEntry;


class ChainNameSet extends AbstractSet<String> {

    private final SortedSet<FilterChainEntry> chainSet;


    /**
     * @param chainSet
     */
    public ChainNameSet ( SortedSet<FilterChainEntry> chainSet ) {
        this.chainSet = chainSet;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<String> iterator () {
        return new PathMappingIterator(this.chainSet.iterator());
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size () {
        return this.chainSet.size();
    }
}