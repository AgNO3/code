/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2013 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;
import javax.faces.validator.BeanValidator;
import javax.servlet.ServletContext;


/**
 * @author mbechler
 * 
 */
public class OSGIExternalContextFactory extends ExternalContextFactory {

    private final ExternalContextFactory delegate;


    /**
     * 
     */
    public OSGIExternalContextFactory () {
        this.delegate = null;
    }


    /**
     * @param delegate
     */
    public OSGIExternalContextFactory ( ExternalContextFactory delegate ) {
        this.delegate = delegate;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.context.ExternalContextFactory#getExternalContext(java.lang.Object, java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public ExternalContext getExternalContext ( Object context, Object request, Object response ) {
        ExternalContext extCtx = this.delegate.getExternalContext(context, request, response);
        ServletContext ctx = (ServletContext) extCtx.getContext();
        JSFServiceProvider sp = JSFServiceProvider.getInstance();
        ctx.setAttribute(BeanValidator.VALIDATOR_FACTORY_KEY, sp.getValidatorFactory());
        return new OSGIExternalContext(extCtx, sp, sp.getResourceProvider(extCtx));
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.context.ExternalContextFactory#getWrapped()
     */
    @Override
    public ExternalContextFactory getWrapped () {
        return this.delegate;
    }
}
