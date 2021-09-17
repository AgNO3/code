/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2016 by mbechler
 */
package eu.agno3.runtime.db.transaction.internal;


import java.util.Objects;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class XAResourceWrapper implements XAResource {

    private static final Logger log = Logger.getLogger(XAResourceWrapper.class);

    private final XAResource delegate;


    /**
     * @param delegate
     * 
     */
    public XAResourceWrapper ( XAResource delegate ) {
        this.delegate = delegate;
        Objects.requireNonNull(delegate);
    }


    /**
     * @return the delegate
     */
    XAResource getDelegate () {
        return this.delegate;
    }


    @Override
    public void commit ( Xid xid, boolean onePhase ) throws XAException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("commit %s %s", xid, onePhase)); //$NON-NLS-1$
        }
        this.delegate.commit(xid, onePhase);
    }


    @Override
    public void rollback ( Xid xid ) throws XAException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("rollback %s", xid)); //$NON-NLS-1$
        }
        this.delegate.rollback(xid);
    }


    @Override
    public void start ( Xid xid, int flags ) throws XAException {
        if ( ( flags & XAResource.TMRESUME ) == XAResource.TMRESUME ) {
            log.debug("Ignoring resume"); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("start %s %d", xid, flags)); //$NON-NLS-1$
        }
        this.delegate.start(xid, flags);
    }


    @Override
    public void end ( Xid xid, int flags ) throws XAException {
        if ( ( flags & XAResource.TMSUSPEND ) == XAResource.TMSUSPEND ) {
            log.debug("Ignoring suspend"); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("end %s %d", xid, flags)); //$NON-NLS-1$
        }
        this.delegate.end(xid, flags);
    }


    @Override
    public void forget ( Xid xid ) throws XAException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("forget %s", xid)); //$NON-NLS-1$
        }
        this.delegate.forget(xid);
    }


    @Override
    public int prepare ( Xid xid ) throws XAException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("prepare %s", xid)); //$NON-NLS-1$
        }
        return this.delegate.prepare(xid);
    }


    @Override
    public Xid[] recover ( int flag ) throws XAException {
        if ( log.isTraceEnabled() ) {
            log.trace("Recover"); //$NON-NLS-1$
        }
        return this.delegate.recover(flag);
    }


    @Override
    public int getTransactionTimeout () throws XAException {
        return this.delegate.getTransactionTimeout();
    }


    @Override
    public boolean setTransactionTimeout ( int seconds ) throws XAException {
        if ( log.isDebugEnabled() ) {
            log.debug("Set transaction timeout " + seconds); //$NON-NLS-1$
        }
        return this.delegate.setTransactionTimeout(seconds);
    }


    @Override
    public boolean isSameRM ( XAResource xares ) throws XAException {
        XAResource actual = xares;
        if ( actual instanceof XAResourceWrapper ) {
            actual = ( (XAResourceWrapper) actual ).delegate;
        }
        return this.delegate.isSameRM(actual);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof XAResourceWrapper ) {
            return ( (XAResourceWrapper) obj ).delegate.equals(this.delegate);
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.delegate.hashCode();
    }

}
