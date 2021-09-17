/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.db.impl;


import java.util.Collections;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.db.BaseLocalUserService;
import eu.agno3.runtime.security.db.BasePermissionService;
import eu.agno3.runtime.security.db.BaseRoleMappingService;
import eu.agno3.runtime.security.db.BaseSecurityInitializer;
import eu.agno3.runtime.security.db.BaseUser;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 * @param <TUser>
 *
 */
public abstract class AbstractLocalSecurityInitializer <TUser extends BaseUser> implements BaseSecurityInitializer {

    /**
     * 
     */

    private static final Logger log = Logger.getLogger(AbstractLocalSecurityInitializer.class);


    protected abstract EntityTransactionContext createTransactionContext () throws EntityTransactionException;

    protected static final String STATIC_USER = "staticUser"; //$NON-NLS-1$
    protected static final String STATIC_USER_PASSWORD = "staticUserPassword"; //$NON-NLS-1$
    protected static final String STATIC_USER_PASSWORD_HASH = "staticUserPasswordHash"; //$NON-NLS-1$
    protected static final Object STATIC_USER_ROLES = "staticUserRoles"; //$NON-NLS-1$

    private static final String WILDCARD_PERMISSION = "*"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String ADMIN_ROLE = "ADMIN"; //$NON-NLS-1$

    private BaseLocalUserService<TUser> localUserService;
    private BaseRoleMappingService roleMappingService;
    private BasePermissionService permissionService;


    /**
     * 
     */
    public AbstractLocalSecurityInitializer () {
        super();
    }


    @Reference
    protected synchronized void setLocalUserService ( BaseLocalUserService<TUser> lus ) {
        this.localUserService = lus;
    }


    protected synchronized void unsetLocalUserService ( BaseLocalUserService<TUser> lus ) {
        if ( this.localUserService == lus ) {
            this.localUserService = null;
        }
    }


    @Reference
    protected synchronized void setRoleMappingService ( BaseRoleMappingService rms ) {
        this.roleMappingService = rms;
    }


    protected synchronized void unsetRoleMappingService ( BaseRoleMappingService rms ) {
        if ( this.roleMappingService == rms ) {
            this.roleMappingService = null;
        }
    }


    @Reference
    protected synchronized void setPermissionService ( BasePermissionService ps ) {
        this.permissionService = ps;
    }


    protected synchronized void unsetPermissionService ( BasePermissionService ps ) {
        if ( this.permissionService == ps ) {
            this.permissionService = null;
        }
    }


    /**
     * @param initUserName
     * @param initUserPassword
     * @param createStaticUser
     */
    protected void initialize ( String initUserName, String initUserPassword, Set<String> roles, boolean resetPasswordIfExists ) {
        boolean createStaticUser = StringUtils.isNotBlank(initUserName) && StringUtils.isNotBlank(initUserPassword);
        SCryptResult r = null;
        if ( createStaticUser ) {
            log.debug("Generating password hash"); //$NON-NLS-1$
            try {
                r = generatePasswordHash(initUserPassword);
            }
            catch ( SecurityManagementException e ) {
                log.error("Failed to generated password hash", e); //$NON-NLS-1$
                return;
            }
            log.debug("Generated password hash"); //$NON-NLS-1$
        }
        initialize(initUserName, r, roles, resetPasswordIfExists);
    }


    protected void initialize ( String initUserName, SCryptResult initUserHash, Set<String> roles, boolean resetPasswordIfExists ) {
        boolean createStaticUser = StringUtils.isNotBlank(initUserName) && initUserHash != null;
        try ( EntityTransactionContext tx = createTransactionContext() ) {
            if ( createStaticUser ) {
                ensureUserExistsInternal(tx, initUserName, initUserHash, roles, resetPasswordIfExists);
            }
            ensureAdminRolePermissions(tx.getEntityManager());
            tx.commit();
            log.debug("Initialized authentication database"); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.error("Failed to initialize authentication database", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.BaseSecurityInitializer#generatePasswordHash(java.lang.String)
     */
    @Override
    public SCryptResult generatePasswordHash ( String password ) throws SecurityManagementException {
        return this.localUserService.generatePasswordHash(password, false);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.BaseSecurityInitializer#ensureAdminPermissions()
     */
    @Override
    public void ensureAdminPermissions () throws SecurityManagementException {
        try ( EntityTransactionContext tx = createTransactionContext() ) {
            ensureAdminRolePermissions(tx.getEntityManager());
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to ensure permissions", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.runtime.security.db.BaseSecurityInitializer#ensureUserExists(java.lang.String,
     *      eu.agno3.runtime.crypto.scrypt.SCryptResult, java.util.Set)
     */
    @Override
    public UserPrincipal ensureUserExists ( String username, SCryptResult r, Set<String> roles ) throws SecurityManagementException {
        try ( EntityTransactionContext tx = createTransactionContext() ) {
            UserPrincipal up = ensureUserExistsInternal(tx, username, r, roles, true);
            tx.commit();
            return up;
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to ensure user exists", e); //$NON-NLS-1$
        }

    }


    private void ensureAdminRolePermissions ( EntityManager em ) throws SecurityManagementException {
        Set<String> rolePermissions = this.permissionService.getRolePermissions(em, ADMIN_ROLE);
        if ( rolePermissions.isEmpty() || !rolePermissions.contains(WILDCARD_PERMISSION) ) { // $NON-NLS-1$
            log.debug("Setting up admin permissions"); //$NON-NLS-1$
            this.permissionService.setRolePermissions(em, ADMIN_ROLE, Collections.singleton(WILDCARD_PERMISSION)); // $NON-NLS-1$
        }
    }


    protected UserPrincipal ensureUserExistsInternal ( EntityTransactionContext tx, String username, SCryptResult pwHash, Set<String> roles,
            boolean resetPasswordIfExists ) throws SecurityManagementException {
        UserPrincipal u = new UserPrincipal();
        u.setUserName(username);
        u.setRealmName("LOCAL"); //$NON-NLS-1$

        UserInfo ui;
        if ( !this.localUserService.userExists(tx, u) ) {
            log.debug("Adding initial user"); //$NON-NLS-1$
            ui = this.localUserService.addUser(tx, username, pwHash);
        }
        else {
            log.debug("Static user exists, making sure enabled"); //$NON-NLS-1$
            ui = this.localUserService.fetchUser(tx, u);
            if ( resetPasswordIfExists ) {
                this.localUserService.changePassword(tx, ui.getUserPrincipal(), pwHash);
            }
            this.localUserService.enableUser(tx, ui.getUserPrincipal());
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Setting roles " + roles); //$NON-NLS-1$
        }
        this.roleMappingService.setMappedRoles(tx, ui.getUserPrincipal(), roles);
        return ui.getUserPrincipal();
    }

}