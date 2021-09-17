/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2016 by mbechler
 */
package eu.agno3.runtime.db.transaction.internal;


import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class FixedGenericObjectPool <T> extends GenericObjectPool<T> {

    private static final Logger log = Logger.getLogger(FixedGenericObjectPool.class);


    /**
     * @param factory
     * @param config
     * @param abandonedConfig
     */
    public FixedGenericObjectPool ( PooledObjectFactory<T> factory, GenericObjectPoolConfig config, AbandonedConfig abandonedConfig ) {
        super(factory, config, abandonedConfig);
    }


    /**
     * @param factory
     * @param config
     */
    public FixedGenericObjectPool ( PooledObjectFactory<T> factory, GenericObjectPoolConfig config ) {
        super(factory, config);
    }


    /**
     * @param factory
     */
    public FixedGenericObjectPool ( PooledObjectFactory<T> factory ) {
        super(factory);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.impl.GenericObjectPool#borrowObject()
     */
    @Override
    public T borrowObject () throws Exception {
        T borrowObject = super.borrowObject();
        if ( log.isTraceEnabled() ) {
            log.trace("Borrow " + borrowObject); //$NON-NLS-1$
        }
        return borrowObject;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.impl.GenericObjectPool#returnObject(java.lang.Object)
     */
    @Override
    public void returnObject ( T obj ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Return " + obj); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() && obj instanceof XAPoolableManagedConnection ) {
            XAPoolableManagedConnection pc = (XAPoolableManagedConnection) obj;
            if ( !pc.isAvailable() ) {
                log.debug("On release: not available " + pc); //$NON-NLS-1$
            }

            if ( pc.isErroneous() ) {
                log.debug("On release: in error " + pc); //$NON-NLS-1$
            }
        }

        super.returnObject(obj);
    }
}
