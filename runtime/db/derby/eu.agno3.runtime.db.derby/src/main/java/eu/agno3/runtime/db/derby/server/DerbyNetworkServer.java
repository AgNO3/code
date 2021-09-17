/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.server;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.derby.authentication.UserAuthenticator;
import org.apache.derby.jdbc.BasicClientDataSource40;
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
 * Embedded derby database with network connector
 * 
 * @author mbechler
 * @see AbstractDerbyServer
 */
@Component ( service = {
    EmbeddedDBServer.class, DerbyServer.class
}, configurationPid = DerbyNetworkServer.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
    DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.apache.derby.jdbc.ClientDriver"
} )
public class DerbyNetworkServer extends AbstractDerbyServer {

    /**
     * Configuration PID
     */
    public static final String PID = "db.server.derby.networked"; //$NON-NLS-1$
    private DerbyNetworkListener networkListener;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.embedded.AbstractEmbeddedDBServer#setAssocDataSourceFactory(org.osgi.service.jdbc.DataSourceFactory)
     */
    @Override
    @Reference ( target = "(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDriver)" )
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


    @Reference
    protected void setDerbyNetworkListener ( DerbyNetworkListener l ) {
        this.networkListener = l;
    }


    protected void unsetDerbyNetworkListener ( DerbyNetworkListener l ) {
        if ( this.networkListener == l ) {
            this.networkListener = null;
        }
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


    // Dependency only
    @Reference
    protected synchronized void bindUserAuthenticator ( UserAuthenticator authenticator ) {
        // dependency only
    }


    protected synchronized void unbindUserAuthenticator ( UserAuthenticator authenticator ) {
        // dependency only
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.embedded.AbstractEmbeddedDBServer#initDataSourceProperties()
     */
    @Override
    protected Properties initDataSourceProperties () {
        Properties p = super.initDataSourceProperties();
        this.networkListener.applySettings(p);
        return p;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     * 
     * @see eu.agno3.runtime.db.embedded.AbstractEmbeddedDBServer#start()
     */
    @Override
    public void start () throws SQLException, CryptoException {

        // component properties are readonly -> copy first
        Dictionary<String, Object> servProps = new Hashtable<>();
        Enumeration<String> it = this.getComponentContext().getProperties().keys();
        while ( it.hasMoreElements() ) {
            String key = it.nextElement();
            servProps.put(key, this.getComponentContext().getProperties().get(key));
        }
        this.networkListener.applySettings(servProps);

        super.start(servProps);

        BasicClientDataSource40 ds = this.getAdminDataSource(new Properties()).unwrap(BasicClientDataSource40.class);
        ds.setShutdownDatabase(DerbyConfigProperties.SHUTDOWN);

        try ( Connection c = ds.getConnection() ) {
            // do nothing
            c.getAutoCommit();
        }
        catch ( SQLException e ) {
            if ( !DerbyConfigProperties.SHUTDOWN_SQLCODE.equals(e.getSQLState()) ) {
                throw e;
            }
        }

        if ( ! ( this.getComponentContext().getProperties().get(DerbyConfiguration.DISABLE_AUTHENTICATION) != null && this.getComponentContext()
                .getProperties().get(DerbyConfiguration.DISABLE_AUTHENTICATION).equals(Boolean.TRUE.toString()) ) ) {
            this.verifyAuthentication();
        }
    }

}
