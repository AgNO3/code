/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.service;


import javax.inject.Named;

import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.structure.menu.AbstractStructuralObjectMenuStateBean;
import eu.agno3.runtime.jsf.windowscope.WindowScoped;


/**
 * @author mbechler
 * 
 */
@Named ( "serviceMenuStateBean" )
@WindowScoped
public class ServiceMenuStateBean extends AbstractStructuralObjectMenuStateBean {

    private static final String MENU_SERVICE_ICON = "ui-icon-gear"; //$NON-NLS-1$
    static final String MENU_SERVICE_MSG_PREFIX = "menu.service."; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -8127309765315278350L;


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractMenuStateBean#getModel()
     */
    @Override
    public MenuModel getModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( ! ( this.getViewContext().getSelectedAnchor() instanceof ServiceStructuralObject ) ) {
            return new DefaultMenuModel();
        }
        return super.getModel(this.getViewContext().getSelectedAnchor());
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
        DefaultSubMenu serviceMenu = new DefaultSubMenu(GuiMessages.get("menu.service.label"), MENU_SERVICE_ICON); //$NON-NLS-1$
        serviceMenu.setExpanded(true);
        this.addObjectMenuEntries(serviceMenu);
        m.addElement(serviceMenu);
        return m;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @Override
    protected String makeDefaultParameters () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return "?faces-redirect=true&service=" + this.getViewContext().getSelectedAnchor().getId(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractMenuStateBean#getLocalized(java.lang.String)
     */
    @Override
    protected String getLocalized ( String label ) {
        return GuiMessages.get(MENU_SERVICE_MSG_PREFIX + label);
    }
}
