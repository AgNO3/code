/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.menu.base;


/**
 * @author mbechler
 * 
 */
public interface ActionMenuContribution {

    /**
     * 
     * @return weight
     */
    float getWeight ();


    /**
     * 
     * @return localization label
     */
    String getLabelKey ();


    /**
     * 
     * @return action to execute
     */
    String getAction ();


    /**
     * 
     * @return icon type
     */
    String getIcon ();


    /**
     * 
     * @return the contributor adding this contribution
     */
    ActionMenuContributor getSource ();
}
