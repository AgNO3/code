/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import java.util.HashSet;
import java.util.Set;

import javax.jws.WebService;

import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.server.security.LocalUserServerService;
import eu.agno3.orchestrator.server.security.RoleMappingServerService;
import eu.agno3.orchestrator.server.security.api.services.LocalUserService;
import eu.agno3.orchestrator.server.security.api.services.LocalUserServiceDescriptor;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.db.BaseUser;
import eu.agno3.runtime.security.db.impl.AbstractLocalUserService;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    LocalUserServerService.class, LocalUserService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.server.security.api.services.LocalUserService",
    targetNamespace = LocalUserServiceDescriptor.NAMESPACE,
    serviceName = "localUserService" )
@WebServiceAddress ( "/security/localUsers" )
public class LocalUserServiceImpl extends AbstractLocalUserService<BaseUser> implements LocalUserServerService, LocalUserService {

    private EntityTransactionService authEts;
    private UserMapper userMapper;
    private PasswordPolicyChecker passwordPolicyChecker;
    private RoleMappingServerService roleMapping;


    @Reference
    protected synchronized void setPasswordPolicyChecker ( PasswordPolicyChecker ppc ) {
        this.passwordPolicyChecker = ppc;
    }


    protected synchronized void unsetPasswordPolicyChecker ( PasswordPolicyChecker ppc ) {
        if ( this.passwordPolicyChecker == ppc ) {
            this.passwordPolicyChecker = null;
        }
    }


    @Reference ( target = "(persistenceUnit=auth)" )
    protected synchronized void bindEntityTransactionService ( EntityTransactionService ets ) {
        this.authEts = ets;
    }


    protected synchronized void unbindEntityTransactionService ( EntityTransactionService ets ) {
        if ( this.authEts == ets ) {
            this.authEts = null;
        }
    }


    @Reference
    protected synchronized void setRoleMappingService ( RoleMappingServerService rms ) {
        this.roleMapping = rms;
    }


    protected synchronized void unsetRoleMappingService ( RoleMappingServerService rms ) {
        if ( this.roleMapping == rms ) {
            this.roleMapping = null;
        }
    }


    @Reference
    protected synchronized void setUserMapper ( UserMapper um ) {
        this.userMapper = um;
    }


    protected synchronized void unsetUserMapper ( UserMapper um ) {
        if ( this.userMapper == um ) {
            this.userMapper = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.impl.AbstractLocalUserService#getPolicyChecker()
     */
    @Override
    protected PasswordPolicyChecker getPolicyChecker () {
        return this.passwordPolicyChecker;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.impl.AbstractLocalUserService#getUserMapper()
     */
    @Override
    protected UserMapper getUserMapper () {
        return this.userMapper;
    }


    @Override
    protected Class<BaseUser> getUserClass () {
        return BaseUser.class;
    }


    /**
     * @return
     */
    @Override
    protected BaseUser createUserEntity () {
        return new BaseUser();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.LocalUserService#getUsers(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    @RequirePermissions ( "security:user:list" )
    public Set<UserInfo> getUsers ( ServiceStructuralObject service ) throws SecurityManagementException {
        try ( EntityTransactionContext tx = this.authEts.startReadOnly() ) {
            Set<UserInfo> res = new HashSet<>();
            res.addAll(getUsers(tx));
            return res;
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to fetch users", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.LocalUserService#addUser(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.lang.String, java.lang.String, java.util.Set, boolean, boolean)
     */
    @Override
    @RequirePermissions ( "security:user:add" )
    public UserInfo addUser ( ServiceStructuralObject service, String username, String password, Set<String> roles, boolean disabled,
            boolean forcePasswordChange ) throws SecurityManagementException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            SCryptResult generatePasswordHash;
            try {
                generatePasswordHash = this.generatePasswordHash(password, true);
            }
            catch ( PasswordPolicyException e ) {
                throw new SecurityManagementException("Password is too weak"); //$NON-NLS-1$
            }

            BaseUser ui = addUser(tx, username, generatePasswordHash);
            ui.setDisabled(disabled);
            if ( forcePasswordChange ) {
                ui.setPwExpiry(DateTime.now());
            }
            tx.getEntityManager().persist(ui);
            if ( roles != null ) {
                this.roleMapping.setMappedRoles(tx, ui.getUserPrincipal(), roles);
            }
            tx.getEntityManager().flush();
            tx.commit();
            return ui;
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to add user", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.LocalUserService#changePassword(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.runtime.security.principal.UserPrincipal, java.lang.String)
     */
    @Override
    @RequirePermissions ( "security:user:changePassword" )
    public void changePassword ( ServiceStructuralObject service, UserPrincipal user, String password ) throws SecurityManagementException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            changePassword(tx, user, generatePasswordHash(password, true));
            tx.commit();
        }
        catch ( PasswordPolicyException e ) {
            throw new SecurityManagementException("Password is too weak"); //$NON-NLS-1$
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to change password", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.LocalUserService#removeUser(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    @RequirePermissions ( "security:user:delete" )
    public void removeUser ( ServiceStructuralObject service, UserPrincipal user ) throws SecurityManagementException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            removeUser(tx, user);
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to remove user", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.LocalUserService#enableUser(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    @RequirePermissions ( "security:user:modify:enable" )
    public void enableUser ( ServiceStructuralObject service, UserPrincipal user ) throws SecurityManagementException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            enableUser(tx, user);
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to enable user", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.security.api.services.LocalUserService#disableUser(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    @RequirePermissions ( "security:user:modify:disable" )
    public void disableUser ( ServiceStructuralObject service, UserPrincipal user ) throws SecurityManagementException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            disableUser(tx, user);
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new SecurityManagementException("Failed to disable user", e); //$NON-NLS-1$
        }
    }

}
