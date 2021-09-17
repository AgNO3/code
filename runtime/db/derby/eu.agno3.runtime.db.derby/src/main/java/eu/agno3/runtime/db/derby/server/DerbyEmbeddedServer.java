/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.server;


import java.sql.SQLException;
import java.util.Properties;

import org.apache.derby.authentication.UserAuthenticator;
import org.apache.derby.jdbc.BasicEmbeddedDataSource40;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.secret.SecretKeyProvider;
import eu.agno3.runtime.db.derby.DerbyConfiguration;
import eu.agno3.runtime.db.derby.DerbyServer;
import eu.agno3.runtime.db.derby.auth.DerbyAuthenticatorBackend;
import eu.agno3.runtime.db.derby.auth.internal.DerbyAuthConfiguration;
import eu.agno3.runtime.db.embedded.EmbeddedDBServer;


/**
 * Embedded derby database
 * 
 * @author mbechler
 * @see AbstractDerbyServer
 */
@Component ( service = {
    EmbeddedDBServer.class, DerbyServer.class
}, configurationPid = DerbyEmbeddedServer.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
    DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.apache.derby.jdbc.EmbeddedDriver"
} )
public class DerbyEmbeddedServer extends AbstractDerbyServer {

    private static final String EXPECTED_EXCEPTION = "Expected derby shutdown exception:"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DerbyEmbeddedServer.class);

    /**
     * Configuration PID
     */
    public static final String PID = "db.server.derby.embedded"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.embedded.AbstractEmbeddedDBServer#setAssocDataSourceFactory(org.osgi.service.jdbc.DataSourceFactory)
     */
    @Override
    @Reference ( target = "(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)" )
    protected void setAssocDataSourceFactory ( DataSourceFactory dsf ) {
        super.setAssocDataSourceFactory(dsf);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.embedded.AbstractEmbeddedDBServer#unsetAssocDataSourceFactory(org.osgi.service.jdbc.DataSourceFactory)
     */
    @Override
    protected void unsetAssocDataSourceFactory ( DataSourceFactory dsf ) {
        super.unsetAssocDataSourceFactory(dsf);
    }


    // dependency only
    @Reference
    protected void setDerbyAuthenticatorBackend ( DerbyAuthenticatorBackend back ) {
        // dependency only
    }


    protected void unsetDerbyAuthenticatorBackend ( DerbyAuthenticatorBackend back ) {
        // dependency only
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.derby.server.AbstractDerbyServer#setSecretKeyProvider(eu.agno3.runtime.crypto.secret.SecretKeyProvider)
     */
    @Reference
    @Override
    protected synchronized void setSecretKeyProvider ( SecretKeyProvider skp ) {
        super.setSecretKeyProvider(skp);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.derby.server.AbstractDerbyServer#unsetSecretKeyProvider(eu.agno3.runtime.crypto.secret.SecretKeyProvider)
     */
    @Override
    protected synchronized void unsetSecretKeyProvider ( SecretKeyProvider skp ) {
        super.unsetSecretKeyProvider(skp);
    }


    @Reference
    protected void setDerbyGlobalConfig ( DerbyGlobalConfig cfg ) {
        // dependency only
    }


    protected void unsetDerbyGlobalConfig ( DerbyGlobalConfig cfg ) {
        // dependency only
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.derby.server.AbstractDerbyServer#setAuthConfiguration(eu.agno3.runtime.db.derby.auth.internal.DerbyAuthConfiguration)
     */
    @Override
    @Reference ( updated = "updatedAuthConfiguration" )
    protected synchronized void setAuthConfiguration ( DerbyAuthConfiguration config ) {
        super.setAuthConfiguration(config);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.derby.server.AbstractDerbyServer#unsetAuthConfiguration(eu.agno3.runtime.db.derby.auth.internal.DerbyAuthConfiguration)
     */
    @Override
    protected synchronized void unsetAuthConfiguration ( DerbyAuthConfiguration config ) {
        super.unsetAuthConfiguration(config);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     * 
     * @see eu.agno3.runtime.db.derby.server.AbstractDerbyServer#updatedAuthConfiguration(eu.agno3.runtime.db.derby.auth.internal.DerbyAuthConfiguration)
     */
    @Override
    protected synchronized void updatedAuthConfiguration ( DerbyAuthConfiguration config ) throws CryptoException {
        super.updatedAuthConfiguration(config);
    }


    @Reference
    protected synchronized void bindUserAuthenticator ( UserAuthenticator authenticator ) {
        // dependency only
    }


    protected synchronized void unbindUserAuthenticator ( UserAuthenticator authenticator ) {
        // dependency only
    }


    @Override
    @Activate
    protected void activate ( ComponentContext context ) throws SQLException, CryptoException {
        super.activate(context);
    }


    @Override
    @Deactivate
    protected void deactivate ( ComponentContext context ) throws CryptoException {
        super.deactivate(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     * 
     * @see eu.agno3.runtime.db.derby.server.AbstractDerbyServer#start()
     */
    @Override
    public void start () throws SQLException, CryptoException {
        log.info("Starting embedded Derby server " + //$NON-NLS-1$
                this.getComponentContext().getProperties().get("instanceId")); //$NON-NLS-1$
        super.start(this.getComponentContext().getProperties());

        BasicEmbeddedDataSource40 ds = this.getAdminDataSource(new Properties()).unwrap(BasicEmbeddedDataSource40.class);
        ds.setShutdownDatabase(DerbyConfigProperties.SHUTDOWN);

        try {
            ds.getConnection();
        }
        catch ( SQLException e ) {

            if ( !DerbyConfigProperties.SHUTDOWN_SQLCODE.equals(e.getSQLState()) ) {
                log.warn("Exception in derby restart for configuration:", e); //$NON-NLS-1$
                throw e;
            }
            log.trace(EXPECTED_EXCEPTION, e);
        }

        if ( ! ( this.getComponentContext().getProperties().get(DerbyConfiguration.DISABLE_AUTHENTICATION) != null
                && this.getComponentContext().getProperties().get(DerbyConfiguration.DISABLE_AUTHENTICATION).equals(Boolean.TRUE.toString()) ) ) {
            this.verifyAuthentication();
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     * 
     * @see eu.agno3.runtime.db.embedded.AbstractEmbeddedDBServer#stop()
     */
    @Override
    public void stop () throws SQLException, CryptoException {
        log.info("Stopping embedded Derby server " + //$NON-NLS-1$
                this.getComponentContext().getProperties().get("instanceId")); //$NON-NLS-1$

        BasicEmbeddedDataSource40 ds = this.getAdminDataSource(new Properties()).unwrap(BasicEmbeddedDataSource40.class);
        super.stop();

        try {
            ds.setShutdownDatabase(DerbyConfigProperties.SHUTDOWN);
            ds.getConnection();
        }
        catch ( SQLException e ) {
            if ( DerbyConfigProperties.SHUTDOWN_SQLCODE.equals(e.getSQLState()) ) {
                log.trace(EXPECTED_EXCEPTION, e);
                return;
            }
            log.warn("Exception in derby shutdown:", e); //$NON-NLS-1$

        }
    }

}
