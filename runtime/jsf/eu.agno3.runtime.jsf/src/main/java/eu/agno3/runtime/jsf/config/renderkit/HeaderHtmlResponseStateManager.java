/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.07.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config.renderkit;


import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;

import org.apache.myfaces.renderkit.html.HtmlResponseStateManager;
import org.apache.myfaces.shared.util.StateUtils;


/**
 * @author mbechler
 *
 */
public class HeaderHtmlResponseStateManager extends HtmlResponseStateManager {

    /**
     * 
     */
    private static final String X_JSF_VIEW_STATE = "X-JSF-View-State"; //$NON-NLS-1$
    private static final String STATELESS_TOKEN = "stateless"; //$NON-NLS-1$


    @Override
    public Object getState ( FacesContext facesContext, String viewId ) {

        Object savedState = getSavedState(facesContext);
        if ( savedState == null ) {
            return null;
        }

        return getStateCache(facesContext).restoreSerializedView(facesContext, viewId, savedState);
    }


    /**
     * Reconstructs the state from the "javax.faces.ViewState" request parameter.
     * 
     * @param facesContext
     *            the current FacesContext
     * 
     * @return the reconstructed state, or <code>null</code> if there was no saved state
     */
    private static Object getSavedState ( FacesContext facesContext ) {
        Object encodedState = facesContext.getExternalContext().getRequestParameterMap().get(STANDARD_STATE_SAVING_PARAM);

        if ( encodedState == null ) {
            encodedState = facesContext.getExternalContext().getRequestHeaderMap().get(X_JSF_VIEW_STATE);
        }

        if ( encodedState == null || ( ( (String) encodedState ).length() == 0 ) ) {
            return null;
        }

        if ( STATELESS_TOKEN.equals(encodedState) ) {
            // Should not happen, because ResponseStateManager.isStateless(context,viewId) should
            // catch it first
            return null;
        }
        return StateUtils.reconstruct((String) encodedState, facesContext.getExternalContext());

    }


    /**
     * Checks if the current request is a postback
     * 
     * @since 1.2
     */
    @Override
    public boolean isPostback ( FacesContext context ) {
        return context.getExternalContext().getRequestParameterMap().containsKey(ResponseStateManager.VIEW_STATE_PARAM)
                || context.getExternalContext().getRequestHeaderMap().containsKey(X_JSF_VIEW_STATE);
    }

}
