/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator.security;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.security.api.services.LocalUserService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.security.principal.UserInfo;


/**
 * @author mbechler
 * 
 */
@Named ( "localUsersTableBean" )
@ViewScoped
public class LocalUsersTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4430859461308151720L;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;

    private List<UserInfo> model;


    public List<UserInfo> getModel () {
        if ( this.model == null ) {
            this.model = this.createModel();
        }
        return this.model;
    }


    public void refresh () {
        this.model = null;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private List<UserInfo> createModel () {
        try {
            Set<UserInfo> objs = this.ssp.getService(LocalUserService.class).getUsers(this.structureContext.getSelectedService());
            if ( objs == null ) {
                return Collections.EMPTY_LIST;
            }
            List<UserInfo> res = new ArrayList<>(objs);
            Collections.sort(res, new UserInfoComparator());
            return res;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return Collections.EMPTY_LIST;
        }
    }

}
