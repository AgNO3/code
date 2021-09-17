/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.menu;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.menu.AbstractMenuStateBean;
import eu.agno3.orchestrator.server.webgui.menu.MenuContributionManager;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractStructuralObjectMenuStateBean extends AbstractMenuStateBean {

    /**
     * 
     */
    private static final long serialVersionUID = 5685667501347087404L;

    @Inject
    private StructureViewContextBean viewContext;

    @Inject
    private MenuContributionManager mcm;

    private StructuralObject contextObject;

    private MenuModel cachedModel;

    private Set<String> listenTo = new HashSet<>();

    private boolean needRefresh;


    /**
     * 
     */
    public AbstractStructuralObjectMenuStateBean () {
        super();
    }


    protected MenuContributionManager getMenuContributionManager () {
        return this.mcm;
    }


    /**
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     */
    public void refresh () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        this.cachedModel = super.createModel();
    }


    public void triggerRefresh () {
        this.needRefresh = true;
    }


    /**
     * @return the listenTo
     */
    public Set<String> getListenTo () {
        try {
            getModel();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return this.listenTo;
    }


    protected void addListenTo ( Collection<String> listen ) {
        this.listenTo.addAll(listen);
    }


    /**
     * 
     * @param context
     * @return the menu model
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public MenuModel getModel ( StructuralObject context ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.contextObject == null || !this.contextObject.equals(context) || this.needsRefresh(context) ) {
            this.needRefresh = false;
            this.cachedModel = super.createModel();
            if ( this.contextObject == null || !this.contextObject.equals(context) ) {
                this.contextChanged(context);
            }
            this.contextObject = context;
        }
        return this.cachedModel;
    }


    /**
     * @param path
     * @param payload
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     */
    public void notifyRefresh ( String path, String payload ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.mcm.notifyRefresh(path, payload, this.viewContext.getSelectedObject(), this.viewContext.getSelectedAnchor()) ) {
            this.cachedModel = super.createModel();
        }
    }


    /**
     * @param context
     * 
     */
    protected void contextChanged ( StructuralObject context ) {}


    /**
     * @param context
     * @return
     */
    protected boolean needsRefresh ( StructuralObject context ) {
        return this.needRefresh;
    }


    protected void addObjectMenuEntries ( DefaultSubMenu menu ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        this.listenTo.addAll(this.mcm.addMenuContributions(menu, this.viewContext.getSelectedAnchor(), null));
    }


    /**
     * @return the viewContext
     */
    protected StructureViewContextBean getViewContext () {
        return this.viewContext;
    }

}