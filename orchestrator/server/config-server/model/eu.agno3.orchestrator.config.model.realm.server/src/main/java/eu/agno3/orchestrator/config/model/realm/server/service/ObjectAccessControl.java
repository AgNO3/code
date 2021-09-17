/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 *
 */
public interface ObjectAccessControl {

    /**
     * @param obj
     * @param permission
     * @return whether the currently active subject has access to the given object
     */
    boolean hasAccess ( @Nullable StructuralObject obj, @Nullable String permission );


    /**
     * @param obj
     * @param permission
     */
    void checkAccess ( @Nullable StructuralObject obj, @Nullable String permission );


    /**
     * @return whether the object access control system is disabled
     */
    boolean isDisabled ();
}
