/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 29, 2017 by mbechler
 */
package eu.agno3.runtime.redis.session.internal;


import java.util.EnumSet;

import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.SessionHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.http.service.session.DelegatingHttpSessionListener;
import eu.agno3.runtime.http.service.session.ExtendedSessionHandler;
import eu.agno3.runtime.http.service.session.SessionBindingGenerator;
import eu.agno3.runtime.http.service.session.SessionManagerFactory;
import eu.agno3.runtime.redis.RedisClientException;
import eu.agno3.runtime.redis.RedisClientProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = SessionManagerFactory.class, configurationPid = "httpservice.session.redis" )
public class RedisSessionManagerFactory implements SessionManagerFactory {

    private static final Logger log = Logger.getLogger(RedisSessionManagerFactory.class);

    private boolean httpOnly = true;
    private boolean secure = true;

    private int sessionTimeoutSecs = 60 * 60; // 1 hour

    private String cookieName = "JSESSIONID"; //$NON-NLS-1$

    private SecureRandomProvider secureRandom;
    private SessionBindingGenerator sessionBindingGenerator;
    private DelegatingHttpSessionListener sessionListener = new DelegatingHttpSessionListener();

    private RedisClientProvider client;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        String savePeriodAttr = (String) ctx.getProperties().get("savePeriod"); //$NON-NLS-1$
        String lazyLoadAttr = (String) ctx.getProperties().get("lazyLoad"); //$NON-NLS-1$
        String httpOnlyAttr = (String) ctx.getProperties().get("httpOnlyCookie"); //$NON-NLS-1$
        String secureAttr = (String) ctx.getProperties().get("secureCookie"); //$NON-NLS-1$
        String sessionTimeoutAttr = (String) ctx.getProperties().get("sessionTimeoutSecs"); //$NON-NLS-1$
        String sessionCookieNameAttr = (String) ctx.getProperties().get("sessionCookieName"); //$NON-NLS-1$

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


    @Reference
    protected synchronized void setRedisClientProvider ( RedisClientProvider cp ) {
        this.client = cp;
    }


    protected synchronized void unsetRedisClientProvider ( RedisClientProvider cp ) {
        if ( this.client == cp ) {
            this.client = null;
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
            log.debug("Creating RedisSessionManager for " + contextName); //$NON-NLS-1$
        }

        SessionHandler esh = new ExtendedSessionHandler(this.sessionBindingGenerator);

        DefaultSessionCache dsc = new DefaultSessionCache(esh);
        esh.setSessionCache(dsc);

        RedisSessionDataStore sds;
        try {
            sds = new RedisSessionDataStore(this.client.getClient());
            // sds.setGracePeriodSec();
            // sds.setSavePeriodSec(savePeriodSec);
            dsc.setSessionDataStore(sds);
        }
        catch ( RedisClientException e ) {
            throw new RuntimeException("Failed to get client", e); //$NON-NLS-1$
        }

        esh.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
        esh.setHttpOnly(this.httpOnly);
        esh.setSecureRequestOnly(this.secure);
        esh.setMaxInactiveInterval(this.sessionTimeoutSecs);
        esh.setSessionCookie(this.cookieName);
        esh.addEventListener(this.sessionListener);
        return esh;
    }

}
