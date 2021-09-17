/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


/**
 * 
 * @author mbechler
 * 
 */
public class MenuEntry {

    private final String label;
    private final String view;
    private final String overrideArgs;


    /**
     * 
     * @param label
     *            menu item label message ID
     * @param view
     *            View path
     */
    public MenuEntry ( String label, String view ) {
        this.label = label;
        this.view = view;
        this.overrideArgs = null;
    }


    /**
     * 
     * @param label
     * @param view
     * @param overrideArgs
     */
    public MenuEntry ( String label, String view, String overrideArgs ) {
        this.label = label;
        this.view = view;
        this.overrideArgs = overrideArgs;
    }


    /**
     * @return the item label message ID
     */
    public String getLabel () {
        return this.label;
    }


    /**
     * @return the view path
     */
    public String getView () {
        return this.view;
    }


    /**
     * @return the overrideArgs
     */
    public String getOverrideArgs () {
        return this.overrideArgs;
    }

}