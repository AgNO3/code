package eu.agno3.runtime.db.transaction.internal;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.XAConnectionFactory;
import org.apache.log4j.Logger;


/**
 * 
 * Analog to the original DBCP2 code
 * 
 * @author mbechler
 * 
 *
 */
public class FixedDataSourceXAConnectionFactory extends DataSourceXAConnectionFactory implements XAConnectionFactory {

    private static final Logger log = Logger.getLogger(FixedDataSourceXAConnectionFactory.class);
    private XADataSource xaDataSource;
    private TransactionManager transactionManager;


    /**
     * @param transactionManager
     * @param xaDataSource
     */
    public FixedDataSourceXAConnectionFactory ( TransactionManager transactionManager, XADataSource xaDataSource ) {
        super(transactionManager, xaDataSource);
        this.transactionManager = transactionManager;
        this.xaDataSource = xaDataSource;
    }


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    /**
     * @return the transactionManager
     */
    public TransactionManager getTransactionManager () {
        return this.transactionManager;
    }


    @Override
    public Connection createConnection () throws SQLException {
        // create a new XAConection
        XAConnection xaConnection = this.xaDataSource.getXAConnection();

        // get the real connection and XAResource from the connection
        Connection connection = xaConnection.getConnection();
        XAResource xaResource = new XAResourceWrapper(xaConnection.getXAResource());

        // register the xa resource for the connection
        this.getTransactionRegistry().registerConnection(connection, xaResource);

        xaConnection.addConnectionEventListener(new ConnectionEventListener() {

            @Override
            public void connectionClosed ( ConnectionEvent event ) {
                PooledConnection pc = (PooledConnection) event.getSource();
                pc.removeConnectionEventListener(this);
                try {
                    if ( getTransactionManager().getStatus() == Status.STATUS_ACTIVE ) {
                        getLog().warn("Rolling back transaction as connection has failed"); //$NON-NLS-1$
                        getTransactionManager().rollback();
                    }
                    pc.close();
                }
                catch (
                    SQLException |
                    SystemException e ) {
                    getLog().error("Failed to close XAConnection", e); //$NON-NLS-1$
                }
            }


            @Override
            public void connectionErrorOccurred ( ConnectionEvent event ) {
                getLog().error("Connection error, closing connection", event.getSQLException()); //$NON-NLS-1$
                connectionClosed(event);
            }
        });

        return connection;
    }
}
