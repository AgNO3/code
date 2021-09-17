/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 29, 2016 by mbechler
 */
package eu.agno3.runtime.http.service.session;


import java.security.MessageDigest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.session.Session;
import org.eclipse.jetty.server.session.SessionHandler;


/**
 * @author mbechler
 *
 */
public class ExtendedSessionHandler extends SessionHandler {

    private static final Logger log = Logger.getLogger(ExtendedSessionHandler.class);

    private final SessionBindingGenerator binding;


    /**
     * @param binding
     */
    public ExtendedSessionHandler ( SessionBindingGenerator binding ) {
        this.binding = binding;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.session.SessionHandler#checkRequestedSessionId(org.eclipse.jetty.server.Request,
     *      javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void checkRequestedSessionId ( Request req, HttpServletRequest httpReq ) {
        super.checkRequestedSessionId(req, httpReq);

        HttpSession session = req.getSession(false);
        if ( session != null ) {
            byte[] h = this.binding.generateHash(httpReq);
            byte[] stored = (byte[]) session.getAttribute(SessionBindingGenerator.SESSION_KEY);
            if ( h != null ) {
                if ( stored == null || !MessageDigest.isEqual(stored, h) ) {
                    log.warn("Suspected session hijacking attempt, binding data changed, rejecting request."); //$NON-NLS-1$
                    if ( log.isDebugEnabled() ) {
                        log.debug("Expected session key " + ( stored != null ? Hex.encodeHexString(stored) : null )); //$NON-NLS-1$
                        log.debug("Request session key " + Hex.encodeHexString(h)); //$NON-NLS-1$
                    }
                    req.setSession(null);
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.session.SessionHandler#newHttpSession(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public HttpSession newHttpSession ( HttpServletRequest request ) {
        HttpSession s = super.newHttpSession(request);
        byte[] h = this.binding.generateHash(request);
        if ( h != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Generated session key " + Hex.encodeHexString(h)); //$NON-NLS-1$
            }
            s.setAttribute(SessionBindingGenerator.SESSION_KEY, h);
        }
        else {
            log.debug("Binding does not provide key"); //$NON-NLS-1$
        }

        return s;
    }


    /**
     * Work around Jetty #1224
     * 
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.session.SessionHandler#removeSession(java.lang.String, boolean)
     */
    @Override
    public Session removeSession ( String id, boolean invalidate ) {
        try {
            // Remove the Session object from the session store and any backing data store
            Session session = this._sessionCache.get(id);
            try {
                if ( session != null ) {
                    if ( invalidate ) {
                        if ( this._sessionListeners != null ) {
                            HttpSessionEvent event = new HttpSessionEvent(session);
                            for ( int i = this._sessionListeners.size() - 1; i >= 0; i-- ) {
                                this._sessionListeners.get(i).sessionDestroyed(event);
                            }
                        }
                    }
                }
                // TODO if session object is not known to this node, how to get rid of it if no other
                // node knows about it?

                return session;
            }
            finally {
                this._sessionCache.delete(id);
            }
        }
        catch ( Exception e ) {
            log.warn("Failed to remove session", e); //$NON-NLS-1$
            return null;
        }
    }
}
