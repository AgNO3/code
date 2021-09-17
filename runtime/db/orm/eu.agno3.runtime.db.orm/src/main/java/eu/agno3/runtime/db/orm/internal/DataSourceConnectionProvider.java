/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.log4j.Logger;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.Service;


/**
 * @author mbechler
 * 
 */
public class DataSourceConnectionProvider implements Service, ConnectionProvider {

    private static final Logger log = Logger.getLogger(DataSourceConnectionProvider.class);

    /**
     * 
     */
    private static final long serialVersionUID = -2245033559832457115L;

    private PersistenceUnitInfo info;

    private final AtomicInteger leased = new AtomicInteger();
    private final Map<Connection, Object> tracebacks = new IdentityHashMap<>();


    /**
     * @param info
     */
    public DataSourceConnectionProvider ( PersistenceUnitInfo info ) {
        this.info = info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.service.spi.Wrapped#unwrap(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> T unwrap ( Class<T> type ) {
        if ( this.isUnwrappableAs(type) ) {
            return (T) this;
        }
        throw new IllegalArgumentException("Cannot unwrap to type " + type.getName()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.engine.jdbc.connections.spi.ConnectionProvider#closeConnection(java.sql.Connection)
     */
    @Override
    public void closeConnection ( Connection conn ) throws SQLException {
        if ( conn == null ) {
            return;
        }
        int rem = this.leased.decrementAndGet();
        if ( log.isTraceEnabled() ) {
            this.tracebacks.remove(conn);

            if ( this.tracebacks.size() > 0 ) {
                for ( Entry<Connection, Object> e : this.tracebacks.entrySet() ) {
                    log.trace(String.format(
                        "Still leased %x: %s", //$NON-NLS-1$
                        System.identityHashCode(e.getKey()),
                        e.getValue()));
                }
            }

            try {
                throw new IllegalStateException("HERE"); //$NON-NLS-1$
            }
            catch ( IllegalStateException e ) {
                log.trace(String.format(
                    "Releasing connection for PU %s: %d leased: %x", //$NON-NLS-1$
                    this.info.getPersistenceUnitName(),
                    rem,
                    System.identityHashCode(conn)), e);
            }
        }
        else if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Releasing connection for PU %s: %d leased: %x", //$NON-NLS-1$
                this.info.getPersistenceUnitName(),
                rem,
                System.identityHashCode(conn)));
        }

        conn.close();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.engine.jdbc.connections.spi.ConnectionProvider#getConnection()
     */
    @Override
    public Connection getConnection () throws SQLException {
        Connection conn = this.info.getJtaDataSource().getConnection();
        int act = this.leased.incrementAndGet();
        if ( log.isTraceEnabled() ) {
            try {
                throw new IllegalStateException("HERE"); //$NON-NLS-1$
            }
            catch ( IllegalStateException e ) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                this.tracebacks.put(conn, sw.toString());

                log.trace(String.format(
                    "Obtained connection for PU %s: %d leased: %x", //$NON-NLS-1$
                    this.info.getPersistenceUnitName(),
                    act - 1,
                    System.identityHashCode(conn)), e);
            }
        }
        else if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Obtained connection for PU %s: %d leased: %x", //$NON-NLS-1$
                this.info.getPersistenceUnitName(),
                act - 1,
                System.identityHashCode(conn)));
        }
        return conn;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.engine.jdbc.connections.spi.ConnectionProvider#supportsAggressiveRelease()
     */
    @Override
    public boolean supportsAggressiveRelease () {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.hibernate.service.spi.Wrapped#isUnwrappableAs(java.lang.Class)
     */
    @Override
    public boolean isUnwrappableAs ( Class type ) {
        return ConnectionProvider.class.equals(type) || DataSourceConnectionProvider.class.isAssignableFrom(type);
    }

}
