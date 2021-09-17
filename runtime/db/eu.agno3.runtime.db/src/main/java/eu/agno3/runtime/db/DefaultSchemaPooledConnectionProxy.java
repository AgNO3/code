/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2013 by mbechler
 */
package eu.agno3.runtime.db;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;


/**
 * @author mbechler
 * 
 */
public class DefaultSchemaPooledConnectionProxy implements PooledConnection {

    private PooledConnection delegate;
    private DatabaseDriverUtil databaseUtil;
    private String defaultCatalog;
    private String defaultSchema;


    /**
     * @param delegate
     * @param databaseUtil
     * @param defaultCatalog
     * @param defaultSchema
     */
    public DefaultSchemaPooledConnectionProxy ( PooledConnection delegate, DatabaseDriverUtil databaseUtil, String defaultCatalog,
            String defaultSchema ) {
        super();
        this.delegate = delegate;
        this.databaseUtil = databaseUtil;
        this.defaultCatalog = defaultCatalog;
        this.defaultSchema = defaultSchema;
    }


    @Override
    public void addConnectionEventListener ( ConnectionEventListener listener ) {
        this.delegate.addConnectionEventListener(listener);
    }


    @Override
    public void addStatementEventListener ( StatementEventListener listener ) {
        this.delegate.addStatementEventListener(listener);
    }


    @Override
    public void close () throws SQLException {
        this.delegate.close();
    }


    @Override
    public Connection getConnection () throws SQLException {
        Connection c = this.delegate.getConnection();
        try {
            this.databaseUtil.setConnectionDefaultSchema(c, this.defaultCatalog, this.defaultSchema);
            return c;
        }
        catch ( SQLException e ) {
            c.close();
            throw e;
        }
    }


    @Override
    public void removeConnectionEventListener ( ConnectionEventListener listener ) {
        this.delegate.removeConnectionEventListener(listener);
    }


    @Override
    public void removeStatementEventListener ( StatementEventListener listener ) {
        this.delegate.removeStatementEventListener(listener);
    }

}
