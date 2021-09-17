/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.02.2015 by mbechler
 */
package eu.agno3.runtime.db.transaction.internal;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.ObjectName;

import org.apache.commons.dbcp2.Constants;
import org.apache.commons.dbcp2.DelegatingPreparedStatement;
import org.apache.commons.dbcp2.PStmtKey;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingConnection;
import org.apache.commons.dbcp2.managed.PoolableManagedConnection;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.dbcp2.managed.XAConnectionFactory;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.log4j.Logger;

import com.atomikos.datasource.xa.XATransactionalResource;


/**
 * 
 * Copied from commons DBCP
 * 
 * 
 * 
 * @author mbechler
 *
 */
public class FixedPoolableManagedConnectionFactory extends PoolableConnectionFactory {

    private static final Logger log = Logger.getLogger(FixedPoolableManagedConnectionFactory.class);

    private final AtomicLong connectionIndex = new AtomicLong(0);


    /**
     * @param connFactory
     * @param dataSourceJmxName
     * @param resource
     */
    public FixedPoolableManagedConnectionFactory ( XAConnectionFactory connFactory, ObjectName dataSourceJmxName, XATransactionalResource resource ) {
        super(connFactory, dataSourceJmxName);
        this.transactionRegistry = connFactory.getTransactionRegistry();
        this.jdbcResource = resource;
        this.classLoader = this.getClass().getClassLoader();
    }


    /**
     * @param connFactory
     * @param resource
     * @param dataSourceJmxName
     * @param cl
     */
    public FixedPoolableManagedConnectionFactory ( XAConnectionFactory connFactory, XATransactionalResource resource, ObjectName dataSourceJmxName,
            ClassLoader cl ) {
        super(connFactory, dataSourceJmxName);
        this.jdbcResource = resource;
        this.transactionRegistry = connFactory.getTransactionRegistry();
        this.classLoader = cl;
    }

    /** Transaction registry associated with connections created by this factory */
    private final TransactionRegistry transactionRegistry;

    /**
     * Classloader to use while initializing pools
     */
    private ClassLoader classLoader;

    private XATransactionalResource jdbcResource;


    /**
     * @return the transactionRegistry
     */
    public TransactionRegistry getTransactionRegistry () {
        return this.transactionRegistry;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.PoolableConnectionFactory#validateConnection(org.apache.commons.dbcp2.PoolableConnection)
     */
    @Override
    public void validateConnection ( PoolableConnection conn ) throws SQLException {
        try {
            if ( log.isTraceEnabled() ) {
                log.trace("Validating connection " + System.identityHashCode(conn)); //$NON-NLS-1$
            }
            XAPoolableManagedConnection c = (XAPoolableManagedConnection) conn;
            if ( c.isErroneous() ) {

            }
            super.validateConnection(conn);
        }
        catch ( SQLException e ) {
            log.warn("Failed to validate connection", e); //$NON-NLS-1$
            throw e;
        }
    }


    /**
     * Uses the configured XAConnectionFactory to create a {@link PoolableManagedConnection}.
     * Throws <code>IllegalStateException</code> if the connection factory returns null.
     * Also initializes the connection using configured initialization sql (if provided)
     * and sets up a prepared statement pool associated with the PoolableManagedConnection
     * if statement pooling is enabled.
     */
    @SuppressWarnings ( "resource" )
    @Override
    public PooledObject<PoolableConnection> makeObject () throws Exception {
        Connection conn = getConnectionFactory().createConnection();
        if ( conn == null ) {
            throw new IllegalStateException("Connection factory returned null from createConnection"); //$NON-NLS-1$
        }
        initializeConnection(conn);
        long connIndex = this.connectionIndex.getAndIncrement();

        if ( getPoolStatements() ) {
            conn = new PoolingConnection(conn);
            GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
            config.setMaxTotalPerKey(-1);
            config.setBlockWhenExhausted(false);
            config.setMaxWaitMillis(0);
            config.setMaxIdlePerKey(1);
            config.setMaxTotal(getMaxOpenPreparedStatements());
            if ( getDataSourceJmxName() != null ) {
                StringBuilder base = new StringBuilder(getDataSourceJmxName().toString());
                base.append(Constants.JMX_CONNECTION_BASE_EXT);
                base.append(Long.toString(connIndex));
                config.setJmxNameBase(base.toString());
                config.setJmxNamePrefix(Constants.JMX_STATEMENT_POOL_PREFIX);
            }
            KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> stmtPool = createPool(conn, config);
            ( (PoolingConnection) conn ).setStatementPool(stmtPool);
            ( (PoolingConnection) conn ).setCacheState(getCacheState());
        }

        ObjectName connJmxName = null;
        if ( getDataSourceJmxName() != null ) {
            connJmxName = new ObjectName(getDataSourceJmxName().toString() + Constants.JMX_CONNECTION_BASE_EXT + connIndex);
        }

        PoolableConnection object = new XAPoolableManagedConnection(this.transactionRegistry, conn, getPool(), this.jdbcResource, connJmxName);
        if ( log.isDebugEnabled() ) {
            log.debug("Created connection " + System.identityHashCode(conn)); //$NON-NLS-1$
        }
        return new DefaultPooledObject<>(object);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.PoolableConnectionFactory#passivateObject(org.apache.commons.pool2.PooledObject)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public void passivateObject ( PooledObject<PoolableConnection> p ) throws Exception {
        PoolableConnection object = p.getObject();
        if ( log.isTraceEnabled() ) {
            log.trace("Released " + System.identityHashCode(object)); //$NON-NLS-1$
        }

        super.passivateObject(p);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.PoolableConnectionFactory#activateObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void activateObject ( PooledObject<PoolableConnection> p ) throws Exception {
        @SuppressWarnings ( "resource" )
        PoolableConnection object = p.getObject();
        super.activateObject(p);
        if ( object instanceof XAPoolableManagedConnection ) {
            ( (XAPoolableManagedConnection) object ).attach();
        }
        if ( log.isTraceEnabled() ) {
            log.debug("Borrowed " + System.identityHashCode(object)); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.PoolableConnectionFactory#destroyObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void destroyObject ( PooledObject<PoolableConnection> p ) throws Exception {
        if ( log.isDebugEnabled() ) {
            try {
                throw new SQLException();
            }
            catch ( SQLException e ) {
                log.debug("Destroying " + System.identityHashCode(p.getObject()), e); //$NON-NLS-1$
            }
        }
        super.destroyObject(p);
    }


    /**
     * @param conn
     * @param config
     * @return
     */
    private KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> createPool ( Connection conn, GenericKeyedObjectPoolConfig config ) {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(this.classLoader);
            return new GenericKeyedObjectPool<>((PoolingConnection) conn, config);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }
}
