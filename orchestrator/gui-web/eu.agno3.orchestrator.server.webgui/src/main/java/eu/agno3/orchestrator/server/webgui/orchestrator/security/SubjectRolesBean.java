/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator.security;


import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.SelectableDataModel;

import eu.agno3.orchestrator.server.security.api.services.RoleMappingService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@Named ( "orch_subjectRolesBean" )
@ViewScoped
public class SubjectRolesBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2857217077071732401L;

    private RoleDataModel model;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;


    /**
     * @return the available role model
     */
    public SelectableDataModel<String> getModel () {
        if ( this.model == null ) {
            this.model = makeModel();
        }
        return this.model;
    }


    /**
     * @return
     */
    private RoleDataModel makeModel () {
        try {
            List<String> roles = new ArrayList<>(
                this.ssp.getService(RoleMappingService.class).getAvailableRoles(this.structureContext.getSelectedService()).getRoles());
            Collections.sort(roles, Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale()));
            return new RoleDataModel(roles);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return new RoleDataModel();
        }

    }

}
