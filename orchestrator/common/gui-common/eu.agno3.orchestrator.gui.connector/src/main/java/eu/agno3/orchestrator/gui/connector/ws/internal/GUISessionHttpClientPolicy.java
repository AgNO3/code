/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.ws.internal;


import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.server.session.SessionInfo;


/**
 * @author mbechler
 *
 */
public class GUISessionHttpClientPolicy extends HTTPClientPolicy {

    private static final Logger log = Logger.getLogger(GUISessionHttpClientPolicy.class);

    private String sessCookieName;


    /**
     * @param sessCookieName
     * 
     */
    public GUISessionHttpClientPolicy ( String sessCookieName ) {
        this.sessCookieName = sessCookieName;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.cxf.transports.http.configuration.HTTPClientPolicy#getCookie()
     */
    @Override
    public String getCookie () {
        SessionInfo sessionInfo = GuiWsClientSessionContextImpl.getSessionInfo();

        if ( sessionInfo == null ) {
            return super.getCookie();
        }

        String sessionId = sessionInfo.getSessionId();
        String otherCookies = super.getCookie();
        return makeCookieHeader(this.sessCookieName, sessionId, otherCookies);
    }


    /**
     * @param sessCookieName
     * @param sessionId
     * @param otherCookies
     * @return a cookie header containing the session id and the given other cookies
     */
    public static String makeCookieHeader ( String sessCookieName, String sessionId, String otherCookies ) {
        String encoded;
        URLCodec codec = new URLCodec();
        try {

            String sessionCookie = String.format("%s=%s", sessCookieName, codec.encode(sessionId)); //$NON-NLS-1$

            if ( !StringUtils.isBlank(otherCookies) ) {
                encoded = sessionCookie + "; " + otherCookies; //$NON-NLS-1$
            }

            encoded = sessionCookie;
        }
        catch ( EncoderException e ) {
            log.warn("Failed to produce session cookie", e); //$NON-NLS-1$
            encoded = StringUtils.EMPTY;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Setting Cookies: " + encoded); //$NON-NLS-1$
        }

        return encoded;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.cxf.transports.http.configuration.HTTPClientPolicy#isSetCookie()
     */
    @Override
    public boolean isSetCookie () {
        return GuiWsClientSessionContextImpl.getSessionInfo() != null || super.isSetCookie();
    }
}
