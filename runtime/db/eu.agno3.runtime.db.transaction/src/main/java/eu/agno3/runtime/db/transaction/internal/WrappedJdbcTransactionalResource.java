/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2016 by mbechler
 */
package eu.agno3.runtime.db.transaction.internal;


import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import org.apache.log4j.Logger;

import com.atomikos.datasource.ResourceException;
import com.atomikos.datasource.xa.jdbc.JdbcTransactionalResource;

import eu.agno3.runtime.db.DatabaseDriverUtil;


/**
 * @author mbechler
 *
 */
public class WrappedJdbcTransactionalResource extends JdbcTransactionalResource {

    private static final Logger log = Logger.getLogger(WrappedJdbcTransactionalResource.class);
    private final DatabaseDriverUtil driverUtil;


    /**
     * @param serverName
     * @param xads
     * @param driverUtil
     */
    public WrappedJdbcTransactionalResource ( String serverName, XADataSource xads, DatabaseDriverUtil driverUtil ) {
        super(serverName, xads);
        this.driverUtil = driverUtil;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.atomikos.datasource.xa.jdbc.JdbcTransactionalResource#refreshXAConnection()
     */
    @Override
    protected synchronized XAResource refreshXAConnection () throws ResourceException {
        log.debug("refreshXAConnection"); //$NON-NLS-1$
        XAResource conn = super.refreshXAConnection();
        if ( conn == null ) {
            // this used to throw, but that prevents recovering from a connection failure for PSQL
            log.warn("Failed to refresh connection to " + getName()); //$NON-NLS-1$
            return null;
        }
        return new XAResourceWrapper(conn);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.atomikos.datasource.xa.XATransactionalResource#needsRefresh()
     */
    @Override
    protected boolean needsRefresh () {
        if ( super.needsRefresh() ) {
            return true;
        }

        XAResource res = this.xares_;

        if ( res instanceof XAResourceWrapper ) {
            XAResource delegate = ( (XAResourceWrapper) res ).getDelegate();
            if ( !this.driverUtil.isAlive(delegate) ) {
                return true;
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.atomikos.datasource.xa.jdbc.JdbcTransactionalResource#close()
     */
    @Override
    public void close () throws ResourceException {
        log.debug("close"); //$NON-NLS-1$
        super.close();
    }

}
