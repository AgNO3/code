/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 22, 2016 by mbechler
 */
package eu.agno3.runtime.http.service.webapp.internal;


import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * @author mbechler
 *
 */
public class ExtendedWebAppContext extends WebAppContext {

    private static final Logger log = Logger.getLogger(ExtendedWebAppContext.class);
    private static final int CACHE_SIZE = 512;
    private final Map<String, Resource> resourceCache = Collections.synchronizedMap(new LRUMap<String, Resource>(CACHE_SIZE));


    /**
     * @param symbolicName
     * @param contextPath
     */
    public ExtendedWebAppContext ( String symbolicName, String contextPath ) {
        super(symbolicName, contextPath);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.webapp.WebAppContext#getResource(java.lang.String)
     */
    @Override
    public Resource getResource ( String path ) throws MalformedURLException {

        if ( this.resourceCache.containsKey(path) ) {
            return this.resourceCache.get(path);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Looking up " + path); //$NON-NLS-1$
        }

        Resource r = super.getResource(path);
        this.resourceCache.put(path, r);
        return r;
    }
}
