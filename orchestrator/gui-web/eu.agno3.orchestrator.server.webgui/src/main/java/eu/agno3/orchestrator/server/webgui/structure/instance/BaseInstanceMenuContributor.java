/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.orchestrator.server.webgui.menu.AbstractMenuContributor;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuElement;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class BaseInstanceMenuContributor extends AbstractMenuContributor {

    private static final Logger log = Logger.getLogger(BaseInstanceMenuContributor.class);

    @Inject
    private AgentStateTracker agentStateTracker;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#isApplicable(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public boolean isApplicable ( StructuralObject selectedObject, StructuralObject refObject ) {
        return selectedObject instanceof InstanceStructuralObject && refObject == null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#getContributions(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public List<WeightedMenuElement> getContributions ( StructuralObject selectedObject, StructuralObject refObject ) {
        return Collections.EMPTY_LIST;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#getListenTo(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public Collection<String> getListenTo ( StructuralObject selectedObject, StructuralObject refObject ) {
        if ( selectedObject instanceof InstanceStructuralObject ) {
            return this.agentStateTracker.getStateListenTo((InstanceStructuralObject) selectedObject);
        }
        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#notifyRefresh(java.lang.String, java.lang.String,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public boolean notifyRefresh ( String path, String payload, StructuralObject selectedObject, StructuralObject refObject ) {
        if ( selectedObject instanceof InstanceStructuralObject ) {
            log.debug("Refreshing state"); //$NON-NLS-1$
            ComponentState old = this.agentStateTracker.getCachedState((InstanceStructuralObject) selectedObject);
            ComponentState refreshed = this.agentStateTracker.forceRefresh((InstanceStructuralObject) selectedObject);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Old status %s new state %s", old, refreshed)); //$NON-NLS-1$
            }
            return old != refreshed;

        }
        return false;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    protected String makeObjectParameters ( StructuralObject selectedObject, StructuralObject refObject ) {
        return "?faces-redirect=true&instance=" + selectedObject.getId() + "&cid="; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
