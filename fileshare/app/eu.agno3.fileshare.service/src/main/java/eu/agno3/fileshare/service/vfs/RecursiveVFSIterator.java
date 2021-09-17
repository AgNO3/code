/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2016 by mbechler
 */
package eu.agno3.fileshare.service.vfs;


import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.runtime.util.iter.ClosableIterator;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class RecursiveVFSIterator <T extends AbstractVFS> implements ClosableIterator<VFSEntity> {

    private static final Logger log = Logger.getLogger(RecursiveVFSIterator.class);

    private AbstractVFSContext<T> ctx;

    private Deque<VFSContainerEntity> toVisit = new LinkedList<>();
    private Iterator<? extends VFSEntity> currentIterator;

    private VFSEntity next;


    /**
     * @param ctx
     * @param root
     */
    public RecursiveVFSIterator ( AbstractVFSContext<T> ctx, VFSContainerEntity root ) {
        this.ctx = ctx;
        if ( root != null ) {
            this.toVisit.add(root);
            this.next = getNext();
        }
    }


    /**
     * @return
     * 
     */
    private VFSEntity getNext () {
        if ( this.currentIterator != null && this.currentIterator.hasNext() ) {
            VFSEntity e = this.currentIterator.next();
            if ( e instanceof VFSContainerEntity ) {
                this.toVisit.add((VFSContainerEntity) e);
            }
            return e;
        }

        try {
            while ( !this.toVisit.isEmpty() ) {
                VFSContainerEntity pop = this.toVisit.pop();
                this.currentIterator = this.ctx.getChildren(pop).iterator();

                if ( this.currentIterator.hasNext() ) {
                    VFSEntity e = this.currentIterator.next();
                    if ( e instanceof VFSContainerEntity ) {
                        this.toVisit.add((VFSContainerEntity) e);
                    }
                    return e;
                }
            }
        }
        catch ( FileshareException e ) {
            log.warn("Failed to enumerate VFS", e); //$NON-NLS-1$
            this.next = null;
            return null;
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        return this.next != null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public VFSEntity next () {
        VFSEntity e = this.next;
        this.next = getNext();
        return e;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.util.iter.ClosableIterator#close()
     */
    @Override
    public void close () {}

}
