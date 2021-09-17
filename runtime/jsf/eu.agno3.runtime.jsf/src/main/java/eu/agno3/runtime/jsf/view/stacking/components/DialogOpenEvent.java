/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;


/**
 * @author mbechler
 *
 */
public class DialogOpenEvent extends ActionEvent {

    /**
     * @param uiComponent
     */
    public DialogOpenEvent ( UIComponent uiComponent ) {
        super(uiComponent);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1290041059386609279L;

}
