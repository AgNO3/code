/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Aug 21, 2017 by mbechler
 */
package eu.agno3.runtime.jsf.components.treemenu;


import org.primefaces.component.panelmenu.PanelMenu;


/**
 * @author mbechler
 *
 */
@javax.faces.application.ResourceDependencies ( value = {
    @javax.faces.application.ResourceDependency ( library = "primefaces", name = "components.css" ),
    @javax.faces.application.ResourceDependency ( library = "primefaces", name = "jquery/jquery.js" ),
    @javax.faces.application.ResourceDependency ( library = "primefaces", name = "jquery/jquery-plugins.js" ),
    @javax.faces.application.ResourceDependency ( library = "primefaces", name = "core.js" ),
    @javax.faces.application.ResourceDependency ( library = "primefaces", name = "components.js" )
})
public class TreeMenu extends PanelMenu {

    @Override
    public String getFamily () {
        return "eu.agno3.jsf.components"; //$NON-NLS-1$
    }

}
