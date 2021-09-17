/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.10.2013 by mbechler
 */
package eu.agno3.runtime.http.service.session.internal;


import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.http.service.session.SessionTracker;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    SessionTracker.class, HttpSessionListener.class
} )
public class SessionTrackerImpl implements HttpSessionListener, SessionTracker {

    private static final Logger log = Logger.getLogger(SessionTrackerImpl.class);

    private Map<String, WeakReference<HttpSession>> tracked = new WeakHashMap<>();


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionCreated ( HttpSessionEvent event ) {
        if ( log.isDebugEnabled() ) {
            log.debug("New session with ID " + event.getSession().getId()); //$NON-NLS-1$
        }
        this.tracked.put(event.getSession().getId(), new WeakReference<>(event.getSession()));
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionDestroyed ( HttpSessionEvent event ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Remove session with ID " + event.getSession().getId()); //$NON-NLS-1$
        }
        this.tracked.remove(event.getSession().getId());
    }


    @Override
    public Collection<WeakReference<HttpSession>> getSessions () {
        return Collections.unmodifiableCollection(this.tracked.values());
    }


    @Override
    public void invalidateAll () {
        log.info("Invalidating all active sessions"); //$NON-NLS-1$
        for ( WeakReference<HttpSession> s : this.tracked.values() ) {
            HttpSession sess = s.get();
            if ( sess != null ) {
                sess.invalidate();
            }
        }
    }


    @Override
    public void cleanSessions () {
        log.info("Cleaning active sessions from stale objects"); //$NON-NLS-1$
        for ( WeakReference<HttpSession> s : this.tracked.values() ) {

            HttpSession sess = s.get();

            if ( sess == null ) {
                continue;
            }

            Enumeration<String> attrs = sess.getAttributeNames();

            while ( attrs.hasMoreElements() ) {
                String attr = attrs.nextElement();
                cleanSessionAttribute(sess, attr);
            }
        }
    }


    /**
     * @param s
     * @param attr
     */
    private static void cleanSessionAttribute ( HttpSession s, String attr ) {
        Object o = s.getAttribute(attr);

        if ( o.getClass().isPrimitive() || o.getClass().getName().startsWith("java") ) { //$NON-NLS-1$
            return;
        }

        removeAttributeIfInvalid(s, attr, o);
    }


    /**
     * @param s
     * @param attr
     * @param o
     */
    private static void removeAttributeIfInvalid ( HttpSession s, String attr, Object o ) {
        Bundle b = FrameworkUtil.getBundle(o.getClass());

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found non-primitive attribute '%s' mapping to class %s", attr, o.getClass().getName())); //$NON-NLS-1$
        }

        if ( b != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(" located in bundle %s with state %s", b.getSymbolicName(), b.getState())); //$NON-NLS-1$
            }

            if ( !classIsValid(o, b) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Removing stale object on key %s from session", attr)); //$NON-NLS-1$
                }
                s.removeAttribute(attr);
            }
        }
    }


    /**
     * @param o
     * @param b
     */
    private static boolean classIsValid ( Object o, Bundle b ) {
        try {
            Class<?> current = b.loadClass(o.getClass().getName());
            current.cast(o);
            return true;
        }
        catch (
            ClassNotFoundException |
            IllegalStateException |
            ClassCastException e ) {
            log.debug("Failed to resolve attribute class:", e); //$NON-NLS-1$
        }
        return false;
    }
}
