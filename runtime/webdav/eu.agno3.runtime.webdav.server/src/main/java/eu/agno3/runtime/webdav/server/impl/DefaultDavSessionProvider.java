/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.util.WebUtils;

import eu.agno3.runtime.http.ua.UADetector;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentType;


/**
 * @author mbechler
 *
 */
public class DefaultDavSessionProvider implements DavSessionProvider {

    /**
     * 
     */
    private static final String WEBDAV_SESSION = "webdav.session"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(DefaultDavSessionProvider.class);

    private static final String SESSION_SUPPORT_COOKIE = "have-session-support"; //$NON-NLS-1$
    private static final String CHECKED_BROWSER_CLIENT = "checked-browser-client"; //$NON-NLS-1$

    private boolean rejectNonDAVClients;
    private UADetector uaDetector;


    /**
     * @param uaDetector
     * 
     */
    public DefaultDavSessionProvider ( UADetector uaDetector ) {
        this(uaDetector, false);
    }


    /**
     * 
     * @param uaDetector
     * @param rejectNonDAVClients
     */
    public DefaultDavSessionProvider ( UADetector uaDetector, boolean rejectNonDAVClients ) {
        this.uaDetector = uaDetector;
        this.rejectNonDAVClients = rejectNonDAVClients;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavSessionProvider#attachSession(org.apache.jackrabbit.webdav.WebdavRequest)
     */
    @Override
    public boolean attachSession ( WebdavRequest req ) throws DavException {
        log.trace("attachSession"); //$NON-NLS-1$
        Subject subject = SecurityUtils.getSubject();

        HttpServletRequest httpReq = WebUtils.getHttpRequest(subject);
        HttpServletResponse httpResp = WebUtils.getHttpResponse(subject);

        Session existingSession = subject.getSession(false);
        boolean haveSessionSupport = existingSession != null;
        if ( !haveSessionSupport && httpReq.getCookies() != null ) {
            for ( Cookie c : httpReq.getCookies() ) {
                if ( SESSION_SUPPORT_COOKIE.equalsIgnoreCase(c.getName()) ) {
                    haveSessionSupport = true;
                }
            }
        }

        if ( !haveSessionSupport ) {
            log.trace("No session support"); //$NON-NLS-1$
            checkBrowser(req, existingSession);
            httpResp.addCookie(new Cookie(SESSION_SUPPORT_COOKIE, Boolean.TRUE.toString()));
            DefaultDavSessionImpl davSession = createAnonymousDAVSession();
            req.setDavSession(davSession);
            setupSession(req, davSession);
            return true;
        }

        if ( this.rejectNonDAVClients && existingSession != null ) {
            statefulCheckBrowser(req, existingSession);
        }
        else if ( this.rejectNonDAVClients ) {
            checkBrowser(req, existingSession);
        }

        log.debug("Client supports session"); //$NON-NLS-1$
        httpReq.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.TRUE);
        Session session = subject.getSession(true);

        if ( session == null ) {
            log.warn("Failed to create session"); //$NON-NLS-1$
            return false;
        }

        session.setAttribute(CHECKED_BROWSER_CLIENT, true);

        DefaultDavSessionImpl davSession = (DefaultDavSessionImpl) session.getAttribute(WEBDAV_SESSION);
        if ( davSession == null ) {
            davSession = new DefaultDavSessionImpl(session.getId().toString());
            session.setAttribute(WEBDAV_SESSION, davSession);
        }
        req.setDavSession(davSession);
        setupSession(req, davSession);
        return true;
    }


    /**
     * @param req
     * @param davSession
     */
    protected void setupSession ( WebdavRequest req, DefaultDavSessionImpl davSession ) {
        setupLocale(req, davSession);

    }


    /**
     * @param req
     * @param davSession
     */
    protected void setupLocale ( WebdavRequest req, DefaultDavSessionImpl davSession ) {
        Enumeration<Locale> ls = req.getLocales();
        while ( ls != null && ls.hasMoreElements() ) {
            Locale l = ls.nextElement();
            if ( isSupportedLocale(l) ) {
                davSession.setAttribute("locale", l); //$NON-NLS-1$
                break;
            }
        }
    }


    /**
     * @param l
     * @return
     */
    protected boolean isSupportedLocale ( Locale l ) {
        return false;
    }


    /**
     * @param req
     * @param existingSession
     * @throws DavException
     */
    private void statefulCheckBrowser ( WebdavRequest req, Session existingSession ) throws DavException {
        Object checked = existingSession.getAttribute(CHECKED_BROWSER_CLIENT);
        if ( checked == null ) {
            checkBrowser(req, existingSession);
        }
        else {
            if ( !(boolean) checked ) {
                throw new DavException(HttpServletResponse.SC_FORBIDDEN, "Accessing WebDAV via a browser is forbidden"); //$NON-NLS-1$
            }
        }

        existingSession.setAttribute(CHECKED_BROWSER_CLIENT, true);
    }


    /**
     * @param req
     * @param existingSession
     * @throws DavException
     */
    private void checkBrowser ( WebdavRequest req, Session existingSession ) throws DavException {
        ReadableUserAgent parsed = this.uaDetector.parse(req);
        UserAgentType type = parsed.getType();
        log.debug("Checking for browser UA, found type " + parsed.getType()); //$NON-NLS-1$
        if ( type != UserAgentType.UNKNOWN && type != UserAgentType.OTHER && type != UserAgentType.LIBRARY ) {
            if ( existingSession != null ) {
                existingSession.setAttribute(CHECKED_BROWSER_CLIENT, true);
            }
            log.warn("Tried to access DAV via browser " + parsed); //$NON-NLS-1$
            throw new DavException(HttpServletResponse.SC_FORBIDDEN, "Accessing WebDAV via a browser is forbidden"); //$NON-NLS-1$
        }
    }


    /**
     * @return
     */
    private static DefaultDavSessionImpl createAnonymousDAVSession () {
        return new DefaultDavSessionImpl();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavSessionProvider#releaseSession(org.apache.jackrabbit.webdav.WebdavRequest)
     */
    @Override
    public void releaseSession ( WebdavRequest resp ) {
        log.trace("releaseSession"); //$NON-NLS-1$
        DavSession davSession = resp.getDavSession();
        if ( davSession != null ) {}

    }
}
