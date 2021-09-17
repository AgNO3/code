/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.ServiceService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.menu.StructuralObjectComparator;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.InstanceStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;
import eu.agno3.orchestrator.server.webgui.structure.menu.AbstractStructuralObjectMenuStateBean;
import eu.agno3.runtime.jsf.windowscope.WindowScoped;


/**
 * @author mbechler
 * 
 */
@Named ( "instanceMenuStateBean" )
@WindowScoped
public class InstanceMenuStateBean extends AbstractStructuralObjectMenuStateBean implements Serializable {

    private static final Logger log = Logger.getLogger(InstanceMenuStateBean.class);

    private static final String MENU_HOST_ICON = "ui-icon-gear"; //$NON-NLS-1$
    private static final String MENU_SERVICES_ICON = "ui-icon-wrench"; //$NON-NLS-1$

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureUtil su;

    @Inject
    private AgentStateTracker stateTracker;

    @Inject
    private InstanceStateTracker instanceTracker;

    private ComponentState lastComponentState;

    /**
     * 
     */
    private static final long serialVersionUID = -8127309765315278350L;


    @Override
    public MenuModel getModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( ! ( this.getViewContext().getSelectedAnchor() instanceof InstanceStructuralObject ) ) {
            return new DefaultMenuModel();
        }

        return super.getModel(this.getViewContext().getSelectedAnchor());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.AbstractStructuralObjectMenuStateBean#contextChanged(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    protected void contextChanged ( StructuralObject context ) {
        this.lastComponentState = this.stateTracker.getAgentState((InstanceStructuralObject) context);
        if ( log.isDebugEnabled() ) {
            log.debug("Component state is " + this.lastComponentState); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.AbstractStructuralObjectMenuStateBean#notifyRefresh(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void notifyRefresh ( String path, String payload ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        this.getViewContext().refreshSelected();
        InstanceStructuralObject instance = (InstanceStructuralObject) this.getViewContext().getSelectedAnchor();
        List<ServiceStructuralObject> services = getServices(instance);
        this.instanceTracker.forceRefresh(instance);
        boolean changed = this.getMenuContributionManager().notifyRefresh(path, payload, instance, null);
        for ( ServiceStructuralObject service : services ) {
            changed |= this.getMenuContributionManager().notifyRefresh(path, payload, instance, service);
        }
        if ( changed ) {
            log.debug("Service entries changed"); //$NON-NLS-1$
            refresh();
        }
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @Override
    protected DefaultMenuModel createMenuModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        DefaultMenuModel m = new DefaultMenuModel();
        DefaultSubMenu hostMenu = new DefaultSubMenu(GuiMessages.get("menu.host.label"), MENU_HOST_ICON); //$NON-NLS-1$
        hostMenu.setExpanded(true);
        DefaultSubMenu servicesMenu = new DefaultSubMenu(GuiMessages.get("menu.services.label"), MENU_SERVICES_ICON); //$NON-NLS-1$
        servicesMenu.setExpanded(true);
        this.addObjectMenuEntries(hostMenu);
        this.buildServiceMenu(servicesMenu);

        if ( hostMenu.getElementsCount() > 0 ) {
            m.addElement(hostMenu);
        }
        m.addElement(servicesMenu);

        return m;
    }


    /**
     * @param servicesMenu
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private void buildServiceMenu ( DefaultSubMenu servicesMenu ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        StructuralObject selectedAnchor = this.getViewContext().getSelectedAnchor();
        if ( ! ( selectedAnchor instanceof InstanceStructuralObject ) ) {
            return;
        }
        InstanceStructuralObject instance = (InstanceStructuralObject) selectedAnchor;
        addListenTo(this.instanceTracker.getStateListenTo(instance));
        List<ServiceStructuralObject> services = getServices(instance);

        for ( ServiceStructuralObject service : services ) {
            log.debug("Service " + service.getServiceType()); //$NON-NLS-1$
            DefaultSubMenu itm = new DefaultSubMenu(this.su.getServiceDisplayName(service));
            itm.setExpanded(true);
            addListenTo(this.getMenuContributionManager().addMenuContributions(itm, instance, service));
            servicesMenu.addElement(itm);
        }
    }


    /**
     * @param instance
     * @return
     * @throws GuiWebServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private List<ServiceStructuralObject> getServices ( InstanceStructuralObject instance )
            throws GuiWebServiceException, ModelObjectNotFoundException, ModelServiceException {
        ServiceService serviceService = this.ssp.getService(ServiceService.class);
        List<ServiceStructuralObject> services = new ArrayList<>(serviceService.getServices(instance));
        Collections.sort(services, new StructuralObjectComparator());
        return services;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @Override
    protected String makeDefaultParameters () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return "?faces-redirect=true&instance=" + this.getViewContext().getSelectedAnchor().getId(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractMenuStateBean#getLocalized(java.lang.String)
     */
    @Override
    protected String getLocalized ( String label ) {
        return GuiMessages.get("menu." + label); //$NON-NLS-1$
    }

}
