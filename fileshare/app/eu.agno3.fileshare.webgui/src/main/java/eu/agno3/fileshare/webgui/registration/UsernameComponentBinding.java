/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.registration;


import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@Named ( "usernameComponentBinding" )
@RequestScoped
public class UsernameComponentBinding {

    private UIComponent component;


    /**
     * @return the component
     */
    public UIComponent getComponent () {
        return this.component;
    }


    /**
     * @param component
     *            the component to set
     */
    public void setComponent ( UIComponent component ) {
        this.component = component;
    }
}
