/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import org.primefaces.model.menu.DefaultMenuItem;


/**
 * @author mbechler
 *
 */
public class WeightedMenuItem extends DefaultMenuItem implements WeightedMenuElement {

    /**
     * 
     */
    private static final long serialVersionUID = 7842803972766405697L;
    private float weight = 0.0f;


    /**
     * 
     */
    public WeightedMenuItem () {
        super();
    }


    /**
     * @param value
     * @param icon
     * @param url
     */
    public WeightedMenuItem ( Object value, String icon, String url ) {
        super(value, icon, url);
    }


    /**
     * @param value
     * @param icon
     */
    public WeightedMenuItem ( Object value, String icon ) {
        super(value, icon);
    }


    /**
     * @param value
     */
    public WeightedMenuItem ( Object value ) {
        super(value);
    }


    /**
     * @param weight
     * @param value
     * @param icon
     * @param url
     */
    public WeightedMenuItem ( float weight, Object value, String icon, String url ) {
        super(value, icon, url);
        this.weight = weight;
    }


    /**
     * @param weight
     * @param value
     * @param icon
     */
    public WeightedMenuItem ( float weight, Object value, String icon ) {
        super(value, icon);
        this.weight = weight;
    }


    /**
     * @param weight
     * @param value
     */
    public WeightedMenuItem ( float weight, Object value ) {
        super(value);
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
