/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
@Named ( "attachedObjectTableBean" )
@ViewScoped
public class AttachedObjectTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4430859461308151720L;

    private static final Logger log = Logger.getLogger(AttachedObjectTableBean.class);

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean viewContext;

    private List<ConfigurationObject> model;


    public List<ConfigurationObject> getModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.model == null ) {
            this.model = this.createModel();
        }
        return this.model;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private List<ConfigurationObject> createModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        StructuralObject selectedObject = this.viewContext.getSelectedObject();
        if ( log.isDebugEnabled() ) {
            log.debug("Fetching attached objects for " + selectedObject); //$NON-NLS-1$
        }
        Set<ConfigurationObject> objs = this.ssp.getService(StructuralObjectService.class).fetchAttachedObjects(selectedObject);
        if ( objs == null ) {
            return Collections.EMPTY_LIST;
        }
        return new ArrayList<>(objs);

    }


    public String getSubtitle () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return GuiMessages.format("structure.objects.title", this.viewContext.getSelectedDisplayName()); //$NON-NLS-1$
    }
}
