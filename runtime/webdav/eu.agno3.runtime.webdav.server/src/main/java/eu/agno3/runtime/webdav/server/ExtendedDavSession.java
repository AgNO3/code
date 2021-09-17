/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import java.io.Serializable;
import java.util.Locale;

import org.apache.jackrabbit.webdav.DavSession;


/**
 * @author mbechler
 *
 */
public interface ExtendedDavSession extends DavSession {

    /**
     * 
     * @return whether the session is persistent
     */
    boolean isPersistent ();


    /**
     * 
     * @param key
     * @return the attribute value
     */
    public Serializable getAttribute ( String key );


    /**
     * 
     * @param key
     * @param val
     */
    public void setAttribute ( String key, Serializable val );


    /**
     * @return the user locale
     */
    Locale getLocale ();
}
