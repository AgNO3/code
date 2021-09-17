/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "app_fs_adm_userAuthInfoBean" )
public class UserAuthInfoBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8950941880617844236L;

    private static final Logger log = Logger.getLogger(UserAuthInfoBean.class);

    private Map<UserPrincipal, UserInfo> userInfoCache = new HashMap<>();

    @Inject
    private FileshareAdminServiceProvider fsp;


    /**
     * 
     * @param u
     * @return whether the user has a local password
     */
    public boolean hasLocalPassword ( User u ) {
        if ( u == null || u.getPrincipal() == null ) {
            return false;
        }
        return "LOCAL".equals(u.getPrincipal().getRealmName()); //$NON-NLS-1$
    }


    /**
     * @param u
     * @return whether the user has login user info
     */
    public boolean hasUserInfo ( User u ) {
        return this.hasLocalPassword(u);
    }


    /**
     * @param u
     * @return whether a user can be enabled
     */
    public boolean canEnable ( User u ) {
        if ( !hasLocalPassword(u) ) {
            return false;
        }

        UserInfo info = getUserInfo(u);

        if ( info == null ) {
            return false;
        }

        return info.getDisabled();
    }


    /**
     * 
     * @param u
     * @return whether a user can be disabled
     */
    public boolean canDisable ( User u ) {
        if ( !hasLocalPassword(u) ) {
            return false;
        }

        UserInfo info = getUserInfo(u);

        if ( info == null ) {
            return false;
        }

        return !info.getDisabled();
    }


    /**
     * @param u
     * @return the local authentication user for the given user
     */
    public UserInfo getUserInfo ( User u ) {
        if ( !this.hasUserInfo(u) ) {
            return null;
        }

        UserInfo cached = this.userInfoCache.get(u.getPrincipal());
        if ( cached != null ) {
            return cached;
        }

        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Loading user info for " + u); //$NON-NLS-1$
            }
            cached = this.fsp.getUserService().getLocalUserInfo(u.getId());
            this.userInfoCache.put(u.getPrincipal(), cached);
            return cached;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            log.debug("Failed to get local user info", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param principal
     */
    public void refreshUserInfo ( UserPrincipal principal ) {
        this.userInfoCache.remove(principal);
    }

}
