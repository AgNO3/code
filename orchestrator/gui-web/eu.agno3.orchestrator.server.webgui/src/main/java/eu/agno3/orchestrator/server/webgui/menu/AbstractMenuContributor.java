/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.12.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.util.Collection;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 *
 */
public abstract class AbstractMenuContributor implements MenuContributor {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#getListenTo(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public Collection<String> getListenTo ( StructuralObject selectedObject, StructuralObject refObject ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#notifyRefresh(java.lang.String, java.lang.String,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public boolean notifyRefresh ( String path, String payload, StructuralObject selectedObject, StructuralObject refObject ) {
        return false;
    }
}
