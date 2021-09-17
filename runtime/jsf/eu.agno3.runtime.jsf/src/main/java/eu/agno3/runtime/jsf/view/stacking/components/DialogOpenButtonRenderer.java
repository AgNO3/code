/**

 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.commandbutton.CommandButtonRenderer;


/**
 * @author mbechler
 * 
 */
public class DialogOpenButtonRenderer extends CommandButtonRenderer {

    private static final Logger log = Logger.getLogger(DialogOpenButtonRenderer.class);


    /**
     * 
     */
    public DialogOpenButtonRenderer () {}


    @Override
    public void decode ( FacesContext context, UIComponent component ) {
        log.trace("decode()"); //$NON-NLS-1$
        CommandButton button = (CommandButton) component;
        boolean rendered = button.isRendered();
        if ( !rendered || button.isDisabled() ) {
            return;
        }

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
}
