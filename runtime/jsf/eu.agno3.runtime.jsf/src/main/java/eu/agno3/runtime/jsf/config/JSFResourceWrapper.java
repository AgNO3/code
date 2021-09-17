/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2014 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.Resource;
import javax.faces.application.ResourceWrapper;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * @author mbechler
 * 
 */
public class JSFResourceWrapper extends ResourceWrapper {

    private final Resource wrapped;
    private final long lastModified;
    private final String versionedPath;
    private final long maxExpires = 604800000L;

    private static final Logger log = Logger.getLogger(JSFResourceWrapper.class);
    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'") //$NON-NLS-1$
            .withZoneUTC();


    /**
     * @param wrapped
     * @param cachedLastMod
     * 
     */
    public JSFResourceWrapper ( Resource wrapped, Long cachedLastMod ) {
        this.wrapped = wrapped;

        URL url = wrapped.getURL();

        if ( log.isDebugEnabled() ) {
            log.debug("Resource URL " + url); //$NON-NLS-1$
        }
        if ( cachedLastMod != null ) {
            this.lastModified = cachedLastMod;
        }
        else if ( "bundleentry".equals(url.getProtocol()) || //$NON-NLS-1$
                "file".equals(url.getProtocol()) ) { //$NON-NLS-1$
            long lm = 0;
            try {
                lm = url.openConnection().getLastModified();
            }
            catch ( IOException e ) {
                log.debug("Failed to get last modified time", e); //$NON-NLS-1$
                lm = -1;
            }
            this.lastModified = lm;
        }
        else {
            this.lastModified = 0;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Last modified is " + this.lastModified); //$NON-NLS-1$
        }

        StringBuilder sb = new StringBuilder(wrapped.getRequestPath());
        if ( this.lastModified > 0 ) {
            if ( wrapped.getRequestPath().indexOf('?') >= 0 ) {
                sb.append("&lm="); //$NON-NLS-1$
                sb.append(this.lastModified);
            }
            else {
                sb.append("?lm="); //$NON-NLS-1$
                sb.append(this.lastModified);
            }
        }

        this.versionedPath = sb.toString();

        if ( log.isDebugEnabled() ) {
            log.debug("Versioned path is " + this.versionedPath); //$NON-NLS-1$
        }
    }


    /**
     * @return the lastModified
     */
    public long getLastModified () {
        return this.lastModified;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ResourceWrapper#getWrapped()
     */
    @Override
    public Resource getWrapped () {
        return this.wrapped;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.application.ResourceWrapper#userAgentNeedsUpdate(javax.faces.context.FacesContext)
     */
    @Override
    public boolean userAgentNeedsUpdate ( FacesContext context ) {
        if ( !super.userAgentNeedsUpdate(context) ) {
            return false;
        }

        if ( this.lastModified > 0 && isStaticCSS() ) {
            String ifModifiedSinceString = context.getExternalContext().getRequestHeaderMap().get("If-Modified-Since"); //$NON-NLS-1$
            if ( ifModifiedSinceString == null ) {
                return true;
            }

            try {
                DateTime ifModSince = RFC1123_DATE_TIME_FORMATTER.parseDateTime(ifModifiedSinceString);
                if ( this.lastModified <= ifModSince.getMillis() ) {
                    return false;
                }
            }
            catch ( IllegalArgumentException e ) {
                log.debug("Failed to parse last modified date"); //$NON-NLS-1$
            }
        }

        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.application.ResourceWrapper#getInputStream()
     */
    @Override
    public InputStream getInputStream () throws IOException {
        if ( isStaticCSS() ) {
            return this.getURL().openStream();
        }
        return super.getInputStream();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ResourceWrapper#getResponseHeaders()
     */
    @Override
    public Map<String, String> getResponseHeaders () {
        FacesContext fc = FacesContext.getCurrentInstance();
        if ( !fc.getApplication().getResourceHandler().isResourceRequest(fc) ) {
            return Collections.EMPTY_MAP;
        }

        DateTime now = DateTime.now();
        if ( this.lastModified < 0 ) {
            // expire immediately
            Map<String, String> headers = new HashMap<>();
            headers.put("Last-Modified", now.minusDays(7).toString(RFC1123_DATE_TIME_FORMATTER)); //$NON-NLS-1$
            return headers;
        }
        else if ( this.lastModified > 0 && isStaticCSS() ) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Last-Modified", new DateTime(this.lastModified).toString(RFC1123_DATE_TIME_FORMATTER)); //$NON-NLS-1$
            headers.put("Expires", new DateTime().plus(this.maxExpires).toString(RFC1123_DATE_TIME_FORMATTER)); //$NON-NLS-1$
            return headers;
        }
        else {
            return super.getResponseHeaders();
        }
    }


    /**
     * @return
     */
    private boolean isStaticCSS () {
        return "text/css".equals(getContentType()) && //$NON-NLS-1$
                getResourceName().endsWith(".static.css"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.application.ResourceWrapper#getRequestPath()
     */
    @Override
    public String getRequestPath () {
        return this.versionedPath;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.application.ResourceWrapper#getURL()
     */
    @Override
    public URL getURL () {
        return getWrapped().getURL();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.Resource#getResourceName()
     */
    @Override
    public String getResourceName () {
        return getWrapped().getResourceName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.Resource#getContentType()
     */
    @Override
    public String getContentType () {
        return getWrapped().getContentType();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.Resource#getLibraryName()
     */
    @Override
    public String getLibraryName () {
        return getWrapped().getLibraryName();
    }
}
