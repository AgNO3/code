/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2015 by mbechler
 */
package eu.agno3.runtime.security.krb.internal;


import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KeyTab;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MapCache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.MutablePrincipalCollection;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.eclipse.jetty.server.HttpConnection;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.i18n.ResourceBundleService;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealm;
import eu.agno3.runtime.net.krb5.Krb5SubjectUtil;
import eu.agno3.runtime.security.AuthorizationInfoProvider;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.event.LoginEventBuilder;
import eu.agno3.runtime.security.krb.KerberosPrincipal;
import eu.agno3.runtime.security.krb.KerberosRealmAuthToken;
import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginRealmType;
import eu.agno3.runtime.security.login.LoginSession;
import eu.agno3.runtime.security.login.MessageLoginChallenge;
import eu.agno3.runtime.security.login.PasswordChangeLoginChallenge;
import eu.agno3.runtime.security.login.PasswordLoginChallenge;
import eu.agno3.runtime.security.login.UsernameLoginChallenge;
import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.principal.factors.SSOFactor;
import eu.agno3.runtime.security.token.FallbackRealmUserPasswordChangeToken;
import eu.agno3.runtime.security.token.FallbackRealmUserPasswordToken;
import eu.agno3.runtime.security.web.login.CustomHttpAuthRealm;
import eu.agno3.runtime.security.web.login.CustomWebAuthAuthenticationException;
import eu.agno3.runtime.security.web.login.CustomWebAuthRealm;
import eu.agno3.runtime.security.web.login.RedirectLoginChallenge;
import eu.agno3.runtime.security.web.login.WebLoginConfig;
import eu.agno3.runtime.security.web.login.WebLoginContext;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    Realm.class, LoginRealm.class, AuthorizingRealm.class
}, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "auth.spnego" )
public class SPNEGOWebAuthRealm extends AuthorizingRealm implements CustomWebAuthRealm, CustomHttpAuthRealm, AuthorizationInfoProvider, Runnable {

    /**
     * 
     */
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate"; //$NON-NLS-1$

    /**
     * 
     */
    protected static final String NEGOTIATE_MECH = "Negotiate"; //$NON-NLS-1$

    protected static final String ANONYMOUS_NAME = "WELLKNOWN/ANONYMOUS"; //$NON-NLS-1$
    protected static final String ANONYMOUS_REALM = "WELLKNOWN:ANONYMOUS"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SPNEGOWebAuthRealm.class);
    private static final long CLEANUP_DELAY = 60;
    private static final long CONTEXT_TIMEOUT = 120000;
    private static final int AUTHZ_CACHE_SIZE = 1024;
    private Map<Object, AuthorizationInfo> authzCache = new LRUMap<>(AUTHZ_CACHE_SIZE);

    protected SPNEGOAcceptor acceptor;
    protected UserMapper userMapper;
    private ResourceBundleService i18n;
    private SecureRandomProvider secureRandomProvider;
    private Map<Long, ContextEntry> contexts = Collections.synchronizedMap(new HashMap<>());
    private SecureRandom secureRandom;
    private String service = "HTTP"; //$NON-NLS-1$

    private ScheduledExecutorService cleanupExecutor;

    private Set<Pattern> acceptPrincipals;
    private Set<Pattern> rejectPrincipals;

    private Map<Pattern, Set<String>> principalAddRoles;
    private Set<String> alwaysAddRoles;
    private String keyTabId;

    private KerberosRealm realm;

    private boolean initFailed = true;

    private String acceptorHostName;

    private boolean allowPasswordFallback = true;

    private javax.security.auth.kerberos.KerberosPrincipal servicePrincipal;

    private Set<String> before;

    private Set<String> after;

    private boolean doRedirect;


    /**
     * 
     */
    public SPNEGOWebAuthRealm () {
        super();
    }


    /**
     * @param cacheManager
     */
    public SPNEGOWebAuthRealm ( CacheManager cacheManager ) {
        super(cacheManager);
    }


    /**
     * @param matcher
     */
    public SPNEGOWebAuthRealm ( CredentialsMatcher matcher ) {
        super(matcher);
    }


    /**
     * @param cacheManager
     * @param matcher
     */
    public SPNEGOWebAuthRealm ( CacheManager cacheManager, CredentialsMatcher matcher ) {
        super(cacheManager, matcher);
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
    protected synchronized void setResourceBundleService ( ResourceBundleService rbs ) {
        this.i18n = rbs;
    }


    protected synchronized void unsetResourceBundleService ( ResourceBundleService rbs ) {
        if ( this.i18n == rbs ) {
            this.i18n = null;
        }
    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.secureRandomProvider = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.secureRandomProvider == srp ) {
            this.secureRandomProvider = null;
        }
    }


    @Reference ( updated = "updatedKerberosRealm" )
    protected synchronized void setKerberosRealm ( KerberosRealm kr ) {
        this.realm = kr;
    }


    protected synchronized void unsetKerberosRealm ( KerberosRealm kr ) {
        if ( this.realm == kr ) {
            this.realm = null;
        }
    }


    /**
     * @param rlm
     */
    public void updatedKerberosRealm ( KerberosRealm rlm ) {
        log.info("Realm has been updated, reloading keys"); //$NON-NLS-1$

        try {
            Subject updatedSubject = this.getSubject(this.servicePrincipal);
            this.acceptor.updateSubject(updatedSubject);
            afterUpdate(updatedSubject);
        }
        catch ( KerberosException e ) {
            log.error("Failed to update keys", e); //$NON-NLS-1$
        }

    }


    /**
     * @param updatedSubject
     */
    protected void afterUpdate ( Subject updatedSubject ) {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getAfter()
     */
    @Override
    public Collection<String> getAfter () {
        return this.before;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getBefore()
     */
    @Override
    public Collection<String> getBefore () {
        return this.after;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.CustomWebAuthRealm#supportsPasswordFallback()
     */
    @Override
    public boolean supportsPasswordFallback () {
        return this.allowPasswordFallback;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#supportPasswordChange()
     */
    @Override
    public boolean supportPasswordChange () {
        return false;
    }


    /**
     * @return the secureRandom
     */
    public SecureRandom getSecureRandom () {
        return this.secureRandom;
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.secureRandom = this.secureRandomProvider.getSecureRandom();

        setAuthorizationCache(new MapCache<>("authzCache", this.authzCache)); //$NON-NLS-1$
        setAuthorizationCachingEnabled(true);

        parseConfig(ctx.getProperties());

        String authRealmName = ConfigUtil.parseString(ctx.getProperties(), "authRealmName", this.realm.getKrbRealm()); //$NON-NLS-1$
        setName(authRealmName);

        this.servicePrincipal = this.setupEarly(ctx);

        if ( this.servicePrincipal == null ) {
            log.error("No principal configured"); //$NON-NLS-1$
            return;
        }

        this.acceptorHostName = makeAcceptorHostName(this.servicePrincipal);

        try {
            Subject subject = this.getSubject(this.servicePrincipal);
            if ( subject == null ) {
                log.error("No credentials found"); //$NON-NLS-1$
            }
            this.acceptor = new SPNEGOAcceptor(subject, this.servicePrincipal);
        }
        catch ( KerberosException e ) {
            log.error("Failed to initialize spengo realm", e); //$NON-NLS-1$
            return;
        }

        setupLate(ctx);

        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        this.cleanupExecutor.scheduleWithFixedDelay(this, CLEANUP_DELAY, CLEANUP_DELAY, TimeUnit.SECONDS);
        this.initFailed = false;
    }


    /**
     * @param servicePrincipal
     * @return
     */
    private static String makeAcceptorHostName ( javax.security.auth.kerberos.KerberosPrincipal servicePrincipal ) {
        String acceptorName = servicePrincipal.getName();

        int realmPos = acceptorName.lastIndexOf('@');
        if ( realmPos >= 0 ) {
            acceptorName = acceptorName.substring(0, realmPos);
        }

        int instanceSepPos = acceptorName.indexOf('/');
        if ( instanceSepPos >= 0 ) {
            acceptorName = acceptorName.substring(instanceSepPos + 1);
        }

        int portSepPos = acceptorName.indexOf(':');
        if ( portSepPos >= 0 ) {
            acceptorName = acceptorName.substring(0, portSepPos);
        }

        return acceptorName;
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @param cfg
     */
    protected void parseConfig ( Dictionary<String, Object> cfg ) {
        this.before = ConfigUtil.parseStringSet(cfg, "before", null); //$NON-NLS-1$
        this.after = ConfigUtil.parseStringSet(cfg, "after", null); //$NON-NLS-1$

        this.doRedirect = ConfigUtil.parseBoolean(cfg, "redirectHost", false); //$NON-NLS-1$

        this.keyTabId = ConfigUtil.parseString(cfg, "keytab", getDefaultKeytabId()); //$NON-NLS-1$

        Set<String> rejectPrincipalPatterns = ConfigUtil.parseStringSet(cfg, "rejectPrincipals", null); //$NON-NLS-1$
        this.rejectPrincipals = makePatternSet(rejectPrincipalPatterns);

        Set<String> acceptPrincipalPatterns = ConfigUtil.parseStringSet(cfg, "acceptPrincipals", null); //$NON-NLS-1$
        this.acceptPrincipals = makePatternSet(acceptPrincipalPatterns);

        this.alwaysAddRoles = ConfigUtil.parseStringSet(cfg, "alwaysAddRoles", Collections.EMPTY_SET); //$NON-NLS-1$

        Map<String, List<String>> addRolePatterns = ConfigUtil.parseStringMultiMap(cfg, "roleAddPatterns", Collections.EMPTY_MAP); //$NON-NLS-1$

        Map<Pattern, Set<String>> rolePatterns = new HashMap<>();
        for ( Entry<String, List<String>> e : addRolePatterns.entrySet() ) {
            Pattern p = makePrincipalPattern(e.getKey());
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Adding pattern %s with roles %s", p, e.getValue())); //$NON-NLS-1$
            }
            rolePatterns.put(p, new LinkedHashSet<>(e.getValue()));
        }
        this.principalAddRoles = rolePatterns;

        this.service = ConfigUtil.parseString(
            cfg,
            "service", //$NON-NLS-1$
            "HTTP"); //$NON-NLS-1$
        this.allowPasswordFallback = ConfigUtil.parseBoolean(cfg, "allowPasswordFallback", true); //$NON-NLS-1$
    }


    protected String getDefaultKeytabId () {
        return "default"; //$NON-NLS-1$
    }


    /**
     * @param patterns
     * @return
     */
    private Set<Pattern> makePatternSet ( Set<String> patterns ) {
        if ( patterns == null ) {
            return null;
        }
        if ( !patterns.isEmpty() ) {
            Set<Pattern> patternSet = new HashSet<>();
            for ( String acceptPrincipalPattern : patterns ) {
                patternSet.add(makePrincipalPattern(acceptPrincipalPattern));
            }
            return patternSet;
        }
        return null;
    }


    /**
     * @param acceptPrincipalPattern
     * @return
     */
    protected Pattern makePrincipalPattern ( String acceptPrincipalPattern ) {
        return Pattern.compile(acceptPrincipalPattern, isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.cleanupExecutor != null ) {
            this.cleanupExecutor.shutdown();
            try {
                this.cleanupExecutor.awaitTermination(30, TimeUnit.SECONDS);
            }
            catch ( InterruptedException e ) {
                log.warn("Failed to properly shut down cleanup executor", e); //$NON-NLS-1$
            }
            this.cleanupExecutor = null;
        }
    }


    protected boolean isCaseSensitive () {
        return true;
    }


    /**
     * @return the realm
     */
    public KerberosRealm getRealm () {
        return this.realm;
    }


    /**
     * @return the keyTabId
     */
    public String getKeyTabId () {
        return this.keyTabId;
    }


    /**
     * @return the service
     */
    public String getService () {
        return this.service;
    }


    /**
     * @param ctx
     * 
     */
    protected void setupLate ( ComponentContext ctx ) {

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
     * @param ctx
     */
    protected javax.security.auth.kerberos.KerberosPrincipal setupEarly ( ComponentContext ctx ) {
        String princName = String.format("%s/%s@%s", this.service, this.realm.getLocalHostname(), this.realm.getKrbRealm()); //$NON-NLS-1$
        return new javax.security.auth.kerberos.KerberosPrincipal(princName, javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_PRINCIPAL);
    }


    /**
     * @param sp
     * @return
     * @throws KerberosException
     */
    protected Subject getSubject ( javax.security.auth.kerberos.KerberosPrincipal sp ) throws KerberosException {
        if ( this.keyTabId == null ) {
            throw new KerberosException("No keytab configured"); //$NON-NLS-1$
        }

        KeyTab kt = this.realm.getKeytab(this.keyTabId, sp);

        try {
            Krb5SubjectUtil.getInitiatorSubject(kt, sp, null, false);
        }
        catch ( KerberosException e ) {
            log.debug("Failed to obtain initiate credentials", e); //$NON-NLS-1$
            String err = Krb5SubjectUtil.getErrorMessage(e);
            int code = Krb5SubjectUtil.getErrorCode(e);
            log.warn(String.format(
                "Service credentials do not seem to be valid (for initiate) %s: %s (%d)", //$NON-NLS-1$
                sp,
                err != null ? err : e.getMessage(),
                code));
        }

        return Krb5SubjectUtil.getAcceptorSubject(kt, sp);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#supports(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    public boolean supports ( AuthenticationToken token ) {
        return doesMatchRealm(token) || isPasswordFallbackToken(token);
    }


    /**
     * @param token
     * @return
     */
    protected boolean isPasswordFallbackToken ( AuthenticationToken token ) {
        return this.supportsPasswordFallback() && ( isMatchingPasswordFallbackToken(token) || isMatchingFallbackPwChangeToken(token) );
    }


    /**
     * @param token
     * @return
     */
    protected boolean isMatchingFallbackPwChangeToken ( AuthenticationToken token ) {
        return ( token instanceof FallbackRealmUserPasswordChangeToken )
                && this.getName().equals( ( (FallbackRealmUserPasswordChangeToken) token ).getRealmName());
    }


    /**
     * @param token
     * @return
     */
    protected boolean isMatchingPasswordFallbackToken ( AuthenticationToken token ) {
        return token instanceof FallbackRealmUserPasswordToken && this.getName().equals( ( (FallbackRealmUserPasswordToken) token ).getRealmName());
    }


    private boolean doesMatchRealm ( AuthenticationToken token ) {
        return token instanceof KerberosRealmAuthToken && this.getName().equals( ( (KerberosRealmAuthToken) token ).getRealm());
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
        return LoginRealmType.CUSTOM;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#getType()
     */
    @Override
    public String getType () {
        return "krb"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#isApplicable(eu.agno3.runtime.security.login.LoginContext)
     */
    @Override
    public boolean isApplicable ( LoginContext ctx ) {
        if ( this.initFailed ) {
            log.debug("Initialization has failed"); //$NON-NLS-1$
            return false;
        }

        if ( ! ( ctx instanceof WebLoginContext ) ) {
            log.debug("Not a web login context"); //$NON-NLS-1$
            return false;
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#preauth(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse preauth ( LoginContext ctx, LoginSession sess ) {
        if ( ! ( ctx instanceof WebLoginContext ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Not a WebLoginContext: " + ( ctx != null ? ctx.getClass().getName() : null )); //$NON-NLS-1$
            }
            return new AuthResponse(AuthResponseType.FAIL);
        }

        WebLoginContext wctx = (WebLoginContext) ctx;
        if ( !wctx.isHttpAuth() && this.doRedirect ) {
            String hostredirid = "host-" + getName(); //$NON-NLS-1$
            RedirectLoginChallenge hostredir = sess.getChallenge(RedirectLoginChallenge.class, hostredirid);
            if ( hostredir == null && !wctx.getLocalHostname().equals(this.realm.getLocalHostname()) ) {
                // this will redirect to the correct host, and restart the auth
                return doHostRedirect(wctx, sess, hostredirid);
            }
        }

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
     * @see eu.agno3.runtime.security.login.LoginRealm#authenticate(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse authenticate ( LoginContext ctx, LoginSession sess ) {

        if ( ! ( ctx instanceof WebLoginContext ) ) {
            return new AuthResponse(AuthResponseType.FAIL);
        }
        WebLoginContext wctx = (WebLoginContext) ctx;

        String redirid = "spnego-" + getName(); //$NON-NLS-1$
        RedirectLoginChallenge redir = sess.getChallenge(RedirectLoginChallenge.class, redirid);
        if ( !wctx.isHttpAuth() ) {
            String hostredirid = "host-" + getName(); //$NON-NLS-1$
            RedirectLoginChallenge hostredir = sess.getChallenge(RedirectLoginChallenge.class, hostredirid);
            if ( this.doRedirect && hostredir == null && !wctx.getLocalHostname().equals(this.realm.getLocalHostname()) ) {
                // this will redirect to the correct host, and restart the auth
                return doHostRedirect(wctx, sess, hostredirid);
            }

            if ( redir == null ) {
                return doNegoRedirect(wctx, sess, redirid);
            }

            if ( !redir.isComplete() ) {
                return new AuthResponse(AuthResponseType.CONTINUE);
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Have return " + redir.getResponse()); //$NON-NLS-1$
            }

            if ( "success".equals(redir.getResponse()) ) { //$NON-NLS-1$
                return new AuthResponse(AuthResponseType.COMPLETE, sess.restoreAuthInfo(this));
            }
        }

        if ( supportsPasswordFallback() ) {
            return handlePasswordFallback(sess, wctx, redir);
        }

        return new AuthResponse(AuthResponseType.FAIL);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#changePassword(eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.security.principal.UserPrincipal, eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public AuthResponse changePassword ( LoginContext ctx, UserPrincipal up, LoginSession sess ) {
        return null;
    }


    /**
     * @param sess
     * @param wctx
     * @param redir
     * @return
     */
    private AuthResponse handlePasswordFallback ( LoginSession sess, WebLoginContext wctx, RedirectLoginChallenge redir ) {
        String msgid = getName() + "-negofail"; //$NON-NLS-1$
        MessageLoginChallenge msg = sess.getChallenge(MessageLoginChallenge.class, msgid);
        if ( !wctx.isHttpAuth() && msg == null ) {
            MessageLoginChallenge mch = new MessageLoginChallenge(
                msgid,
                "custom", //$NON-NLS-1$
                handleNonSuccessReturn(redir.getResponse(), wctx.getLocale()));
            sess.addChallenge(mch);
        }

        UsernameLoginChallenge username = sess.getChallenge(UsernameLoginChallenge.class, usernameChallengeId());
        if ( username == null ) {
            log.debug("Have not yet sent username challenge"); //$NON-NLS-1$
            sess.addChallenge(new UsernameLoginChallenge(usernameChallengeId()));
        }

        PasswordLoginChallenge password = sess.getChallenge(PasswordLoginChallenge.class, passwordChallengeId());
        if ( password == null ) {
            log.debug("Have not yet sent password challenge"); //$NON-NLS-1$
            sess.addChallenge(new PasswordLoginChallenge(passwordChallengeId()));
        }

        if ( ( username == null || !username.isPrompted() ) || ( password == null || !password.isPrompted() )
                || ( !wctx.isHttpAuth() && msg == null ) ) {
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        PasswordChangeLoginChallenge pwch = sess.getChallenge(PasswordChangeLoginChallenge.class, passwordChangeChallengeId());

        AuthenticationInfo authInfo;
        if ( pwch != null ) {
            doPasswordChange(username.getResponse(), password.getResponse(), pwch.getResponse());
            authInfo = doPasswordAuth(username.getResponse(), pwch.getResponse(), null);
        }
        else {
            authInfo = doPasswordAuth(username.getResponse(), password.getResponse(), null);
        }

        username.markComplete();
        password.markComplete();
        return new AuthResponse(AuthResponseType.COMPLETE, authInfo);
    }


    protected String passwordChangeChallengeId () {
        return PasswordChangeLoginChallenge.PRIMARY_ID + "-" + getId(); //$NON-NLS-1$
    }


    protected String passwordChallengeId () {
        return PasswordLoginChallenge.PRIMARY_ID + "-" + getId(); //$NON-NLS-1$
    }


    protected String usernameChallengeId () {
        return UsernameLoginChallenge.PRIMARY_ID;
    }


    /**
     * @param ctx
     * @param sess
     * @param redirid
     * @return
     */
    private AuthResponse doHostRedirect ( WebLoginContext ctx, LoginSession sess, String redirid ) {
        String port = ":" + ctx.getLocalPort(); //$NON-NLS-1$
        String proto = ctx.isTransportSecure() ? "https://" //$NON-NLS-1$
                : "http://"; //$NON-NLS-1$
        RedirectLoginChallenge redir = new RedirectLoginChallenge(redirid);
        try {
            String authBase = ctx.getAuthBase() != null ? ctx.getAuthBase() : "/auth/"; //$NON-NLS-1$
            // using absolute here to redirect to the correct hostname for the SPN
            redir.setAbsoluteTarget(new URI(String.format(
                "%s%s%s%s%sindex.xhtml?realm=%s&authReturn=restart", //$NON-NLS-1$
                proto,
                this.realm.getLocalHostname(),
                port,
                ctx.getLocalContextPath(),
                authBase,
                this.getId())));
        }
        catch ( URISyntaxException e ) {
            throw new AuthenticationException("Failed to build uri", e); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Sending redirect to " + redir.getAbsoluteTarget() + //$NON-NLS-1$
                    " host is " + ctx.getLocalHostname() + //$NON-NLS-1$
                    " realm " + getId()); //$NON-NLS-1$
        }
        sess.addChallenge(redir);
        return new AuthResponse(AuthResponseType.BREAK);
    }


    /**
     * @param ctx
     * @param sess
     * @param redirid
     * @return
     */
    private AuthResponse doNegoRedirect ( WebLoginContext ctx, LoginSession sess, String redirid ) {
        RedirectLoginChallenge redir = new RedirectLoginChallenge(redirid);
        // using absolute here to redirect to the correct hostname for the SPN

        String port = ":" + ctx.getLocalPort(); //$NON-NLS-1$
        String proto = ctx.isTransportSecure() ? "https://" //$NON-NLS-1$
                : "http://"; //$NON-NLS-1$
        try {
            String authBase = ctx.getAuthBase() != null ? ctx.getAuthBase() : "/auth/"; //$NON-NLS-1$
            // using absolute here to redirect to the correct hostname for the SPN
            redir.setAbsoluteTarget(new URI(String.format(
                "%s%s%s%s%srealm/%s/%s", //$NON-NLS-1$
                proto,
                this.doRedirect ? this.realm.getLocalHostname() : ctx.getLocalHostname(),
                port,
                ctx.getLocalContextPath(),
                authBase,
                this.getId(),
                redir.getId())));
        }
        catch ( URISyntaxException e ) {
            throw new AuthenticationException("Failed to build uri", e); //$NON-NLS-1$
        }

        // redir.setRelativeTarget(String.format(
        // "realm/%s/%s", //$NON-NLS-1$
        // this.getId(),
        // redir.getId()));
        if ( log.isDebugEnabled() ) {
            log.debug("Sending redirect to " + redir.getRelativeTarget() + //$NON-NLS-1$
                    " host is " + ctx.getLocalHostname() + //$NON-NLS-1$
                    " realm " + getId()); //$NON-NLS-1$
        }
        sess.addChallenge(redir);
        return new AuthResponse(AuthResponseType.CONTINUE);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.CustomWebAuthRealm#handleNonSuccessReturn(java.lang.String,
     *      java.util.Locale)
     */
    @Override
    public String handleNonSuccessReturn ( String returnParam, Locale l ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Error encountered " + returnParam); //$NON-NLS-1$
        }

        ResourceBundle b = this.i18n.getBundle("eu.agno3.runtime.security.krb", l, this.getClass().getClassLoader()); //$NON-NLS-1$
        try {
            return b.getString("fail." + returnParam.trim()); //$NON-NLS-1$
        }
        catch ( MissingResourceException e ) {
            log.debug("Missing resource", e); //$NON-NLS-1$
            log.warn("Missing krb i18n resource for " + returnParam); //$NON-NLS-1$
            return b.getString("fail.unknown"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.CustomWebAuthRealm#doAuthentication(eu.agno3.runtime.security.web.login.WebLoginConfig,
     *      eu.agno3.runtime.security.login.LoginContext, eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public String doAuthentication ( WebLoginConfig config, LoginContext loginContext, LoginSession loginSession ) throws Exception {

        if ( ! ( loginContext instanceof WebLoginContext ) ) {
            throw new AuthenticationException("Is not a web login"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("redirect to handler with hostname " + this.realm.getLocalHostname()); //$NON-NLS-1$
        }
        WebLoginContext ctx = (WebLoginContext) loginContext;

        String port = ":" + ctx.getLocalPort(); //$NON-NLS-1$
        String proto = ctx.isTransportSecure() ? "https://" //$NON-NLS-1$
                : "http://"; //$NON-NLS-1$

        return String.format(
            "%s%s%s%s%srealm/%s/", //$NON-NLS-1$
            proto,
            this.doRedirect ? this.realm.getLocalHostname() : ctx.getLocalHostname(),
            port,
            ctx.getLocalContextPath(),
            ctx.getAuthBase() != null ? ctx.getAuthBase() : "/auth/", //$NON-NLS-1$
            this.getId());
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo ( AuthenticationToken tok ) {
        log.debug("doGetAuthenticationInfo"); //$NON-NLS-1$

        if ( tok instanceof FallbackRealmUserPasswordToken || tok instanceof FallbackRealmUserPasswordChangeToken ) {
            return doFallbackAuthentication(tok);
        }

        if ( ! ( tok instanceof KerberosRealmAuthToken ) ) {
            throw new UnsupportedTokenException();
        }

        try {
            return doGetPureKerberosAuthInfo((KerberosRealmAuthToken) tok);
        }
        catch ( UserLicenseLimitExceededException e ) {
            throw e.asRuntimeException();
        }
        catch ( CustomWebAuthAuthenticationException e ) {
            throw new AccountException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.AuthorizationInfoProvider#fetchAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    public AuthorizationInfo fetchAuthorizationInfo ( PrincipalCollection princs ) {
        return doGetAuthorizationInfo(princs);
    }


    /**
     * @param tok
     * @return
     */
    protected AuthenticationInfo doFallbackAuthentication ( AuthenticationToken tok ) {
        if ( tok instanceof FallbackRealmUserPasswordChangeToken ) {
            FallbackRealmUserPasswordChangeToken pwChangeTok = (FallbackRealmUserPasswordChangeToken) tok;
            String oldPassword = new String(pwChangeTok.getPassword());
            doPasswordChange(pwChangeTok.getUsername(), oldPassword, pwChangeTok.getNewPassword());
            return doPasswordAuth(pwChangeTok.getUsername(), pwChangeTok.getNewPassword(), tok.getCredentials());
        }
        else if ( tok instanceof FallbackRealmUserPasswordToken ) {
            return doPasswordAuth(
                ( (FallbackRealmUserPasswordToken) tok ).getUsername(),
                new String( ( (FallbackRealmUserPasswordToken) tok ).getPassword()),
                tok.getCredentials());
        }

        return null;
    }


    /**
     * @param username
     * @param password
     * @return
     */
    protected AuthenticationInfo doPasswordAuth ( String username, String password, Object credentials ) {
        try {
            javax.security.auth.kerberos.KerberosPrincipal userPrincipal = makePasswordPrincipal(username);

            if ( log.isDebugEnabled() ) {
                log.debug(getName() + " - Checking password for " + userPrincipal.toString()); //$NON-NLS-1$
            }

            GSSContext ctx = makePasswordCheckContext(password, userPrincipal);

            if ( ctx != null ) {
                log.debug("Got context"); //$NON-NLS-1$
                try {
                    return haveFallbackContext(userPrincipal, ctx, credentials);
                }
                finally {
                    ctx.dispose();
                }
            }

            throw new IncorrectCredentialsException();
        }
        catch ( UserLicenseLimitExceededException e ) {
            throw e.asRuntimeException();
        }
        catch ( KerberosException e ) {
            throw handlePasswordAuthError(e);
        }
        catch (
            IOException |
            GSSException e ) {
            log.debug("GSSAPI/password authentication did not succeed", e); //$NON-NLS-1$
            throw new IncorrectCredentialsException(e);
        }
    }


    /**
     * @param username
     * @return
     */
    protected javax.security.auth.kerberos.KerberosPrincipal makePasswordPrincipal ( String username ) {
        String principalName = username;
        if ( principalName.indexOf('@') < 0 ) {
            principalName = principalName + "@" + this.getRealm().getKrbRealm(); //$NON-NLS-1$
        }
        javax.security.auth.kerberos.KerberosPrincipal userPrincipal = new javax.security.auth.kerberos.KerberosPrincipal(
            principalName,
            javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_PRINCIPAL);
        return userPrincipal;
    }


    /**
     * @param e
     * @return
     */
    protected IncorrectCredentialsException handlePasswordAuthError ( KerberosException e ) {
        int errorCode = Krb5SubjectUtil.getErrorCode(e);
        String errorMessage = Krb5SubjectUtil.getErrorMessage(e);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Error %d: %s", errorCode, errorMessage)); //$NON-NLS-1$
        }

        if ( errorCode == 23 ) {
            throw new ExpiredCredentialsException(errorMessage);
        }

        log.debug("Kerberos password authentication did not succeed", e); //$NON-NLS-1$
        return new IncorrectCredentialsException(e);
    }


    /**
     * @param password
     * @param userPrincipal
     * @return
     * @throws KerberosException
     * @throws IOException
     * @throws GSSException
     */
    protected GSSContext makePasswordCheckContext ( String password, javax.security.auth.kerberos.KerberosPrincipal userPrincipal )
            throws KerberosException, IOException, GSSException {
        Subject initiatorSubject = Krb5SubjectUtil.getInitiatorSubject(userPrincipal, password, null, false);

        GSSContext ctx = Krb5SubjectUtil.validateServiceCredentials(
            userPrincipal,
            initiatorSubject,
            this.acceptor.getServicePrincipal(),
            this.acceptor.getAcceptorSubject(),
            100);
        return ctx;
    }


    /**
     * @param userPrincipal
     * @param ctx
     * @param credentials
     * @return
     * @throws GSSException
     * @throws IOException
     * @throws UserLicenseLimitExceededException
     */
    protected AuthenticationInfo haveFallbackContext ( javax.security.auth.kerberos.KerberosPrincipal userPrincipal, GSSContext ctx,
            Object credentials ) throws IOException, GSSException, UserLicenseLimitExceededException {
        MutablePrincipalCollection col = getPureKerberosPrincipalCollection(userPrincipal);
        col.addAll(getAuthFactor(), getName());
        return new SimpleAuthenticationInfo(col, credentials);
    }


    /**
     * @param username
     * @param oldPassword
     * @param newPassword
     */
    protected void doPasswordChange ( String username, String oldPassword, String newPassword ) {
        // TODO: generic kpasswd change not yet implemented
    }


    /**
     * @param tok
     * @return
     * @throws UserLicenseLimitExceededException
     */
    protected AuthenticationInfo doGetPureKerberosAuthInfo ( KerberosRealmAuthToken tok ) throws UserLicenseLimitExceededException {
        MutablePrincipalCollection col = getPureKerberosPrincipalCollection(tok.getPrincipal());
        col.addAll(getAuthFactor(), getName());
        return new SimpleAuthenticationInfo(col, tok.getPrincipal());
    }


    /**
     * @param principal
     * @return
     * @throws UserLicenseLimitExceededException
     */
    protected MutablePrincipalCollection getPureKerberosPrincipalCollection ( javax.security.auth.kerberos.KerberosPrincipal principal )
            throws UserLicenseLimitExceededException {
        String princName = principal.getName();
        int sep = princName.lastIndexOf('@');
        if ( sep >= 0 ) {
            princName = princName.substring(0, sep);
        }

        checkPrincipalName(principal.getName());

        String authRealm;
        if ( this.getRealm().getKrbRealm().equals(principal.getRealm()) ) {
            authRealm = getName();
        }
        else {
            authRealm = getName() + "/" + principal.getRealm(); //$NON-NLS-1$
        }
        UserPrincipal mappedUser = this.userMapper.getMappedUser(princName, authRealm, null);
        MutablePrincipalCollection col = new SimplePrincipalCollection(mappedUser, getName());
        col.add(new KerberosPrincipal(principal), getName());
        return col;
    }


    /**
     * @return
     */
    protected Collection<AuthFactor> getAuthFactor () {
        return Arrays.asList(new SSOFactor(this.realm.getAuthFactors()));
    }


    /**
     * @param name
     */
    protected void checkPrincipalName ( String name ) {
        int sep = name.lastIndexOf('@');
        if ( sep < 0 ) {
            throw new UnknownAccountException("Principal does not contain realm: " + name); //$NON-NLS-1$
        }
        String princName = name.substring(0, sep);
        String princRealm = name.substring(sep + 1);

        if ( ANONYMOUS_NAME.equalsIgnoreCase(princName) || ANONYMOUS_REALM.equalsIgnoreCase(princRealm) ) {
            throw new CustomWebAuthAuthenticationException(
                "anonymous", //$NON-NLS-1$
                "Anonymous principal rejected"); //$NON-NLS-1$
        }
        if ( this.rejectPrincipals != null ) {
            if ( checkPatternMatch(this.rejectPrincipals, name) ) {
                throw new CustomWebAuthAuthenticationException(
                    "principalRejected", //$NON-NLS-1$
                    "Principal rejected"); //$NON-NLS-1$
            }
        }
        if ( this.acceptPrincipals != null ) {
            if ( !checkPatternMatch(this.acceptPrincipals, name) ) {
                throw new CustomWebAuthAuthenticationException(
                    "principalNotAccepted", //$NON-NLS-1$
                    "Principal not accepted"); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param patterns
     * @param name
     * @return
     */
    private static boolean checkPatternMatch ( Set<Pattern> patterns, String name ) {
        for ( Pattern p : patterns ) {
            if ( p.matcher(name).matches() ) {
                return true;
            }
        }

        return false;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.CustomWebAuthRealm#doExternalAuthentication(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, eu.agno3.runtime.eventlog.AuditContext)
     */
    @Override
    public AuthResponse doExternalAuthentication ( HttpServletRequest req, HttpServletResponse resp, AuditContext<LoginEventBuilder> audit )
            throws IOException {

        String header = req.getHeader("Authorization"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(header) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Got header " + header); //$NON-NLS-1$
            }

            return doAuthInternal(req, resp, header, false);
        }

        String failParam = req.getParameter("fail"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(failParam) ) {
            throw new CustomWebAuthAuthenticationException(
                "noresponse", //$NON-NLS-1$
                "We did not recieve a response to our challenge"); //$NON-NLS-1$
        }

        sendChallenge(resp);
        return new AuthResponse(AuthResponseType.CONTINUE);
    }


    /**
     * @param req
     * @param resp
     * @param header
     * @param audit
     * @throws IOException
     */
    @SuppressWarnings ( "resource" )
    private AuthResponse doAuthInternal ( HttpServletRequest req, HttpServletResponse resp, String header, boolean noRemoveContext )
            throws IOException {
        int sep = header.indexOf(' ');
        if ( sep < 0 ) {
            resp.sendError(400, "Illegal authorization header"); //$NON-NLS-1$
            return new AuthResponse(AuthResponseType.FAIL);
        }

        String mech = header.substring(0, sep);
        long connId = getConnId(req);

        if ( !isAcceptedMechanism(mech) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found unsupported mechanism " + mech); //$NON-NLS-1$
            }
            throw new CustomWebAuthAuthenticationException(
                "nonegotiate", //$NON-NLS-1$
                "The browse responded with another mechanism instead of Negotiate: " + mech); //$NON-NLS-1$
        }

        String token = header.substring(sep + 1);
        byte[] byteToken = Base64.decodeBase64(token);

        AuthResponse preAuthStatus = preAuthentication(req, resp, mech, connId, byteToken);
        if ( preAuthStatus.getType() == AuthResponseType.BREAK ) {
            return new AuthResponse(AuthResponseType.CONTINUE);
        }
        else if ( preAuthStatus.getType() != AuthResponseType.CONTINUE ) {
            return preAuthStatus;
        }

        HttpConnection connection = getConnection(req);
        try {
            GSSContext ctx = getOrCreateContext(connId, connection);
            byte[] res = this.acceptor.doAccept(ctx, byteToken);
            if ( !ctx.isEstablished() ) {
                log.debug("Context not established in single request"); //$NON-NLS-1$
                this.storeContext(ctx, connection, connId);
                resp.setHeader(
                    WWW_AUTHENTICATE, // $NON-NLS-1$
                    NEGOTIATE_MECH + " " + Base64.encodeBase64String(res)); //$NON-NLS-1$
                resp.setStatus(401);
                return new AuthResponse(AuthResponseType.CONTINUE);
            }

            if ( !noRemoveContext ) {
                this.contexts.remove(connId);
            }

            if ( res != null && res.length > 0 ) {
                String resString = Base64.encodeBase64String(res);
                if ( log.isDebugEnabled() ) {
                    log.debug("Response token " + resString); //$NON-NLS-1$
                }
                resp.setHeader(
                    WWW_AUTHENTICATE, // $NON-NLS-1$
                    NEGOTIATE_MECH + " " + resString); //$NON-NLS-1$
            }

            if ( !ctx.isEstablished() || ctx.getMech() == null ) {
                throw new CustomWebAuthAuthenticationException(
                    "contextfail", //$NON-NLS-1$
                    "Context establishment failed"); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Replay detection: " + ctx.getReplayDetState()); //$NON-NLS-1$
                log.debug("Delegated: " + ctx.getCredDelegState()); //$NON-NLS-1$
                log.debug("Mech: " + ctx.getMech()); //$NON-NLS-1$
                log.debug("Mutual auth " + ctx.getMutualAuthState()); //$NON-NLS-1$
                log.debug("Remote " + ctx.getSrcName()); //$NON-NLS-1$
            }

            if ( noRemoveContext ) {
                this.storeContext(ctx, connection, connId);
            }
            return new AuthResponse(AuthResponseType.COMPLETE, contextEstablished(ctx));
        }
        catch ( CustomWebAuthAuthenticationException e ) {
            this.removeContext(connId);
            throw e;
        }
        catch ( GSSException e ) {
            log.debug("Accept failure", e); //$NON-NLS-1$
            this.contexts.remove(connId);
            throw new CustomWebAuthAuthenticationException(
                "generalacceptfail", //$NON-NLS-1$
                "Failed to accept GSSAPI context", //$NON-NLS-1$
                e);
        }
    }


    /**
     * @param req
     * @return
     */
    @SuppressWarnings ( "resource" )
    private static long getConnId ( HttpServletRequest req ) {
        HttpConnection conn = getConnection(req);
        // this is hopefully stable and unique
        return ( System.identityHashCode(conn) << 48 ) + ( req.getRemotePort() << 32 ) + req.getRemoteAddr().hashCode();
    }


    /**
     * @param req
     * @return
     */
    protected static HttpConnection getConnection ( HttpServletRequest req ) {
        return (HttpConnection) req.getAttribute("org.eclipse.jetty.server.HttpConnection"); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.CustomHttpAuthRealm#doHTTPAuthentication(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, eu.agno3.runtime.security.login.LoginContext,
     *      eu.agno3.runtime.eventlog.AuditContext)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public AuthResponse doHTTPAuthentication ( HttpServletRequest req, HttpServletResponse resp, LoginContext loginContext,
            AuditContext<LoginEventBuilder> audit ) throws IOException {

        if ( !getAcceptorHostName().equalsIgnoreCase(getRequestedHostName(req)) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Rejecting request as host name does not match service principal name: req %s actual %s", //$NON-NLS-1$
                    getRequestedHostName(req),
                    getAcceptorHostName()));
            }
            return new AuthResponse(AuthResponseType.FAIL);
        }

        long connId = getConnId(req);
        ContextEntry ctx = this.contexts.get(connId);

        if ( ctx != null ) {
            try {
                log.debug("Found existing context"); //$NON-NLS-1$
                if ( !ctx.getCtx().isEstablished() || ctx.getCtx().getMech() == null ) {
                    log.debug("Not using invalid context"); //$NON-NLS-1$
                }
                else {

                    HttpConnection conn = ctx.getConnection();

                    if ( conn == null || !conn.equals(getConnection(req)) ) {
                        log.warn("Not the same connection"); //$NON-NLS-1$
                    }
                    else {
                        return new AuthResponse(AuthResponseType.COMPLETE, contextEstablished(ctx.getCtx()));
                    }
                }
            }
            catch (
                GSSException |
                CustomWebAuthAuthenticationException e ) {
                log.debug("Destroying invalid context", e); //$NON-NLS-1$
                removeContext(connId);
            }

        }

        String header = req.getHeader("Authorization"); //$NON-NLS-1$
        if ( StringUtils.isBlank(header) ) {
            log.debug("No authorization header"); //$NON-NLS-1$
            return new AuthResponse(AuthResponseType.CONTINUE);
        }

        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Trying Negotiate auth against " + this.realm.getKrbRealm()); //$NON-NLS-1$
            }
            AuthResponse r = doAuthInternal(req, resp, header, true);
            if ( r != null && r.getType() == AuthResponseType.COMPLETE ) {
                log.debug("Completed"); //$NON-NLS-1$
                return r;
            }

            log.debug("Incomplete"); //$NON-NLS-1$

            return new AuthResponse(AuthResponseType.CONTINUE);
        }
        catch ( Exception e ) {
            log.debug("Failed", e); //$NON-NLS-1$
        }
        return new AuthResponse(AuthResponseType.FAIL);
    }


    /**
     * @return
     */
    private String getAcceptorHostName () {
        return this.acceptorHostName;
    }


    private static String getRequestedHostName ( HttpServletRequest req ) {
        String host = req.getHeader("Host"); //$NON-NLS-1$
        int portPos = host.indexOf(':');
        if ( portPos >= 0 ) {
            host = host.substring(0, portPos);
        }
        return host;
    }


    /**
     * @param ctx
     * @throws IOException
     * @throws GSSException
     */
    protected AuthenticationInfo contextEstablished ( GSSContext ctx ) throws IOException, GSSException {
        try {
            log.debug("Loggin in (GSSAPI)"); //$NON-NLS-1$
            return doGetAuthenticationInfo(new KerberosRealmAuthToken(getName(), ctx));
        }
        catch (
            AuthenticationException |
            UndeclaredThrowableException e ) {
            handleLoginFailure(e);
            return null;
        }
    }


    /**
     * @param e
     */
    protected static void handleLoginFailure ( Exception e ) {

        Throwable re = e;
        if ( e.getCause() instanceof InvocationTargetException && e.getCause().getCause() != null ) {
            re = e.getCause().getCause();
        }

        if ( re.getCause() instanceof CustomWebAuthAuthenticationException ) {
            throw (CustomWebAuthAuthenticationException) re.getCause();
        }
        throw new CustomWebAuthAuthenticationException(
            "generalauthfail", //$NON-NLS-1$
            "Failed to authenticate", //$NON-NLS-1$
            re);
    }


    /**
     * @param req
     * @param resp
     * @param mech
     * @param connId
     * @param byteToken
     */
    protected AuthResponse preAuthentication ( HttpServletRequest req, HttpServletResponse resp, String mech, long connId, byte[] byteToken ) {
        return new AuthResponse(AuthResponseType.CONTINUE);
    }


    /**
     * @param mech
     * @return
     */
    protected boolean isAcceptedMechanism ( String mech ) {
        return NEGOTIATE_MECH.equalsIgnoreCase(mech);
    }


    /**
     * @param connId
     * @param connection
     * @return
     * @throws GSSException
     */
    @SuppressWarnings ( "resource" )
    private GSSContext getOrCreateContext ( long connId, HttpConnection connection ) throws GSSException {
        ContextEntry ctx = this.contexts.get(connId);

        if ( ctx != null ) {
            HttpConnection conn = ctx.getConnection();

            if ( conn == null || !conn.equals(connection) ) {
                log.warn("Is not the same connection"); //$NON-NLS-1$
            }
            else {
                log.debug("Restoring saved context"); //$NON-NLS-1$
                ctx.used();
                return ctx.getCtx();
            }
        }

        return this.acceptor.createContext();
    }


    /**
     * @param ctx
     */
    private void storeContext ( GSSContext ctx, HttpConnection conn, long connId ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Storing context " + connId); //$NON-NLS-1$
        }
        this.contexts.put(connId, new ContextEntry(CONTEXT_TIMEOUT, ctx, conn));
    }


    /**
     * 
     * @param connId
     */
    private void removeContext ( long connId ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Removing context " + connId); //$NON-NLS-1$
        }
        this.contexts.remove(connId);
    }


    /**
     * @return the mechanisms to send a challenge for
     */
    protected List<String> getChallenges () {
        return new LinkedList<>(Arrays.asList(NEGOTIATE_MECH));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.CustomHttpAuthRealm#getChallenges(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public List<String> getChallenges ( HttpServletRequest req ) {
        if ( !getAcceptorHostName().equalsIgnoreCase(getRequestedHostName(req)) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Rejecting request as host name does not match service principal name: req %s actual %s", //$NON-NLS-1$
                    getRequestedHostName(req),
                    getAcceptorHostName()));
            }
            return Collections.EMPTY_LIST;
        }
        return getChallenges();
    }


    /**
     * @param resp
     * @throws IOException
     */
    protected void sendChallenge ( HttpServletResponse resp ) throws IOException {
        log.debug("Sending negotiate challenge"); //$NON-NLS-1$

        for ( String challenge : getChallenges() ) {
            resp.addHeader(
                WWW_AUTHENTICATE, // $NON-NLS-1$
                challenge);
        }

        resp.setStatus(401);
        resp.setContentType("text/html; charset=UTF-8"); //$NON-NLS-1$
        try ( PrintWriter writer = resp.getWriter() ) {
            writer.append("<html>"); //$NON-NLS-1$
            writer.append("<head>"); //$NON-NLS-1$
            writer.append("<title>No credentials</title>"); //$NON-NLS-1$
            writer.append("<meta http-equiv=\"refresh\" content=\"0; url=?fail=no-auth\">"); //$NON-NLS-1$
            writer.append("</head>"); //$NON-NLS-1$
            writer.append("<body>"); //$NON-NLS-1$
            writer.append("<p>We did not recieve a response for our authentication challenge,"); //$NON-NLS-1$
            writer.append(" this means either that your browser is not properly configured"); //$NON-NLS-1$
            writer.append(" or that you don't have logged in to your kerberos realm (do not have a TGT)</p>"); //$NON-NLS-1$
            writer.append("</body>"); //$NON-NLS-1$
            writer.append("</html>"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo ( PrincipalCollection princs ) {
        KerberosPrincipal kerberosPrincipal = princs.oneByType(KerberosPrincipal.class);
        if ( kerberosPrincipal == null ) {
            return null;
        }
        return new SimpleAuthorizationInfo(getPrincipalRoles(kerberosPrincipal.getKerberosPrincipal().getName()));
    }


    protected Set<String> getPrincipalRoles ( String name ) {
        Set<String> addRoles = new HashSet<>(this.alwaysAddRoles);
        for ( Entry<Pattern, Set<String>> p : this.principalAddRoles.entrySet() ) {
            if ( p.getKey().matcher(name).matches() ) {
                addRoles.addAll(p.getValue());
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Resolved roles " + addRoles); //$NON-NLS-1$
        }
        return addRoles;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        this.doCleanup();
    }


    /**
     * 
     */
    protected void doCleanup () {
        log.debug("Running cleanup"); //$NON-NLS-1$
        long now = System.currentTimeMillis();
        Set<Long> toRemove = new HashSet<>();
        for ( Entry<Long, ContextEntry> e : this.contexts.entrySet() ) {
            if ( e.getValue().getExpires() < now ) {
                toRemove.add(e.getKey());
            }
        }

        for ( Long connId : toRemove ) {
            ContextEntry remove = this.contexts.remove(connId);

            if ( remove != null ) {
                try {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Destroying context after timeout " + connId); //$NON-NLS-1$
                    }
                    remove.getCtx().dispose();
                }
                catch ( GSSException e ) {
                    log.warn("Failed to dispose of context", e); //$NON-NLS-1$
                }
            }
        }

    }

    private static class ContextEntry {

        private WeakReference<HttpConnection> conn;
        private long expires;
        private GSSContext ctx;
        private long timeout;


        /**
         * @param timeout
         * @param ctx
         * @param conn
         * 
         */
        public ContextEntry ( long timeout, GSSContext ctx, HttpConnection conn ) {
            this.timeout = timeout;
            this.expires = System.currentTimeMillis() + timeout;
            this.ctx = ctx;
            this.conn = new WeakReference<>(conn);
        }


        /**
         * 
         */
        public void used () {
            this.expires = System.currentTimeMillis() + this.timeout;
        }


        /**
         * @return the expires
         */
        public long getExpires () {
            return this.expires;
        }


        /**
         * @return the ctx
         */
        public GSSContext getCtx () {
            return this.ctx;
        }


        /**
         * 
         * @return the connection
         */
        public HttpConnection getConnection () {
            return this.conn.get();
        }
    }

}