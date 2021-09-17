/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.db.impl;


import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.runtime.crypto.scrypt.SCryptParams;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.crypto.scrypt.SCryptUtil;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.db.BaseLocalUserService;
import eu.agno3.runtime.security.db.BaseUser;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 * @param <TUser>
 *
 */
public abstract class AbstractLocalUserService <TUser extends BaseUser> implements BaseLocalUserService<TUser> {

    /**
     * 
     */
    private static final String SHA1PRNG = "SHA1PRNG"; //$NON-NLS-1$

    private static final SCryptParams PARAMS = new SCryptParams(1 << 14 - 1, 8, 1);
    /**
     * 
     */
    private static final String USER_NAME_ATTR = "userName"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(AbstractLocalUserService.class);


    /**
     * 
     */
    public AbstractLocalUserService () {
        super();
    }


    protected abstract TUser createUserEntity ();


    protected abstract Class<TUser> getUserClass ();


    protected abstract UserMapper getUserMapper ();


    protected abstract PasswordPolicyChecker getPolicyChecker ();


    @Override
    public Set<TUser> getUsers ( EntityTransactionContext tx ) throws SecurityManagementException {
        EntityManager em = tx.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TUser> q = cb.createQuery(getUserClass());
        q.from(getUserClass());
        List<TUser> result = em.createQuery(q).getResultList();
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found %d users", result.size())); //$NON-NLS-1$
        }
        return new HashSet<>(result);
    }


    @Override
    public UserInfo fetchUser ( EntityTransactionContext tx, UserPrincipal u ) throws SecurityManagementException {
        return getUser(tx, u);
    }


    @Override
    public boolean userExists ( EntityTransactionContext tx, UserPrincipal user ) throws SecurityManagementException {
        try {
            EntityManager em = tx.getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<TUser> q = cb.createQuery(getUserClass());
            Root<TUser> from = q.from(getUserClass());

            SingularAttribute<? super TUser, ?> usernameAttr = from.getModel().getSingularAttribute(USER_NAME_ATTR);
            q.where(cb.equal(from.get(usernameAttr), user.getUserName()));

            List<TUser> resultList = em.createQuery(q).getResultList();

            if ( resultList.size() == 1 ) {
                return true;
            }
            else if ( resultList.size() > 1 ) {
                throw new SecurityManagementException("Multiple user entries for user " + user.getUserName()); //$NON-NLS-1$
            }

            return false;
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to check whether user exists", e); //$NON-NLS-1$
        }
    }


    @Override
    public TUser addUser ( EntityTransactionContext tx, String username, SCryptResult passwordHash )
            throws SecurityManagementException, UserLicenseLimitExceededException {
        try {
            EntityManager em = tx.getEntityManager();
            TUser u = createUserEntity();

            u.setUserName(username);
            if ( passwordHash != null ) {
                u.setSalt(passwordHash.getSalt());
                u.setPasswordHash(encodePasswordHash(passwordHash));
            }
            else {
                u.setDisabled(true);
            }
            u.setCreated(DateTime.now());
            em.persist(u);
            em.flush();

            // create a mapping now
            getUserMapper().getMappedUser(username, getLocalRealmName(), u.getId());

            if ( log.isDebugEnabled() ) {
                log.debug("Added user " + username); //$NON-NLS-1$
            }

            return u;
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to create user", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.BaseLocalUserService#generatePasswordHash(java.lang.String, boolean)
     */
    @Override
    public SCryptResult generatePasswordHash ( String password, boolean checkPolicy ) throws SecurityManagementException, PasswordPolicyException {
        if ( checkPolicy ) {
            this.getPolicyChecker().checkPasswordChangeValid(password, null);
        }
        try {
            byte[] salt = new byte[32];
            SecureRandom.getInstance(SHA1PRNG).nextBytes(salt);
            return SCryptUtil.generate(encodePassword(password), salt, PARAMS);
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new SecurityManagementException("Failed to generate password hash", e); //$NON-NLS-1$
        }

    }


    /**
     * 
     * @param password
     * @return the encoded password
     */
    protected byte[] encodePassword ( String password ) {
        return password.getBytes(Charset.forName("UTF-8")); //$NON-NLS-1$
    }


    /**
     * 
     * @param passwordHash
     * @return the password hash portion as a string
     */
    protected String encodePasswordHash ( SCryptResult passwordHash ) {
        return Base64.encodeBase64String(passwordHash.getKey());
    }


    /**
     * 
     * @param hash
     * @return the raw password hash
     */
    protected byte[] decodePasswordHash ( String hash ) {
        return Base64.decodeBase64(hash);
    }


    @Override
    public void changePassword ( EntityTransactionContext tx, UserPrincipal user, SCryptResult passwordHash ) throws SecurityManagementException {
        try {
            EntityManager em = tx.getEntityManager();
            TUser u = getUser(tx, user);

            u.setSalt(passwordHash.getSalt());
            u.setPasswordHash(this.encodePasswordHash(passwordHash));
            u.setLastPwChange(DateTime.now());
            u.setPwExpiry(null);

            em.persist(u);
            em.flush();

            if ( log.isDebugEnabled() ) {
                log.debug("Changed password for user " + user); //$NON-NLS-1$
            }
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to update user password", e); //$NON-NLS-1$
        }
    }


    @Override
    public void removeUser ( EntityTransactionContext tx, UserPrincipal username ) throws SecurityManagementException {
        try {
            TUser u = null;
            try {
                u = getUser(tx, username);
            }
            catch ( SecurityManagementException e ) {
                log.warn("User to delete not found", e); //$NON-NLS-1$
            }

            if ( u != null ) {
                tx.getEntityManager().remove(u);
            }

            UserMapper um = getUserMapper();
            if ( um != null ) {
                log.debug("Removing user mapping"); //$NON-NLS-1$
                um.removeMapping(username);
            }
            else {
                log.warn("No user mapper found"); //$NON-NLS-1$
            }
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to remove user", e); //$NON-NLS-1$
        }

    }


    @Override
    public void enableUser ( EntityTransactionContext tx, UserPrincipal user ) throws SecurityManagementException {
        if ( log.isDebugEnabled() ) {
            log.debug("Enabling user " + user); //$NON-NLS-1$
        }
        try {
            EntityManager em = tx.getEntityManager();
            TUser u = getUser(tx, user);
            u.setDisabled(false);
            em.persist(u);
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to enable user", e); //$NON-NLS-1$
        }
    }


    @Override
    public void disableUser ( EntityTransactionContext tx, UserPrincipal user ) throws SecurityManagementException {
        if ( log.isDebugEnabled() ) {
            log.debug("Disabling user " + user); //$NON-NLS-1$
        }
        try {
            EntityManager em = tx.getEntityManager();
            TUser u = getUser(tx, user);
            u.setDisabled(true);
            em.persist(u);
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to disable user", e); //$NON-NLS-1$
        }
    }


    @Override
    public void setUserExpiry ( EntityTransactionContext tx, UserPrincipal user, DateTime expiry ) throws SecurityManagementException {
        if ( log.isDebugEnabled() ) {
            log.debug("Setting expiration for " + user); //$NON-NLS-1$
        }
        try {
            EntityManager em = tx.getEntityManager();
            TUser u = getUser(tx, user);
            u.setExpires(expiry);
            em.persist(u);
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to set expiration", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param user
     * @return the user object for the given user
     * @throws SecurityManagementException
     */
    protected TUser getUser ( EntityTransactionContext tx, UserPrincipal user ) throws SecurityManagementException {
        if ( !getLocalRealmName().equals(user.getRealmName()) ) { // $NON-NLS-1$
            throw new SecurityManagementException("Can only handle local users"); //$NON-NLS-1$
        }
        try {
            EntityManager em = tx.getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<TUser> q = cb.createQuery(getUserClass());
            Root<TUser> from = q.from(getUserClass());

            SingularAttribute<? super TUser, String> userNameAttr = from.getModel().getSingularAttribute(USER_NAME_ATTR, String.class);
            q.where(cb.equal(from.get(userNameAttr), user.getUserName()));

            return em.createQuery(q).getSingleResult();
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to find user", e); //$NON-NLS-1$
        }
    }


    protected String getLocalRealmName () {
        return "LOCAL"; //$NON-NLS-1$
    }


    /**
     * @param em
     * @param up
     * @param password
     * @return
     * @throws SecurityManagementException
     */
    protected boolean verifyPassword ( EntityTransactionContext tx, UserPrincipal up, String password ) throws SecurityManagementException {
        BaseUser user = getUser(tx, up);
        if ( StringUtils.isBlank(user.getSalt()) || StringUtils.isBlank(user.getPasswordHash()) ) {
            return false;
        }
        return SCryptUtil.check(encodePassword(password), user.getSalt(), decodePasswordHash(user.getPasswordHash()));
    }

}