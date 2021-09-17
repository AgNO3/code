/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.notify.EntityNotificationData;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.fileshare.model.notify.UserExpirationNotificationData;
import eu.agno3.fileshare.service.LinkService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.EntityServiceInternal;
import eu.agno3.fileshare.service.api.internal.MailNotifier;
import eu.agno3.fileshare.service.api.internal.NotificationService;
import eu.agno3.fileshare.service.api.internal.PreferenceServiceInternal;
import eu.agno3.fileshare.service.api.internal.ShareServiceInternal;
import eu.agno3.fileshare.service.api.internal.UserServiceInternal;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;


/**
 * @author mbechler
 *
 */
@Component ( service = ExpirationRunner.class, immediate = true )
public class ExpirationRunner implements Runnable {

    private static final Logger log = Logger.getLogger(ExpirationRunner.class);

    private DefaultServiceContext sctx;
    private EntityServiceInternal entityService;
    private UserServiceInternal userService;
    private NotificationService notifyService;
    private PreferenceServiceInternal prefService;

    private ShareServiceInternal shareService;
    private LinkService linkService;

    private VFSServiceInternal vfs;


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setEntityService ( EntityServiceInternal es ) {
        this.entityService = es;
    }


    protected synchronized void unsetEntityService ( EntityServiceInternal es ) {
        if ( this.entityService == es ) {
            this.entityService = null;
        }
    }


    @Reference
    protected synchronized void setUserService ( UserServiceInternal us ) {
        this.userService = us;
    }


    protected synchronized void unsetUserService ( UserServiceInternal us ) {
        if ( this.userService == us ) {
            this.userService = null;
        }
    }


    @Reference
    protected synchronized void setShareService ( ShareServiceInternal ss ) {
        this.shareService = ss;
    }


    protected synchronized void unsetShareService ( ShareServiceInternal ss ) {
        if ( this.shareService == ss ) {
            this.shareService = null;
        }
    }


    @Reference
    protected synchronized void setNotifyService ( NotificationService ns ) {
        this.notifyService = ns;
    }


    protected synchronized void unsetNotifyService ( NotificationService ns ) {
        if ( this.notifyService == ns ) {

        }
    }


    @Reference
    protected synchronized void setPrefService ( PreferenceServiceInternal ps ) {
        this.prefService = ps;
    }


    protected synchronized void unsetPrefService ( PreferenceServiceInternal ps ) {
        if ( this.prefService == ps ) {
            this.prefService = null;
        }
    }


    @Reference
    protected synchronized void setLinkService ( LinkService ls ) {
        this.linkService = ls;
    }


    protected synchronized void unsetLinkService ( LinkService ls ) {
        if ( this.linkService == ls ) {
            this.linkService = null;
        }
    }


    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        this.vfs = vs;
    }


    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        if ( this.vfs == vs ) {
            this.vfs = null;
        }
    }

    private ScheduledExecutorService executor;
    private long period = 60;
    private boolean exit;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.exit = false;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.executor.scheduleAtFixedRate(this, 0, this.period, TimeUnit.MINUTES);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {

        if ( this.executor != null ) {
            this.exit = true;
            this.executor.shutdown();
            try {
                this.executor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch ( InterruptedException e ) {
                log.warn("Interrupted while waiting for executor to finish", e); //$NON-NLS-1$
            }
            this.executor = null;
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {

        runDeletion();

        if ( this.exit ) {
            return;
        }

        runNotification();

    }


    /**
     * 
     */
    private void runNotification () {
        runEntityExpiryNotification();

        if ( this.exit ) {
            return;
        }

        runUserExpiryNotification();
    }


    private void runUserExpiryNotification () {
        if ( this.sctx.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            return;
        }

        log.debug("Notifying users of pending user expiry"); //$NON-NLS-1$
        Duration notificationPeriod = this.sctx.getConfigurationProvider().getNotificationConfiguration().getExpirationNotificationPeriod();

        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            CriteriaQuery<User> query = makeUserExpirationQuery(tx, DateTime.now().plus(notificationPeriod));
            List<User> resultList = em.createQuery(query).getResultList();

            MailNotifier<UserExpirationNotificationData> notifier = this.notifyService.makeUserExpirationNotification();

            for ( User u : resultList ) {

                if ( this.exit ) {
                    tx.commit();
                    return;
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Expiring within notification period " + u); //$NON-NLS-1$
                }

                String dedupId = makeDedupId(u);

                if ( this.notifyService.haveNotified(tx, dedupId) ) {
                    log.debug("Already notified"); //$NON-NLS-1$
                    continue;
                }

                UserExpirationNotificationData notification = new UserExpirationNotificationData();

                boolean allowExtension = false;
                Set<MailRecipient> recpts = null;
                if ( u.getCreator() != null ) {
                    recpts = this.notifyService.getRecipientsForSubject(u.getCreator(), null);
                    allowExtension = this.sctx.getConfigurationProvider().getUserConfig().isAllowInvitingUserExtension();
                }
                else if ( !StringUtils.isBlank(this.sctx.getConfigurationProvider().getNotificationConfiguration().getAdminContact()) ) {
                    recpts = this.notifyService.getAdminRecipients();
                    allowExtension = true;
                }

                if ( recpts == null || recpts.isEmpty() ) {
                    log.warn("Noone found to warn about the user expiry for " + u); //$NON-NLS-1$
                    continue;
                }

                notification.setRecpients(recpts);
                notification.setExpiringUser(u);
                if ( allowExtension ) {
                    notification.setExtensionLink(this.linkService.makeGenericLink("/actions/extendUser.xhtml?user=" + u.getId(), null)); //$NON-NLS-1$
                }
                notifier.notify(notification);
                this.notifyService.trackNotification(tx, dedupId, u.getExpiration().plusDays(1));
            }

            tx.commit();
        }
        catch ( Exception e ) {
            log.warn("Failed to notify about expiring users", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    private void runEntityExpiryNotification () {
        if ( this.sctx.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            return;
        }

        log.debug("Notifying users of pending entity expiry"); //$NON-NLS-1$

        Duration notificationPeriod = this.sctx.getConfigurationProvider().getNotificationConfiguration().getExpirationNotificationPeriod();

        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getNative().begin(tx) ) {
            EntityManager em = tx.getEntityManager();
            CriteriaQuery<ContentEntity> query = makeExpirationQuery(tx, DateTime.now().plus(notificationPeriod), null);
            List<ContentEntity> resultList = em.createQuery(query).getResultList();

            MailNotifier<EntityNotificationData> notifier = this.notifyService.makeExpirationNotification();

            for ( ContentEntity e : resultList ) {

                if ( this.exit ) {
                    tx.commit();
                    return;
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Expiring within notification period " + e); //$NON-NLS-1$
                }

                String dedupId = makeDedupId(e);

                if ( isNotificationDisabled(e) ) {
                    log.debug("Notification disabled by preference"); //$NON-NLS-1$
                    continue;
                }

                if ( this.notifyService.haveNotified(tx, dedupId) ) {
                    log.debug("Already notified"); //$NON-NLS-1$
                    continue;
                }

                EntityNotificationData notification = new EntityNotificationData();

                notification.setEntity(e);

                Set<MailRecipient> recpts = this.notifyService.getRecipientsForSubject(e.getOwner(), new ExpirationNotificationChecker());
                if ( recpts == null || recpts.isEmpty() ) {
                    continue;
                }

                notification.setRecipients(recpts);

                notification.setFullPath(StringUtils.join(this.entityService.getFullPath(v, e, false), '/'));
                notification.setOwnerIsGroup(e.getOwner() instanceof Group);
                notification.setHideSensitive(this.sctx.getConfigurationProvider().getNotificationConfiguration().isHideSensitiveInformation());

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Send notification to %d recipients", recpts.size())); //$NON-NLS-1$
                }

                notifier.notify(notification);

                DateTime nextNotify = e.getExpires().plusDays(1);
                this.notifyService.trackNotification(tx, dedupId, nextNotify);
            }
            tx.commit();

        }
        catch ( Exception e ) {
            log.warn("Failed to notify users about expired entities", e); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     */
    private static boolean isNotificationDisabled ( ContentEntity e ) {
        Subject s = e.getOwner();
        if ( s instanceof User ) {
            User u = (User) s;
            String disableNotification = u.getPreferences() != null ? u.getPreferences().get("disableExpirationNotification") //$NON-NLS-1$
                    : null;
            return !StringUtils.isBlank(disableNotification) && Boolean.parseBoolean(disableNotification);
        }
        return false;
    }


    /**
     * @param e
     * @return
     */
    private static String makeDedupId ( ContentEntity e ) {
        return "expiryNotification/" + e.getId(); //$NON-NLS-1$
    }


    /**
     * @param u
     * @return
     */
    private static String makeDedupId ( User u ) {
        return "userExpiryNotification/" + u.getId(); //$NON-NLS-1$
    }


    /**
     * 
     */
    private void runDeletion () {
        removeExpiredEntities();

        if ( this.exit ) {
            return;
        }

        removeExpiredGrants();

        if ( this.exit ) {
            return;
        }

        removeExpiredUsers();
    }


    /**
     * 
     */
    private void removeExpiredUsers () {
        log.debug("Removing expired users"); //$NON-NLS-1$
        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            CriteriaQuery<User> query = makeUserExpirationQuery(tx, DateTime.now());
            List<User> resultList = em.createQuery(query).getResultList();

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Removing %d users", resultList.size())); //$NON-NLS-1$
            }

            for ( User user : resultList ) {
                this.userService.deleteUser(tx, user);
            }
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            log.error("Failed to remove expired users", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    private void removeExpiredGrants () {
        log.debug("Removing expired grants"); //$NON-NLS-1$
        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            CriteriaQuery<Grant> query = makeGrantExpirationQuery(tx, DateTime.now());
            List<Grant> resultList = em.createQuery(query).getResultList();

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Removing %d grants", resultList.size())); //$NON-NLS-1$
            }

            if ( !resultList.isEmpty() ) {
                this.shareService.doRevokeGrants(tx, resultList, true);
            }
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            log.error("Failed to remove expired grants", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    private void removeExpiredEntities () {
        log.debug("Removing expired entries"); //$NON-NLS-1$
        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getNative().begin(tx) ) {
            EntityManager em = tx.getEntityManager();
            CriteriaQuery<ContentEntity> query = makeExpirationQuery(tx, DateTime.now(), null);
            List<ContentEntity> resultList = em.createQuery(query).getResultList();

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Removing %d entities", resultList.size())); //$NON-NLS-1$
            }

            if ( !resultList.isEmpty() ) {
                this.entityService.doDelete(tx, v, null, new HashSet<>(resultList), true);
            }

            tx.commit();
        }
        catch ( Exception e ) {
            log.error("Failed to remove expired entities", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param now
     * @return
     */
    private static CriteriaQuery<User> makeUserExpirationQuery ( EntityTransactionContext tx, DateTime expiryThreshold ) {
        EntityManager em = tx.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> from = query.from(User.class);
        SingularAttribute<? super User, DateTime> expires = em.getMetamodel().entity(User.class).getSingularAttribute("expiration", DateTime.class); //$NON-NLS-1$
        query.where(cb.lessThanOrEqualTo(from.get(expires), expiryThreshold));
        return query;
    }


    /**
     * @param em
     * @param now
     * @return
     */
    private static CriteriaQuery<Grant> makeGrantExpirationQuery ( EntityTransactionContext tx, DateTime expiryThreshold ) {
        EntityManager em = tx.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Grant> query = cb.createQuery(Grant.class);
        Root<Grant> from = query.from(Grant.class);
        SingularAttribute<? super Grant, DateTime> expires = em.getMetamodel().entity(Grant.class).getSingularAttribute("expires", DateTime.class); //$NON-NLS-1$
        query.where(cb.lessThanOrEqualTo(from.get(expires), expiryThreshold));
        return query;
    }


    /**
     * @param em
     * @param expiryThreshold
     * @return
     */
    private static CriteriaQuery<ContentEntity> makeExpirationQuery ( EntityTransactionContext tx, DateTime expiryThreshold, DateTime limit ) {
        EntityManager em = tx.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ContentEntity> query = cb.createQuery(ContentEntity.class);
        Root<ContentEntity> from = query.from(ContentEntity.class);
        SingularAttribute<? super ContentEntity, DateTime> expires = em.getMetamodel().entity(ContentEntity.class)
                .getSingularAttribute("expires", DateTime.class); //$NON-NLS-1$
        Predicate p = cb.lessThanOrEqualTo(from.get(expires), expiryThreshold);
        if ( limit != null ) {
            p = cb.and(p, cb.greaterThan(from.get(expires), limit));
        }
        query.where(p);
        return query;
    }
}
