/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.session;


import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


/**
 * 
 * @author mbechler
 *
 */
public class DelegatingHttpSessionListener implements HttpSessionListener {

    private Set<HttpSessionListener> delegates = new HashSet<>();
    private Map<String, WeakReference<HttpSessionEvent>> existingSessions = new ConcurrentHashMap<>(new WeakHashMap<>());


    /**
     * 
     */
    public DelegatingHttpSessionListener () {}


    /**
     * 
     * @param listener
     */
    public synchronized void addListener ( HttpSessionListener listener ) {
        this.delegates.add(listener);
        for ( Entry<String, WeakReference<HttpSessionEvent>> existingSession : this.existingSessions.entrySet() ) {
            HttpSessionEvent ev = existingSession.getValue().get();
            if ( ev == null ) {
                this.existingSessions.remove(existingSession.getKey());
            }
            else {
                listener.sessionCreated(ev);
            }
        }
    }


    /**
     * 
     * @param listener
     */
    public synchronized void removeListener ( HttpSessionListener listener ) {
        this.delegates.remove(listener);
        for ( Entry<String, WeakReference<HttpSessionEvent>> existingSession : this.existingSessions.entrySet() ) {
            HttpSessionEvent ev = existingSession.getValue().get();
            if ( ev == null ) {
                this.existingSessions.remove(existingSession.getKey());
            }
            else {
                listener.sessionDestroyed(ev);
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public synchronized void sessionCreated ( HttpSessionEvent event ) {
        for ( HttpSessionListener l : this.delegates ) {
            l.sessionCreated(event);
        }
        this.existingSessions.put(event.getSession().getId(), new WeakReference<>(event));
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public synchronized void sessionDestroyed ( HttpSessionEvent event ) {
        for ( HttpSessionListener l : this.delegates ) {
            l.sessionDestroyed(event);
        }
        this.existingSessions.remove(event.getSession().getId());
    }

}