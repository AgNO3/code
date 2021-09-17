/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.ws.internal;


import java.net.HttpURLConnection;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.security.auth.login.CredentialExpiredException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWsClientSessionContext;
import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.session.SessionInfo;
import eu.agno3.orchestrator.server.session.service.SessionService;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.ServerPubkeyVerifier;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.security.cas.client.CasPrincipalWrapper;


/**
 * @author mbechler
 *
 */
@Component ( service = GuiWsClientSessionContext.class )
public class GuiWsClientSessionContextImpl implements GuiWsClientSessionContext, HttpURLConnectionFactory {

    /**
     * 
     */
    private static final long serialVersionUID = 5704436523129866015L;
    /**
     * 
     */
    private static final String NOT_AUTHENTICATED = "Not authenticated"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String NO_SESSION_AVAILABLE = "No session available for current subject"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String CAS_CLIENT_PRINCIPAL = "casClientPrincipal"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String API_SESSION_ATTR = "apiSession"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(GuiWsClientSessionContextImpl.class);
    private GuiConfig guiConfig;
    private TLSContext tlsContext;


    @Reference ( target = "(subsystem=webgui/casClient)" )
    protected synchronized void setTlsContext ( TLSContext tc ) {
        this.tlsContext = tc;
    }


    protected synchronized void unsetTlsContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    @Reference
    protected synchronized void setGuiConfig ( GuiConfig cfg ) {
        this.guiConfig = cfg;
    }


    protected synchronized void unsetGuiConfig ( GuiConfig cfg ) {
        if ( this.guiConfig == cfg ) {
            this.guiConfig = null;
        }
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
                HttpsURLConnection httpConn = (HttpsURLConnection) conn;
                httpConn.setSSLSocketFactory(this.tlsContext.getSocketFactory());
                if ( this.guiConfig.getAuthServerPubKey() != null ) {
                    httpConn.setHostnameVerifier(new ServerPubkeyVerifier(this.guiConfig.getAuthServerPubKey(), this.tlsContext.getHostnameVerifier()));
                }
                else {
                    httpConn.setHostnameVerifier(this.tlsContext.getHostnameVerifier());
                }
            }
            catch ( CryptoException e ) {
                log.warn("Failed to set up SSL security", e); //$NON-NLS-1$
            }
        }

        return (HttpURLConnection) conn;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.connector.ws.GuiWsClientSessionContext#login(eu.agno3.orchestrator.server.session.service.SessionService,
     *      org.jasig.cas.client.authentication.AttributePrincipal)
     */
    @Override
    public synchronized SessionInfo login ( SessionService service, AttributePrincipal principal ) throws CredentialExpiredException,
            SessionException, GuiWebServiceException {

        Subject subject = SecurityUtils.getSubject();
        synchronized ( subject ) {
            Session s = subject.getSession(true);

            if ( s == null ) {
                throw new SessionException("Could not get session"); //$NON-NLS-1$
            }

            s.setAttribute(CAS_CLIENT_PRINCIPAL, new CasPrincipalWrapper((AttributePrincipalImpl) principal));

            String wsTicket = principal.getProxyTicketFor(this.guiConfig.getWebServiceBaseAddress().toString());
            if ( StringUtils.isBlank(wsTicket) ) {
                log.debug("Failed to obtain proxy ticket for backend service"); //$NON-NLS-1$
                throw new CredentialExpiredException("Failed to obtain proxy ticket"); //$NON-NLS-1$
            }

            return this.login(service, wsTicket);
        }
    }


    @Override
    public synchronized SessionInfo renewSession ( SessionService service ) throws SessionException, CredentialExpiredException,
            GuiWebServiceException {
        log.debug("Renewing backend session"); //$NON-NLS-1$
        try {
            return this.login(service, getSavedPrincipal());
        }
        catch ( CredentialExpiredException e ) {
            SecurityUtils.getSubject().logout();
            throw e;
        }
    }


    private AttributePrincipal getSavedPrincipal () throws SessionException {
        Subject subject = SecurityUtils.getSubject();
        synchronized ( subject ) {
            if ( !subject.isAuthenticated() ) {
                log.warn("Not authenticated while running webservice client"); //$NON-NLS-1$
                throw new SessionException(NOT_AUTHENTICATED);
            }

            Session s = subject.getSession();
            CasPrincipalWrapper p = (CasPrincipalWrapper) s.getAttribute(CAS_CLIENT_PRINCIPAL);
            if ( p == null ) {
                throw new SessionException("No saved principal found"); //$NON-NLS-1$
            }
            return p.getAttributePrincipal(this.getProxyRetriever());
        }
    }


    /**
     * @return
     */
    private ProxyRetriever getProxyRetriever () {
        return new Cas20ProxyRetriever(this.getCasServerBase(), "UTF-8", this); //$NON-NLS-1$
    }


    /**
     * @return
     */
    private String getCasServerBase () {
        return this.guiConfig.getAuthServerURL().toString();
    }


    protected SessionInfo login ( SessionService service, String proxyTicket ) throws CredentialExpiredException, SessionException {
        Subject subject = SecurityUtils.getSubject();
        Session s = subject.getSession(true);

        if ( s == null ) {
            log.warn(NO_SESSION_AVAILABLE);
            throw new SessionException(NO_SESSION_AVAILABLE);
        }
        SessionInfo info = service.login(proxyTicket);
        s = subject.getSession(true);

        if ( log.isDebugEnabled() ) {
            log.debug("Setting new session to " + info.getSessionId()); //$NON-NLS-1$
        }
        s.setAttribute(API_SESSION_ATTR, info);
        return info;
    }


    @Override
    public void logout ( SessionService service ) throws SessionException, GuiWebServiceException {
        Subject subject = SecurityUtils.getSubject();
        synchronized ( subject ) {
            if ( !subject.isAuthenticated() ) {
                throw new SessionException(NOT_AUTHENTICATED);
            }

            Session s = subject.getSession();

            if ( s == null ) {
                log.warn(NO_SESSION_AVAILABLE);
                throw new SessionException(NO_SESSION_AVAILABLE);
            }

            s.removeAttribute(API_SESSION_ATTR);
            service.logout();
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.connector.ws.GuiWsClientSessionContext#getCurrentSessionInfo()
     */
    @Override
    public SessionInfo getCurrentSessionInfo () throws SessionException {
        SessionInfo info = getSessionInfo();
        if ( info == null ) {
            throw new SessionException("No session available"); //$NON-NLS-1$
        }
        return info;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.gui.connector.ws.GuiWsClientSessionContext#getSessionCookieName()
     */
    @Override
    public String getSessionCookieName () {
        return this.guiConfig.getSessionCookieName();
    }


    /**
     * @return the currently active session
     */
    public static SessionInfo getSessionInfo () {
        Subject subject = SecurityUtils.getSubject();
        Session s = subject.getSession();

        if ( s == null ) {
            log.warn(NO_SESSION_AVAILABLE);
            return null;
        }

        Object apiSessionAttr = s.getAttribute(API_SESSION_ATTR);

        if ( ! ( apiSessionAttr instanceof SessionInfo ) ) {
            log.debug("API Session unavailable"); //$NON-NLS-1$
            return null;
        }

        return (SessionInfo) apiSessionAttr;
    }

}
