/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.10.2013 by mbechler
 */
package eu.agno3.runtime.http.service.session.internal;


import java.io.File;
import java.util.EnumSet;

import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.FileSessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.http.service.session.DelegatingHttpSessionListener;
import eu.agno3.runtime.http.service.session.ExtendedSessionHandler;
import eu.agno3.runtime.http.service.session.SessionBindingGenerator;
import eu.agno3.runtime.http.service.session.SessionManagerConfiguration;
import eu.agno3.runtime.http.service.session.SessionManagerFactory;


/**
 * @author mbechler
 * 
 */
@Component (
    service = SessionManagerFactory.class,
    configurationPid = SessionManagerConfiguration.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class HashSessionManagerFactory implements SessionManagerFactory {

    private static final Logger log = Logger.getLogger(HashSessionManagerFactory.class);

    private DelegatingHttpSessionListener sessionListener = new DelegatingHttpSessionListener();

    private String sessionStoreBase = "/var/tmp/runtime-session/"; //$NON-NLS-1$
    private boolean deleteUnrestorableSessions = true;

    private boolean httpOnly = true;
    private boolean secure = true;

    private int sessionTimeoutSecs = 60 * 60; // 1 hour
    private int sessionSaveIntervalSecs = 60;

    private String cookieName = "JSESSIONID"; //$NON-NLS-1$

    private SecureRandomProvider secureRandom;

    private SessionBindingGenerator sessionBindingGenerator;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        String sessionStoreBaseAttr = (String) ctx.getProperties().get("sessionStoreBase"); //$NON-NLS-1$
        String deleteUnrestorableSessionsAttr = (String) ctx.getProperties().get("deleteUnrestorableSessions"); //$NON-NLS-1$
        String savePeriodAttr = (String) ctx.getProperties().get("savePeriod"); //$NON-NLS-1$
        String lazyLoadAttr = (String) ctx.getProperties().get("lazyLoad"); //$NON-NLS-1$
        String httpOnlyAttr = (String) ctx.getProperties().get("httpOnlyCookie"); //$NON-NLS-1$
        String secureAttr = (String) ctx.getProperties().get("secureCookie"); //$NON-NLS-1$
        String sessionTimeoutAttr = (String) ctx.getProperties().get("sessionTimeoutSecs"); //$NON-NLS-1$
        String sessionCookieNameAttr = (String) ctx.getProperties().get("sessionCookieName"); //$NON-NLS-1$

        if ( sessionStoreBaseAttr != null ) {
            this.sessionStoreBase = sessionStoreBaseAttr;
        }

        if ( Boolean.FALSE.toString().equals(deleteUnrestorableSessionsAttr) ) {
            this.deleteUnrestorableSessions = false;
        }

        parseSessionProperties(savePeriodAttr, lazyLoadAttr, sessionTimeoutAttr);

        parseCookieParams(httpOnlyAttr, secureAttr, sessionCookieNameAttr);

    }


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.secureRandom = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.secureRandom == srp ) {
            this.secureRandom = null;
        }
    }


    @Reference
    protected synchronized void setSessionBindingGenerator ( SessionBindingGenerator sbg ) {
        this.sessionBindingGenerator = sbg;
    }


    protected synchronized void unsetSessionBindingGenerator ( SessionBindingGenerator sbg ) {
        if ( this.sessionBindingGenerator == sbg ) {
            this.sessionBindingGenerator = sbg;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.session.SessionManagerFactory#getSessionCookieName()
     */
    @Override
    public String getSessionCookieName () {
        return this.cookieName;
    }


    /**
     * @param savePeriodAttr
     * @param lazyLoadAttr
     * @param sessionTimeoutAttr
     */
    private void parseSessionProperties ( String savePeriodAttr, String lazyLoadAttr, String sessionTimeoutAttr ) {
        if ( !StringUtils.isBlank(sessionTimeoutAttr) ) {
            try {
                this.sessionTimeoutSecs = Integer.parseInt(sessionTimeoutAttr);
            }
            catch ( NumberFormatException e ) {
                log.error("Illegal 'sessionTimeoutSecs' configuration", e); //$NON-NLS-1$
            }
        }

        if ( !StringUtils.isBlank(savePeriodAttr) ) {
            try {
                this.sessionSaveIntervalSecs = Integer.parseInt(savePeriodAttr);
            }
            catch ( NumberFormatException e ) {
                log.error("Illegal 'savePeriodAttr' configuration", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param httpOnlyAttr
     * @param secureAttr
     * @param sessionCookieNameAttr
     */
    private void parseCookieParams ( String httpOnlyAttr, String secureAttr, String sessionCookieNameAttr ) {
        if ( !StringUtils.isBlank(httpOnlyAttr) && Boolean.parseBoolean(httpOnlyAttr) == false ) {
            this.httpOnly = false;
        }

        if ( !StringUtils.isBlank(secureAttr) && Boolean.parseBoolean(secureAttr) == false ) {
            this.secure = false;
        }

        if ( !StringUtils.isBlank(sessionCookieNameAttr) ) {
            this.cookieName = sessionCookieNameAttr.trim();
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindSessionListener ( HttpSessionListener listener ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Adding session listener " + listener.getClass().getName()); //$NON-NLS-1$
        }
        this.sessionListener.addListener(listener);
    }


    protected synchronized void unbindSessionListener ( HttpSessionListener listener ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Removing session listener " + listener.getClass().getName()); //$NON-NLS-1$
        }
        this.sessionListener.removeListener(listener);
    }


    @Override
    public SessionHandler createSessionHandler ( String contextName ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Creating HashSessionManager for " + contextName); //$NON-NLS-1$
        }

        ExtendedSessionHandler esh = new ExtendedSessionHandler(this.sessionBindingGenerator);

        DefaultSessionCache dsc = new DefaultSessionCache(esh);
        esh.setSessionCache(dsc);

        FileSessionDataStore sds = new FixedFileSessionDataStore();
        sds.setDeleteUnrestorableFiles(this.deleteUnrestorableSessions);
        sds.setStoreDir(new File(this.sessionStoreBase, contextName));
        sds.setSavePeriodSec(this.sessionSaveIntervalSecs);
        dsc.setSessionDataStore(sds);

        esh.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
        esh.setHttpOnly(this.httpOnly);
        esh.setSecureRequestOnly(this.secure);
        esh.setMaxInactiveInterval(this.sessionTimeoutSecs);
        esh.setSessionCookie(this.cookieName);
        esh.addEventListener(this.sessionListener);
        return esh;
    }
}
