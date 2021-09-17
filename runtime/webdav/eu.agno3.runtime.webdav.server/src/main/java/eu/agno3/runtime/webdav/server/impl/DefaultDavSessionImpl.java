/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.runtime.webdav.server.ExtendedDavSession;


/**
 * @author mbechler
 *
 */
public class DefaultDavSessionImpl implements ExtendedDavSession, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4972194705473662146L;

    private static final Logger log = Logger.getLogger(DefaultDavSessionImpl.class);

    private Map<String, Serializable> attributes = new HashMap<>();
    private Set<String> tokens = new HashSet<>();
    private String sessionId;


    /**
     * 
     */
    public DefaultDavSessionImpl () {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.ExtendedDavSession#getLocale()
     */
    @Override
    public Locale getLocale () {
        Locale l = (Locale) this.attributes.get("locale"); //$NON-NLS-1$
        if ( l != null ) {
            return l;
        }
        return Locale.ROOT;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.ExtendedDavSession#isPersistent()
     */
    @Override
    public boolean isPersistent () {
        return this.sessionId != null;
    }


    /**
     * 
     * @param sessionId
     */
    public DefaultDavSessionImpl ( String sessionId ) {
        this.sessionId = sessionId;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavSession#addLockToken(java.lang.String)
     */
    @Override
    public void addLockToken ( String token ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Add lock token %s to %s", token, this.sessionId)); //$NON-NLS-1$
        }
        this.tokens.add(token);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavSession#getLockTokens()
     */
    @Override
    public String[] getLockTokens () {
        return this.tokens.toArray(new String[] {});
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavSession#removeLockToken(java.lang.String)
     */
    @Override
    public void removeLockToken ( String token ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Remove lock token %s from %s", token, this.sessionId)); //$NON-NLS-1$
        }
        this.tokens.remove(token);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavSession#addReference(java.lang.Object)
     */
    @Override
    public void addReference ( Object ref ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Add reference " + ref); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavSession#removeReference(java.lang.Object)
     */
    @Override
    public void removeReference ( Object ref ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Remove reference " + ref); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.ExtendedDavSession#getAttribute(java.lang.String)
     */
    @Override
    public Serializable getAttribute ( String key ) {
        return this.attributes.get(key);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.ExtendedDavSession#setAttribute(java.lang.String, java.io.Serializable)
     */
    @Override
    public void setAttribute ( String key, Serializable val ) {
        this.attributes.put(key, val);
    }

}
