/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.subject;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.SubjectType;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Named ( "currentUserBean" )
@SessionScoped
public class CurrentUserBean implements Serializable {

    private static final Logger log = Logger.getLogger(CurrentUserBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 681179161302544432L;

    private boolean currentUserLoaded;
    private User currentUser;

    private Map<UUID, Boolean> membershipCache = new HashMap<>();
    private Map<String, Boolean> permissionCache = new HashMap<>();

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @return whether a user is current authenticated
     */
    public boolean isAuthenticated () {
        return SecurityUtils.getSubject().isAuthenticated() && SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class) != null;
    }


    /**
     * @return the currentUser
     */
    public User getCurrentUser () {
        if ( SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class) == null ) {
            return null;
        }

        if ( !this.currentUserLoaded ) {
            this.currentUserLoaded = true;
            try {
                this.currentUser = this.fsp.getUserService().getCurrentUser();
            }
            catch (
                AuthenticationException |
                UndeclaredThrowableException |
                UserNotFoundException e ) {
                ExceptionHandler.handleException(e);
            }
        }
        return this.currentUser;
    }


    /**
     * @return whether current user has a local password
     */
    public boolean hasLocalPassword () {
        User u = getCurrentUser();
        if ( u == null || u.getPrincipal() == null ) {
            return false;
        }
        return "LOCAL".equals(u.getPrincipal().getRealmName()); //$NON-NLS-1$
    }


    /**
     * 
     * @return whether current user is a local user
     */
    public boolean isLocalUser () {
        User u = getCurrentUser();
        if ( u == null || u.getPrincipal() == null ) {
            return false;
        }

        return u.getType() == SubjectType.LOCAL;
    }


    /**
     * @param group
     * @return whether the current user is member of the given group
     */
    public boolean isMemberOf ( Group group ) {

        if ( SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class) == null ) {
            return false;
        }

        Object cached = this.membershipCache.get(group.getId());
        if ( cached != null ) {
            return (boolean) cached;
        }

        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Checking membership in " + group.getName()); //$NON-NLS-1$
            }
            if ( this.fsp.getUserService().isCurrentUserMember(group.getId()) ) {
                this.membershipCache.put(group.getId(), true);
                return true;
            }
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        this.membershipCache.put(group.getId(), false);
        return false;
    }


    /**
     * 
     * @param permission
     * @return whether the current user has the given permission
     */
    public boolean hasPermission ( String permission ) {
        Object cached = this.permissionCache.get(permission);
        if ( cached != null ) {
            return (boolean) cached;
        }
        boolean res = SecurityUtils.getSubject().isPermitted(permission);
        this.permissionCache.put(permission, res);
        return res;
    }


    /**
     * 
     * @param permissions
     * @return whether the current user has any of the given permissions
     */
    private boolean hasAnyPermissionInternal ( String... permissions ) {
        boolean needCheck = false;
        for ( String perm : permissions ) {
            Object cached = this.permissionCache.get(perm);
            if ( cached != null ) {
                if ( (boolean) cached ) {
                    return true;
                }
            }
            else {
                needCheck = true;
            }
        }

        if ( needCheck ) {
            for ( boolean b : SecurityUtils.getSubject().isPermitted(permissions) ) {
                if ( b ) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * 
     * @param a
     * @param b
     * @return whether the current user has any of the given permissions
     */
    public boolean hasAnyPermission ( String a, String b ) {
        return hasAnyPermissionInternal(a, b);
    }


    /**
     * 
     * @param a
     * @param b
     * @param c
     * @return whether the current user has any of the given permissions
     */
    public boolean hasAnyPermission ( String a, String b, String c ) {
        return hasAnyPermissionInternal(a, b, c);
    }


    /**
     * 
     * @param a
     * @param b
     * @param c
     * @param d
     * @return whether the current user has any of the given permissions
     */
    public boolean hasAnyPermission ( String a, String b, String c, String d ) {
        return hasAnyPermissionInternal(a, b, c, d);
    }


    /**
     * 
     * @param permissions
     * @return whether the current user has all of the given permissions
     */
    private static boolean hasAllPermissionsInternal ( String... permissions ) {
        return SecurityUtils.getSubject().isPermittedAll(permissions);
    }


    /**
     * 
     * @param a
     * @param b
     * @return whether the current user has all of the given permissions
     */
    public boolean hasAllPermissions ( String a, String b ) {
        return hasAllPermissionsInternal(a, b);
    }


    /**
     * 
     * @param a
     * @param b
     * @param c
     * @return whether the current user has all of the given permissions
     */
    public boolean hasAllPermissions ( String a, String b, String c ) {
        return hasAllPermissionsInternal(a, b, c);
    }


    /**
     * 
     * @param a
     * @param b
     * @param c
     * @param d
     * @return whether the current user has all of the given permissions
     */
    public boolean hasAllPermissions ( String a, String b, String c, String d ) {
        return hasAllPermissionsInternal(a, b, c, d);
    }

}
