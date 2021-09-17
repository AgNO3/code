/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2015 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.component.commandlink.CommandLinkRenderer;
import org.primefaces.util.WidgetBuilder;


/**
 * @author mbechler
 *
 */
public class DialogOpenCommandLinkRenderer extends CommandLinkRenderer {

    private static final Logger log = Logger.getLogger(DialogOpenCommandLinkRenderer.class);


    @Override
    public void decode ( FacesContext context, UIComponent component ) {
        log.trace("decode()"); //$NON-NLS-1$
        String param = component.getClientId(context);

        if ( context.getExternalContext().getRequestParameterMap().containsKey(param) ) {
            DialogOpenComponent dialog = (DialogOpenComponent) component;

            if ( log.isDebugEnabled() ) {
                log.debug("Publishing dialog open event for dialog " + dialog.getDialog()); //$NON-NLS-1$
            }

            component.queueEvent(new DialogOpenEvent(component));
        }

        decodeBehaviors(context, component);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
     */
    @Override
    public void encodeBegin ( FacesContext context, UIComponent component ) throws IOException {
        DialogOpenCommandLink link = (DialogOpenCommandLink) component;
        String clientId = link.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("DialogOpenCommandLink", link.resolveWidgetVar(), clientId); //$NON-NLS-1$
        encodeClientBehaviors(context, link);
        wb.finish();
        super.encodeBegin(context, component);
    }
}
