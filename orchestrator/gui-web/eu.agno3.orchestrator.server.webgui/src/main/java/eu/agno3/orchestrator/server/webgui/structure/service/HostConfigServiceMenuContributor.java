/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.service;


import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuElement;
import eu.agno3.orchestrator.server.webgui.menu.WeightedMenuItem;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class HostConfigServiceMenuContributor extends BaseServiceMenuContributor {

    @Inject
    private AgentStateTracker agentStateTracker;

    @Inject
    private StructureCacheBean structureCache;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.MenuContributor#isApplicable(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public boolean isApplicable ( StructuralObject selectedObject, StructuralObject refObject ) {
        ServiceStructuralObject serviceObject = getServiceObject(selectedObject, refObject);
        return serviceObject != null && HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(serviceObject.getServiceType());
    }


    /**
     * 
     */
    private static ServiceStructuralObject getServiceObject ( StructuralObject selectedObject, StructuralObject refObject ) {
        if ( selectedObject instanceof ServiceStructuralObject && refObject == null ) {
            return (ServiceStructuralObject) selectedObject;
        }
        else if ( refObject instanceof ServiceStructuralObject ) {
            return (ServiceStructuralObject) refObject;
        }

        return null;
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
        WeightedMenuItem itm;

        InstanceStructuralObject instance;
        if ( refObject instanceof InstanceStructuralObject ) {
            instance = (InstanceStructuralObject) refObject;
        }
        else {
            try {
                instance = (InstanceStructuralObject) this.structureCache.getParentFor(getServiceObject(selectedObject, refObject));
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                return res;
            }
        }

        itm = new WeightedMenuItem(-100.0f, GuiMessages.get("menu.host.dashboard")); //$NON-NLS-1$
        itm.setOutcome("/structure/instance/index.xhtml" + makeInstanceParameters(instance)); //$NON-NLS-1$
        res.add(itm);

        itm = new WeightedMenuItem(-20.0f, GuiMessages.get("menu.host.update")); //$NON-NLS-1$
        itm.setOutcome("/structure/instance/update.xhtml" + makeInstanceParameters(instance)); //$NON-NLS-1$
        res.add(itm);

        itm = new WeightedMenuItem(-5.0f, GuiMessages.get("menu.host.systemInfo")); //$NON-NLS-1$
        itm.setOutcome("/structure/instance/sysinfo/index.xhtml" + makeInstanceParameters(instance)); //$NON-NLS-1$
        if ( !this.agentStateTracker.isAgentOnline(instance) ) {
            itm.setDisabled(true);
            itm.setTitle(GuiMessages.get(GuiMessages.AGENT_STATE_DETACHED));
        }
        res.add(itm);

        itm = new WeightedMenuItem(10.0f, GuiMessages.get("menu.host.license")); //$NON-NLS-1$
        itm.setOutcome("/structure/instance/license.xhtml" + makeInstanceParameters(instance)); //$NON-NLS-1$
        res.add(itm);

        itm = new WeightedMenuItem(30.0f, GuiMessages.get("menu.host.logs")); //$NON-NLS-1$
        itm.setOutcome("/logs/log.xhtml?faces-redirect=true&object=" + //$NON-NLS-1$
                instance.getId() + "&cid="); //$NON-NLS-1$
        res.add(itm);

        return res;
    }


    /**
     * @param instance
     * @return
     */
    private static String makeInstanceParameters ( InstanceStructuralObject instance ) {
        return "?faces-redirect=true&instance=" + instance.getId() + "&cid="; //$NON-NLS-1$ //$NON-NLS-2$

    }

}
