/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2016 by mbechler
 */
package eu.agno3.runtime.db.transaction.internal;


import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;
import javax.transaction.xa.XAException;

import org.apache.commons.dbcp2.DelegatingPreparedStatement;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.pool2.ObjectPool;
import org.apache.derby.iapi.jdbc.BrokeredPreparedStatement;
import org.apache.log4j.Logger;

import com.atomikos.datasource.xa.XATransactionalResource;
import com.atomikos.datasource.xa.session.InvalidSessionHandleStateException;
import com.atomikos.datasource.xa.session.SessionHandleState;
import com.atomikos.datasource.xa.session.SessionHandleStateChangeListener;
import com.atomikos.icatch.CompositeTransaction;
import com.atomikos.icatch.CompositeTransactionManager;
import com.atomikos.icatch.Synchronization;
import com.atomikos.icatch.config.Configuration;
import com.atomikos.icatch.jta.TransactionManagerImp;
import com.atomikos.recovery.TxState;


/**
 * @author mbechler
 *
 */
public class XAPoolableManagedConnection extends PoolableConnection {

    private static final Logger log = Logger.getLogger(XAPoolableManagedConnection.class);
    SessionHandleState sessionHandleState;
    volatile boolean enlisted;
    private ObjectPool<PoolableConnection> pool;
    private TransactionRegistry transactionRegistry;

    private final List<Statement> statements = new ArrayList<>();

    private final Map<Statement, Throwable> creation = new HashMap<>();
    private static final boolean TRACE_CREATION = false;


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    /**
     * @param transactionRegistry
     * @param conn
     * @param pool
     * @param jdbcTransactionalResource
     * @param connJmxName
     * @throws SQLException
     */
    public XAPoolableManagedConnection ( TransactionRegistry transactionRegistry, Connection conn, ObjectPool<PoolableConnection> pool,
            XATransactionalResource jdbcTransactionalResource, ObjectName connJmxName ) throws SQLException {
        super(conn, pool, connJmxName);
        this.transactionRegistry = transactionRegistry;
        this.pool = pool;
        this.sessionHandleState = new SessionHandleState(jdbcTransactionalResource, transactionRegistry.getXAResource(conn));
        this.sessionHandleState.registerSessionHandleStateChangeListener(new SessionHandleStateChangeListener() {

            @Override
            public void onTerminated () {
                getLog().debug("Attached transaction terminated"); //$NON-NLS-1$
                XAPoolableManagedConnection.this.enlisted = false;
            }
        });
    }


    /**
     * Actually close the underlying connection.
     */
    @Override
    public void reallyClose () throws SQLException {
        try {
            super.reallyClose();
        }
        finally {
            this.enlisted = false;
            this.transactionRegistry.unregisterConnection(this);
        }
    }


    /**
     * 
     */
    public void attach () {
        this.sessionHandleState.notifySessionBorrowed();
    }


    protected synchronized <T extends Statement> T addStatement ( T s ) {
        this.statements.add(s);
        if ( TRACE_CREATION ) {
            try {
                throw new RuntimeException();
            }
            catch ( RuntimeException e ) {
                this.creation.put(s, e);
            }
        }
        return s;
    }


    protected synchronized void forceCloseAllPendingStatements () {
        Iterator<Statement> it = this.statements.iterator();
        while ( it.hasNext() ) {

            try ( Statement s = it.next() ) {
                if ( !s.isClosed() ) {
                    dumpStatement(s);
                }

                if ( log.isDebugEnabled() ) {
                    log.trace("Forcing close of pending statement: " + s); //$NON-NLS-1$
                }
            }
            catch ( Exception e ) {
                log.warn("Error closing pending statement: ", e); //$NON-NLS-1$
            }
            it.remove();
        }
        this.creation.clear();
    }


    /**
     * @param s
     */
    @SuppressWarnings ( "resource" )
    void dumpStatement ( Statement s ) {
        Statement unwrapped = unwrapStatement(s);
        if ( unwrapped instanceof BrokeredPreparedStatement ) {
            try {
                Field f = BrokeredPreparedStatement.class.getDeclaredField("sql"); //$NON-NLS-1$
                f.setAccessible(true);
                String sql = (String) f.get(unwrapped);
                try {
                    throw new RuntimeException();
                }
                catch ( RuntimeException e ) {
                    log.warn(
                        "Unclosed statement " + sql + //$NON-NLS-1$
                                " @ " + s, //$NON-NLS-1$
                        e);
                    Throwable throwable = this.creation.get(s);
                    if ( throwable != null ) {
                        log.warn("Created at", throwable); //$NON-NLS-1$
                    }
                }
                return;
            }
            catch ( Exception e ) {
                log.warn("Failed to get statement SQL", e); //$NON-NLS-1$
            }
        }
        try {
            throw new RuntimeException();
        }
        catch ( RuntimeException e ) {
            log.warn("Unclosed unknown statement " + unwrapped.getClass(), e); //$NON-NLS-1$
            Throwable throwable = this.creation.get(s);
            if ( throwable != null ) {
                log.warn("Created at", throwable); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param s
     * @return
     */
    private static Statement unwrapStatement ( Statement s ) {
        Statement u = s;
        if ( u instanceof DelegatingPreparedStatement ) {
            u = ( (DelegatingPreparedStatement) s ).getInnermostDelegate();
        }
        return u;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.PoolableConnection#close()
     */
    @Override
    public synchronized void close () throws SQLException {
        log.trace("Releasing connection"); //$NON-NLS-1$
        forceCloseAllPendingStatements();
        if ( this.enlisted ) {
            this.sessionHandleState.notifySessionClosed();
            this.enlisted = false;
        }
        super.close();
    }


    /**
     * @return whether the connection is available for reuse
     */
    public boolean isAvailable () {
        return this.sessionHandleState.isTerminated();
    }


    /**
     * @return whether a transaction error occured
     */
    public boolean isErroneous () {
        return this.sessionHandleState.isErroneous();

    }


    /**
     * @param ct
     * @return whether this connection is enlisted in a transaction
     */
    public boolean isInTransaction ( CompositeTransaction ct ) {
        return this.sessionHandleState.isActiveInTransaction(ct);

    }


    private static CompositeTransactionManager getCompositeTransactionManager () {
        CompositeTransactionManager ret = Configuration.getCompositeTransactionManager();
        if ( ret == null ) {
            log.warn("transaction manager not running?"); //$NON-NLS-1$
        }
        return ret;
    }


    private boolean isEnlistedInGlobalTransaction () {
        CompositeTransactionManager compositeTransactionManager = getCompositeTransactionManager();
        if ( compositeTransactionManager == null ) {
            return false; // TM is not running, we can only be in local TX mode
        }
        CompositeTransaction ct = compositeTransactionManager.getCompositeTransaction();
        return this.sessionHandleState.isActiveInTransaction(ct);

    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.PoolableConnection#validate(java.lang.String, int)
     */
    @Override
    public void validate ( String sql, int timeout ) throws SQLException {
        if ( this.isErroneous() ) {
            throw new SQLException("Transaction error"); //$NON-NLS-1$
        }
        super.validate(sql, timeout);
    }


    /**
     * @return whther the connection can be reused in this thread
     */
    public boolean canBeRecycledForCallingThread () {
        boolean ret = false;
        CompositeTransactionManager tm = Configuration.getCompositeTransactionManager();
        if ( tm != null ) { // null for non-JTA use where recycling is pointless anyway
            CompositeTransaction current = tm.getCompositeTransaction();
            if ( ( current != null ) && ( current.getProperty(TransactionManagerImp.JTA_PROPERTY_NAME) != null ) ) {
                ret = this.sessionHandleState.isInactiveInTransaction(current);
            }
        }
        return ret;

    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#createStatement()
     */
    @Override
    public Statement createStatement () throws SQLException {
        ensureEnlisted();
        return addStatement(super.createStatement());
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#createStatement(int, int)
     */
    @Override
    public Statement createStatement ( int resultSetType, int resultSetConcurrency ) throws SQLException {
        ensureEnlisted();
        return addStatement(super.createStatement(resultSetType, resultSetConcurrency));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#createStatement(int, int, int)
     */
    @Override
    public Statement createStatement ( int resultSetType, int resultSetConcurrency, int resultSetHoldability ) throws SQLException {
        ensureEnlisted();
        return addStatement(super.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#prepareCall(java.lang.String)
     */
    @Override
    public CallableStatement prepareCall ( String sql ) throws SQLException {
        ensureEnlisted();
        return addStatement(super.prepareCall(sql));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#prepareCall(java.lang.String, int, int)
     */
    @Override
    public CallableStatement prepareCall ( String sql, int resultSetType, int resultSetConcurrency ) throws SQLException {
        ensureEnlisted();
        return addStatement(super.prepareCall(sql, resultSetType, resultSetConcurrency));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#prepareCall(java.lang.String, int, int, int)
     */
    @Override
    public CallableStatement prepareCall ( String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability ) throws SQLException {
        ensureEnlisted();
        return addStatement(super.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#prepareStatement(java.lang.String)
     */
    @Override
    public PreparedStatement prepareStatement ( String sql ) throws SQLException {
        try {
            ensureEnlisted();
            return addStatement(super.prepareStatement(sql));
        }
        catch ( Throwable t ) {
            log.warn("prepareStatement", t); //$NON-NLS-1$
            throw t;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#prepareStatement(java.lang.String, int)
     */
    @Override
    public PreparedStatement prepareStatement ( String sql, int autoGeneratedKeys ) throws SQLException {
        ensureEnlisted();
        return addStatement(super.prepareStatement(sql, autoGeneratedKeys));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#prepareStatement(java.lang.String, int, int)
     */
    @Override
    public PreparedStatement prepareStatement ( String sql, int resultSetType, int resultSetConcurrency ) throws SQLException {
        ensureEnlisted();
        return addStatement(super.prepareStatement(sql, resultSetType, resultSetConcurrency));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#prepareStatement(java.lang.String, int, int, int)
     */
    @Override
    public PreparedStatement prepareStatement ( String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability )
            throws SQLException {
        ensureEnlisted();
        return addStatement(super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#prepareStatement(java.lang.String, int[])
     */
    @Override
    public PreparedStatement prepareStatement ( String sql, int[] columnIndexes ) throws SQLException {
        ensureEnlisted();
        return addStatement(super.prepareStatement(sql, columnIndexes));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#prepareStatement(java.lang.String, java.lang.String[])
     */
    @Override
    public PreparedStatement prepareStatement ( String sql, String[] columnNames ) throws SQLException {
        ensureEnlisted();
        return addStatement(super.prepareStatement(sql, columnNames));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#commit()
     */
    @Override
    public void commit () throws SQLException {
        if ( isEnlistedInGlobalTransaction() ) {
            throw new SQLException("Cannot perform while in global transaction"); //$NON-NLS-1$
        }
        super.commit();
    }


    private synchronized void ensureEnlisted () throws SQLException {
        if ( this.enlisted ) {
            log.trace("Already enlisted in transaction"); //$NON-NLS-1$
            return;
        }
        try {
            CompositeTransaction ct = null;
            CompositeTransactionManager ctm = getCompositeTransactionManager();

            if ( ctm != null ) {
                ct = ctm.getCompositeTransaction();

                if ( ct == null ) {
                    return;
                }

                if ( this.sessionHandleState.isInactiveInTransaction(ct) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Enlisting in " + ct.getTid()); //$NON-NLS-1$
                    }
                    this.sessionHandleState.notifyBeforeUse(ct);
                }
                if ( !this.enlisted && ct.getProperty(TransactionManagerImp.JTA_PROPERTY_NAME) != null ) {
                    if ( log.isTraceEnabled() ) {
                        log.trace("Detected transaction " + ct.getTid()); //$NON-NLS-1$
                    }
                    if ( ct.getState().equals(TxState.ACTIVE) ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug(String.format("Registering synchronization for %s on %d", ct.getTid(), System.identityHashCode(this))); //$NON-NLS-1$
                        }
                        this.enlisted = true;
                        ct.registerSynchronization(new JdbcRequeueSynchronization(this, ct));
                    }
                    else {
                        throw new SQLException("The transaction has timed out - try increasing the timeout if needed"); //$NON-NLS-1$
                    }
                }
            }

        }
        catch ( InvalidSessionHandleStateException ex ) {
            this.notifySessionErrorOccurred(ex);
            throw new SQLException(ex.getMessage(), ex);
        }
        catch ( Exception e ) {
            this.notifySessionErrorOccurred(e);
        }

    }


    /**
     * @param e
     */
    private void notifySessionErrorOccurred ( Exception e ) {
        if ( e.getCause() instanceof XAException ) {
            log.warn("notifySessionErrorOccurred XAException " + ( (XAException) e.getCause() ).errorCode, e); //$NON-NLS-1$
        }
        else {
            log.debug("notifySessionErrorOccurred", e); //$NON-NLS-1$
        }
        this.sessionHandleState.notifySessionErrorOccurred();
        try {
            this.pool.invalidateObject(this);
        }
        catch ( Exception e1 ) {
            log.warn("Failed to invalidate connection", e1); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#rollback()
     */
    @Override
    public void rollback () throws SQLException {
        if ( isEnlistedInGlobalTransaction() ) {
            throw new SQLException("Cannot perform while in global transaction"); //$NON-NLS-1$
        }
        super.rollback();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#setSavepoint()
     */
    @Override
    public Savepoint setSavepoint () throws SQLException {
        if ( isEnlistedInGlobalTransaction() ) {
            throw new SQLException("Cannot perform while in global transaction"); //$NON-NLS-1$
        }
        return super.setSavepoint();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#setSavepoint(java.lang.String)
     */
    @Override
    public Savepoint setSavepoint ( String name ) throws SQLException {
        if ( isEnlistedInGlobalTransaction() ) {
            throw new SQLException("Cannot perform while in global transaction"); //$NON-NLS-1$
        }
        return super.setSavepoint(name);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#releaseSavepoint(java.sql.Savepoint)
     */
    @Override
    public void releaseSavepoint ( Savepoint savepoint ) throws SQLException {
        if ( isEnlistedInGlobalTransaction() ) {
            throw new SQLException("Cannot perform while in global transaction"); //$NON-NLS-1$
        }
        super.releaseSavepoint(savepoint);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#setAutoCommit(boolean)
     */
    @Override
    public void setAutoCommit ( boolean autoCommit ) throws SQLException {
        if ( isEnlistedInGlobalTransaction() ) {
            throw new SQLException("Cannot perform while in global transaction"); //$NON-NLS-1$
        }
        super.setAutoCommit(autoCommit);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.dbcp2.DelegatingConnection#getAutoCommit()
     */
    @Override
    public boolean getAutoCommit () throws SQLException {
        return false;
    }

    private static class JdbcRequeueSynchronization implements Synchronization {

        private static final long serialVersionUID = 1L;

        private CompositeTransaction compositeTransaction;
        private XAPoolableManagedConnection proxy;
        private boolean afterCompletionDone;


        public JdbcRequeueSynchronization ( XAPoolableManagedConnection proxy, CompositeTransaction compositeTransaction ) {
            this.compositeTransaction = compositeTransaction;
            this.proxy = proxy;
            this.afterCompletionDone = false;
        }


        @Override
        public void afterCompletion ( TxState state ) {
            if ( this.afterCompletionDone ) {
                return;
            }

            if ( getLog().isDebugEnabled() ) {
                getLog().debug(String.format(
                    "Synchronization called %s on %s on %d", //$NON-NLS-1$
                    state,
                    this.compositeTransaction.getTid(),
                    System.identityHashCode(this.proxy)));
            }

            if ( state.equals(TxState.ABORTING) ) {
                this.proxy.forceCloseAllPendingStatements();
            }

            if ( state.equals(TxState.TERMINATED) || state.equals(TxState.HEUR_MIXED) || state.equals(TxState.HEUR_HAZARD)
                    || state.equals(TxState.HEUR_ABORTED) || state.equals(TxState.HEUR_COMMITTED) ) {
                if ( getLog().isTraceEnabled() ) {
                    getLog().debug("Terminated transaction " + this.compositeTransaction.getTid()); //$NON-NLS-1$
                }
                this.proxy.sessionHandleState.notifyTransactionTerminated(this.compositeTransaction);
                this.afterCompletionDone = true;
                this.proxy.forceCloseAllPendingStatements();
            }
        }


        @Override
        public void beforeCompletion () {}


        @Override
        public boolean equals ( Object other ) {
            boolean ret = false;
            if ( other instanceof JdbcRequeueSynchronization ) {
                JdbcRequeueSynchronization o = (JdbcRequeueSynchronization) other;
                ret = this.compositeTransaction.isSameTransaction(o.compositeTransaction);
            }
            return ret;

        }


        @Override
        public int hashCode () {
            return this.compositeTransaction.hashCode();
        }
    }
}
