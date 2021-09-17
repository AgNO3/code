/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@Named ( "resourceLibraryTableBean" )
@ViewScoped
public class ResourceLibraryTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 238796953441923108L;

    private List<ResourceLibrary> model;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean viewContext;


    public List<ResourceLibrary> getModel () {
        if ( this.model == null ) {
            this.model = makeModel();
        }
        return this.model;
    }


    /**
     * @return
     */
    private List<ResourceLibrary> makeModel () {

        try {
            return this.ssp.getService(ResourceLibraryService.class).getResourceLibraries(this.viewContext.getSelectedObject());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return Collections.EMPTY_LIST;
        }

    }


    public void refresh () {
        this.model = makeModel();
    }


    public String getSubtitle () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return GuiMessages.format("structure.resourceLibraries.title", this.viewContext.getSelectedDisplayName()); //$NON-NLS-1$
    }
}
