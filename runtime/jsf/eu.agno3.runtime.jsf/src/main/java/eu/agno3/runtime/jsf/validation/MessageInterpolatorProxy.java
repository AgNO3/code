/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2013 by mbechler
 */
package eu.agno3.runtime.jsf.validation;


import java.util.Locale;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.MessageInterpolator;

import org.apache.log4j.Logger;
import org.ops4j.pax.cdi.api.OsgiService;


/**
 * @author mbechler
 * 
 */
@Named ( "javax_validation_MessageInterpolator" )
@RequestScoped
public class MessageInterpolatorProxy implements MessageInterpolator {

    private static final Logger log = Logger.getLogger(MessageInterpolatorProxy.class);

    @Inject
    @OsgiService ( dynamic = true )
    private MessageInterpolator proxied;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.MessageInterpolator#interpolate(java.lang.String,
     *      javax.validation.MessageInterpolator.Context)
     */
    @Override
    public String interpolate ( String msg, Context ctx ) {
        return this.interpolate(msg, ctx, null);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.MessageInterpolator#interpolate(java.lang.String,
     *      javax.validation.MessageInterpolator.Context, java.util.Locale)
     */
    @Override
    public String interpolate ( String msg, Context ctx, Locale l ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Context: " + ctx); //$NON-NLS-1$
        }
        return this.proxied.interpolate(msg, ctx, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

}
