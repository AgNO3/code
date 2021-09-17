/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security.cas.client;


import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MapCache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.ServerPubkeyVerifier;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.security.AuthorizationInfoProvider;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public abstract class AbstractCasRealm extends AuthorizingRealm implements HttpURLConnectionFactory, SessionListener, AuthorizationInfoProvider {

    /**
     * 
     */
    private static final long serialVersionUID = -455107334570135727L;
    /**
     * 
     */
    private static final String CAS_CLIENT_DEFAULT_TLS_SUBSYS = "casClient"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(AbstractCasRealm.class);
    private Object sessionStoreLock = new Object();

    private static final int AUTHZ_CACHE_SIZE = 1024;
    private Map<Object, AuthorizationInfo> authzCache = new LRUMap<>(AUTHZ_CACHE_SIZE);

    private Map<Serializable, String> sessionIdToTicket = new HashMap<>();
    private Map<String, Session> ticketToSession = new WeakHashMap<>();
    /**
     * 
     */
    private Cas20ProxyTicketValidator ticketValidator;
    private TLSContext tlsContext;
    private ProxyGrantingTicketStorage proxyTicketStorage;
    private CasAuthConfiguration authConfig;


    protected void postCasAuthenticate ( String ticket, AttributePrincipal principal, SimplePrincipalCollection principalCollection ) {}


    protected void contributeAuthorizationInfo ( PrincipalCollection princs, SimpleAuthorizationInfo authInfo ) {}


    /**
     * 
     */
    public AbstractCasRealm () {
        setAuthenticationTokenClass(CasToken.class);
        setName("CAS"); //$NON-NLS-1$
        setAuthorizationCache(new MapCache<>("authzCache", this.authzCache)); //$NON-NLS-1$
        setAuthorizationCachingEnabled(true);
    }


    /**
     * @param tlsContext
     * @param authConfig
     * @param proxyTicketStorage
     */
    public AbstractCasRealm ( TLSContext tlsContext, CasAuthConfiguration authConfig, ProxyGrantingTicketStorage proxyTicketStorage ) {
        this();
        setTlsContext(tlsContext);
        setProxyTicketStorage(proxyTicketStorage);
        setAuthConfig(authConfig);
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.AuthorizationInfoProvider#fetchAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    public AuthorizationInfo fetchAuthorizationInfo ( PrincipalCollection princs ) {
        return doGetAuthorizationInfo(princs);
    }


    /**
     * @param tlsContextFactory
     *            the tlsContextFactory to set
     */
    protected final void setTlsContext ( TLSContext tlsContext ) {
        this.tlsContext = tlsContext;
    }


    /**
     * @param proxyTicketStorage
     *            the proxyTicketStorage to set
     */
    protected final void setProxyTicketStorage ( ProxyGrantingTicketStorage proxyTicketStorage ) {
        this.proxyTicketStorage = proxyTicketStorage;
    }


    /**
     * @param authConfig
     *            the authConfig to set
     */
    protected final void setAuthConfig ( CasAuthConfiguration authConfig ) {
        this.authConfig = authConfig;
    }


    @Override
    protected void onInit () {
        super.onInit();
        getTicketValidator();
    }


    protected Cas20ProxyTicketValidator getTicketValidator () {
        if ( this.ticketValidator == null ) {
            this.ticketValidator = createTicketValidator();
        }
        return this.ticketValidator;
    }


    /**
     * @return the proxy return filter instance
     */
    public Filter getProxyReturnFilter () {
        return new CasProxyReturnFilter(this);
    }


    /**
     * @param pgtId
     * @param pgtIou
     */
    public void handleProxyReturn ( String pgtId, String pgtIou ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Saving proxy ticket with IOU " + pgtIou); //$NON-NLS-1$
        }
        this.proxyTicketStorage.save(pgtIou, pgtId);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.client.ssl.HttpURLConnectionFactory#buildHttpURLConnection(java.net.URLConnection)
     */
    @Override
    public HttpURLConnection buildHttpURLConnection ( URLConnection conn ) {

        if ( ! ( conn instanceof HttpURLConnection ) ) {
            throw new IllegalArgumentException("No HTTP Connection"); //$NON-NLS-1$
        }

        if ( conn instanceof HttpsURLConnection ) {
            // setup SSL
            try {
                ( (HttpsURLConnection) conn ).setSSLSocketFactory(this.tlsContext.getSocketFactory());
                if ( this.authConfig.getAuthServerPubKey() != null ) {
                    ( (HttpsURLConnection) conn ).setHostnameVerifier(
                        new ServerPubkeyVerifier(this.authConfig.getAuthServerPubKey(), this.tlsContext.getHostnameVerifier()));
                }
                else {
                    ( (HttpsURLConnection) conn ).setHostnameVerifier(this.tlsContext.getHostnameVerifier());
                }
            }
            catch ( CryptoException e ) {
                log.warn("Failed to set up SSL security", e); //$NON-NLS-1$
            }
        }

        return (HttpURLConnection) conn;
    }


    protected String getTlsSubsystem () {
        return CAS_CLIENT_DEFAULT_TLS_SUBSYS;
    }


    protected Cas20ProxyTicketValidator createTicketValidator () {
        Cas20ProxyTicketValidator tv = new Cas20ProxyTicketValidator(this.authConfig.getAuthServerBase());
        tv.setProxyRetriever(createProxyRetriever());
        tv.setURLConnectionFactory(this);
        tv.setProxyGrantingTicketStorage(this.proxyTicketStorage);
        if ( this.authConfig.getLocalProxyCallbackAddress() != null ) {
            tv.setProxyCallbackUrl(this.authConfig.getLocalProxyCallbackAddress());
        }
        return tv;
    }


    private Cas20ProxyRetriever createProxyRetriever () {
        return new Cas20ProxyRetriever(this.authConfig.getAuthServerBase(), "UTF-8", this); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo ( PrincipalCollection princs ) {
        CasPrincipalWrapper casPrinc = princs.oneByType(CasPrincipalWrapper.class);

        if ( casPrinc == null ) {
            log.debug("No cas principal present"); //$NON-NLS-1$
            return new SimpleAuthorizationInfo();
        }
        SimpleAuthorizationInfo authInfo = new SimpleAuthorizationInfo();
        log.debug("Adding roles and permissions"); //$NON-NLS-1$
        addDefaultRolesAndPermissions(authInfo);
        addCasRolesAndPermissions(casPrinc.getAttributes(), authInfo);
        contributeAuthorizationInfo(princs, authInfo);

        return authInfo;
    }


    /**
     * @param authInfo
     */
    protected void addDefaultRolesAndPermissions ( SimpleAuthorizationInfo authInfo ) {
        authInfo.addRoles(this.authConfig.getDefaultRoles());
        authInfo.addStringPermissions(this.authConfig.getDefaultPermissions());
    }


    /**
     * @param attributes
     * @param authInfo
     */
    protected void addCasRolesAndPermissions ( Map<String, Serializable> attributes, SimpleAuthorizationInfo authInfo ) {
        String roleAttribute = this.authConfig.getRoleAttribute();
        String permissionAttribute = this.authConfig.getPermissionAttribute();

        if ( roleAttribute != null && !StringUtils.isBlank((String) attributes.get(roleAttribute)) ) {
            List<String> casRoles = Arrays.asList(StringUtils.split((String) attributes.get(roleAttribute), ','));
            if ( log.isDebugEnabled() ) {
                log.debug("CAS returned roles are " + casRoles); //$NON-NLS-1$
            }
            authInfo.addRoles(casRoles);
        }

        if ( permissionAttribute != null && !StringUtils.isBlank((String) attributes.get(permissionAttribute)) ) {
            for ( String perm : StringUtils.split((String) attributes.get(permissionAttribute), ',') ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Perm " + perm); //$NON-NLS-1$
                }
                authInfo.addObjectPermission(this.getPermissionResolver().resolvePermission(perm));
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo ( AuthenticationToken token ) throws CasAuthenticationException {
        CasToken casToken = (CasToken) token;
        String ticket = getTicket(token, casToken);
        if ( ticket == null ) {
            return null;
        }

        Assertion casAssertion;
        try {
            log.debug("Validating CAS ticket against server"); //$NON-NLS-1$
            casAssertion = getTicketValidator().validate(ticket, this.authConfig.getLocalService());
        }
        catch ( TicketValidationException e ) {
            log.warn("Failed to validate ticket:", e); //$NON-NLS-1$
            throw new CasAuthenticationException("Unable to validate ticket", e); //$NON-NLS-1$
        }

        // get principal, user id and attributes
        AttributePrincipal principal = casAssertion.getPrincipal();

        if ( ! ( principal instanceof AttributePrincipalImpl ) ) {
            log.debug("Principal is not AttributePrincipalImpl"); //$NON-NLS-1$
            throw new CasAuthenticationException("Cas principal is not AttributePrincipalImpl"); //$NON-NLS-1$
        }
        dumpAssertion(ticket, casAssertion, principal.getName());

        AttributePrincipalImpl casPrincipal = (AttributePrincipalImpl) principal;

        SimplePrincipalCollection principalCollection = new SimplePrincipalCollection(casPrincipal.getName(), getName());
        principalCollection.add(new CasPrincipalWrapper(casPrincipal), getName());
        postCasAuthenticate(ticket, principal, principalCollection);

        Session sess = SecurityUtils.getSubject().getSession(true);
        synchronized ( this.sessionStoreLock ) {
            this.sessionIdToTicket.put(sess.getId(), ticket);
            this.ticketToSession.put(ticket, sess);
        }
        refreshToken(casToken, principal);
        return new SimpleAuthenticationInfo(principalCollection, ticket);

    }


    /**
     * @param token
     * @param casToken
     * @return
     */
    protected String getTicket ( AuthenticationToken token, CasToken casToken ) {
        if ( token == null ) {
            log.debug("No token available"); //$NON-NLS-1$
            return null;
        }

        String ticket = (String) casToken.getCredentials();
        if ( StringUtils.isBlank(ticket) ) {
            log.debug("No ticket available"); //$NON-NLS-1$
            return null;
        }
        return ticket;
    }


    /**
     * @param casToken
     * @param casPrincipal
     * @param attributes
     */
    protected void refreshToken ( CasToken casToken, AttributePrincipal casPrincipal ) {
        casToken.setPrincipal(casPrincipal.getName());
    }


    /**
     * @param ticket
     * @param casAssertion
     * @param userId
     */
    protected void dumpAssertion ( String ticket, Assertion casAssertion, String userId ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Validate ticket : %s in CAS server : %s to retrieve user : %s", //$NON-NLS-1$
                ticket,
                this.authConfig.getAuthServerBase(),
                userId));
            for ( Entry<String, Object> entry : casAssertion.getAttributes().entrySet() ) {
                log.debug(String.format("CAS Attribute: %s=%s", entry.getKey(), entry.getValue())); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param sessionTicketId
     * @param req
     * @param resp
     */
    public void doLogout ( String sessionTicketId, ServletRequest req, ServletResponse resp ) {
        synchronized ( this.sessionStoreLock ) {
            Session s = this.ticketToSession.remove(sessionTicketId);
            if ( s != null ) {
                try {
                    s.stop();
                }
                catch (
                    IllegalStateException |
                    InvalidSessionException e ) {
                    log.debug("Session seems to be already destroyed", e); //$NON-NLS-1$
                }
            }
            else {
                log.debug("No session available"); //$NON-NLS-1$
            }
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.session.SessionListener#onExpiration(org.apache.shiro.session.Session)
     */
    @Override
    public void onExpiration ( Session sess ) {
        synchronized ( this.sessionStoreLock ) {
            String ticketId = this.sessionIdToTicket.remove(sess.getId());
            if ( ticketId != null ) {
                this.ticketToSession.remove(ticketId);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.session.SessionListener#onStop(org.apache.shiro.session.Session)
     */
    @Override
    public void onStop ( Session sess ) {
        synchronized ( this.sessionStoreLock ) {
            String ticketId = this.sessionIdToTicket.remove(sess.getId());
            if ( ticketId != null ) {
                this.ticketToSession.remove(ticketId);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.session.SessionListener#onStart(org.apache.shiro.session.Session)
     */
    @Override
    public void onStart ( Session sess ) {
        // nothing
    }

}