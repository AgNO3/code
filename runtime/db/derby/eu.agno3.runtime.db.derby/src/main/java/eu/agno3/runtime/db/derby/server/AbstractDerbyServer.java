/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.server;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.secret.SecretKeyProvider;
import eu.agno3.runtime.db.DatabaseConfigurationException;
import eu.agno3.runtime.db.derby.DerbyConfiguration;
import eu.agno3.runtime.db.derby.DerbyServer;
import eu.agno3.runtime.db.derby.auth.DerbyAuthenticatorAdapter;
import eu.agno3.runtime.db.derby.auth.UserAccess;
import eu.agno3.runtime.db.derby.auth.internal.DerbyAuthConfiguration;
import eu.agno3.runtime.db.derby.client.DerbyEmbeddedDataSourceFactory;
import eu.agno3.runtime.db.embedded.AbstractEmbeddedDBServer;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * 
 * Security: The embedded derby database can be encrypted to prevent third parties, which do not have the encryption
 * key, from opening it. In the embedded scenario access to this key is most definitely possible for code running inside
 * the virtual machine. The same holds for user authentication credentials.
 * 
 * @author mbechler
 * 
 */
public abstract class AbstractDerbyServer extends AbstractEmbeddedDBServer implements DerbyServer {

    private static final Logger log = Logger.getLogger(AbstractDerbyServer.class);

    private DerbyAuthConfiguration authConfig;

    private Properties initDataSourceProperties;
    private DataSource initDataSource;

    private SecretKeyProvider secretProvider;


    @Reference ( updated = "updatedAuthConfiguration" )
    protected synchronized void setAuthConfiguration ( DerbyAuthConfiguration config ) {
        this.authConfig = config;
    }


    protected synchronized void unsetAuthConfiguration ( DerbyAuthConfiguration config ) {
        if ( this.authConfig == config ) {
            this.authConfig = null;
        }
    }


    @Reference
    protected synchronized void setSecretKeyProvider ( SecretKeyProvider skp ) {
        this.secretProvider = skp;
    }


    protected synchronized void unsetSecretKeyProvider ( SecretKeyProvider skp ) {
        if ( this.secretProvider == skp ) {
            this.secretProvider = null;
        }
    }


    protected synchronized void updatedAuthConfiguration ( DerbyAuthConfiguration config ) throws CryptoException {
        log.debug("Updating ACLs"); //$NON-NLS-1$
        try {
            updateACLs(
                (String) this.getComponentContext().getProperties().get(JDBC_DATABASE_NAME),
                this.getAdminDataSource(new Properties()).getConnection());
        }
        catch ( SQLException e ) {
            log.error("Failed to update ACLs:", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws SQLException
     * @throws CryptoException
     */
    protected DataSource getAdminDataSource ( Properties extraProperties ) throws SQLException, CryptoException {
        Properties userProperties = new Properties();
        userProperties.putAll(extraProperties);

        userProperties.setProperty(JDBC_USER, this.getAuthConfig().getAdminUser());
        userProperties.setProperty(JDBC_PASSWORD, this.getAuthConfig().getAdminPassword());
        return this.createDataSource(userProperties);
    }


    /**
     * @return the initDataSource
     */
    protected DataSource getInitDataSource () {
        return this.initDataSource;
    }


    /**
     * @return the initDataSourceProperties
     */
    protected Properties getInitDataSourceProperties () {
        return this.initDataSourceProperties;
    }


    /**
     * @return the authConfig
     */
    protected DerbyAuthConfiguration getAuthConfig () {
        return this.authConfig;
    }


    /**
     * @param servProps
     * @param extraAttributes
     * @throws SQLException
     * @throws CryptoException
     */
    protected void setupAuthentication ( Dictionary<String, Object> servProps, Set<String> extraAttributes, Properties dsProperties )
            throws SQLException, CryptoException {

        if ( !this.authConfig.isActive() ) {
            throw new DatabaseConfigurationException("Authentication enabled, but no auth configuration"); //$NON-NLS-1$
        }

        try ( Connection configConn = this.createDataSource(dsProperties)
                .getConnection(this.getAuthConfig().getAdminUser(), this.getAuthConfig().getAdminPassword()) ) {

            setPropertyQuery(configConn, DerbyConfigProperties.CONN_REQUIRE_AUTH, Boolean.TRUE.toString());
            setPropertyQuery(configConn, DerbyConfigProperties.AUTH_PROVIDER, DerbyAuthenticatorAdapter.class.getName());
            setPropertyQuery(configConn, DerbyConfigProperties.DEFAULT_CONN_MODE, DerbyConfigProperties.NO_ACCESS);

            updateACLs((String) servProps.get(JDBC_DATABASE_NAME), configConn);

            setPropertyQuery(configConn, DerbyConfigProperties.DATABASE_PROPERTIES_ONLY, Boolean.TRUE.toString());

        }
        catch ( SQLException e ) {
            throw new DatabaseConfigurationException("Failed to setup database authentication", e); //$NON-NLS-1$
        }

    }


    protected void verifyAuthentication () throws SQLException, CryptoException {
        try ( Connection configConn = this.getAdminDataSource(this.initDataSourceProperties).getConnection() ) {

            if ( !Boolean.TRUE.toString().equals(getPropertyQuery(configConn, DerbyConfigProperties.DATABASE_PROPERTIES_ONLY)) ) {
                throw new DatabaseConfigurationException("Failure to set propertiesOnly"); //$NON-NLS-1$
            }

            if ( !Boolean.TRUE.toString().equals(getPropertyQuery(configConn, DerbyConfigProperties.CONN_REQUIRE_AUTH)) ) {
                throw new DatabaseConfigurationException("Failure to set requireAuthentication"); //$NON-NLS-1$
            }

            if ( !DerbyAuthenticatorAdapter.class.getName().equals(getPropertyQuery(configConn, DerbyConfigProperties.AUTH_PROVIDER)) ) {
                throw new DatabaseConfigurationException("Failure to set provider"); //$NON-NLS-1$
            }

            if ( !DerbyConfigProperties.NO_ACCESS.equals(getPropertyQuery(configConn, DerbyConfigProperties.DEFAULT_CONN_MODE)) ) {
                throw new DatabaseConfigurationException("Failure to set defaultConnectionMode"); //$NON-NLS-1$
            }

        }
    }


    /**
     * @param servProps
     * @throws SQLException
     * @throws CryptoException
     */
    public void start ( Dictionary<String, Object> servProps ) throws SQLException, CryptoException {
        Set<String> extraAttributes = new HashSet<>();

        if ( Boolean.TRUE.toString().equals(servProps.get(DerbyConfiguration.DATA_ENCRYPTION_ATTR)) ) {
            setupEncryption(servProps, extraAttributes, (String) servProps.get(DataSourceFactory.JDBC_DATABASE_NAME));
        }

        try {
            Properties testProperties = this.initDataSourceProperties();
            testProperties.put(DerbyEmbeddedDataSourceFactory.EXTRA_ATTRIBUTES, StringUtils.join(extraAttributes, ";")); //$NON-NLS-1$
            try ( Connection c = getAdminDataSource(testProperties).getConnection() ) {
                c.getAutoCommit();
                // do nothing
            }
        }
        catch ( SQLException e ) {
            log.debug("Database does not exist"); //$NON-NLS-1$
            log.trace("Exception:", e); //$NON-NLS-1$
            extraAttributes.add("create=true"); //$NON-NLS-1$
        }

        this.initDataSourceProperties = this.initDataSourceProperties();
        this.initDataSourceProperties.put(DerbyEmbeddedDataSourceFactory.EXTRA_ATTRIBUTES, StringUtils.join(extraAttributes, ";")); //$NON-NLS-1$

        if ( !Boolean.TRUE.toString().equals(servProps.get(DerbyConfiguration.DISABLE_AUTHENTICATION)) ) {
            this.setupAuthentication(servProps, extraAttributes, this.initDataSourceProperties);
        }

        this.initDataSource = getAdminDataSource(this.initDataSourceProperties);
        try ( Connection c = this.initDataSource.getConnection() ) {
            setupGeneral(servProps, c);
        }
    }


    /**
     * @param servProps
     * @param c
     * @throws SQLException
     */
    void setupGeneral ( Dictionary<String, Object> servProps, Connection c ) throws SQLException {
        setupLogging(servProps, c);

        setPropertyQuery(
            c,
            "derby.locks.deadlockTimeout", //$NON-NLS-1$
            String.valueOf(ConfigUtil.parseInt(servProps, "deadlockTimeout", 20))); //$NON-NLS-1$

        setPropertyQuery(
            c,
            "derby.locks.waitTimeout", //$NON-NLS-1$
            String.valueOf(ConfigUtil.parseInt(servProps, "waitTimeout", 60))); //$NON-NLS-1$

        setPropertyQuery(
            c,
            "derby.locks.escalationThreshold", //$NON-NLS-1$
            String.valueOf(ConfigUtil.parseInt(servProps, "locksEscalationThreshold", 5000))); //$NON-NLS-1$

        setPropertyQuery(
            c,
            "derby.storage.pageCacheSize", //$NON-NLS-1$
            String.valueOf(ConfigUtil.parseInt(servProps, "pageCacheSize", 1024))); //$NON-NLS-1$

        setPropertyQuery(
            c,
            "derby.storage.pageSize", //$NON-NLS-1$
            String.valueOf(ConfigUtil.parseInt(servProps, "pageSize", 32768))); //$NON-NLS-1$

    }


    /**
     * @param servProps
     * @param c
     */
    void setupLogging ( Dictionary<String, Object> servProps, Connection c ) {

        try {
            if ( Boolean.TRUE.toString().equals(servProps.get(DerbyConfiguration.ENABLE_DEBUG)) || log.isDebugEnabled() ) {
                log.warn("Enabling derby debug logging, expect poor performance"); //$NON-NLS-1$
                setPropertyQuery(c, DerbyConfigProperties.LOG_SEVERITY, String.valueOf(0));
            }
            else {
                setPropertyQuery(c, DerbyConfigProperties.LOG_SEVERITY, String.valueOf(20000));
            }
        }
        catch ( SQLException e ) {
            log.warn("Failed to set logging:", e); //$NON-NLS-1$
        }

        if ( Boolean.TRUE.toString().equals(servProps.get(DerbyConfiguration.DEBUG_LOCKS_ATTR)) ) {
            log.info("Enabling lock debugging"); //$NON-NLS-1$

            try {
                setPropertyQuery(c, DerbyConfigProperties.LOCKS_MONITOR, Boolean.TRUE.toString());
                setPropertyQuery(c, DerbyConfigProperties.DEADLOCK_TRACE, Boolean.TRUE.toString());
                setPropertyQuery(c, DerbyConfigProperties.LOG_STATEMENTS, Boolean.TRUE.toString());
            }
            catch ( SQLException e ) {
                log.warn("Failed to enable lock debugging:", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     * 
     * @see eu.agno3.runtime.db.embedded.EmbeddedDBServer#stop()
     */
    @Override
    public void stop () throws SQLException, CryptoException {
        this.initDataSource = null;
        this.initDataSourceProperties = null;
    }


    /**
     * @param servProps
     * @param configConn
     * @throws SQLException
     */
    private void updateACLs ( String dbName, Connection configConn ) throws SQLException {
        Map<String, UserAccess> dbAccess = this.authConfig.getAuthorizedUsers(dbName);
        List<String> fullAccessUsers = new ArrayList<>();
        List<String> readOnlyUsers = new ArrayList<>();

        for ( Entry<String, UserAccess> e : dbAccess.entrySet() ) {

            if ( e.getValue() == UserAccess.ADMIN || e.getValue() == UserAccess.WRITE ) {
                fullAccessUsers.add(e.getKey());
            }

            if ( e.getValue() == UserAccess.READ ) {
                readOnlyUsers.add(e.getKey());
            }
        }

        if ( !fullAccessUsers.isEmpty() ) {
            setPropertyQuery(configConn, DerbyConfigProperties.RW_USERS, StringUtils.join(fullAccessUsers, ",")); //$NON-NLS-1$
        }

        if ( !readOnlyUsers.isEmpty() ) {
            setPropertyQuery(configConn, DerbyConfigProperties.RO_USERS, StringUtils.join(readOnlyUsers, ",")); //$NON-NLS-1$
        }
    }


    /**
     * @param servProps
     * @param extraAttributes
     * @param dbName
     * @throws DatabaseConfigurationException
     * @throws CryptoException
     */
    protected void setupEncryption ( Dictionary<String, Object> servProps, Set<String> extraAttributes, String dbName )
            throws DatabaseConfigurationException, CryptoException {
        log.info("Enabling data encryption"); //$NON-NLS-1$

        String encryptionAlgorithm = "AES/CBC/NoPadding"; //$NON-NLS-1$

        if ( servProps.get(DerbyConfiguration.ENCRYPTION_ALGORITHM_ATTR) != null ) {
            encryptionAlgorithm = (String) servProps.get(DerbyConfiguration.ENCRYPTION_ALGORITHM_ATTR);
        }

        int keyLength = 128;

        if ( servProps.get(DerbyConfiguration.ENCRYPTION_KEY_LENGTH_ATTR) != null ) {
            keyLength = Integer.parseInt((String) servProps.get(DerbyConfiguration.ENCRYPTION_KEY_LENGTH_ATTR));
        }

        extraAttributes.add("dataEncryption=true"); //$NON-NLS-1$
        extraAttributes.add("bootPassword=" //$NON-NLS-1$
                + Base64.getEncoder().encodeToString(
                    this.secretProvider.getOrCreateExportableSecret(String.format("db-encryption-%s", dbName), 0, keyLength).getEncoded())); //$NON-NLS-1$
        extraAttributes.add("encryptionAlgorithm=" + encryptionAlgorithm); //$NON-NLS-1$
        extraAttributes.add("encryptionKeyLength=" + keyLength); //$NON-NLS-1$
    }


    protected static String getPropertyQuery ( Connection conn, String property ) throws SQLException {

        try ( PreparedStatement s = conn.prepareStatement("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(?)") ) { //$NON-NLS-1$
            s.setString(1, property);
            s.execute();
            try ( ResultSet r = s.getResultSet() ) {
                r.next();
                return r.getString(1);
            }
        }

    }


    protected static void setPropertyQuery ( Connection conn, String property, String value ) throws SQLException {
        try ( PreparedStatement s = conn.prepareStatement("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?,?)") ) { //$NON-NLS-1$
            s.setString(1, property);
            s.setString(2, value);
            s.execute();
        }
    }
}
