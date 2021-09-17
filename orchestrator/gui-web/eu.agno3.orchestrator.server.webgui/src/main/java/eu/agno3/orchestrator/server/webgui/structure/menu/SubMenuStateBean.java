/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.menu;


import java.io.Serializable;

import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.server.webgui.structure.group.StructuralGroupMenuStateBean;
import eu.agno3.orchestrator.server.webgui.structure.instance.InstanceMenuStateBean;
import eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuBuilder;
import eu.agno3.orchestrator.server.webgui.structure.service.ServiceMenuStateBean;


/**
 * @author mbechler
 * 
 */
@Named ( "subMenuStateBean" )
@ViewScoped
public class SubMenuStateBean implements Serializable {

    /**
     * 
     */
    private static final String BLANK = "/tpl/menu/blank.xhtml"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -1843522478539120006L;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private StructuralGroupMenuStateBean groupMenu;

    @Inject
    private InstanceMenuStateBean instanceMenu;

    @Inject
    private ServiceMenuStateBean serviceMenu;

    @Inject
    private ActionMenuBuilder amb;

    private MenuModel groupActionModel;
    private MenuModel groupContextModel;

    private MenuModel instanceActionModel;
    private MenuModel instanceContextModel;

    private MenuModel serviceActionModel;
    private MenuModel serviceContextModel;

    private String cachedListenTo;


    public MenuModel getModel () {
        try {
            StructuralObject obj = this.structureContext.getSelectedAnchor();
            AbstractStructuralObjectMenuStateBean mb = getMenuBean(obj);
            if ( mb != null ) {
                return mb.getModel();
            }
            return new DefaultMenuModel();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return new DefaultMenuModel();
        }
    }


    public String getListenTo () {
        if ( this.cachedListenTo != null ) {
            return this.cachedListenTo;
        }
        try {
            StructuralObject obj = this.structureContext.getSelectedAnchor();
            AbstractStructuralObjectMenuStateBean mb = getMenuBean(obj);

            if ( mb != null ) {
                this.cachedListenTo = StringUtils.join(mb.getListenTo(), '|');
            }
            else {
                this.cachedListenTo = StringUtils.EMPTY;
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            this.cachedListenTo = StringUtils.EMPTY;
        }

        return this.cachedListenTo;
    }


    public void notifyRefresh ( ActionEvent ev ) {
        notifyRefresh();
    }


    public void notifyRefresh () {
        String path = null;
        String payload = null;
        try {
            StructuralObject obj = this.structureContext.getSelectedAnchor();
            AbstractStructuralObjectMenuStateBean mb = getMenuBean(obj);
            if ( mb != null ) {
                mb.notifyRefresh(path, payload);
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            this.cachedListenTo = StringUtils.EMPTY;
        }
    }


    /**
     * @param obj
     * @return
     */
    private AbstractStructuralObjectMenuStateBean getMenuBean ( StructuralObject obj ) {
        AbstractStructuralObjectMenuStateBean mb = null;
        if ( obj instanceof GroupStructuralObject ) {
            mb = this.groupMenu;
        }
        else if ( obj instanceof InstanceStructuralObject ) {
            mb = this.instanceMenu;
        }
        else if ( obj instanceof ServiceStructuralObject ) {
            mb = this.serviceMenu;
        }
        return mb;
    }


    public MenuModel getActionModel () {
        try {
            StructuralObject obj = this.structureContext.getSelectedAnchor();

            if ( obj instanceof GroupStructuralObject ) {
                if ( this.groupActionModel == null ) {
                    this.groupActionModel = this.createGroupActionModel(true);
                }
                return this.groupActionModel;
            }
            else if ( obj instanceof InstanceStructuralObject ) {
                if ( this.instanceActionModel == null ) {
                    this.instanceActionModel = this.createInstanceActionModel(true);
                }
                return this.instanceActionModel;
            }
            else if ( obj instanceof ServiceStructuralObject ) {
                if ( this.serviceActionModel == null ) {
                    this.serviceActionModel = this.createServiceActionModel(true);
                }
                return this.serviceActionModel;
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return new DefaultMenuModel();
    }


    public void refreshContextModel () {
        this.groupContextModel = this.createGroupActionModel(false);
        this.instanceContextModel = this.createInstanceActionModel(false);
        this.serviceContextModel = this.createServiceActionModel(false);
        this.groupMenu.triggerRefresh();
        this.instanceMenu.triggerRefresh();
        this.serviceMenu.triggerRefresh();
    }


    public MenuModel getGroupContextModel () {
        if ( this.groupContextModel == null ) {
            this.groupContextModel = this.createGroupActionModel(false);
        }
        return this.groupContextModel;
    }


    public MenuModel getInstanceContextModel () {
        if ( this.instanceContextModel == null ) {
            this.instanceContextModel = this.createInstanceActionModel(false);
        }
        return this.instanceContextModel;
    }


    public MenuModel getServiceContextModel () {
        if ( this.serviceContextModel == null ) {
            this.serviceContextModel = this.createServiceActionModel(false);
        }
        return this.serviceContextModel;
    }


    protected MenuModel createGroupActionModel ( boolean fromContext ) {
        return this.amb.createActionModel(fromContext, StructuralObjectType.GROUP, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    protected MenuModel createInstanceActionModel ( boolean fromContext ) {
        return this.amb.createActionModel(fromContext, StructuralObjectType.INSTANCE, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    protected MenuModel createServiceActionModel ( boolean fromContext ) {
        return this.amb.createActionModel(fromContext, StructuralObjectType.SERVICE, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    public String getCustomPanel () {
        try {
            StructuralObject obj = this.structureContext.getSelectedAnchor();
            if ( obj instanceof InstanceStructuralObject ) {
                return "/tpl/menu/instance_panel.xhtml"; //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return BLANK;
    }


    public String getCustomMenuActions () {
        try {
            StructuralObject obj = this.structureContext.getSelectedAnchor();
            if ( obj instanceof InstanceStructuralObject ) {
                return "/tpl/menu/instance_actions.xhtml"; //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return BLANK;
    }


    public boolean haveActionMenu () {
        return true;
    }
}
