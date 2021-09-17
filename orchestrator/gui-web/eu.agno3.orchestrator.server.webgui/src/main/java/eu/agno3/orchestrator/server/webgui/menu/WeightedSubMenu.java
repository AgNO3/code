/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import org.primefaces.model.menu.DefaultSubMenu;


/**
 * @author mbechler
 *
 */
public class WeightedSubMenu extends DefaultSubMenu implements WeightedMenuElement {

    /**
     * 
     */
    private static final long serialVersionUID = 7842803972766405697L;
    private float weight = 0.0f;


    /**
     * 
     */
    public WeightedSubMenu () {
        super();
    }


    /**
     * @param label
     * @param icon
     */
    public WeightedSubMenu ( String label, String icon ) {
        super(label, icon);
    }


    /**
     * @param label
     */
    public WeightedSubMenu ( String label ) {
        super(label);
    }


    /**
     * @param weight
     * @param label
     * @param icon
     */
    public WeightedSubMenu ( float weight, String label, String icon ) {
        super(label, icon);
        this.weight = weight;
    }


    /**
     * @param weight
     * @param label
     */
    public WeightedSubMenu ( float weight, String label ) {
        super(label);
        this.weight = weight;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.WeightedMenuElement#getWeight()
     */
    @Override
    public float getWeight () {
        return this.weight;
    }

}
