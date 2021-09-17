/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.enforcements;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.CellEditEvent;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.EnforcementService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
@Named ( "enforcementsController" )
@ApplicationScoped
public class EnforcementsController {

    @Inject
    private StructureViewContextBean viewContext;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private EnforcementAddContextBean addContext;

    @Inject
    private EnforcementTableBean enforcementTable;


    public String addDefault () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return "/config/default/add.xhtml?faces-redirect=true&object=" + this.viewContext.getSelectedObject().getId(); //$NON-NLS-1$
    }


    public String doAdd () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        this.ssp.getService(EnforcementService.class).setEnforcement(this.viewContext.getSelectedObject(), this.addContext.getObject());
        this.enforcementTable.refresh();
        return null;
    }


    public String doRemove ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        this.ssp.getService(EnforcementService.class).unsetEnforcement(this.viewContext.getSelectedObject(), obj);
        this.enforcementTable.refresh();
        return null;
    }


    public void saveTemplateChange ( CellEditEvent ev ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject old = (ConfigurationObject) ev.getOldValue();
        ConfigurationObject toSet = (ConfigurationObject) ev.getNewValue();

        if ( old != null && !old.equals(toSet) ) {
            this.ssp.getService(EnforcementService.class).setEnforcement(this.viewContext.getSelectedObject(), toSet);
            this.enforcementTable.refresh();
        }
    }
}
