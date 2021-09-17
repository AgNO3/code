/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminMessages;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.fileshare.webgui.admin.group.GroupComparator;
import eu.agno3.fileshare.webgui.admin.group.GroupSelectionBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.password.PasswordPolicyException;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "app_fs_adm_userController" )
public class UserController {

    private static final Logger log = Logger.getLogger(UserController.class);

    @Inject
    private UsersTableBean tableBean;

    @Inject
    private UserAuthInfoBean authInfoBean;

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;


    /**
     * 
     * @return the password policy checker
     */
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.fsp.getPasswordPolicy();
    }


    /**
     * 
     * @param u
     * @return null
     */
    public String enableUser ( User u ) {
        if ( !this.authInfoBean.hasLocalPassword(u) ) {
            return null;
        }
        try {
            this.fsp.getUserService().enableLocalUser(u.getId());
            this.authInfoBean.refreshUserInfo(u.getPrincipal());
            this.tableBean.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * 
     * @param u
     * @return null
     */
    public String disableUser ( User u ) {
        if ( !this.authInfoBean.hasLocalPassword(u) ) {
            return null;
        }
        try {
            this.fsp.getUserService().disableLocalUser(u.getId());
            this.authInfoBean.refreshUserInfo(u.getPrincipal());
            this.tableBean.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * 
     * @param us
     * @param data
     * @return null
     */
    public String updateExpirationDate ( UserSelectionBean us, UserExpirationEditorBean data ) {

        User u = us.getSingleSelection();
        if ( u == null ) {
            return null;
        }

        DateTime expiration = null;
        if ( data != null ) {
            expiration = data.getExpires();
            data.reset(null);
        }

        try {
            this.fsp.getUserService().setUserExpiry(u.getId(), expiration);
            us.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param ctx
     * @return outcome
     */
    public String createUser ( UserCreateContext ctx ) {
        User user = createUserInternal(ctx);
        if ( user != null ) {
            return DialogContext.closeDialog(user);
        }
        return null;
    }


    /**
     * 
     * @param ctx
     * @return null
     */
    public String createMoreUsers ( UserCreateContext ctx ) {
        User user = createUserInternal(ctx);
        if ( user != null ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    FileshareAdminMessages.format(FileshareAdminMessages.USER_CREATED, ctx.getUserName()),
                    StringUtils.EMPTY));
            ctx.reset();
            return this.fsp.wrapURL(
                FacesContext.getCurrentInstance().getViewRoot().getViewId() + "?faces-redirect=true&returnTo=" + DialogContext.getCurrentParent()); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param ctx
     * @param user
     */
    private User createUserInternal ( UserCreateContext ctx ) {
        try {
            User user = this.fsp.getUserService().createLocalUser(ctx.getData());
            this.tableBean.refresh();
            return user;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param us
     * @return outcome
     */
    public String deleteUsers ( UserSelectionBean us ) {
        if ( us == null ) {
            return null;
        }

        try {
            this.fsp.getUserService().deleteUsers(us.getMultiSelectionIds());
            us.setMultiSelection((List<@Nullable User>) null);
            this.tableBean.refresh();
            return DialogContext.closeDialog(null);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * 
     * @param us
     * @param gs
     * @return outcome
     */
    public String addToGroup ( UserSelectionBean us, GroupSelectionBean gs ) {
        if ( us == null || gs == null ) {
            return null;
        }

        User u = us.getSingleSelection();
        Group g = gs.getSingleSelection();

        if ( u == null || g == null ) {
            return null;
        }

        try {
            this.fsp.getGroupService().addToGroup(u.getId(), g.getId());
            us.refreshSelection();
            gs.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * @param us
     * @param groupId
     * @return outcome
     */
    public String removeFromGroup ( UserSelectionBean us, UUID groupId ) {
        if ( us == null ) {
            return null;
        }
        User u = us.getSingleSelection();

        if ( u == null || groupId == null ) {
            return null;
        }

        try {
            this.fsp.getGroupService().removeFromGroup(u.getId(), groupId);
            us.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * @param u
     * @return the users group memberships
     */
    public List<Group> getMemberships ( User u ) {
        List<Group> groups = new ArrayList<>(u.getMemberships());
        Collections.sort(groups, new GroupComparator());
        return groups;
    }


    /**
     * @param us
     * @param groupIds
     * @return outcome
     */
    public String addToGroups ( UserSelectionBean us, Set<UUID> groupIds ) {
        if ( us == null || groupIds == null || groupIds.isEmpty() ) {
            return null;
        }
        User u = us.getSingleSelection();

        if ( u == null ) {
            return null;
        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Add %s to groups %s", u, groupIds)); //$NON-NLS-1$
        }

        try {
            this.fsp.getGroupService().addToGroups(u.getId(), groupIds);
            us.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * @param us
     * @param groupIds
     * @return outcome
     */
    public String removeFromGroups ( UserSelectionBean us, Set<UUID> groupIds ) {
        if ( us == null || groupIds == null || groupIds.isEmpty() ) {
            return null;
        }
        User u = us.getSingleSelection();

        if ( u == null ) {
            return null;
        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Remove %s from groups %s", u, groupIds)); //$NON-NLS-1$
        }

        try {
            this.fsp.getGroupService().removeFromGroups(u.getId(), groupIds);
            us.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * @param us
     * @param pwChange
     * @return outcome
     */
    public String changeUserPassword ( UserSelectionBean us, UserPasswordChangeBean pwChange ) {
        if ( us == null || pwChange == null ) {
            return null;
        }
        User u = us.getSingleSelection();

        if ( u == null ) {
            return null;
        }

        try {
            this.fsp.getUserService().changePassword(u.getId(), pwChange.getNewPassword());
            return DialogContext.closeDialog(true);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException |
            PasswordPolicyException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * 
     * @param us
     * @return null
     */
    public String enableUserRoot ( UserSelectionBean us ) {
        if ( us == null ) {
            return null;
        }
        User u = us.getSingleSelection();
        if ( u == null ) {
            return null;
        }

        try {
            this.fsp.getUserService().enableUserRoot(u.getId());
            us.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * 
     * @param us
     * @return null
     */
    public String disableUserRoot ( UserSelectionBean us ) {
        if ( us == null ) {
            return null;
        }
        User u = us.getSingleSelection();
        if ( u == null ) {
            return null;
        }

        try {
            this.fsp.getUserService().disableUserRoot(u.getId());
            us.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param pwChange
     * @return outcome
     */
    public String changeCurrentUserPassword ( UserPasswordChangeBean pwChange ) {
        try {
            this.fsp.getUserService().changeCurrentUserPassword(pwChange.getOldPassword(), pwChange.getNewPassword());
            return DialogContext.closeDialog(true);
        }
        catch (
            PasswordPolicyException |
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }

        return null;
    }

}
