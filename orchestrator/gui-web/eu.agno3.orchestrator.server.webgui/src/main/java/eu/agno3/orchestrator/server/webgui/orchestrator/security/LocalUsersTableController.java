/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator.security;


import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.server.security.api.Roles;
import eu.agno3.orchestrator.server.security.api.services.LocalUserService;
import eu.agno3.orchestrator.server.security.api.services.RoleMappingService;
import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.webgui.auth.SessionInfoBean;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "localUsersTableController" )
public class LocalUsersTableController {

    @Inject
    private LocalUsersTableBean tableBean;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private SessionInfoBean sessionInfo;


    public boolean isCurrentUser ( UserPrincipal princ ) {
        UserPrincipal loggedIn;
        try {
            loggedIn = this.sessionInfo.getSessionInfo().getUserPrincipal();
        }
        catch ( SessionException e ) {
            ExceptionHandler.handle(e);
            return false;
        }

        return princ != null && princ.equals(loggedIn);
    }


    public boolean isLocalUser ( UserPrincipal princ ) {
        return princ != null && "LOCAL".equals(princ.getRealmName()); //$NON-NLS-1$
    }


    public String enableUser ( UserPrincipal princ ) {
        if ( !isLocalUser(princ) ) {
            return null;
        }
        try {
            this.ssp.getService(LocalUserService.class).enableUser(this.structureContext.getSelectedService(), princ);
            this.tableBean.refresh();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }


    public String disableUser ( UserPrincipal princ ) {
        if ( !isLocalUser(princ) ) {
            return null;
        }
        try {
            this.ssp.getService(LocalUserService.class).disableUser(this.structureContext.getSelectedService(), princ);
            this.tableBean.refresh();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }


    public String deleteUsers ( UserSelectionBean us ) {
        try {
            for ( UserPrincipal up : us.getMultiSelection() ) {
                if ( isCurrentUser(up) ) {
                    continue;
                }
                this.ssp.getService(LocalUserService.class).removeUser(this.structureContext.getSelectedService(), up);
            }
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        this.tableBean.refresh();
        return null;
    }


    public Set<String> getRoles ( UserPrincipal princ ) {
        try {
            Roles mappedRoles = this.ssp.getService(RoleMappingService.class).getMappedRoles(this.structureContext.getSelectedService(), princ);
            return mappedRoles.getRoles();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return Collections.EMPTY_SET;
        }
    }
}
