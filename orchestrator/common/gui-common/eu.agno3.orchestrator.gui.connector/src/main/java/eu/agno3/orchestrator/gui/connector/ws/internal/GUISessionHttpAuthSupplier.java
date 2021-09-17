/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.ws.internal;


import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.CredentialExpiredException;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.auth.HttpAuthSupplier;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWsClientSessionContext;
import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.session.SessionInfo;
import eu.agno3.orchestrator.server.session.service.SessionService;


/**
 * @author mbechler
 *
 */
public class GUISessionHttpAuthSupplier implements HttpAuthSupplier {

    private static final Logger log = Logger.getLogger(GUISessionHttpAuthSupplier.class);

    private static final String COOKIE = "Cookie"; //$NON-NLS-1$

    private GuiWsClientSessionContext guiWsSessionContext;

    private SessionService service;


    /**
     * @param service
     * @param guiWsClientSessionContext
     * 
     */
    public GUISessionHttpAuthSupplier ( SessionService service, GuiWsClientSessionContext guiWsClientSessionContext ) {
        this.service = service;
        this.guiWsSessionContext = guiWsClientSessionContext;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.cxf.transport.http.auth.HttpAuthSupplier#getAuthorization(org.apache.cxf.configuration.security.AuthorizationPolicy,
     *      java.net.URI, org.apache.cxf.message.Message, java.lang.String)
     */
    @Override
    public String getAuthorization ( AuthorizationPolicy policy, URI uri, Message msg, String header ) {
        if ( "CAS".equals(header) ) { //$NON-NLS-1$
            log.info("Server indicates authentication expired, trying to renew session"); //$NON-NLS-1$

            try {
                SessionInfo renewSession = this.guiWsSessionContext.renewSession(this.service);
                updateHeaders(msg, renewSession.getSessionId());

                return "RETRY"; //$NON-NLS-1$
            }
            catch (
                SessionException |
                CredentialExpiredException |
                GuiWebServiceException e ) {
                log.warn("Failed to renew backend session, destroying session", e); //$NON-NLS-1$

                Subject s = SecurityUtils.getSubject();
                if ( s != null ) {
                    s.logout();
                }

                throw new AccessDeniedException("Failed to renew session"); //$NON-NLS-1$
            }
        }

        return null;
    }


    /**
     * @param msg
     */
    protected void updateHeaders ( Message msg, String sessionId ) {
        @SuppressWarnings ( "unchecked" )
        Map<String, List<String>> headers = (Map<String, List<String>>) msg.getContextualProperty(Message.PROTOCOL_HEADERS);

        if ( headers == null ) {
            headers = new HashMap<>();
        }

        String cookiesHeader = null;

        for ( String header : headers.keySet() ) {
            if ( COOKIE.equalsIgnoreCase(header) ) {
                cookiesHeader = header;
            }
        }

        if ( cookiesHeader == null ) {
            headers.put(
                COOKIE,
                Arrays.asList(GUISessionHttpClientPolicy.makeCookieHeader(this.guiWsSessionContext.getSessionCookieName(), sessionId, null)));
        }
        else {
            headers.put(
                COOKIE,
                Arrays.asList(GUISessionHttpClientPolicy.makeCookieHeader(
                    this.guiWsSessionContext.getSessionCookieName(),
                    sessionId,
                    StringUtils.join(headers.get(cookiesHeader), ';'))));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.cxf.transport.http.auth.HttpAuthSupplier#requiresRequestCaching()
     */
    @Override
    public boolean requiresRequestCaching () {
        return true;
    }

}
