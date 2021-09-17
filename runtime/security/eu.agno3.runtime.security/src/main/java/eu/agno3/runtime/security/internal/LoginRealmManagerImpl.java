/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.realm.Realm;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.eventlog.EventLogger;
import eu.agno3.runtime.security.AuthPhase;
import eu.agno3.runtime.security.DynamicModularRealmAuthenticator;
import eu.agno3.runtime.security.event.LoginEventBuilder;
import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.ChallengeUtils;
import eu.agno3.runtime.security.login.LoginChallenge;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginRealmManager;
import eu.agno3.runtime.security.login.LoginSession;
import eu.agno3.runtime.security.login.NetworkLoginContext;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.ratelimit.LoginRateLimiter;
import eu.agno3.runtime.security.token.MultiFactorAuthenticationToken;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = LoginRealmManager.class, configurationPid = "login" )
public class LoginRealmManagerImpl implements LoginRealmManager {

    private static final Logger log = Logger.getLogger(LoginRealmManagerImpl.class);

    private LoginRateLimiter rateLimiter;
    private EventLogger eventLogger;
    private boolean allowInsecureLogins;
    private DynamicModularRealmAuthenticator authenticator;


    @Reference
    protected synchronized void setAuthenticator ( DynamicModularRealmAuthenticator dmra ) {
        this.authenticator = dmra;
    }


    protected synchronized void unsetAuthenticator ( DynamicModularRealmAuthenticator dmra ) {
        if ( this.authenticator == dmra ) {
            this.authenticator = null;
        }
    }


    @Reference
    protected synchronized void setRateLimiter ( LoginRateLimiter lrl ) {
        this.rateLimiter = lrl;
    }


    protected synchronized void unsetRateLimiter ( LoginRateLimiter lrl ) {
        if ( this.rateLimiter == lrl ) {
            this.rateLimiter = null;
        }
    }


    @Reference
    protected synchronized void setEventLogger ( EventLogger el ) {
        this.eventLogger = el;
    }


    protected synchronized void unsetEventLogger ( EventLogger el ) {
        if ( this.eventLogger == el ) {
            this.eventLogger = null;
        }
    }


    @Activate
    @Modified
    protected synchronized void activate ( ComponentContext ctx ) {
        this.allowInsecureLogins = ConfigUtil.parseBoolean(ctx.getProperties(), "allowInsecureLogins", false); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealmManager#isMultiRealm()
     */
    @Override
    public boolean isMultiRealm () {
        Collection<Realm> rlms = this.authenticator.getRealms();
        boolean found = false;
        for ( Realm r : rlms ) {
            if ( ! ( r instanceof LoginRealm ) ) {
                continue;
            }

            if ( ! ( (LoginRealm) r ).isPrimary() ) {
                continue;
            }

            if ( found ) {
                return true;
            }
            found = true;
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealmManager#getAllowInsecureLogins()
     */
    @Override
    public boolean getAllowInsecureLogins () {
        return this.allowInsecureLogins;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealmManager#getAuditContext()
     */
    @Override
    public AuditContext<LoginEventBuilder> getAuditContext () {
        return this.eventLogger.audit(LoginEventBuilder.class);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealmManager#getRateLimiter()
     */
    @Override
    public LoginRateLimiter getRateLimiter () {
        return this.rateLimiter;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealmManager#getApplicableRealms(eu.agno3.runtime.security.login.LoginContext)
     */
    @Override
    public List<LoginRealm> getApplicableRealms ( LoginContext context ) {
        List<LoginRealm> applicable = new ArrayList<>();
        List<String> realmIds = new ArrayList<>();
        for ( Realm r : this.authenticator.getRealms() ) {
            if ( ! ( r instanceof LoginRealm ) ) {
                continue;
            }

            LoginRealm lr = (LoginRealm) r;
            realmIds.add(lr.getId());

            if ( lr.isApplicable(context) ) {
                applicable.add(lr);
            }
        }
        if ( log.isTraceEnabled() ) {
            log.trace("Registered realms are " + realmIds); //$NON-NLS-1$
            log.trace("Applicable realms are " + applicable); //$NON-NLS-1$
        }
        return applicable;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealmManager#getApplicableRealmIds(eu.agno3.runtime.security.login.LoginContext)
     */
    @Override
    public List<String> getApplicableRealmIds ( LoginContext context ) {
        List<String> applicable = new ArrayList<>();
        List<String> realmIds = new ArrayList<>();
        for ( Realm r : this.authenticator.getRealms() ) {
            if ( ! ( r instanceof LoginRealm ) ) {
                continue;
            }

            LoginRealm lr = (LoginRealm) r;
            realmIds.add(lr.getId());

            if ( lr.isApplicable(context) ) {
                applicable.add(lr.getId());
            }
        }
        if ( log.isTraceEnabled() ) {
            log.trace("Registered realms are " + realmIds); //$NON-NLS-1$
            log.trace("Applicable realm ids are " + applicable); //$NON-NLS-1$
        }
        return applicable;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealmManager#mapRealmIds(java.util.List)
     */
    @Override
    public List<LoginRealm> mapRealmIds ( List<String> applicableRealms ) {
        if ( applicableRealms == null ) {
            return Collections.EMPTY_LIST;
        }

        Set<String> selected = new HashSet<>(applicableRealms);
        List<LoginRealm> applicable = new ArrayList<>();

        for ( Realm r : this.authenticator.getRealms() ) {
            if ( ! ( r instanceof LoginRealm ) ) {
                continue;
            }
            LoginRealm lr = (LoginRealm) r;
            if ( selected.contains(lr.getId()) ) {
                applicable.add(lr);
            }
        }
        return applicable;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealmManager#getStaticDefaultRealm(eu.agno3.runtime.security.login.LoginContext)
     */
    @Override
    public LoginRealm getStaticDefaultRealm ( LoginContext context ) {
        List<LoginRealm> applicableRealms = this.getApplicableRealms(context);

        if ( applicableRealms.isEmpty() ) {
            return null;
        }

        return applicableRealms.get(0);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealmManager#getRealm(java.lang.String)
     */
    @Override
    public LoginRealm getRealm ( String id ) {
        for ( Realm r : this.authenticator.getRealms() ) {
            if ( ! ( r instanceof LoginRealm ) ) {
                continue;
            }
            LoginRealm lr = (LoginRealm) r;

            if ( id.equals(lr.getId()) ) {
                return lr;
            }
        }
        return null;
    }


    @Override
    public AuthResponse changePassword ( LoginRealm primary, UserPrincipal up, LoginContext ctx, LoginSession sess ) {
        String sourceAddress = ctx instanceof NetworkLoginContext ? ( (NetworkLoginContext) ctx ).getRemoteAddress() : null;
        try ( AuditContext<LoginEventBuilder> audit = getAuditContext() ) {
            audit.builder().action("CHANGE_PASSWORD").context(ctx); //$NON-NLS-1$
            audit.builder().principal(up);

            int remainThrottle = getRateLimiter().getNextLoginDelay(up, sourceAddress);
            if ( remainThrottle > 0 ) {
                audit.builder().status("THROTTLE"); //$NON-NLS-1$
                sess.setThrottleDelay(remainThrottle);
                return null;
            }

            try {
                AuthResponse resp = doChangePasswordStack(primary, up, ctx, sess);
                if ( log.isDebugEnabled() ) {
                    log.debug("Response is " + ( resp != null ? resp.getType() : null )); //$NON-NLS-1$
                }

                if ( resp != null && resp.getType() == AuthResponseType.COMPLETE ) {
                    if ( resp.getAuthInfo() == null ) {
                        audit.builder().fail(AuditStatus.UNAUTHENTICATED);
                        throw new AuthenticationException("authInfo must not be NULL"); //$NON-NLS-1$
                    }
                    UserPrincipal resolved = resp.getAuthInfo().getPrincipals().oneByType(UserPrincipal.class);
                    if ( resolved != null ) {
                        audit.builder().principal(resolved);
                    }
                    SecurityUtils.getSubject().login(new MultiFactorAuthenticationToken(resp.getAuthInfo()));
                    audit.builder().success();
                    getRateLimiter().recordSuccessAttempt(up, sourceAddress);
                }
                else if ( resp != null && resp.getType() == AuthResponseType.CONTINUE ) {
                    audit.suppress();
                }
                else {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Invalid auth state " + ( resp != null ? resp.getType() : null )); //$NON-NLS-1$
                    }
                    audit.builder().fail(AuditStatus.INTERNAL);
                    throw new AuthenticationException();
                }
                return resp;
            }
            catch ( AuthenticationException e ) {
                resetFailedChallenges(sess);
                if ( ! ( e instanceof PasswordPolicyException ) ) {
                    int newThrottleDelay = getRateLimiter().recordFailAttempt(up, sourceAddress);
                    if ( log.isDebugEnabled() ) {
                        log.debug("Throttle delay is " + newThrottleDelay); //$NON-NLS-1$
                    }
                    if ( newThrottleDelay > 0 ) {
                        sess.setThrottleDelay(newThrottleDelay);
                    }
                    else {
                        sess.setThrottleDelay(null);
                    }
                }
                else {
                    log.debug("Skipping rate limiting for password policy error"); //$NON-NLS-1$
                    getRateLimiter().recordSuccessAttempt(up, sourceAddress);
                }
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL); // $NON-NLS-1$
                throw e;
            }
        }
    }


    /**
     * @param sess
     */
    private static void resetFailedChallenges ( LoginSession sess ) {
        log.debug("Resetting failed challenges"); //$NON-NLS-1$
        for ( LoginChallenge<?> ch : sess.getChallenges() ) {
            if ( !ch.isComplete() && ch.isResetOnFailure() ) {
                ch.reset();
            }
        }
    }


    /**
     * @param r
     * @param p
     * @param ctx
     * @param sess
     * @param up
     * @return
     */
    private static AuthResponse doRunPhase ( LoginRealm r, AuthPhase p, LoginContext ctx, LoginSession sess, UserPrincipal up ) {
        switch ( p ) {
        case PREAUTH:
            return r.preauth(ctx, sess);

        case AUTH:
            return r.authenticate(ctx, sess);

        case POSTAUTH:
            return r.postauth(ctx, sess);

        case PWCHANGE:
            return r.changePassword(ctx, up, sess);

        default:
            throw new AuthenticationException("Invalid auth phase " + p); //$NON-NLS-1$
        }

    }


    protected AuthResponse doRunPhases ( LoginRealm primary, LoginContext ctx, LoginSession sess, UserPrincipal up, AuthPhase... phases ) {
        List<LoginRealm> stack = this.authenticator.getStack(primary);
        Set<String> stackIds = stack.stream().map(x -> x.getId()).collect(Collectors.toSet());
        Iterator<AuthPhase> iterator = Arrays.asList(phases).iterator();
        while ( iterator.hasNext() ) {
            AuthPhase p = iterator.next();
            Map<String, AuthResponse> pr = sess.getPhaseResponses(p);
            for ( LoginRealm r : stack ) {
                String rid = r.getId();
                AuthResponse prevResponse = pr.get(rid);

                if ( prevResponse != null && prevResponse.getType() == AuthResponseType.COMPLETE ) {
                    // skip if response already indicated completion
                    continue;
                }

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Running phase %s on %s", p, rid)); //$NON-NLS-1$
                }
                AuthResponse response = doRunPhase(r, p, ctx, sess, up);
                if ( log.isDebugEnabled() ) {
                    log.debug("Response is " + response); //$NON-NLS-1$
                }
                pr.put(rid, response);
                if ( response == null || response.getType() == AuthResponseType.FAIL || response.getType() == AuthResponseType.THROTTLE ) {
                    return response;
                }
                else if ( response.getType() == AuthResponseType.BREAK ) {
                    break;
                }
                else if ( response.getType() == AuthResponseType.COMPLETE ) {
                    if ( response.getAuthInfo() != null ) {
                        sess.storeAuthInfo(r, response.getAuthInfo());
                    }
                }
            }

            if ( pr.keySet().size() == stackIds.size() && pr.keySet().containsAll(stackIds) ) {
                boolean allComplete = true;
                for ( AuthResponse res : pr.values() ) {
                    allComplete &= res.getType() == AuthResponseType.COMPLETE;
                }

                if ( allComplete ) {
                    if ( !iterator.hasNext() ) {
                        // this is the last phase
                        return new AuthResponse(AuthResponseType.COMPLETE, sess.getMergedAuthInfo());
                    }
                    log.debug("all are complete, move to next phase"); //$NON-NLS-1$
                    continue;
                }
            }

            log.debug("There are incomplete modules, continue in " + p); //$NON-NLS-1$
            // a module might have returned BREAK so this is not a failure
            return new AuthResponse(AuthResponseType.CONTINUE);
        }
        // empty phase set
        return new AuthResponse(AuthResponseType.FAIL);
    }


    /**
     * @param primary
     * @param ctx
     * @param sess
     * @return
     */
    protected AuthResponse doChangePasswordStack ( LoginRealm primary, UserPrincipal up, LoginContext ctx, LoginSession sess ) {
        return doRunPhases(primary, ctx, sess, up, AuthPhase.PREAUTH, AuthPhase.PWCHANGE, AuthPhase.POSTAUTH);
    }


    @Override
    public AuthResponse authenticate ( LoginRealm primary, LoginContext ctx, LoginSession sess ) {
        if ( primary == null ) {
            log.debug("No primary realm"); //$NON-NLS-1$
            return new AuthResponse(AuthResponseType.FAIL);
        }

        if ( ctx == null ) {
            log.debug("No login context"); //$NON-NLS-1$
            return new AuthResponse(AuthResponseType.FAIL);
        }

        if ( sess.getThrottleDelay() != null ) {
            return new AuthResponse(AuthResponseType.THROTTLE);
        }

        try ( AuditContext<LoginEventBuilder> audit = getAuditContext() ) {
            audit.builder().realm(primary);
            audit.builder().context(sess.getLoginContext());

            UserPrincipal up = makePrincipal(primary, sess);
            if ( up != null ) {
                audit.builder().principal(up);
            }

            String sourceAddress = ( ctx instanceof NetworkLoginContext ) ? ( (NetworkLoginContext) ctx ).getRemoteAddress() : null;
            int remainThrottle = getRateLimiter().getNextLoginDelay(up, sourceAddress);
            if ( remainThrottle > 0 ) {
                audit.builder().status("THROTTLE"); //$NON-NLS-1$
                sess.setThrottleDelay(remainThrottle);
                return new AuthResponse(AuthResponseType.THROTTLE);
            }

            try {
                AuthResponse resp = doAuthenticateStack(primary, ctx, sess);
                if ( log.isDebugEnabled() ) {
                    log.debug("Response is " + ( resp != null ? resp.getType() : null )); //$NON-NLS-1$
                }

                if ( resp != null && resp.getType() == AuthResponseType.COMPLETE ) {
                    if ( resp.getAuthInfo() == null ) {
                        audit.builder().fail(AuditStatus.UNAUTHENTICATED);
                        throw new AuthenticationException("authInfo must not be NULL"); //$NON-NLS-1$
                    }
                    UserPrincipal resolved = resp.getAuthInfo().getPrincipals().oneByType(UserPrincipal.class);
                    if ( resolved != null ) {
                        audit.builder().principal(resolved);
                    }
                    sess.destroy();
                    log.debug("Doing multifactor login"); //$NON-NLS-1$
                    SecurityUtils.getSubject().login(new MultiFactorAuthenticationToken(resp.getAuthInfo()));
                    audit.builder().success();
                }
                else if ( resp != null && resp.getType() == AuthResponseType.CONTINUE ) {
                    audit.suppress();
                }
                else {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Invalid auth state " + ( resp != null ? resp.getType() : null )); //$NON-NLS-1$
                    }
                    audit.builder().fail(AuditStatus.INTERNAL);
                    throw new AuthenticationException();
                }
                return resp;
            }
            catch ( AuthenticationException e ) {
                resetFailedChallenges(sess);
                audit.builder().fail(e);
                log.debug("Login failed", e); //$NON-NLS-1$
                int newThrottleDelay = getRateLimiter().recordFailAttempt(up, sourceAddress);
                if ( log.isDebugEnabled() ) {
                    log.debug("Throttle delay is " + newThrottleDelay); //$NON-NLS-1$
                }
                if ( newThrottleDelay > 0 ) {
                    sess.setThrottleDelay(newThrottleDelay);
                }
                else {
                    sess.setThrottleDelay(null);
                }
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                log.warn("Internal authentication error", e); //$NON-NLS-1$
                throw e;
            }
        }

    }


    /**
     * @param primary
     * @param ctx
     * @param sess
     * @return
     */
    protected AuthResponse doAuthenticateStack ( LoginRealm primary, LoginContext ctx, LoginSession sess ) {
        return doRunPhases(primary, ctx, sess, null, AuthPhase.PREAUTH, AuthPhase.AUTH, AuthPhase.POSTAUTH);
    }


    /**
     * @param primary
     * @param sess
     * @return
     */
    private static UserPrincipal makePrincipal ( LoginRealm primary, LoginSession sess ) {
        AuthenticationInfo ai = sess.getMergedAuthInfo();
        if ( ai != null ) {
            UserPrincipal up = ai.getPrincipals().oneByType(UserPrincipal.class);
            if ( up != null ) {
                return up;
            }
        }
        String username = ChallengeUtils.getUsername(sess);
        return new UserPrincipal(primary.getId(), null, username != null ? username.toLowerCase(Locale.ROOT) : null);
    }
}
