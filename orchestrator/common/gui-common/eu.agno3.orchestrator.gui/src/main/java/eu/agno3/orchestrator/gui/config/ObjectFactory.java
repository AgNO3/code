/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.config;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     */
    public ObjectFactory () {}


    /**
     * @return a default implementation
     */
    public GuiConfig createGuiConfig () {
        return new GuiConfigImpl();
    }

}
