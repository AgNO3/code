/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.06.2015 by mbechler
 */
package eu.agno3.runtime.security.web.internal;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.http.ua.UACapability;
import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.security.event.LoginEventBuilder;
import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.LoginChallenge;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginRealmManager;
import eu.agno3.runtime.security.login.LoginRealmType;
import eu.agno3.runtime.security.login.LoginSession;
import eu.agno3.runtime.security.login.PasswordLoginChallenge;
import eu.agno3.runtime.security.login.UsernameLoginChallenge;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.token.MultiFactorAuthenticationToken;
import eu.agno3.runtime.security.web.ContextUtil;
import eu.agno3.runtime.security.web.filter.HTTPAuthenticationFilter;
import eu.agno3.runtime.security.web.login.CustomHttpAuthRealm;

import net.sf.uadetector.ReadableUserAgent;


/**
 * @author mbechler
 *
 */

@Component ( service = HTTPAuthenticationFilter.class )
public class HTTPAuthenticationFilterImpl implements HTTPAuthenticationFilter {

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(HTTPAuthenticationFilterImpl.class);

    private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"; //$NON-NLS-1$
    private static final String AUTHORIZATION_HEADER = "Authorization"; //$NON-NLS-1$
    private static final Charset ISO88591 = Charset.forName("ISO-8859-1"); //$NON-NLS-1$

    private LoginRealmManager loginRealmManager;
    private UADetector uaDetector;


    @Reference
    protected synchronized void setLoginRealmManager ( LoginRealmManager lrm ) {
        this.loginRealmManager = lrm;
    }


    protected synchronized void unsetLoginRealmManager ( LoginRealmManager lrm ) {
        if ( this.loginRealmManager == lrm ) {
            this.loginRealmManager = null;
        }
    }


    @Reference
    protected synchronized void setUADetector ( UADetector uad ) {
        this.uaDetector = uad;
    }


    protected synchronized void unsetUADetector ( UADetector uad ) {
        if ( this.uaDetector == uad ) {
            this.uaDetector = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy () {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init ( FilterConfig cfg ) throws ServletException {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    public void doFilter ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws IOException, ServletException {

        if ( SecurityUtils.getSubject().isAuthenticated() ) {
            log.debug("Already authenticated as " + SecurityUtils.getSubject().getPrincipals()); //$NON-NLS-1$
            chain.doFilter(req, resp);
            return;
        }

        try ( AuditContext<LoginEventBuilder> audit = this.loginRealmManager.getAuditContext() ) {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            boolean disableNonPasswordAuth = isClientPasswordOnly(httpReq);
            boolean multiRealmChallenge = httpReq.getHeader("X-Multi-Realm-Auth") != null; //$NON-NLS-1$
            LoginContext loginContext = ContextUtil.makeLoginContext(null, httpReq, true);

            audit.builder().context(loginContext);
            try {

                String defaultPasswordRealm = null;
                boolean doPasswordAuth = false;

                List<CustomHttpAuthRealm> httpAuthRealms = new ArrayList<>();
                List<String> challenges = new ArrayList<>();

                List<LoginRealm> applicableRealms = this.loginRealmManager.getApplicableRealms(loginContext);
                if ( log.isDebugEnabled() ) {
                    log.debug("Applicable realms are " + applicableRealms); //$NON-NLS-1$
                }
                for ( LoginRealm realm : applicableRealms ) {

                    if ( realm.getAuthType() == LoginRealmType.OTP || realm.getAuthType() == LoginRealmType.PASSWORD ) {
                        doPasswordAuth = true;
                        if ( multiRealmChallenge ) {
                            challenges.add(makeBasicHeader(realm.getId(), realm.getAuthType()));
                        }
                    }
                    else if ( realm instanceof CustomHttpAuthRealm ) {
                        CustomHttpAuthRealm webRealm = (CustomHttpAuthRealm) realm;

                        if ( !disableNonPasswordAuth ) {
                            httpAuthRealms.add(webRealm);
                            challenges.addAll(webRealm.getChallenges(httpReq));
                            if ( webRealm.supportsPasswordFallback() ) {
                                doPasswordAuth = true;
                                if ( multiRealmChallenge ) {
                                    challenges.add(makeBasicHeader(realm.getId(), LoginRealmType.PASSWORD));
                                }
                            }
                        }
                        else {
                            log.debug("Skipping custom realm " + realm.getId()); //$NON-NLS-1$
                        }
                    }
                    else {
                        log.debug("Skipping unsupported realm " + realm.getId()); //$NON-NLS-1$
                    }
                }

                if ( !multiRealmChallenge && doPasswordAuth ) {
                    LoginRealm staticDefaultRealm = this.loginRealmManager.getStaticDefaultRealm(loginContext);
                    if ( staticDefaultRealm == null ) {
                        log.debug("No default auth realm found"); //$NON-NLS-1$
                        audit.builder().fail(AuditStatus.UNAUTHENTICATED);
                        httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No authentication mechanisms found"); //$NON-NLS-1$
                        return;
                    }

                    defaultPasswordRealm = staticDefaultRealm.getId();
                    challenges.add(0, makeBasicHeader(defaultPasswordRealm));
                }

                if ( doPasswordAuth ) {
                    Enumeration<String> authHeader = httpReq.getHeaders(AUTHORIZATION_HEADER);
                    while ( authHeader != null && authHeader.hasMoreElements() ) {
                        String headerVal = authHeader.nextElement();
                        if ( log.isDebugEnabled() ) {
                            log.debug("Found auth header " + headerVal); //$NON-NLS-1$
                        }

                        if ( doPasswordAuthentication(httpReq, httpResp, loginContext, defaultPasswordRealm, headerVal, audit) ) {
                            doIfAuthed(httpReq, httpResp, chain, audit);
                            return;
                        }
                    }
                }

                for ( CustomHttpAuthRealm r : httpAuthRealms ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Trying realm " + r.getId()); //$NON-NLS-1$
                    }

                    audit.builder().realm(r);
                    AuthResponse ar = r.doHTTPAuthentication(httpReq, httpResp, loginContext, audit);
                    if ( ar != null && ar.getType() == AuthResponseType.COMPLETE && ar.getAuthInfo() != null ) {
                        UserPrincipal resolved = ar.getAuthInfo().getPrincipals().oneByType(UserPrincipal.class);
                        if ( resolved != null ) {
                            audit.builder().principal(resolved);
                        }
                        SecurityUtils.getSubject().login(new MultiFactorAuthenticationToken(ar.getAuthInfo()));
                        doIfAuthed(httpReq, httpResp, chain, audit);
                        return;
                    }
                    else if ( ar != null && ar.getType() == AuthResponseType.CONTINUE ) {
                        log.debug("Auth needs to continue"); //$NON-NLS-1$
                    }
                    else {
                        audit.builder().fail(AuditStatus.UNAUTHENTICATED);
                        sendChallenges(httpReq, httpResp, challenges);
                        httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }

                audit.builder().ignore();
                sendChallenges(httpReq, httpResp, challenges);
                httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            catch ( AuthenticationException e ) {
                log.debug("Auth failed", e); //$NON-NLS-1$
                audit.builder().fail(e);
                throw e;
            }

        }
    }


    /**
     * @param httpReq
     * @return
     */
    private boolean isClientPasswordOnly ( HttpServletRequest httpReq ) {
        ReadableUserAgent parsed = this.uaDetector.parse(httpReq);
        if ( parsed == null ) {
            // default to proper challenge
            return false;
        }

        boolean res = this.uaDetector.hasCapability(UACapability.NO_MULTI_AUTH, parsed, this.uaDetector.getUA(httpReq));
        if ( res && log.isDebugEnabled() ) {
            log.debug("Does not support multiple auth challenges " + parsed); //$NON-NLS-1$
        }
        return res;
    }


    /**
     * @param httpReq
     * @param httpResp
     * @param challenges
     */
    private static void sendChallenges ( HttpServletRequest httpReq, HttpServletResponse httpResp, List<String> challenges ) {
        Set<String> dedup = new HashSet<>();
        boolean mergeHeaders = false;
        List<String> merge = new ArrayList<>();

        for ( String challenge : challenges ) {
            if ( dedup.contains(challenge) ) {
                continue;
            }
            dedup.add(challenge);

            if ( !mergeHeaders ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Sending challenge: " + challenge); //$NON-NLS-1$
                }

                httpResp.addHeader(WWW_AUTHENTICATE_HEADER, challenge);
            }
            else {
                merge.add(challenge);
            }
        }

        if ( mergeHeaders ) {
            httpResp.setHeader(WWW_AUTHENTICATE_HEADER, StringUtils.join(merge, ", ")); //$NON-NLS-1$
        }
    }


    /**
     * @param req
     * @param resp
     * @param chain
     * @param audit
     * @throws ServletException
     * @throws IOException
     */
    private static void doIfAuthed ( HttpServletRequest req, HttpServletResponse resp, FilterChain chain, AuditContext<LoginEventBuilder> audit )
            throws IOException, ServletException {
        if ( SecurityUtils.getSubject().isAuthenticated() ) {
            audit.builder().log();
            audit.builder().ignore();
            chain.doFilter(req, resp);
            return;
        }
        audit.builder().fail(AuditStatus.UNAUTHENTICATED);
        log.debug("Not authenticated"); //$NON-NLS-1$
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed"); //$NON-NLS-1$
    }


    /**
     * @param httpResp
     * @param loginContext
     * @param passwordRealm
     * @param authHeader
     * @param audit
     * @return
     * @throws IOException
     */
    private boolean doPasswordAuthentication ( HttpServletRequest httpReq, HttpServletResponse httpResp, LoginContext loginContext,
            String passwordRealm, String authHeader, AuditContext<LoginEventBuilder> audit ) throws IOException {

        try {

            if ( StringUtils.isBlank(authHeader) || !authHeader.toUpperCase().startsWith("BASIC ") ) { //$NON-NLS-1$
                return false;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Got basic auth header"); //$NON-NLS-1$
            }

            byte[] decoded = Base64.getDecoder().decode(authHeader.substring(6));
            String decodedStr = new String(decoded, ISO88591);

            int indexOfSep = decodedStr.indexOf(':');
            if ( indexOfSep < 0 ) {
                return false;
            }

            try {
                AuthResponse resp = makePasswordAuth(audit, loginContext, passwordRealm, decodedStr, indexOfSep);

                if ( resp == null || resp.getType() != AuthResponseType.COMPLETE || resp.getAuthInfo() == null ) {
                    audit.builder().fail(AuditStatus.VALIDATION);
                    httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed"); //$NON-NLS-1$
                    return false;
                }

                httpReq.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);

                UserPrincipal resolved = resp.getAuthInfo().getPrincipals().oneByType(UserPrincipal.class);
                if ( resolved != null ) {
                    audit.builder().principal(resolved);
                }

                SecurityUtils.getSubject().login(new MultiFactorAuthenticationToken(resp.getAuthInfo()));
                if ( log.isDebugEnabled() ) {
                    log.debug("Logged in as " + SecurityUtils.getSubject()); //$NON-NLS-1$
                }

                if ( SecurityUtils.getSubject().isAuthenticated() ) {
                    audit.builder().success();
                    return true;
                }
            }
            catch (
                AuthenticationException |
                UndeclaredThrowableException e ) {

                if ( e instanceof AuthenticationException ) {
                    audit.builder().fail((AuthenticationException) e);
                }
                else if ( e instanceof UndeclaredThrowableException && e.getCause() instanceof InvocationTargetException
                        && e.getCause().getCause() instanceof AuthenticationException ) {
                    audit.builder().fail((AuthenticationException) e.getCause().getCause());
                }
                else {
                    audit.builder().fail(AuditStatus.INTERNAL);
                }

                log.debug("Authentication failed", e); //$NON-NLS-1$
                httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed"); //$NON-NLS-1$
            }

            return false;
        }
        catch (
            IllegalArgumentException |
            UnsupportedEncodingException e ) {
            log.debug("Invalid auth header", e); //$NON-NLS-1$
            audit.builder().fail(AuditStatus.INTERNAL);
            httpResp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal authorization header"); //$NON-NLS-1$
            return false;
        }

    }


    /**
     * @param realm
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String makeBasicHeader ( String realm ) throws UnsupportedEncodingException {
        return String.format("Basic realm=\"%s\"", URLEncoder.encode(realm, UTF_8)); //$NON-NLS-1$
    }


    /**
     * @param realm
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String makeBasicHeader ( String realm, LoginRealmType type ) throws UnsupportedEncodingException {
        return String.format("Basic realm=\"%s\",type=\"%s\"", URLEncoder.encode(realm, UTF_8), type.name()); //$NON-NLS-1$
    }


    /**
     * @param audit
     * @param ctx
     * @param defaultPasswordRealm
     * @param decodedStr
     * @param indexOfSep
     * @return
     */
    private AuthResponse makePasswordAuth ( AuditContext<LoginEventBuilder> audit, LoginContext ctx, String defaultPasswordRealm, String decodedStr,
            int indexOfSep ) {
        String user = decodedStr.substring(0, indexOfSep);
        String pass = decodedStr.substring(indexOfSep + 1);

        String realm = defaultPasswordRealm;

        int realmSepPos = user.lastIndexOf('@');
        if ( realmSepPos >= 0 ) {
            String realmName = user.substring(realmSepPos + 1);
            if ( this.loginRealmManager.getRealm(realmName) != null ) {
                realm = realmName;
                user = user.substring(0, realmSepPos);
            }
        }

        audit.builder().principal(new UserPrincipal(realm, null, user));

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Got realm %s username %s", realm, user)); //$NON-NLS-1$
        }

        LoginRealm r = this.loginRealmManager.getRealm(realm);
        LoginSession sess = new LoginSession();
        sess.setSelectedRealmId(realm);

        AuthResponse resp = r.authenticate(ctx, sess);

        if ( resp.getType() != AuthResponseType.CONTINUE ) {
            return resp;
        }

        for ( LoginChallenge<?> ch : sess.getChallenges() ) {

            if ( log.isDebugEnabled() ) {
                log.debug("Have challenge " + ch); //$NON-NLS-1$
            }

            if ( ch instanceof UsernameLoginChallenge ) {
                ( (UsernameLoginChallenge) ch ).setResponse(user);
            }
            else if ( ch instanceof PasswordLoginChallenge ) {
                ( (PasswordLoginChallenge) ch ).setResponse(pass);
            }
            else if ( ch.getRequired() ) {
                return new AuthResponse(AuthResponseType.FAIL);
            }
            ch.markPrompted();
        }
        return r.authenticate(ctx, sess);
    }

}
