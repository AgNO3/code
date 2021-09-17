/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.menu.base;


import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;


/**
 * @author mbechler
 * 
 */
public interface ActionMenuContributor {

    /**
     * 
     * @param type
     * @param withContext
     *            whether a context for the object is avialable
     * @return whether this contributor generally has contributes for the given context
     */
    boolean isApplicable ( StructuralObjectType type, boolean withContext );


    /**
     * 
     * @param fromContext
     *            whether this is a contribution for the currently selected object
     * @return the contributions
     */
    Set<ActionMenuContribution> getContributions ( boolean fromContext );


    /**
     * @return the localization resource bundle base name
     */
    String getBaseName ();


    /**
     * 
     * @return a prefix for the label key
     */
    String getLabelKeyPrefix ();
}
