/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator.security;


import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.server.security.api.services.LocalUserService;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.principal.UserInfo;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "orch_userController" )
public class UserController {

    @Inject
    private LocalUsersTableBean tableBean;

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;


    /**
     * 
     * @return the password policy checker
     */
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.csp.getPasswordPolicy();
    }


    /**
     * @param ctx
     * @return outcome
     */
    public String createUser ( UserCreateContext ctx ) {
        UserInfo user = createUserInternal(ctx);
        if ( user != null ) {
            return DialogContext.closeDialog(user.getUserPrincipal());
        }
        return null;
    }


    /**
     * 
     * @param ctx
     * @return null
     */
    public String createMoreUsers ( UserCreateContext ctx ) {
        UserInfo user = createUserInternal(ctx);
        if ( user != null ) {
            ctx.reset();
            return String.format(
                "/security/addUserDialog.xhtml?faces-redirect=true&service=%s&returnTo=%s", //$NON-NLS-1$
                this.structureContext.getSelectedObjectId(),
                DialogContext.getCurrentParent());
        }
        return null;
    }


    /**
     * @param ctx
     * @param user
     */
    private UserInfo createUserInternal ( UserCreateContext ctx ) {
        try {
            UserInfo user = this.ssp.getService(LocalUserService.class).addUser(
                this.structureContext.getSelectedService(),
                ctx.getUserName(),
                ctx.getPassword(),
                new HashSet<>(ctx.getRoles()),
                ctx.getDisabled(),
                ctx.getForcePasswordChange());
            this.tableBean.refresh();
            return user;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }


    /**
     * @param us
     * @param pwChange
     * @return outcome
     */
    public String changeUserPassword ( UserSelectionBean us, UserPasswordChangeBean pwChange ) {
        if ( us == null || us.getSingleSelection() == null || pwChange == null ) {
            return null;
        }

        try {
            this.ssp.getService(LocalUserService.class)
                    .changePassword(this.structureContext.getSelectedService(), us.getSingleSelection(), pwChange.getNewPassword());
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }

}
