/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2013 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import javax.faces.context.ExternalContext;

import org.apache.log4j.Logger;
import org.apache.myfaces.spi.AnnotationProvider;
import org.apache.myfaces.spi.AnnotationProviderFactory;


/**
 * @author mbechler
 * 
 */
public class OSGIAnnotationProviderFactory extends AnnotationProviderFactory {

    private static final Logger log = Logger.getLogger(OSGIAnnotationProviderFactory.class);


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.myfaces.spi.AnnotationProviderFactory#createAnnotationProvider(javax.faces.context.ExternalContext)
     */
    @Override
    public AnnotationProvider createAnnotationProvider ( ExternalContext ctx ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Return annotation provider for context " + ctx.getContextName()); //$NON-NLS-1$
        }
        return new OSGIAnnotationProvider();
    }

}
