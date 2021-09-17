/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiFeatureConfig;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.menu.AbstractMenuContributor;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuElement;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuItem;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class KeystoreMenuContributor extends AbstractMenuContributor {

    @Inject
    private AgentStateTracker agentStateTracker;

    @Inject
    private GuiFeatureConfig guiFeatures;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#isApplicable(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public boolean isApplicable ( StructuralObject selectedObject, StructuralObject refObject ) {
        return ( selectedObject instanceof InstanceStructuralObject && ( refObject == null ) ) || refObject instanceof InstanceStructuralObject;
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

        if ( this.guiFeatures.getShowDevTools() ) {
            WeightedMenuItem itm = new WeightedMenuItem(300.0f, GuiMessages.get("menu.host.keystores")); //$NON-NLS-1$
            itm.setOutcome("/crypto/keystores/manage.xhtml" + makeObjectParameters(selectedObject, refObject)); //$NON-NLS-1$
            itm.setDisabled(!this.agentStateTracker.isAgentOnline((InstanceStructuralObject) selectedObject));
            res.add(itm);
        }

        return res;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    protected String makeObjectParameters ( StructuralObject selectedObject, StructuralObject refObject ) {
        if ( refObject != null ) {
            return String.format("?faces-redirect=true&cid=&anchor=%s&instance=%s", selectedObject.getId(), refObject.getId()); //$NON-NLS-1$
        }
        return "?faces-redirect=true&cid=&instance=" + selectedObject.getId(); //$NON-NLS-1$
    }

}
