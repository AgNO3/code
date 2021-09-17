/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2013 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextWrapper;

import org.apache.log4j.Logger;
import org.apache.myfaces.shared.util.StateUtils;


/**
 * @author mbechler
 * 
 */
public class OSGIExternalContext extends ExternalContextWrapper {

    private static final Logger log = Logger.getLogger(OSGIExternalContext.class);

    private Map<String, Object> applicationMap;

    private final JSFServiceProvider serviceProvider;
    private final ExternalContext delegate;

    private OSGIResourceProvider resourceProvider;


    /**
     * @param context
     * @param serviceProvider
     * @param resourceProvider
     */
    public OSGIExternalContext ( ExternalContext context, JSFServiceProvider serviceProvider, OSGIResourceProvider resourceProvider ) {
        this.delegate = context;
        this.serviceProvider = serviceProvider;
        this.resourceProvider = resourceProvider;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.context.ExternalContextWrapper#getWrapped()
     */
    @Override
    public ExternalContext getWrapped () {
        return this.delegate;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.context.ExternalContextWrapper#getApplicationMap()
     */
    @Override
    public Map<String, Object> getApplicationMap () {

        if ( this.applicationMap == null ) {
            Map<String, Object> appMap = wrapApplicationMap();
            this.applicationMap = appMap;
        }

        return this.applicationMap;
    }


    /**
     * @return
     */
    private Map<String, Object> wrapApplicationMap () {
        Map<String, Object> appMap = this.delegate.getApplicationMap();
        appMap.put(StateUtils.SERIAL_FACTORY, this.serviceProvider.getSerialFactory());
        return appMap;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.context.ExternalContextWrapper#getResource(java.lang.String)
     */
    @Override
    public URL getResource ( String path ) throws MalformedURLException {
        return this.resourceProvider.getResource(path, this.delegate);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.context.ExternalContextWrapper#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream ( String path ) {

        if ( log.isTraceEnabled() ) {
            log.trace("Get resource as stream " + path); //$NON-NLS-1$
        }
        try {
            URL u = this.getResource(path);

            if ( u == null ) {
                log.warn("Failed to open resource as stream " + path); //$NON-NLS-1$
                return null;
            }

            return u.openStream();
        }
        catch ( IOException e ) {
            log.warn("Failed to open resource as stream:", e); //$NON-NLS-1$
            return null;
        }
    }

}
