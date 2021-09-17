/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
public class MenuItemAdapterFactoryBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8733468450625377874L;
    private static final Logger log = Logger.getLogger(MenuItemAdapterFactoryBean.class);

    @Inject
    private ServerServiceProvider ssp;


    /**
     * @param parent
     * @param obj
     * @return A TreeNode adapter for the given object
     */
    public <T extends StructuralObject> StructuralObjectAdapter<T> getMenuItemAdapter ( TreeNode parent, T obj ) {
        return new StructuralObjectAdapter<>(obj, parent, this);
    }


    /**
     * @param parent
     * @param c
     * @return cluster children
     */
    public List<StructuralObjectTreeNode> getChildren ( TreeNode parent, StructuralObject c ) {
        List<StructuralObjectTreeNode> adapters = new ArrayList<>();
        List<StructuralObject> objs = new ArrayList<>();

        if ( c instanceof InstanceStructuralObject ) {
            return Collections.EMPTY_LIST;
        }

        try {
            objs.addAll(this.ssp.getService(StructuralObjectService.class).fetchChildren(c));
        }
        catch ( Exception e ) {
            log.warn("Failed to fetch structural children:", e); //$NON-NLS-1$
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get(GuiMessages.MENU_FAILED_TO_LOAD_CHILDREN), null));
            return Collections.EMPTY_LIST;
        }
        Collections.sort(objs, new StructuralObjectComparator());
        for ( StructuralObject o : objs ) {
            adapters.add(this.getMenuItemAdapter(parent, o));
        }
        return adapters;
    }
}
