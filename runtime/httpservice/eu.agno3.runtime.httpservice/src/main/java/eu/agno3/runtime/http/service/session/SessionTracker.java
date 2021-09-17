/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.10.2013 by mbechler
 */
package eu.agno3.runtime.http.service.session;


import java.lang.ref.WeakReference;
import java.util.Collection;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionListener;


/**
 * @author mbechler
 * 
 */
public interface SessionTracker extends HttpSessionListener {

    /**
     * Invalidate all active sessions
     */
    void invalidateAll ();


    /**
     * @return all currently active sessions
     */
    Collection<WeakReference<HttpSession>> getSessions ();


    /**
     * Cleans the session objects referencing stale classes.
     * 
     * Only handles objects which are directly used as attributes, not their references.
     */
    void cleanSessions ();

}
