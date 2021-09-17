package eu.agno3.orchestrator.server.webgui.orchestrator;


import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.orchestrator.desc.OrchestratorServiceTypeDescriptor;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.menu.AbstractMenuContributor;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuElement;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuItem;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.ServiceStateTracker;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class OrchestratorMenuContributor extends AbstractMenuContributor {

    @Inject
    private AgentStateTracker stateTracker;

    @Inject
    private ServiceStateTracker serviceTracker;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#isApplicable(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public boolean isApplicable ( StructuralObject selectedObject, StructuralObject refObject ) {
        if ( selectedObject instanceof ServiceStructuralObject && OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE
                .equals( ( (ServiceStructuralObject) selectedObject ).getServiceType()) ) {
            return true;
        }

        if ( refObject instanceof ServiceStructuralObject
                && OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE.equals( ( (ServiceStructuralObject) refObject ).getServiceType()) ) {
            return true;
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#getContributions(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public List<WeightedMenuElement> getContributions ( StructuralObject selectedObject, StructuralObject refObject ) {
        List<WeightedMenuElement> res = new ArrayList<>();

        boolean active = checkActive(selectedObject, refObject);

        WeightedMenuItem itm = new WeightedMenuItem(400.0f, GuiMessages.get("menu.users")); //$NON-NLS-1$
        itm.setOutcome("/security/localUsers.xhtml" + makeObjectParameters(selectedObject, refObject)); //$NON-NLS-1$

        if ( !active ) {
            itm.setDisabled(true);
            itm.setTitle(GuiMessages.get(GuiMessages.AGENT_NOT_CONNECTED));
        }
        res.add(itm);
        return res;
    }


    /**
     * @param selectedObject
     * @param refObject
     * @return
     */
    private boolean checkActive ( StructuralObject selectedObject, StructuralObject refObject ) {
        if ( selectedObject instanceof ServiceStructuralObject ) {
            ConfigurationState cfgState = ( (ServiceStructuralObject) selectedObject ).getState();
            boolean configured = cfgState != ConfigurationState.UNCONFIGURED && cfgState != ConfigurationState.UNKNOWN;
            if ( !configured ) {
                return false;
            }
            return this.serviceTracker.isServiceOnline((ServiceStructuralObject) selectedObject);
        }
        else if ( selectedObject instanceof InstanceStructuralObject && refObject instanceof ServiceStructuralObject ) {
            boolean online = this.stateTracker.isAgentOnline((InstanceStructuralObject) selectedObject)
                    && this.serviceTracker.isServiceOnline((ServiceStructuralObject) refObject);
            ConfigurationState cfgState = ( (ServiceStructuralObject) refObject ).getState();
            return online && cfgState != ConfigurationState.UNCONFIGURED && cfgState != ConfigurationState.UNKNOWN;
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
        if ( refObject != null ) {
            return String.format("?faces-redirect=true&cid=&anchor=%s&service=%s", selectedObject.getId(), refObject.getId()); //$NON-NLS-1$
        }
        return "?faces-redirect=true&cid=&service=" + selectedObject.getId(); //$NON-NLS-1$
    }

}
