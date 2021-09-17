/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.util.Collection;
import java.util.List;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 *
 */
public interface MenuContributor {

    /**
     * @param selectedObject
     * @param refObject
     * @return whether this menu contributor has entries for this constellation
     */
    boolean isApplicable ( StructuralObject selectedObject, StructuralObject refObject );


    /**
     * @param selectedObject
     * @param refObject
     * @return the menu contributions
     */
    List<WeightedMenuElement> getContributions ( StructuralObject selectedObject, StructuralObject refObject );


    /**
     * @param selectedObject
     * @param refObject
     * @return the events to listen to
     */
    Collection<String> getListenTo ( StructuralObject selectedObject, StructuralObject refObject );


    /**
     * @param path
     * @param payload
     * @param selectedObject
     * @param refObject
     * @return whether a change has been detected
     */
    boolean notifyRefresh ( String path, String payload, StructuralObject selectedObject, StructuralObject refObject );

}
