/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.security.db.internal;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MapCache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.MutablePrincipalCollection;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.DataSourceUtil;
import eu.agno3.runtime.db.schema.SchemaManagedDataSource;
import eu.agno3.runtime.security.AuthorizationInfoProvider;
import eu.agno3.runtime.security.DefaultCredentialsMatcher;
import eu.agno3.runtime.security.ExtendedCredentialsMatcher;
import eu.agno3.runtime.security.PermissionMapper;
import eu.agno3.runtime.security.SaltedHash;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.db.impl.DatabaseAuthenticationInfo;
import eu.agno3.runtime.security.db.impl.UserQueryResult;
import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.ChallengeUtils;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginRealmType;
import eu.agno3.runtime.security.login.LoginSession;
import eu.agno3.runtime.security.login.MessageLoginChallenge;
import eu.agno3.runtime.security.login.PasswordChangeLoginChallenge;
import eu.agno3.runtime.security.login.PasswordLoginChallenge;
import eu.agno3.runtime.security.login.UsernameLoginChallenge;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.token.RealmUserPasswordChangeToken;
import eu.agno3.runtime.security.token.RealmUserPasswordToken;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    Realm.class, LoginRealm.class, PermissionMapper.class, AuthorizingRealm.class
}, configurationPid = DatabaseRealm.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class DatabaseRealm extends AuthorizingRealm implements AuthorizationInfoProvider, LoginRealm, PermissionMapper {

    /**
     * 
     */
    private static final String AUTHENTICATION_DATABASE_ERROR = "Authentication database error"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DatabaseRealm.class);

    /**
     * 
     */
    public static final String PID = "auth.db"; //$NON-NLS-1$

    private static final int AUTHZ_CACHE_SIZE = 1024;
    private Map<Object, AuthorizationInfo> authzCache = new LRUMap<>(AUTHZ_CACHE_SIZE);

    private Collection<String> before;
    private Collection<String> after;
    private boolean enabled = true;


    /**
     * 
     */
    public DatabaseRealm () {
        setName("LOCAL"); //$NON-NLS-1$
        setAuthenticationTokenClass(RealmUserPasswordToken.class);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#isPrimary()
     */
    @Override
    public boolean isPrimary () {
        return true;
    }

    private DataSource dataSource;
    private ExtendedCredentialsMatcher defaultCredentialMatcher;
    private UserMapper userMapper;
    private PasswordPolicyChecker passwordPolicy;
    private Integer maxFailedAttempts;
    private PermissionMapper permissionMapper = this;
    private DataSourceUtil dataSourceUtil;


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


    // no reference here, default implementation is self, override class to customize
    protected synchronized void setPermissionMapper ( PermissionMapper pm ) {
        this.permissionMapper = pm;
    }


    protected synchronized void unsetPermissionMapper ( PermissionMapper pm ) {
        if ( this.permissionMapper == pm ) {
            this.permissionMapper = this;
        }
    }


    @Reference
    protected synchronized void setUserMapper ( UserMapper um ) {
        this.userMapper = um;
    }


    protected synchronized void unsetUserMapper ( UserMapper um ) {
        if ( this.userMapper == um ) {
            this.userMapper = null;
        }
    }


    @Reference
    protected synchronized void setPasswordPolicyChecker ( PasswordPolicyChecker ppc ) {
        this.passwordPolicy = ppc;
    }


    protected synchronized void unsetPasswordPolicyChecker ( PasswordPolicyChecker ppc ) {
        if ( this.passwordPolicy == ppc ) {
            this.passwordPolicy = null;
        }
    }


    @Reference
    protected synchronized void setCredentialsMatcher ( DefaultCredentialsMatcher matcher ) {
        this.defaultCredentialMatcher = matcher;
    }


    protected synchronized void unsetCredentialsMatcher ( DefaultCredentialsMatcher matcher ) {
        if ( this.defaultCredentialMatcher == matcher ) {
            this.defaultCredentialMatcher = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        log.debug("Activating DatabaseRealm"); //$NON-NLS-1$
        setAuthorizationCache(new MapCache<>("authzCache", this.authzCache)); //$NON-NLS-1$
        setAuthorizationCachingEnabled(true);
        this.enabled = ConfigUtil.parseBoolean(ctx.getProperties(), "enabled", true); //$NON-NLS-1$
        this.before = ConfigUtil.parseStringSet(ctx.getProperties(), "before", null); //$NON-NLS-1$
        this.after = ConfigUtil.parseStringSet(ctx.getProperties(), "after", null); //$NON-NLS-1$
        this.init();
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        this.enabled = ConfigUtil.parseBoolean(ctx.getProperties(), "enabled", true); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getId()
     */
    @Override
    public String getId () {
        return getName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getAuthType()
     */
    @Override
    public LoginRealmType getAuthType () {
        return LoginRealmType.PASSWORD;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#supportPasswordChange()
     */
    @Override
    public boolean supportPasswordChange () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getAfter()
     */
    @Override
    public Collection<String> getAfter () {
        return this.after;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getBefore()
     */
    @Override
    public Collection<String> getBefore () {
        return this.before;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getType()
     */
    @Override
    public String getType () {
        return "db"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#isApplicable(eu.agno3.runtime.security.login.LoginContext)
     */
    @Override
    public boolean isApplicable ( LoginContext ctx ) {
        return this.enabled;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#preauth(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse preauth ( LoginContext ctx, LoginSession sess ) {
        return new AuthResponse(AuthResponseType.COMPLETE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#postauth(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse postauth ( LoginContext ctx, LoginSession sess ) {
        PasswordChangeLoginChallenge challenge = sess.getChallenge(PasswordChangeLoginChallenge.class, passwordChangeChallengeId());

        if ( challenge != null && challenge.isComplete() ) {
            try ( Connection conn = this.dataSource.getConnection() ) {
                // do change password
                if ( log.isDebugEnabled() ) {
                    log.debug("Changing password of user " + challenge.getPrincipal()); //$NON-NLS-1$
                }
                updateUserHashValue(
                    conn,
                    challenge.getPrincipal(),
                    this.defaultCredentialMatcher.generatePasswordHash(challenge.getResponse()),
                    challenge.getOldCreds(),
                    true);
            }
            catch ( SQLException e ) {
                throw new AuthenticationException("Failed to update user password", e); //$NON-NLS-1$
            }
        }

        return new AuthResponse(AuthResponseType.COMPLETE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#authenticate(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse authenticate ( LoginContext ctx, LoginSession sess ) {
        UsernameLoginChallenge username = sess.getChallenge(UsernameLoginChallenge.class, userNameChallengeId());
        if ( username == null ) {
            log.debug("Have not yet sent username challenge"); //$NON-NLS-1$
            sess.addChallenge(new UsernameLoginChallenge(userNameChallengeId()));
        }

        PasswordLoginChallenge password = sess.getChallenge(PasswordLoginChallenge.class, passwordChallengeId());
        if ( password == null ) {
            log.debug("Have not yet sent password challenge"); //$NON-NLS-1$
            sess.addChallenge(new PasswordLoginChallenge(passwordChallengeId()));
        }

        if ( ( username == null || !username.isPrompted() ) || ( password == null || !password.isPrompted() ) ) {
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        CredentialsMatcher cm = this.defaultCredentialMatcher;
        if ( cm == null ) {
            throw new AuthenticationException("No credentials matcher configured"); //$NON-NLS-1$
        }

        try ( Connection conn = this.dataSource.getConnection() ) {
            String uname = username.getResponse();

            UserQueryResult r = getHashAndSaltForUser(conn, uname);

            if ( r == null ) {
                throw new UnknownAccountException("User not found"); //$NON-NLS-1$
            }

            checkValidUser(r);

            DatabaseAuthenticationInfo info = new DatabaseAuthenticationInfo(
                r,
                this.getPrincipalCollectionForUser(uname, r),
                r.getHash().toCharArray());

            RealmUserPasswordToken token = new RealmUserPasswordToken(uname, password.getResponse(), false);
            token.setRealmName(this.getId());

            if ( !cm.doCredentialsMatch(token, info) ) {
                handleFailedAuthAttempt(conn, token, info);
                throw new IncorrectCredentialsException();
            }

            username.markComplete();

            UserPrincipal up = info.getPrincipals().oneByType(UserPrincipal.class);

            if ( r.getPwExpiry() != null && r.getPwExpiry().isBeforeNow() ) {
                // password expired, change forced
                // force to reenter old password
                return handlePwChange("pwChange.required", sess, up, conn, info); //$NON-NLS-1$
            }

            try {
                AuthFactor pwFactor = this.passwordPolicy.checkPasswordValid(new String(token.getPassword()), info.getUserResult().getLastPwChange());
                MutablePrincipalCollection princCol = (MutablePrincipalCollection) info.getPrincipals();
                princCol.add(pwFactor, getName());
            }
            catch ( PasswordPolicyException e ) {
                log.debug("Password policy not fulfilled", e); //$NON-NLS-1$
                // password does not fulfill current policy, change forced
                // force to reenter old password
                return handlePwChange("pwChange.required", sess, up, conn, info); //$NON-NLS-1$
            }

            password.markComplete();
            handleRehashing(conn, up, token, info);
            updateDbAfterSucessfulLogin(conn, uname);
            return new AuthResponse(AuthResponseType.COMPLETE, info);
        }
        catch ( UserLicenseLimitExceededException e ) {
            throw e.asRuntimeException();
        }
        catch ( SQLException e ) {
            throw new AuthenticationException(AUTHENTICATION_DATABASE_ERROR, e);
        }
    }


    /**
     * @return
     */
    protected String passwordChallengeId () {
        return PasswordLoginChallenge.PRIMARY_ID + "-" + getId(); //$NON-NLS-1$
    }


    protected String passwordChangeChallengeId () {
        return PasswordChangeLoginChallenge.PRIMARY_ID + "-" + getId(); //$NON-NLS-1$
    }


    protected String userNameChallengeId () {
        return UsernameLoginChallenge.PRIMARY_ID;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#changePassword(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.principal.UserPrincipal, eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse changePassword ( LoginContext ctx, UserPrincipal up, LoginSession sess ) {
        if ( !getId().equals(up.getRealmName()) ) {
            return new AuthResponse(AuthResponseType.FAIL);
        }

        CredentialsMatcher cm = this.defaultCredentialMatcher;
        if ( cm == null ) {
            throw new AuthenticationException("No credentials matcher configured"); //$NON-NLS-1$
        }

        PasswordLoginChallenge password = sess.getChallenge(PasswordLoginChallenge.class, passwordChallengeId());
        if ( password == null ) {
            log.debug("Have not yet sent password challenge"); //$NON-NLS-1$
            sess.addChallenge(new PasswordLoginChallenge(passwordChallengeId()));
        }

        PasswordChangeLoginChallenge pwch = sess.getChallenge(PasswordChangeLoginChallenge.class, passwordChangeChallengeId());
        if ( pwch == null ) {
            log.debug("Have not yet sent password change challenge"); //$NON-NLS-1$
            PasswordChangeLoginChallenge pwchlg = new PasswordChangeLoginChallenge(passwordChangeChallengeId());
            pwchlg.setMinimumEntropy(this.passwordPolicy.getEntropyLowerLimit());
            pwchlg.setPrincipal(up);
            sess.addChallenge(pwchlg);
        }

        if ( ( pwch == null || !pwch.isPrompted() ) || ( password == null || !password.isPrompted() ) ) {
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        try ( Connection conn = this.dataSource.getConnection() ) {
            UserQueryResult r = getHashAndSaltForUser(conn, up.getUserId());

            if ( r == null ) {
                throw new UnknownAccountException("User not found"); //$NON-NLS-1$
            }

            checkValidUser(r);

            DatabaseAuthenticationInfo info = new DatabaseAuthenticationInfo(r, this.getPrincipalCollection(up), r.getHash().toCharArray());

            RealmUserPasswordToken token = new RealmUserPasswordToken(up.getUserName(), password.getResponse(), false);
            token.setRealmName(this.getId());

            if ( !cm.doCredentialsMatch(token, info) ) {
                handleFailedAuthAttempt(conn, token, info);
                throw new IncorrectCredentialsException();
            }
            return handlePwChange(null, sess, up, conn, info);
        }
        catch ( SQLException e ) {
            throw new AuthenticationException(AUTHENTICATION_DATABASE_ERROR, e);
        }
    }


    /**
     * @param sess
     * @param conn
     * @param info
     * @param token
     * @return
     * @throws SQLException
     */
    private AuthResponse handlePwChange ( String msgid, LoginSession sess, UserPrincipal up, Connection conn, DatabaseAuthenticationInfo info )
            throws SQLException {

        PasswordChangeLoginChallenge challenge = sess.getChallenge(PasswordChangeLoginChallenge.class, passwordChangeChallengeId());
        MessageLoginChallenge message = sess.getChallenge(MessageLoginChallenge.class, msgid);

        if ( challenge == null ) {
            PasswordChangeLoginChallenge e = new PasswordChangeLoginChallenge(passwordChangeChallengeId());
            e.setMinimumEntropy(this.passwordPolicy.getEntropyLowerLimit());
            e.setOldCredentials(new String((char[]) info.getCredentials()));
            e.setPrincipal(up);
            if ( msgid != null ) {
                message = new MessageLoginChallenge(msgid); // $NON-NLS-1$
                sess.addChallenge(message);
            }
            sess.addChallenge(e);
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        if ( !challenge.isPrompted() ) {
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        AuthFactor pwFactor = this.passwordPolicy.checkPasswordChangeValid(challenge.getResponse(), ChallengeUtils.getPassword(sess));
        MutablePrincipalCollection princCol = (MutablePrincipalCollection) info.getPrincipals();
        princCol.add(pwFactor, getName());
        challenge.markComplete();
        challenge.setOldCredentials(new String((char[]) info.getCredentials()));
        challenge.setPrincipal(up);
        if ( message != null ) {
            message.markComplete();
        }
        return new AuthResponse(AuthResponseType.COMPLETE, info);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#supports(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    public boolean supports ( AuthenticationToken token ) {
        if ( ! ( token instanceof RealmUserPasswordToken ) ) {
            return false;
        }
        String realmName = ( (RealmUserPasswordToken) token ).getRealmName();
        return realmName == null || this.getName().equals(realmName);
    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo ( AuthenticationToken token ) {
        RealmUserPasswordToken upToken = (RealmUserPasswordToken) token;
        String username = upToken.getUsername();

        if ( username == null ) {
            throw new AccountException("No username available"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Checking authentication for " + username); //$NON-NLS-1$
        }

        SimpleAuthenticationInfo info = null;
        try ( Connection conn = this.dataSource.getConnection() ) {
            UserQueryResult r = getHashAndSaltForUser(conn, username);

            if ( r == null ) {
                throw new UnknownAccountException("User not found"); //$NON-NLS-1$
            }

            this.checkValidUser(r);
            this.checkPasswordValid(r, upToken);

            info = new DatabaseAuthenticationInfo(r, this.getPrincipalCollectionForUser(username, r), r.getHash().toCharArray());
            return info;
        }
        catch ( UserLicenseLimitExceededException e ) {
            throw e.asRuntimeException();
        }
        catch ( SQLException e ) {
            throw new AuthenticationException(AUTHENTICATION_DATABASE_ERROR, e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#assertCredentialsMatch(org.apache.shiro.authc.AuthenticationToken,
     *      org.apache.shiro.authc.AuthenticationInfo)
     */
    @Override
    protected void assertCredentialsMatch ( AuthenticationToken token, AuthenticationInfo info ) {
        RealmUserPasswordToken upToken = (RealmUserPasswordToken) token;
        CredentialsMatcher cm = this.defaultCredentialMatcher;
        if ( cm == null ) {
            throw new AuthenticationException("No credentials matcher configured"); //$NON-NLS-1$
        }

        if ( ! ( info instanceof DatabaseAuthenticationInfo ) ) {
            throw new AuthenticationException("Wrong auth info type"); //$NON-NLS-1$
        }

        try ( Connection conn = this.dataSource.getConnection() ) {
            if ( !cm.doCredentialsMatch(token, info) ) {
                this.handleFailedAuthAttempt(conn, token, info);
                throw new IncorrectCredentialsException();
            }
            this.handleSuccessfulAuthAttempt(conn, upToken, (DatabaseAuthenticationInfo) info);
        }
        catch ( SQLException e ) {
            throw new AuthenticationException(AUTHENTICATION_DATABASE_ERROR, e);
        }
    }


    /**
     * @param conn
     * @param token
     * @param info
     * @throws SQLException
     */
    protected void handleSuccessfulAuthAttempt ( Connection conn, RealmUserPasswordToken token, DatabaseAuthenticationInfo info )
            throws SQLException {

        log.debug("handleSuccessfulAuthAttempt"); //$NON-NLS-1$
        AuthFactor pwFactor;
        if ( token instanceof RealmUserPasswordChangeToken ) {
            RealmUserPasswordChangeToken upcToken = (RealmUserPasswordChangeToken) token;
            pwFactor = this.passwordPolicy.checkPasswordValid(upcToken.getNewPassword(), DateTime.now());
        }
        else {
            pwFactor = this.passwordPolicy.checkPasswordValid(new String(token.getPassword()), info.getUserResult().getLastPwChange());
        }
        MutablePrincipalCollection princCol = (MutablePrincipalCollection) info.getPrincipals();
        princCol.add(pwFactor, getName());

        UserPrincipal up = princCol.oneByType(UserPrincipal.class);

        boolean pwChanged = handlePasswordChangeRequest(conn, up, token, new String((char[]) info.getCredentials()));

        if ( !pwChanged && info.getCredentialsSalt() != null ) {
            handleRehashing(conn, up, token, info);
        }

        updateDbAfterSucessfulLogin(conn, (String) token.getPrincipal());
    }


    /**
     * @param conn
     * @param token
     * @throws SQLException
     */
    private void updateDbAfterSucessfulLogin ( Connection conn, String username ) throws SQLException {
        final String q = "UPDATE " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "users") + //$NON-NLS-1$
                " SET fail_attempts = 0, last_success = CURRENT_TIMESTAMP WHERE username = ?"; //$NON-NLS-1$
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            ps.setString(1, username);
            if ( ps.executeUpdate() != 1 ) {
                throw new AuthenticationException("Failed to update auth info"); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param conn
     * @param token
     * @param info
     * @throws SQLException
     */
    protected void handleRehashing ( Connection conn, UserPrincipal up, RealmUserPasswordToken token, AuthenticationInfo info ) {
        // handle credential rehashing if needed
        try {
            if ( this.defaultCredentialMatcher.hashNeedsUpdate(info) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Rehashing password of user " + token.getPrincipal()); //$NON-NLS-1$
                }
                updateUserHashValue(
                    conn,
                    up,
                    this.defaultCredentialMatcher.updateHash(token, info),
                    new String((char[]) info.getCredentials()),
                    false);
            }
        }
        catch ( SQLException e ) {
            throw new AuthenticationException("Failed to perform required password rehashing", e); //$NON-NLS-1$
        }
    }


    /**
     * @param conn
     * @param token
     * @return
     * @throws SQLException
     */
    protected boolean handlePasswordChangeRequest ( Connection conn, UserPrincipal up, RealmUserPasswordToken token, String oldHash )
            throws SQLException {
        try {
            if ( token instanceof RealmUserPasswordChangeToken ) {
                // do change password
                RealmUserPasswordChangeToken upcToken = (RealmUserPasswordChangeToken) token;
                if ( log.isDebugEnabled() ) {
                    log.debug("Changing password of user " + up); //$NON-NLS-1$
                }
                updateUserHashValue(conn, up, this.defaultCredentialMatcher.generatePasswordHash(upcToken.getNewPassword()), oldHash, true);
                return true;
            }
            return false;
        }
        catch ( SQLException e ) {
            throw new AuthenticationException("Failed to update user password", e); //$NON-NLS-1$
        }
    }


    /**
     * @param conn
     * @param principal
     * @param updateHash
     * @param b
     * @throws SQLException
     */
    private void updateUserHashValue ( Connection conn, UserPrincipal principal, SaltedHash updateHash, String oldHash, boolean updatePwChangeTime )
            throws SQLException {
        String updateChangeTime = updatePwChangeTime ? ", last_pw_change = CURRENT_TIMESTAMP" : StringUtils.EMPTY; //$NON-NLS-1$
        final String q = "UPDATE " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "users") + //$NON-NLS-1$
                " SET password_salt = ?, password = ?, pw_expires = NULL" + //$NON-NLS-1$
                updateChangeTime + " WHERE username = ? AND password = ?"; //$NON-NLS-1$
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            ps.setString(1, updateHash.getSalt());
            ps.setString(2, updateHash.getHash());
            ps.setString(3, principal.getUserName());
            ps.setString(4, oldHash);
            if ( ps.executeUpdate() != 1 ) {
                throw new AuthenticationException("Failed to update auth info"); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param conn
     * @param info
     * @param token
     * @throws SQLException
     * 
     */
    protected void handleFailedAuthAttempt ( Connection conn, AuthenticationToken token, AuthenticationInfo info ) throws SQLException {
        final String q = "UPDATE " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "users") + //$NON-NLS-1$
                " SET fail_attempts = fail_attempts + 1, last_failed = CURRENT_TIMESTAMP WHERE username = ?"; //$NON-NLS-1$
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            ps.setString(1, (String) token.getPrincipal());
            if ( ps.executeUpdate() != 1 ) {
                throw new AuthenticationException("Failed to update auth info"); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param r
     * @param upToken
     */
    protected void checkPasswordValid ( UserQueryResult r, UsernamePasswordToken upToken ) {
        if ( r.getPwExpiry() != null && r.getPwExpiry().isBeforeNow() ) {
            // password expired, change needed
            if ( ! ( upToken instanceof RealmUserPasswordChangeToken ) ) {
                throw new ExpiredCredentialsException("Account password expired, change needed"); //$NON-NLS-1$
            }

            RealmUserPasswordChangeToken upcToken = (RealmUserPasswordChangeToken) upToken;
            this.passwordPolicy.checkPasswordChangeValid(upcToken.getNewPassword(), new String(upcToken.getPassword()));
        }
    }


    /**
     * @param r
     */
    protected void checkValidUser ( UserQueryResult r ) {

        if ( r.isDisabled() ) {
            throw new LockedAccountException("Account disabled"); //$NON-NLS-1$
        }

        if ( r.getExpires() != null && r.getExpires().isBeforeNow() ) {
            throw new LockedAccountException("Account expired"); //$NON-NLS-1$
        }

        if ( this.maxFailedAttempts != null && r.getFailedLoginAttempts() >= this.maxFailedAttempts ) {
            throw new LockedAccountException("Account locked after too many failed attempts"); //$NON-NLS-1$
        }
    }


    /**
     * @param conn
     * @param username
     * @param authFactor
     * @return
     * @throws UserLicenseLimitExceededException
     * @throws SQLException
     */
    private PrincipalCollection getPrincipalCollectionForUser ( String username, UserQueryResult r ) throws UserLicenseLimitExceededException {
        return getPrincipalCollection(this.userMapper.getMappedUser(username, getName(), r.getUserId()));
    }


    /**
     * @param mappedUser
     * @return
     */
    private PrincipalCollection getPrincipalCollection ( UserPrincipal mappedUser ) {
        SimplePrincipalCollection col = new SimplePrincipalCollection(mappedUser, getName());
        this.addExtraPrincipals(col);
        return col;
    }


    /**
     * @param col
     */
    protected void addExtraPrincipals ( SimplePrincipalCollection col ) {}


    private UserQueryResult getHashAndSaltForUser ( Connection conn, String username ) throws SQLException {
        final String q = "SELECT id, password, password_salt, fail_attempts, disabled, expires, pw_expires, last_pw_change FROM " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "users") + //$NON-NLS-1$
                " WHERE username = ?"; //$NON-NLS-1$
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            ps.setString(1, username);
            try ( ResultSet rs = ps.executeQuery() ) {
                return readHashAndSaltForUser(rs);
            }
        }
    }


    private UserQueryResult getHashAndSaltForUser ( Connection conn, UUID userId ) throws SQLException {
        final String q = "SELECT id, password, password_salt, fail_attempts, disabled, expires, pw_expires, last_pw_change FROM " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "users") + //$NON-NLS-1$
                " WHERE id = ?"; //$NON-NLS-1$
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            this.dataSourceUtil.setParameter(ps, 1, userId);
            try ( ResultSet rs = ps.executeQuery() ) {
                return readHashAndSaltForUser(rs);
            }
        }
    }


    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    protected UserQueryResult readHashAndSaltForUser ( ResultSet rs ) throws SQLException {
        UserQueryResult foundResult = null;
        while ( rs.next() ) {
            if ( foundResult != null ) {
                throw new AuthenticationException("Usernames must be unique."); //$NON-NLS-1$
            }
            foundResult = processUserResult(rs);
        }
        return foundResult;
    }


    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    protected UserQueryResult processUserResult ( ResultSet rs ) throws SQLException {
        Timestamp expireTimestamp = rs.getTimestamp(6);
        Timestamp pwExpireTimestamp = rs.getTimestamp(7);
        Timestamp lastPwChangeTimestamp = rs.getTimestamp(8);

        DateTime pwExpires = null;
        if ( pwExpireTimestamp != null ) {
            pwExpires = new DateTime(pwExpireTimestamp, DateTimeZone.UTC);
        }

        DateTime expires = null;
        if ( expireTimestamp != null ) {
            expires = new DateTime(expireTimestamp, DateTimeZone.UTC);
        }

        DateTime lastPwChange = null;
        if ( lastPwChangeTimestamp != null ) {
            lastPwChange = new DateTime(lastPwChangeTimestamp, DateTimeZone.UTC);
        }

        return new UserQueryResult(
            this.dataSourceUtil.extractUUID(rs, 1),
            rs.getString(2),
            rs.getString(3),
            rs.getInt(4),
            rs.getBoolean(5),
            expires,
            pwExpires,
            lastPwChange);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.AuthorizationInfoProvider#fetchAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    public AuthorizationInfo fetchAuthorizationInfo ( PrincipalCollection princs ) {
        return this.doGetAuthorizationInfo(princs);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo ( PrincipalCollection principals ) {
        if ( log.isDebugEnabled() ) {
            log.debug("fetchAuthorizationInfo for " + principals); //$NON-NLS-1$
        }

        if ( principals == null ) {
            return null;
        }

        UserPrincipal princ = getPrincipal(principals);

        if ( princ == null ) {
            return new SimpleAuthorizationInfo();
        }

        try ( Connection conn = this.dataSource.getConnection() ) {
            Set<String> roleNames = fetchPrincipalRoles(conn, princ);
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
            Set<Permission> dbPrincPermissions = fetchRolePermissions(conn, roleNames);
            info.setObjectPermissions(dbPrincPermissions);
            return info;
        }
        catch ( SQLException e ) {
            throw new AuthorizationException("Database error while fetching roles and permissions", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#getAuthenticationCacheKey(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected Object getAuthenticationCacheKey ( PrincipalCollection principals ) {
        return principals.oneByType(UserPrincipal.class);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthorizingRealm#getAuthorizationCacheKey(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected Object getAuthorizationCacheKey ( PrincipalCollection principals ) {
        return principals.oneByType(UserPrincipal.class);
    }


    /**
     * 
     * @param princ
     */
    @Override
    public void clearCaches ( UserPrincipal princ ) {
        this.getAuthorizationCache().remove(princ);
    }


    /**
     * @param principals
     * @return
     */
    protected UserPrincipal getPrincipal ( PrincipalCollection principals ) {
        Collection<UserPrincipal> princs = principals.byType(UserPrincipal.class);

        if ( princs.size() != 1 ) {
            return null;
        }

        return princs.iterator().next();
    }


    protected Set<String> fetchPrincipalRoles ( Connection conn, UserPrincipal princ ) throws SQLException {
        final String q = "SELECT role_name,userid FROM " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "user_roles") + //$NON-NLS-1$
                " WHERE userid = ? "; //$NON-NLS-1$
        if ( log.isDebugEnabled() ) {
            log.debug("Fetching principal roles for " + princ); //$NON-NLS-1$
        }
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            this.dataSourceUtil.setParameter(ps, 1, princ.getUserId());
            try ( ResultSet rs = ps.executeQuery() ) {
                Set<String> roleNames = new LinkedHashSet<>();
                while ( rs.next() ) {
                    String roleName = rs.getString(1);
                    if ( roleName != null ) {
                        roleNames.add(roleName);
                    }
                }
                if ( log.isDebugEnabled() ) {
                    log.debug("Returning roles " + roleNames); //$NON-NLS-1$
                }
                return roleNames;
            }
        }
    }


    protected Set<Permission> fetchRolePermissions ( Connection conn, Collection<String> roleNames ) throws SQLException {
        final String q = "SELECT permission FROM " + //$NON-NLS-1$
                this.dataSourceUtil.quoteIdentifier(conn, "roles_permissions") + //$NON-NLS-1$
                " WHERE role_name = ?"; //$NON-NLS-1$
        Set<Permission> permissions = new LinkedHashSet<>();
        try ( PreparedStatement ps = conn.prepareStatement(q) ) {
            for ( String roleName : roleNames ) {
                ps.setString(1, roleName);
                try ( ResultSet rs = ps.executeQuery() ) {
                    while ( rs.next() ) {
                        String stringPerm = rs.getString(1);
                        permissions.add(this.getPermissionResolver().resolvePermission(stringPerm));
                    }
                }
            }
        }

        return permissions;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.PermissionMapper#getDefinedRoles()
     */
    @Override
    public Collection<String> getDefinedRoles () {
        try ( Connection conn = this.dataSource.getConnection() ) {
            final String q = "SELECT DISTINCT role_name FROM " + //$NON-NLS-1$
                    this.dataSourceUtil.quoteIdentifier(conn, "roles_permissions"); //$NON-NLS-1$
            Set<String> permissions = new LinkedHashSet<>();
            try ( PreparedStatement ps = conn.prepareStatement(q) ) {
                try ( ResultSet rs = ps.executeQuery() ) {
                    while ( rs.next() ) {
                        permissions.add(rs.getString(1));
                    }
                }
            }
            return permissions;
        }
        catch ( SQLException e ) {
            log.warn("Failed to enumerate principal roles", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authz.permission.RolePermissionResolver#resolvePermissionsInRole(java.lang.String)
     */
    @Override
    public Collection<Permission> resolvePermissionsInRole ( String role ) {
        return getPermissionsForRoles(Arrays.asList(role));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.PermissionMapper#getPermissionsForRoles(java.util.Collection)
     */
    @Override
    public Set<Permission> getPermissionsForRoles ( Collection<String> role ) {
        try ( Connection conn = this.dataSource.getConnection() ) {
            return this.fetchRolePermissions(conn, role);
        }
        catch ( SQLException e ) {
            log.warn("Failed to fetch principal roles", e); //$NON-NLS-1$
            return Collections.EMPTY_SET;
        }
    }
}
