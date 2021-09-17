/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.group;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.menu.MenuEntry;
import eu.agno3.orchestrator.server.webgui.structure.menu.AbstractStructuralObjectMenuStateBean;
import eu.agno3.runtime.jsf.windowscope.WindowScoped;


/**
 * @author mbechler
 * 
 */
@Named ( "structuralGroupMenuStateBean" )
@WindowScoped
public class StructuralGroupMenuStateBean extends AbstractStructuralObjectMenuStateBean {

    private static final String MENU_GROUP_ICON = "ui-icon-gear"; //$NON-NLS-1$
    private static final String MENU_GROUP_MSG_PREFIX = "menu.group."; //$NON-NLS-1$
    private static final List<MenuEntry> DEFAULT_GROUP_MENU_ENTRIES = new ArrayList<>();


    static {
        DEFAULT_GROUP_MENU_ENTRIES.add(new MenuEntry(
            "dashboard", //$NON-NLS-1$
            "/structure/group/index.xhtml")); //$NON-NLS-1$
    }

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
        if ( ! ( this.getViewContext().getSelectedAnchor() instanceof GroupStructuralObject ) ) {
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

        DefaultSubMenu groupMenu = new DefaultSubMenu(GuiMessages.get("menu.group.label"), MENU_GROUP_ICON); //$NON-NLS-1$
        groupMenu.setExpanded(true);
        this.addEntries(groupMenu, DEFAULT_GROUP_MENU_ENTRIES, MENU_GROUP_MSG_PREFIX);
        this.addObjectMenuEntries(groupMenu);
        m.addElement(groupMenu);

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
        return "?faces-redirect=true&group=" + this.getViewContext().getSelectedAnchor().getId(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractMenuStateBean#getLocalized(java.lang.String)
     */
    @Override
    protected String getLocalized ( String label ) {
        return GuiMessages.get(MENU_GROUP_MSG_PREFIX + label);
    }

}
