/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.07.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config.renderkit;


import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.PartialViewContextFactory;
import javax.faces.render.ResponseStateManager;


/**
 * @author mbechler
 *
 */
public class HeaderPartialViewContextWrapperFactory extends PartialViewContextFactory {

    private PartialViewContextFactory wrapped;


    /**
     * @param wrapped
     * 
     */
    public HeaderPartialViewContextWrapperFactory ( PartialViewContextFactory wrapped ) {
        this.wrapped = wrapped;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.context.PartialViewContextFactory#getPartialViewContext(javax.faces.context.FacesContext)
     */
    @Override
    public PartialViewContext getPartialViewContext ( FacesContext context ) {
        if ( context.isPostback() && !context.getExternalContext().getRequestParameterMap().containsKey(ResponseStateManager.VIEW_STATE_PARAM) ) {
            // only wrap if no parameters are given
            return new HeaderPartialViewContext(getWrapped().getPartialViewContext(context));
        }
        return getWrapped().getPartialViewContext(context);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.context.PartialViewContextFactory#getWrapped()
     */
    @Override
    public PartialViewContextFactory getWrapped () {
        return this.wrapped;
    }

}
