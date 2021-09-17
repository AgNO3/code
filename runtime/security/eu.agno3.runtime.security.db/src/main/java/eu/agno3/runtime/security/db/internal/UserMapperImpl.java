/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security.db.internal;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.db.schema.SchemaManagedDataSource;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.update.LicensingException;
import eu.agno3.runtime.update.LicensingService;


/**
 * @author mbechler
 *
 */
@Component ( service = UserMapper.class )
public class UserMapperImpl implements UserMapper {

    private static final Logger log = Logger.getLogger(UserMapperImpl.class);

    private DataSource dataSource;
    private DataSourceUtil dataSourceUtil;
    private LicensingService licensingService;


    @Reference ( target = "(dataSourceName=auth)" )
    protected synchronized void setDataSource ( SchemaManagedDataSource ds ) {
        this.dataSource = ds;
    }


    protected synchronized void unsetDataSource ( SchemaManagedDataSource ds ) {
        if ( this.dataSource == ds ) {
            this.dataSource = null;
        }
    }


    @Reference ( target = "(dataSourceName=auth)" )
    protected synchronized void setDataSourceUtil ( DataSourceUtil dsu ) {
        this.dataSourceUtil = dsu;
    }


    protected synchronized void unsetDataSourceUtil ( DataSourceUtil dsu ) {
        if ( this.dataSourceUtil == dsu ) {
            this.dataSourceUtil = null;
        }
    }


    @Reference
    protected synchronized void setLicensingService ( LicensingService ls ) {
        this.licensingService = ls;
    }


    protected synchronized void unsetLicensingService ( LicensingService ls ) {
        if ( this.licensingService == ls ) {
            this.licensingService = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws UserLicenseLimitExceededException
     * 
     * @see eu.agno3.runtime.security.UserMapper#getMappedUser(java.lang.String, java.lang.String, java.util.UUID)
     */
    @Override
    public UserPrincipal getMappedUser ( String username, String realmname, UUID knownUserId ) throws UserLicenseLimitExceededException {
        try ( Connection conn = this.dataSource.getConnection() ) {
            UUID found = fetchMappedUser(conn, username, realmname);

            if ( found != null ) {
                refreshMapping(conn, username, realmname, found);
                return new UserPrincipal(realmname, found, username);
            }

            return createUserMapping(conn, username, realmname, knownUserId);
        }
        catch ( SQLException e ) {
            throw new AuthenticationException("Failed to get user mapping", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.UserMapper#getExistingMappedUser(java.lang.String, java.lang.String,
     *      java.util.UUID)
     */
    @Override
    public UserPrincipal getExistingMappedUser ( String username, String realmname, UUID userId ) {
        try ( Connection conn = this.dataSource.getConnection() ) {
            UUID found = fetchMappedUser(conn, username, realmname);

            if ( found != null ) {
                refreshMapping(conn, username, realmname, found);
                return new UserPrincipal(realmname, found, username);
            }

            return null;
        }
        catch ( SQLException e ) {
            throw new AuthenticationException("Failed to get user mapping", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.UserMapper#removeMapping(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public void removeMapping ( UserPrincipal princ ) {
        try ( Connection conn = this.dataSource.getConnection() ) {
            final String q = "DELETE FROM " + //$NON-NLS-1$
                    this.dataSourceUtil.quoteIdentifier(conn, "user_mapping") //$NON-NLS-1$
                    + " WHERE userId = ? AND realm = ? AND username = ?"; //$NON-NLS-1$
            try ( PreparedStatement ps = conn.prepareStatement(q) ) {
                this.dataSourceUtil.setParameter(ps, 1, princ.getUserId());
                ps.setString(2, princ.getRealmName());
                ps.setString(3, princ.getUserName());
                ps.executeUpdate();
            }
        }
        catch ( SQLException e ) {
            throw new AuthenticationException("Failed to remove user mapping", e); //$NON-NLS-1$
        }
    }


    /**
     * @param conn
     * @param username
     * @param realmname
     * @param found
     * @throws SQLException
     */
    private void refreshMapping ( Connection conn, String username, String realmname, UUID userId ) throws SQLException {

        final String q = "UPDATE " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "user_mapping") + //$NON-NLS-1$
                " SET last_used = CURRENT_TIMESTAMP WHERE userId = ? AND realm = ? AND username = ?"; //$NON-NLS-1$
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            this.dataSourceUtil.setParameter(ps, 1, userId);
            ps.setString(2, realmname);
            ps.setString(3, username);

            if ( ps.executeUpdate() != 1 ) {
                throw new AuthenticationException("Could not update user mapping last used timestamp for " + userId); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param conn
     * @param username
     * @return
     * @throws SQLException
     * @throws UserLicenseLimitExceededException
     * @throws LicensingException
     */
    private UserPrincipal createUserMapping ( Connection conn, String username, String realmname, UUID userId )
            throws SQLException, UserLicenseLimitExceededException {

        checkLicensing();

        UUID mappedUserId = userId;
        if ( mappedUserId == null ) {
            mappedUserId = UUID.randomUUID();
        }

        final String q = "INSERT INTO " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "user_mapping") + //$NON-NLS-1$
                " (id, version, userId, realm, username) VALUES (?,0,?,?,?)"; //$NON-NLS-1$
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            this.dataSourceUtil.setParameter(ps, 1, UUID.randomUUID());
            this.dataSourceUtil.setParameter(ps, 2, mappedUserId);
            ps.setString(3, realmname);
            ps.setString(4, username);

            if ( ps.executeUpdate() != 1 ) {
                throw new AuthenticationException("Could not create user mapping"); //$NON-NLS-1$
            }
        }

        return new UserPrincipal(realmname, mappedUserId, username);

    }


    /**
     * @throws UserLicenseLimitExceededException
     * @throws LicensingException
     * 
     */
    private void checkLicensing () throws UserLicenseLimitExceededException {
        long uc = getUserCount();

        Long limit = this.licensingService.getLicenseLimit(
            null, // $NON-NLS-1$
            "totalUsers", //$NON-NLS-1$
            5L);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("User count is %d limit is %d", uc, limit)); //$NON-NLS-1$
        }

        if ( limit < 0 ) {
            // unlimited
            log.debug("License is unlimited"); //$NON-NLS-1$
            return;
        }

        if ( uc >= limit ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("License limit exceeded (users: %d, limit %d)", uc, limit)); //$NON-NLS-1$
            }
            throw new UserLicenseLimitExceededException(uc, limit);
        }
    }


    /**
     * @param username
     * @return
     * @throws SQLException
     */
    protected UUID fetchMappedUser ( Connection conn, String username, String realmname ) throws SQLException {
        UUID found = null;
        final String q = "SELECT userId FROM " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "user_mapping") + //$NON-NLS-1$
                " WHERE username = ? AND realm = ?"; //$NON-NLS-1$
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            ps.setString(1, username);
            ps.setString(2, realmname);

            try ( ResultSet rs = ps.executeQuery() ) {
                while ( rs.next() ) {
                    if ( found != null ) {
                        throw new AuthenticationException("User mappings must be unique."); //$NON-NLS-1$
                    }
                    found = this.dataSourceUtil.extractUUID(rs, 1);
                }
            }
        }
        return found;
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.runtime.security.UserMapper#getUserCount()
     */
    @Override
    public long getUserCount () {
        try ( Connection conn = this.dataSource.getConnection() ) {
            final String q = "SELECT count(userId) FROM " + //$NON-NLS-1$
                    this.dataSourceUtil.quoteIdentifier(conn, "user_mapping"); //$NON-NLS-1$
            try ( PreparedStatement ps = conn.prepareStatement(q) ) {
                try ( ResultSet rs = ps.executeQuery() ) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }
        catch ( SQLException e ) {
            log.error("Failed to  retrieve user count", e); //$NON-NLS-1$
            return Long.MAX_VALUE;
        }
    }
}
