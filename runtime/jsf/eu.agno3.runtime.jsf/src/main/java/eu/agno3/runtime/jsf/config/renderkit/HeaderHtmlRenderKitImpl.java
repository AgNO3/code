/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.07.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config.renderkit;


import javax.faces.render.ResponseStateManager;

import org.apache.myfaces.renderkit.html.HtmlRenderKitImpl;


/**
 * @author mbechler
 *
 */
public class HeaderHtmlRenderKitImpl extends HtmlRenderKitImpl {

    private ResponseStateManager responseStateManager;


    /**
     * 
     */
    public HeaderHtmlRenderKitImpl () {
        super();
        this.responseStateManager = new HeaderHtmlResponseStateManager();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.myfaces.renderkit.html.HtmlRenderKitImpl#getResponseStateManager()
     */
    @Override
    public ResponseStateManager getResponseStateManager () {
        return this.responseStateManager;
    }
}
