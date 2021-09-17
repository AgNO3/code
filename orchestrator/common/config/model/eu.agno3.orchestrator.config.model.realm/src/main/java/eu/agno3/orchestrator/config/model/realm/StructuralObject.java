/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.Set;

import eu.agno3.orchestrator.config.model.base.BaseObject;


/**
 * @author mbechler
 * 
 */
public interface StructuralObject extends BaseObject {

    /**
     * 
     * @return the object type
     */
    StructuralObjectType getType ();


    /**
     * 
     * @return the type specific type
     */
    String getLocalType ();


    /**
     * 
     * @return the parent types to which these objects may be attached
     */
    Set<StructuralObjectType> getAllowedParents ();


    /**
     * @return the displayName
     */
    String getDisplayName ();


    /**
     * 
     * @return the overall state for this object
     */
    StructuralObjectState getOverallState ();

}