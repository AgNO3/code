/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.security.internal;


import java.util.Set;

import javax.persistence.EntityManager;

import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.PasswordChangeException;
import eu.agno3.fileshare.exceptions.SecurityException;
import eu.agno3.fileshare.exceptions.UserExistsException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.security.LocalUserService;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.db.BaseUser;
import eu.agno3.runtime.security.db.impl.AbstractLocalUserService;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    LocalUserService.class
} )
public class LocalUserServiceImpl extends AbstractLocalUserService<BaseUser> implements LocalUserService {

    private EntityTransactionService authEts;
    private UserMapper userMapper;
    private PasswordPolicyChecker passwordPolicyChecker;


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


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.security.LocalUserService#getUser(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public BaseUser getUser ( UserPrincipal principal ) throws FileshareException {
        try ( EntityTransactionContext tx = this.authEts.startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            BaseUser user = em.find(BaseUser.class, principal.getUserId());
            if ( user == null ) {
                throw new UserNotFoundException("User not found by principal " + principal); //$NON-NLS-1$
            }
            return user;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get user", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.security.LocalUserService#getMappedUser(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public UserInfo getMappedUser ( UserPrincipal princ ) throws FileshareException {
        UserPrincipal mu = this.userMapper.getExistingMappedUser(princ.getUserName(), princ.getRealmName(), princ.getUserId());
        if ( mu == null ) {
            throw new UserNotFoundException();
        }
        return getUser(mu);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.security.LocalUserService#getUsers()
     */
    @Override
    public Set<BaseUser> getUsers () throws FileshareException {
        try ( EntityTransactionContext tx = this.authEts.startReadOnly() ) {
            return getUsers(tx);
        }
        catch (
            SecurityManagementException |
            EntityTransactionException e ) {
            throw new FileshareException("Failed to enumerate users", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.security.LocalUserService#userExists(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public boolean userExists ( UserPrincipal userPrincipal ) throws FileshareException {
        try ( EntityTransactionContext tx = this.authEts.startReadOnly() ) {
            return userExists(tx, userPrincipal);
        }
        catch (
            SecurityManagementException |
            EntityTransactionException e ) {
            throw new FileshareException("Failed to check whether user exists", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.LocalUserService#enableUser(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public void enableUser ( UserPrincipal princ ) throws FileshareException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            enableUser(tx, princ);
            tx.commit();
        }
        catch (
            SecurityManagementException |
            EntityTransactionException e ) {
            throw new FileshareException("Failed to enable user", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.LocalUserService#disableUser(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public void disableUser ( UserPrincipal princ ) throws FileshareException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            disableUser(tx, princ);
            tx.commit();
        }
        catch (
            SecurityManagementException |
            EntityTransactionException e ) {
            throw new FileshareException("Failed to disable user", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws UserLicenseLimitExceededException
     *
     * @see eu.agno3.fileshare.security.LocalUserService#createUser(java.lang.String,
     *      eu.agno3.runtime.crypto.scrypt.SCryptResult, boolean, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public UserInfo createUser ( String userName, SCryptResult pwHash, boolean disabled, DateTime pwExpiry, DateTime expires )
            throws FileshareException, UserLicenseLimitExceededException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            EntityManager em = tx.getEntityManager();

            UserPrincipal user = new UserPrincipal(getLocalRealmName(), null, userName);
            if ( userExists(tx, user) ) {
                throw new UserExistsException();
            }

            BaseUser u = addUser(tx, userName, pwHash);
            u.setDisabled(disabled);
            u.setExpires(expires);
            u.setPwExpiry(pwExpiry);

            em.persist(u);
            em.flush();

            tx.commit();
            return u;
        }
        catch ( UserLicenseLimitExceededException e ) {
            throw e;
        }
        catch (
            SecurityManagementException |
            EntityTransactionException e ) {
            throw new SecurityException("Failed to create user", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.LocalUserService#removeUsers(java.util.Set)
     */
    @Override
    public void removeUsers ( Set<UserPrincipal> remove ) throws SecurityException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            for ( UserPrincipal princ : remove ) {
                removeUser(tx, princ);
            }

            tx.getEntityManager().flush();
            tx.commit();
        }
        catch (
            SecurityManagementException |
            EntityTransactionException e ) {
            throw new SecurityException("Failed to remove local user", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SecurityException
     *
     * @see eu.agno3.fileshare.security.LocalUserService#setUserExpiry(eu.agno3.runtime.security.principal.UserPrincipal,
     *      org.joda.time.DateTime)
     */
    @Override
    public void setUserExpiry ( UserPrincipal up, DateTime expiration ) throws SecurityException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            setUserExpiry(tx, up, expiration);
            tx.getEntityManager().flush();
            tx.commit();
        }
        catch (
            SecurityManagementException |
            EntityTransactionException e ) {
            throw new SecurityException("Failed to set user expiration", e); //$NON-NLS-1$
        }

    }


    @Override
    public void changePassword ( UserPrincipal user, SCryptResult passwordHash ) throws FileshareException {
        try ( EntityTransactionContext tx = this.authEts.start() ) {
            super.changePassword(tx, user, passwordHash);
            tx.getEntityManager().flush();
            tx.commit();
        }
        catch (
            SecurityManagementException |
            EntityTransactionException e ) {
            throw new PasswordChangeException("Failed to change password", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws SecurityException
     *
     * @see eu.agno3.fileshare.security.LocalUserService#verifyPassword(eu.agno3.runtime.security.principal.UserPrincipal,
     *      java.lang.String)
     */
    @Override
    public boolean verifyPassword ( UserPrincipal up, String oldPassword ) throws SecurityException {
        try ( EntityTransactionContext tx = this.authEts.startReadOnly() ) {
            return verifyPassword(tx, up, oldPassword);
        }
        catch (
            SecurityManagementException |
            EntityTransactionException e ) {
            throw new SecurityException("Failed to verify password hash", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.impl.AbstractLocalUserService#getUserClass()
     */
    @Override
    protected Class<BaseUser> getUserClass () {
        return BaseUser.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.impl.AbstractLocalUserService#createUserEntity()
     */
    @Override
    protected BaseUser createUserEntity () {
        return new BaseUser();
    }

}
