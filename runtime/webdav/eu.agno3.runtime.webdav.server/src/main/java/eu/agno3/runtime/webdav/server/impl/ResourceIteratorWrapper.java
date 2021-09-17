/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.util.Collection;
import java.util.Iterator;

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;

import eu.agno3.runtime.webdav.server.DAVTreeNode;


/**
 * @author mbechler
 * @param <T>
 *            node identifier type
 *
 */
public class ResourceIteratorWrapper <T> implements DavResourceIterator {

    private DefaultDavResourceFactory<T> factory;
    private Collection<DAVTreeNode<T>> nodeChildren;
    private Iterator<DAVTreeNode<T>> it;
    private DavResourceWrapper<T> parent;


    /**
     * @param factory
     * @param parent
     * @param nodeChildren
     */
    public ResourceIteratorWrapper ( DefaultDavResourceFactory<T> factory, DavResourceWrapper<T> parent, Collection<DAVTreeNode<T>> nodeChildren ) {
        this.factory = factory;
        this.parent = parent;
        this.it = nodeChildren.iterator();
        this.nodeChildren = nodeChildren;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        return this.it != null && this.it.hasNext();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public DavResource next () {
        DAVTreeNode<T> next = this.it.next();
        return this.factory.getWrapperFor(this.parent, next);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResourceIterator#nextResource()
     */
    @Override
    public DavResource nextResource () {
        return next();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResourceIterator#size()
     */
    @Override
    public int size () {
        return this.nodeChildren.size();
    }

}
