/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Collection;
import java.util.Dictionary;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MapCache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.ExtendedRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.controls.PasswordExpiredControl;
import com.unboundid.ldap.sdk.controls.PasswordExpiringControl;
import com.unboundid.ldap.sdk.extensions.PasswordModifyExtendedRequest;

import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.security.AuthorizationInfoProvider;
import eu.agno3.runtime.security.PermissionMapper;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.ldap.LDAPObjectMapper;
import eu.agno3.runtime.security.ldap.LDAPOperational;
import eu.agno3.runtime.security.ldap.LDAPOperationalAttrs;
import eu.agno3.runtime.security.ldap.LDAPPrincipal;
import eu.agno3.runtime.security.ldap.LDAPRealmConfig;
import eu.agno3.runtime.security.ldap.LDAPSchemaStyle;
import eu.agno3.runtime.security.ldap.LDAPUserAttrs;
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
import eu.agno3.runtime.security.password.PasswordChangePolicyException;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.UserDetails;
import eu.agno3.runtime.security.principal.UserDetailsImpl;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.principal.factors.PasswordFactor;
import eu.agno3.runtime.security.token.RealmUserPasswordChangeToken;
import eu.agno3.runtime.security.token.RealmUserPasswordToken;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.uuid.UUIDUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    Realm.class, LoginRealm.class, AuthorizingRealm.class
}, configurationPid = LDAPRealm.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class LDAPRealm extends AuthorizingRealm implements AuthorizationInfoProvider, LoginRealm {

    /**
     * 
     */
    public static final String PID = "auth.ldap"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(LDAPRealm.class);

    private static final DateTimeFormatter RFC4517_TIME_FORMAT = DateTimeFormat.forPattern("yyyyMMddHHmmss'Z'"); //$NON-NLS-1$
    private static final DateTime AD_TIME_BASE = new DateTime(1601, 1, 1, 0, 0, DateTimeZone.UTC);
    private static final int AUTHZ_CACHE_SIZE = 1024;
    private Map<Object, AuthorizationInfo> authzCache = new LRUMap<>(AUTHZ_CACHE_SIZE);
    private LDAPClientFactory ldapClientFactory;
    private long bindTimeout = 1000;
    private UserMapper userMapper;
    private PermissionMapper permissionMapper;
    private PasswordPolicyChecker passwordPolicy;

    private LDAPRealmConfig config;

    private Set<String> before;
    private Set<String> after;
    private boolean disableAuth;
    private boolean ignoreNotFound;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws LDAPException {
        log.debug("Activating LdapRealm"); //$NON-NLS-1$
        parseConfig(ctx.getProperties());
        setName((String) ctx.getProperties().get("instanceId")); //$NON-NLS-1$
        setAuthorizationCache(new MapCache<>("authzCache", this.authzCache)); //$NON-NLS-1$
        setAuthorizationCachingEnabled(true);
        this.init();
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) throws LDAPException {
        log.debug("Reloading LdapRealm config"); //$NON-NLS-1$
        parseConfig(ctx.getProperties());
        this.authzCache.clear();
    }


    /**
     * @param dictionary
     * @throws LDAPException
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) throws LDAPException {
        this.config = LDAPRealmConfigImpl.parseConfig(cfg);
        this.before = ConfigUtil.parseStringSet(cfg, "before", null); //$NON-NLS-1$
        this.after = ConfigUtil.parseStringSet(cfg, "after", null); //$NON-NLS-1$
        this.disableAuth = ConfigUtil.parseBoolean(cfg, "disableAuth", false); //$NON-NLS-1$
        this.ignoreNotFound = ConfigUtil.parseBoolean(cfg, "ignoreNotFound", false); //$NON-NLS-1$
        this.bindTimeout = ConfigUtil.parseDuration(cfg, "bindTimeout", Duration.standardSeconds(2)).getMillis(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#isPrimary()
     */
    @Override
    public boolean isPrimary () {
        return !this.disableAuth;
    }


    @Reference
    protected synchronized void setLDAPClientFactory ( LDAPClientFactory lcf ) {
        this.ldapClientFactory = lcf;
    }


    protected synchronized void unsetLDAPClientFactory ( LDAPClientFactory lcf ) {
        if ( this.ldapClientFactory == lcf ) {
            this.ldapClientFactory = null;
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
    protected synchronized void setPermissionMapper ( PermissionMapper pm ) {
        this.permissionMapper = pm;
    }


    protected synchronized void unsetPermissionMapper ( PermissionMapper pm ) {
        if ( this.permissionMapper == pm ) {
            this.permissionMapper = null;
        }
    }


    @Reference
    protected synchronized void setPasswordPolicy ( PasswordPolicyChecker pp ) {
        this.passwordPolicy = pp;
    }


    protected synchronized void unsetPasswordPolicy ( PasswordPolicyChecker pp ) {
        if ( this.passwordPolicy == pp ) {
            this.passwordPolicy = null;
        }
    }


    /**
     * 
     */
    public LDAPRealm () {
        setAuthenticationTokenClass(RealmUserPasswordToken.class);
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
     * @see eu.agno3.runtime.security.login.LoginRealm#getType()
     */
    @Override
    public String getType () {
        return "ldap"; //$NON-NLS-1$
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
     * @see eu.agno3.runtime.security.login.LoginRealm#supportPasswordChange()
     */
    @Override
    public boolean supportPasswordChange () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#isApplicable(eu.agno3.runtime.security.login.LoginContext)
     */
    @Override
    public boolean isApplicable ( LoginContext ctx ) {
        return !this.disableAuth;
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
        return new AuthResponse(AuthResponseType.COMPLETE);
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
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo ( PrincipalCollection col ) {
        LDAPPrincipal ldapPrinc = col.oneByType(LDAPPrincipal.class);
        if ( ldapPrinc == null || !col.getRealmNames().contains(getName()) ) {
            log.trace("Not authenticated via LDAP"); //$NON-NLS-1$
            return null;
        }

        Set<String> roles;
        try ( LDAPClient cl = this.ldapClientFactory.getConnection() ) {
            roles = LDAPGroupResolverUtil.resolveUserRoles(this.config, cl, ldapPrinc.getUserDn().toString(), ldapPrinc.getUserName(), null);
        }
        catch ( LDAPException e ) {
            log.warn("Failed to resolve group memberships", e); //$NON-NLS-1$
            return null;
        }

        SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo(roles);
        authzInfo.setObjectPermissions(this.resolvePermissions(roles));
        return authzInfo;
    }


    /**
     * @return
     */
    private Set<Permission> resolvePermissions ( Set<String> roles ) {
        Set<Permission> perms = new LinkedHashSet<>();
        for ( String role : roles ) {
            perms.addAll(this.getRolePermissionResolver().resolvePermissionsInRole(role));
        }
        return perms;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#authenticate(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse authenticate ( LoginContext ctx, LoginSession sess ) {

        if ( this.disableAuth ) {
            return handleAuthDisabled(sess);
        }

        UsernameLoginChallenge username = sess.getChallenge(UsernameLoginChallenge.class, UsernameLoginChallenge.PRIMARY_ID);
        if ( username == null ) {
            sess.addChallenge(new UsernameLoginChallenge());
        }

        PasswordLoginChallenge password = sess.getChallenge(PasswordLoginChallenge.class, passwordChallengeId());
        if ( password == null ) {
            sess.addChallenge(new PasswordLoginChallenge(passwordChallengeId()));
        }

        if ( ( username == null || !username.isPrompted() ) || ( password == null || !password.isPrompted() ) ) {
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        try {
            SearchResultEntry userResult = findUser(username.getResponse());
            DN bindDn = userResult.getParsedDN();
            if ( log.isDebugEnabled() ) {
                log.debug("Found user DN " + bindDn); //$NON-NLS-1$
            }
            SimpleBindRequest bindReq = new SimpleBindRequest(bindDn, password.getResponse());
            bindReq.setResponseTimeoutMillis(this.bindTimeout);
            BindResult r;

            try {
                r = this.ldapClientFactory.tryBind(bindReq);
            }
            catch ( LDAPException e ) {
                log.debug("Exception during bind", e); //$NON-NLS-1$
                throw new IncorrectCredentialsException();
            }

            checkExpiration(r);

            if ( r.getResultCode() != ResultCode.SUCCESS ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Bind failed " + r); //$NON-NLS-1$
                }
                throw new IncorrectCredentialsException();
            }

            PasswordFactor authFactor;
            try {
                // if ( tok instanceof RealmUserPasswordChangeToken ) {
                // RealmUserPasswordChangeToken changeTok = (RealmUserPasswordChangeToken) tok;
                // userResult = doPasswordChange(tok, bindDn, bindReq, changeTok);
                // authFactor = checkPasswordPolicy(changeTok.getNewPassword(), userResult);
                // }
                // else {
                authFactor = checkPasswordPolicy(password.getResponse(), userResult);
                // }
            }
            catch ( PasswordPolicyException e ) {
                log.debug("Password policy not fulfilled", e); //$NON-NLS-1$

                return handlePwChange("pwChange.required", ctx, sess, bindReq, userResult); //$NON-NLS-1$
            }

            AuthenticationInfo ai = doBuildAuthInfo(userResult, getName(), null, bindDn, null, authFactor);
            if ( log.isDebugEnabled() ) {
                log.debug("Bind ok " + ai); //$NON-NLS-1$
            }
            return new AuthResponse(AuthResponseType.COMPLETE, ai);
        }
        catch ( UserLicenseLimitExceededException e ) {
            throw e.asRuntimeException();
        }
        catch ( LDAPException e ) {
            log.debug("LDAP failure", e); //$NON-NLS-1$
            throw new AuthenticationException("Bind authentication failed"); //$NON-NLS-1$
        }
    }


    /**
     * @param sess
     * @return
     */
    private AuthResponse handleAuthDisabled ( LoginSession sess ) {
        AuthenticationInfo mergedAuthInfo = sess.getMergedAuthInfo();
        if ( mergedAuthInfo == null ) {
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        UserPrincipal up = mergedAuthInfo.getPrincipals().oneByType(UserPrincipal.class);

        if ( up == null ) {
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        try {
            SearchResultEntry userResult;
            try {
                userResult = findUser(up.getUserName());
            }
            catch ( UnknownAccountException e ) {
                if ( this.ignoreNotFound ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("User was not found " + up.getUserName()); //$NON-NLS-1$
                    }
                    return new AuthResponse(AuthResponseType.COMPLETE);
                }
                throw e;
            }
            DN bindDn = userResult.getParsedDN();
            return new AuthResponse(AuthResponseType.COMPLETE, doBuildAuthInfo(userResult, sess.getSelectedRealmId(), up, bindDn, null, null));
        }
        catch ( UserLicenseLimitExceededException e ) {
            throw e.asRuntimeException();
        }
        catch ( LDAPException e ) {
            throw new AuthenticationException("Failed to locate LDAP user", e); //$NON-NLS-1$
        }
    }


    /**
     * @param userResult
     * @param bindDn
     * @param authFactor
     * @return
     * @throws UserLicenseLimitExceededException
     */
    private AuthenticationInfo doBuildAuthInfo ( SearchResultEntry userResult, String realm, UserPrincipal up, DN bindDn, Object credentials,
            AuthFactor authFactor ) throws UserLicenseLimitExceededException {
        String userName = userResult.getAttributeValue(this.config.getUserMapper().getAttributeName(LDAPUserAttrs.NAME));
        UUID uuid = getUUID(userResult);
        UserPrincipal mappedUser;
        if ( up != null ) {
            mappedUser = up;
        }
        else {
            mappedUser = this.userMapper.getMappedUser(userName, realm, uuid);
        }
        SimplePrincipalCollection princCollection = new SimplePrincipalCollection(mappedUser, mappedUser.getRealmName());
        princCollection.add(new LDAPPrincipal(userName, bindDn), getName());
        if ( this.config.isProvideUserDetails() ) {
            princCollection.add(makeUserDetails(userResult), getName());
        }
        if ( authFactor != null ) {
            princCollection.add(authFactor, getName());
        }
        return new SimpleAuthenticationInfo(princCollection, credentials);
    }


    /**
     * @param ctx
     * @param sess
     * @param bindReq
     * @param userResult
     * @return
     * @throws UserLicenseLimitExceededException
     */
    private AuthResponse handlePwChange ( String msgid, LoginContext ctx, LoginSession sess, SimpleBindRequest bindReq, SearchResultEntry userResult )
            throws UserLicenseLimitExceededException {
        PasswordChangeLoginChallenge challenge = sess.getChallenge(PasswordChangeLoginChallenge.class, passwordChangeChallengeId());
        MessageLoginChallenge message = sess.getChallenge(MessageLoginChallenge.class, msgid);

        if ( challenge == null ) {
            PasswordChangeLoginChallenge e = new PasswordChangeLoginChallenge(passwordChangeChallengeId());
            e.setMinimumEntropy(this.passwordPolicy.getEntropyLowerLimit());
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

        AuthFactor pwFactor = this.passwordPolicy
                .checkPasswordChangeValid(challenge.getResponse(), ChallengeUtils.getPassword(sess, passwordChallengeId()));
        challenge.markComplete();
        if ( message != null ) {
            message.markComplete();
        }
        AuthenticationInfo info = null;

        // do change password

        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Changing password of user " + userResult.getDN()); //$NON-NLS-1$
            }
            DN parsedDN = userResult.getParsedDN();
            info = doBuildAuthInfo(
                doPasswordChange(parsedDN, bindReq, ChallengeUtils.getUsername(sess), ChallengeUtils.getPassword(sess), challenge.getResponse()),
                getName(),
                null,
                parsedDN,
                null,
                pwFactor);
        }
        catch ( LDAPException e ) {
            throw new AuthenticationException("Failed to update user password", e); //$NON-NLS-1$
        }

        return new AuthResponse(AuthResponseType.COMPLETE, info);

    }


    /**
     * @return
     */
    private String passwordChallengeId () {
        return PasswordLoginChallenge.PRIMARY_ID + "-" + getId(); //$NON-NLS-1$
    }


    /**
     * @return
     */
    private String passwordChangeChallengeId () {
        return PasswordChangeLoginChallenge.PRIMARY_ID + "-" + getId(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#changePassword(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.principal.UserPrincipal, eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse changePassword ( LoginContext ctx, UserPrincipal up, LoginSession sess ) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo ( AuthenticationToken tok ) throws AuthenticationException {
        log.debug("doGetAuthenticationInfo"); //$NON-NLS-1$

        try {
            SearchResultEntry userResult = findUser((String) tok.getPrincipal());
            DN bindDn = userResult.getParsedDN();
            SimpleBindRequest bindReq = new SimpleBindRequest(bindDn, new String((char[]) tok.getCredentials()));
            bindReq.setResponseTimeoutMillis(this.bindTimeout);
            BindResult r = this.ldapClientFactory.tryBind(bindReq);

            checkExpiration(r);

            if ( r.getResultCode() != ResultCode.SUCCESS ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Bind failed " + r); //$NON-NLS-1$
                }
                throw new IncorrectCredentialsException();
            }

            PasswordFactor authFactor;
            if ( tok instanceof RealmUserPasswordChangeToken ) {
                RealmUserPasswordChangeToken changeTok = (RealmUserPasswordChangeToken) tok;
                userResult = doPasswordChange(tok, bindDn, bindReq, changeTok);
                authFactor = checkPasswordPolicy(changeTok.getNewPassword(), userResult);
            }
            else {
                authFactor = checkPasswordPolicy(new String( ( (RealmUserPasswordToken) tok ).getPassword()), userResult);
            }

            AuthenticationInfo authInfo = doBuildAuthInfo(userResult, getName(), null, bindDn, tok.getCredentials(), authFactor);
            if ( log.isDebugEnabled() ) {
                log.debug("Bind ok " + authInfo); //$NON-NLS-1$
            }
            return authInfo;
        }
        catch ( UserLicenseLimitExceededException e ) {
            throw e.asRuntimeException();
        }
        catch ( LDAPException e ) {
            log.debug("LDAP failure", e); //$NON-NLS-1$
            throw new AuthenticationException("Bind authentication failed"); //$NON-NLS-1$
        }

    }


    /**
     * @param userResult
     * @return
     */
    private UserDetails makeUserDetails ( SearchResultEntry userResult ) {
        return new UserDetailsImpl(this.config.getUserMapper().mapObject(userResult));
    }


    /**
     * @param tok
     * @param bindDn
     * @param bindReq
     * @param changeTok
     * @return
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private SearchResultEntry doPasswordChange ( AuthenticationToken tok, DN bindDn, SimpleBindRequest bindReq,
            RealmUserPasswordChangeToken changeTok ) throws LDAPException, LDAPSearchException {

        String username = (String) tok.getPrincipal();
        String password = new String(changeTok.getPassword());
        String newPassword = changeTok.getNewPassword();

        return doPasswordChange(bindDn, bindReq, username, password, newPassword);
    }


    /**
     * @param bindDn
     * @param bindReq
     * @param username
     * @param password
     * @param newPassword
     * @return
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private SearchResultEntry doPasswordChange ( DN bindDn, SimpleBindRequest bindReq, String username, String password, String newPassword )
            throws LDAPException, LDAPSearchException {
        if ( StringUtils.isBlank(newPassword) ) {
            // prevent the server from autogenerating a new password
            throw new PasswordChangePolicyException("Password is empty"); //$NON-NLS-1$
        }

        if ( this.config.isEnforcePasswordPolicyOnChange() ) {
            this.passwordPolicy.checkPasswordChangeValid(newPassword, password);
        }
        try ( LDAPClient cl = this.ldapClientFactory.getIndependedConnection(bindReq) ) {
            ExtendedRequest req = new PasswordModifyExtendedRequest(bindDn.toNormalizedString(), password, newPassword);
            ExtendedResult pwChangeRes = cl.processExtendedOperation(req);

            if ( pwChangeRes.getResultCode() != ResultCode.SUCCESS ) {
                throw new LDAPException(pwChangeRes);
            }
        }

        // bind using new password to check whether the change really worked
        SearchResultEntry userResult = findUser(username);
        BindRequest newBindReq = new SimpleBindRequest(bindDn, newPassword);
        newBindReq.setResponseTimeoutMillis(this.bindTimeout);
        BindResult r = this.ldapClientFactory.tryBind(newBindReq);

        if ( r.getResultCode() != ResultCode.SUCCESS ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Bind failed with new password " + r); //$NON-NLS-1$
            }
            throw new IncorrectCredentialsException();
        }
        return userResult;
    }


    /**
     * @param tok
     * @param userResult
     * @param dateTime
     * @return
     */
    private PasswordFactor checkPasswordPolicy ( String pw, SearchResultEntry userResult ) {
        Duration age = null;
        String lastPwChangeAttr = this.config.getUserMapper().getAttributeName(LDAPUserAttrs.LAST_PW_CHANGE);
        if ( lastPwChangeAttr != null ) {
            String lastPwChange = userResult.getAttributeValue(lastPwChangeAttr);
            if ( !StringUtils.isBlank(lastPwChange) ) {
                age = new Duration(parseUserTime(lastPwChange), DateTime.now());
            }
        }

        int entropy = this.passwordPolicy.estimateEntropy(pw);
        if ( this.config.isEnforcePasswordPolicy() && ( entropy < this.passwordPolicy.getEntropyLowerLimit() ) ) {
            throw new PasswordPolicyException();
        }

        return new PasswordFactor(entropy, age);
    }


    private DateTime parseUserTime ( String lastPwChange ) {
        if ( this.config.getStyle() == LDAPSchemaStyle.AD ) {
            // TODO: check whether this really works
            // 100-nanoseconds intervals since Jan 1, 1601 UTC.
            return AD_TIME_BASE.plus(Long.parseLong(lastPwChange) / 10);
        }

        return RFC4517_TIME_FORMAT.parseDateTime(lastPwChange);
    }


    /**
     * @param r
     * @throws LDAPException
     */
    private static void checkExpiration ( BindResult r ) throws LDAPException {
        PasswordExpiredControl pwExpired = PasswordExpiredControl.get(r);
        if ( pwExpired != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Expired credentials " + pwExpired); //$NON-NLS-1$
            }
            throw new ExpiredCredentialsException();
        }

        PasswordExpiringControl pwdExpiring = PasswordExpiringControl.get(r);
        if ( pwdExpiring != null ) {
            log.info("Password is expiring in " + pwdExpiring.getSecondsUntilExpiration()); //$NON-NLS-1$
        }
    }


    /**
     * @param userResult
     * @return
     */
    private UUID getUUID ( SearchResultEntry userResult ) {
        UUID uuid = null;
        LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> opMapper = this.config.getOperationalMapper();
        String uuidAttr = opMapper.getAttributeName(LDAPOperationalAttrs.UUID);

        if ( !StringUtils.isBlank(uuidAttr) && userResult.hasAttribute(uuidAttr) ) {
            if ( this.config.getStyle().isIdsAreBinary() ) {
                uuid = UUIDUtil.fromBytes(userResult.getAttributeValueBytes(uuidAttr));
            }
            else {
                uuid = UUID.fromString(userResult.getAttributeValue(uuidAttr));
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Found UUID " + uuid); //$NON-NLS-1$
            }
        }
        return uuid;
    }


    /**
     * @param tok
     * @return
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private SearchResultEntry findUser ( String uid ) throws LDAPException, LDAPSearchException {
        try ( LDAPClient cl = this.ldapClientFactory.getConnection() ) {
            Filter userFilter = Filter.createANDFilter(
                this.config.getUserConfig().getFilter(),
                Filter.createEqualityFilter(this.config.getUserMapper().getAttributeName(LDAPUserAttrs.NAME), uid));
            String baseDn = cl.relativeDN(this.config.getUserConfig().getBaseDN()).toString();

            if ( log.isDebugEnabled() ) {
                log.debug("Filter is " + userFilter); //$NON-NLS-1$
                log.debug("Base is " + baseDn); //$NON-NLS-1$
            }

            SearchRequest req = new SearchRequest(baseDn, this.config.getUserConfig().getScope(), userFilter.toString(), getUserAttributes());
            SearchResultEntry userRes = cl.searchForEntry(req);

            if ( userRes == null || userRes.getParsedDN() == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Could not locate user " + uid); //$NON-NLS-1$
                }
                throw new UnknownAccountException();
            }

            return userRes;
        }
    }


    /**
     * @return
     */
    private String[] getUserAttributes () {
        return new String[] {
            SearchRequest.ALL_USER_ATTRIBUTES, this.config.getOperationalMapper().getAttributeName(LDAPOperationalAttrs.UUID)
        };
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
}
