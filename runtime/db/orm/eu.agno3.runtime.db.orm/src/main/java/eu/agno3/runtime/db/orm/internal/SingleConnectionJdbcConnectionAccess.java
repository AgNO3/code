/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.11.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;


/**
 * @author mbechler
 *
 */
public class SingleConnectionJdbcConnectionAccess implements JdbcConnectionAccess {

    /**
     * 
     */
    private static final long serialVersionUID = -9203902753291203237L;

    private JdbcConnectionAccess bootstrapJdbcConnectionAccess;
    private Connection cachedConnection;


    /**
     * @param connectionAccess
     */
    public SingleConnectionJdbcConnectionAccess ( JdbcConnectionAccess connectionAccess ) {
        this.bootstrapJdbcConnectionAccess = connectionAccess;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess#obtainConnection()
     */
    @Override
    public Connection obtainConnection () throws SQLException {
        if ( this.cachedConnection == null ) {
            this.cachedConnection = this.bootstrapJdbcConnectionAccess.obtainConnection();
        }

        return this.cachedConnection;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess#releaseConnection(java.sql.Connection)
     */
    @Override
    public void releaseConnection ( Connection arg0 ) throws SQLException {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess#supportsAggressiveRelease()
     */
    @Override
    public boolean supportsAggressiveRelease () {
        return this.bootstrapJdbcConnectionAccess.supportsAggressiveRelease();
    }


    /**
     * @throws SQLException
     * 
     */
    public void cleanup () throws SQLException {
        if ( this.cachedConnection != null ) {
            this.bootstrapJdbcConnectionAccess.releaseConnection(this.cachedConnection);
        }
    }

}
