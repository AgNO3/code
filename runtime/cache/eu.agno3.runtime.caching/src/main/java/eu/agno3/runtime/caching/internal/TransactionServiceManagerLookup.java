/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2014 by mbechler
 */
package eu.agno3.runtime.caching.internal;


import java.util.Properties;

import javax.cache.CacheException;
import javax.transaction.TransactionManager;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.transaction.TransactionService;

import net.sf.ehcache.transaction.manager.TransactionManagerLookup;
import net.sf.ehcache.transaction.xa.EhcacheXAResource;


/**
 * @author mbechler
 * 
 */
@Component ( service = TransactionManagerLookup.class, immediate = true )
public class TransactionServiceManagerLookup implements TransactionManagerLookup {

    /**
     * 
     */
    private static final String TS_NOT_INITIALIZED = "Transaction service not initialized"; //$NON-NLS-1$
    private static TransactionService ts;


    @Reference
    protected synchronized void setTransactionService ( TransactionService txs ) {
        ts = txs;
    }


    protected synchronized void unsetTransactionService ( TransactionService txs ) {
        if ( ts == txs ) {
            ts = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see net.sf.ehcache.transaction.manager.TransactionManagerLookup#init()
     */
    @Override
    public synchronized void init () {
        if ( ts == null ) {
            throw new CacheException(TS_NOT_INITIALIZED);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see net.sf.ehcache.transaction.manager.TransactionManagerLookup#getTransactionManager()
     */
    @Override
    public synchronized TransactionManager getTransactionManager () {
        if ( ts == null ) {
            throw new CacheException(TS_NOT_INITIALIZED);
        }
        return ts.getTransactionManager();
    }


    /**
     * {@inheritDoc}
     * 
     * @see net.sf.ehcache.transaction.manager.TransactionManagerLookup#register(net.sf.ehcache.transaction.xa.EhcacheXAResource,
     *      boolean)
     */
    @Override
    public void register ( EhcacheXAResource arg0, boolean arg1 ) {
        // nop
    }


    /**
     * {@inheritDoc}
     * 
     * @see net.sf.ehcache.transaction.manager.TransactionManagerLookup#setProperties(java.util.Properties)
     */
    @Override
    public void setProperties ( Properties arg0 ) {
        // nop
    }


    /**
     * {@inheritDoc}
     * 
     * @see net.sf.ehcache.transaction.manager.TransactionManagerLookup#unregister(net.sf.ehcache.transaction.xa.EhcacheXAResource,
     *      boolean)
     */
    @Override
    public void unregister ( EhcacheXAResource arg0, boolean arg1 ) {
        // nop
    }

}
