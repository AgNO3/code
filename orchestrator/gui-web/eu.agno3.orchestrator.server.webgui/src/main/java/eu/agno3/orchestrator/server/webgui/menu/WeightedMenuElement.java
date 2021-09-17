/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import org.primefaces.model.menu.MenuElement;


/**
 * @author mbechler
 *
 */
public interface WeightedMenuElement extends MenuElement {

    /**
     * @return the menu entry weight, lower floats up, heavier down
     */
    float getWeight ();

}
