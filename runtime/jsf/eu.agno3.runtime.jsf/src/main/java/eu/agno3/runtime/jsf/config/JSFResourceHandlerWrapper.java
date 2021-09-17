/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2014 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;


/**
 * @author mbechler
 * 
 */
public class JSFResourceHandlerWrapper extends ResourceHandlerWrapper {

    private final ResourceHandler wrapped;
    private final Map<String, Long> lastModifiedCache = new ConcurrentHashMap<>();
    private static final long MAX_DELTA = 10000;


    /**
     * @param wrapped
     */
    public JSFResourceHandlerWrapper ( ResourceHandler wrapped ) {
        this.wrapped = wrapped;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ResourceHandlerWrapper#getWrapped()
     */
    @Override
    public ResourceHandler getWrapped () {
        return this.wrapped;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ResourceHandlerWrapper#createResource(java.lang.String)
     */
    @Override
    public Resource createResource ( String resourceName ) {
        return wrapResource(super.createResource(resourceName));
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ResourceHandlerWrapper#createResource(java.lang.String, java.lang.String)
     */
    @Override
    public Resource createResource ( String resourceName, String libraryName ) {
        return wrapResource(super.createResource(resourceName, libraryName));
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ResourceHandlerWrapper#createResource(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Resource createResource ( String resourceName, String libraryName, String contentType ) {
        return wrapResource(super.createResource(resourceName, libraryName, contentType));
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ResourceHandlerWrapper#createResourceFromId(java.lang.String)
     */
    @Override
    public Resource createResourceFromId ( String resourceId ) {
        return wrapResource(super.createResourceFromId(resourceId));
    }


    /**
     * @param createResourceFromId
     * @return
     */
    private Resource wrapResource ( Resource resource ) {
        if ( resource != null ) {
            JSFResourceWrapper lm = new JSFResourceWrapper(resource, getCachedLastModifiedTime(resource));
            this.lastModifiedCache.put(resource.getRequestPath(), lm.getLastModified());
            return lm;
        }
        return null;
    }


    /**
     * @param resource
     * @return
     */
    private Long getCachedLastModifiedTime ( Resource resource ) {
        Long c = this.lastModifiedCache.get(resource.getRequestPath());
        if ( c != null && c < System.currentTimeMillis() + MAX_DELTA ) {
            return c;
        }
        return null;
    }

}
