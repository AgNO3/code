/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.FlaggingService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.FlaggingServiceInternal;
import eu.agno3.fileshare.service.api.internal.LastUsedTracker;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    FlaggingService.class, FlaggingServiceInternal.class
} )
public class FlaggingServiceImpl implements FlaggingServiceInternal {

    private static final Logger log = Logger.getLogger(FlaggingServiceImpl.class);

    private DefaultServiceContext ctx;
    private AccessControlService accessControl;
    private VFSServiceInternal vfs;
    private LastUsedTracker tracker;


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    @Reference
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
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


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setLastUsedTracker ( LastUsedTracker lut ) {
        this.tracker = lut;
    }


    protected synchronized void unsetLastUsedTracker ( LastUsedTracker lut ) {
        if ( this.tracker == lut ) {
            this.tracker = null;
        }
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    @Override
    public Set<EntityKey> getHiddenEntities ( EntityTransactionContext tx ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return Collections.EMPTY_SET;
        }
        return toEntityKeys(tx, this.accessControl.getCurrentUser(tx).getHiddenEntities());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#getHiddenEntities()
     */
    @Override
    public Set<EntityKey> getHiddenEntities () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getHiddenEntities(tx);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch hidden entities", e); //$NON-NLS-1$
        }
    }


    @Override
    public Set<UUID> getHiddenSubjects ( EntityTransactionContext tx ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return Collections.EMPTY_SET;
        }
        return toSubjectIds(this.accessControl.getCurrentUser(tx).getHiddenSubjects());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#getHiddenSubjects()
     */
    @Override
    public Set<UUID> getHiddenSubjects () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getHiddenSubjects(tx);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch hidden subjects", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#markEntitiesHidden(java.util.Collection)
     */
    @Override
    public void markEntitiesHidden ( Collection<EntityKey> es ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getVFS(es).begin(tx) ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            Set<ContentEntity> entities = new HashSet<>();
            for ( EntityKey id : es ) {
                VFSEntity e = v.load(id);
                if ( e == null ) {
                    throw new EntityNotFoundException();
                }
                this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
                ContentEntity entity = v.getOrCreateMappedEntity(e);
                entities.add(entity);
            }
            currentUser.getHiddenEntities().addAll(entities);
            em.persist(currentUser);
            em.flush();
            v.commit();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to mark entities hidden", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#markSubjectsHidden(java.util.Collection)
     */
    @Override
    public void markSubjectsHidden ( Collection<UUID> es ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            Set<Subject> subjects = new HashSet<>();
            for ( UUID id : es ) {
                Subject entity = em.find(Subject.class, id);
                subjects.add(entity);
            }
            currentUser.getHiddenSubjects().addAll(subjects);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to mark subjects hidden", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#markEntitiesVisible(java.util.Collection)
     */
    @Override
    public void markEntitiesVisible ( Collection<EntityKey> es ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getVFS(es).begin(tx) ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            Set<ContentEntity> entities = new HashSet<>();
            for ( EntityKey id : es ) {
                VFSEntity e = v.load(id);
                if ( e == null ) {
                    throw new EntityNotFoundException();
                }
                this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
                ContentEntity entity = v.getOrCreateMappedEntity(e);
                entities.add(entity);
            }
            currentUser.getHiddenEntities().removeAll(entities);
            em.persist(currentUser);
            em.flush();
            v.commit();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to mark entities visible", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#markSubjectsVisible(java.util.Collection)
     */
    @Override
    public void markSubjectsVisible ( Collection<UUID> es ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            Set<Subject> subjects = new HashSet<>();
            for ( UUID id : es ) {
                Subject entity = em.find(Subject.class, id);
                subjects.add(entity);
            }
            currentUser.getHiddenSubjects().removeAll(subjects);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to mark subjects visible", e); //$NON-NLS-1$
        }
    }


    @Override
    public Set<EntityKey> getFavoriteEntityIds ( EntityTransactionContext tx ) throws FileshareException {
        return toEntityKeys(tx, getFavoriteEntities(tx));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#getFavoriteEntityIds()
     */
    @Override
    public Set<EntityKey> getFavoriteEntityIds () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getFavoriteEntityIds(tx);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch favorite entity ids", e); //$NON-NLS-1$
        }
    }


    @Override
    public List<VFSEntity> getFavoriteEntities ( EntityTransactionContext tx ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return Collections.EMPTY_LIST;
        }
        User currentUser = this.accessControl.getCurrentUser(tx);
        List<VFSEntity> sorted = new ArrayList<>();

        for ( ContentEntity e : currentUser.getFavoriteEntities() ) {
            sorted.add(ServiceUtil.unwrapEntity(tx, this.vfs, e));
        }

        LastUsedTracker track = this.tracker;
        if ( track != null ) {
            Map<EntityKey, DateTime> lastUsedByEntity = track.getLastUsedEntities(currentUser.getId());
            Collections.sort(sorted, new FavoritesSorter(lastUsedByEntity));
            if ( log.isDebugEnabled() ) {
                log.debug("Last used " + lastUsedByEntity); //$NON-NLS-1$
            }
        }
        return sorted;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#getFavoriteEntities()
     */
    @Override
    public List<VFSEntity> getFavoriteEntities () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            List<VFSEntity> res = new ArrayList<>();
            for ( VFSEntity vfsEntity : getFavoriteEntities(tx) ) {
                res.add(vfsEntity.cloneShallow());
            }
            return res;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch favorite entities", e); //$NON-NLS-1$
        }
    }


    @Override
    public void trackEntityFavorityUsage ( EntityKey targetId ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Tracking favorite usage " + targetId); //$NON-NLS-1$
        }
        User u = this.accessControl.getCurrentUser();

        try ( VFSContext v = this.vfs.getVFS(targetId).begin(true) ) {
            VFSEntity find = v.load(targetId);
            if ( find == null ) {
                return;
            }
            this.accessControl.checkAccess(v, find, GrantPermission.READ);

            LastUsedTracker track = this.tracker;
            if ( track != null ) {
                track.trackUsage(u.getId(), find.getEntityKey());
            }
        }
    }


    @Override
    public Set<UUID> getFavoriteSubjectIds ( EntityTransactionContext tx ) throws FileshareException {
        return toSubjectIds(getFavoriteSubjects(tx));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#getFavoriteSubjectIds()
     */
    @Override
    public Set<UUID> getFavoriteSubjectIds () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getFavoriteSubjectIds(tx);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch favorite subject ids", e); //$NON-NLS-1$
        }
    }


    @Override
    public Set<String> getFavoriteMails ( EntityTransactionContext tx ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return Collections.EMPTY_SET;
        }
        return this.accessControl.getCurrentUser(tx).getMailFavorites();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#getFavoriteMails()
     */
    @Override
    public Set<String> getFavoriteMails () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return new HashSet<>(getFavoriteMails(tx));
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch favorite mails", e); //$NON-NLS-1$
        }
    }


    @Override
    public Set<String> getHiddenMails ( EntityTransactionContext tx ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return Collections.EMPTY_SET;
        }
        return this.accessControl.getCurrentUser(tx).getMailHidden();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#getHiddenMails()
     */
    @Override
    public Set<String> getHiddenMails () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return new HashSet<>(getHiddenMails(tx));
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch hidden mails", e); //$NON-NLS-1$
        }
    }


    @Override
    public Set<Subject> getFavoriteSubjects ( EntityTransactionContext tx ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return Collections.EMPTY_SET;
        }
        return this.accessControl.getCurrentUser(tx).getFavoriteSubjects();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#getFavoriteSubjects()
     */
    @Override
    public Set<Subject> getFavoriteSubjects () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            Set<Subject> res = new HashSet<>();
            for ( Subject subject : getFavoriteSubjects(tx) ) {
                res.add(subject.cloneShallow());
            }
            return res;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch favorite subjects", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#markEntitiesFavorite(java.util.Collection)
     */
    @Override
    public void markEntitiesFavorite ( Collection<EntityKey> es ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getVFS(es).begin(tx) ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);

            Set<ContentEntity> entities = new HashSet<>();
            for ( EntityKey id : es ) {
                VFSEntity e = v.load(id);
                if ( e == null ) {
                    throw new EntityNotFoundException();
                }

                this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
                ContentEntity entity = v.getOrCreateMappedEntity(e);
                entities.add(entity);
            }
            currentUser.getFavoriteEntities().addAll(entities);
            em.persist(currentUser);
            em.flush();
            v.commit();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to mark entities as favorite", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#markSubjectsFavorite(java.util.Collection)
     */
    @Override
    public void markSubjectsFavorite ( Collection<UUID> es ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            Set<Subject> subjects = new HashSet<>();
            for ( UUID id : es ) {
                Subject entity = em.find(Subject.class, id);
                subjects.add(entity);
            }
            currentUser.getFavoriteSubjects().addAll(subjects);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to mark subjects as favorite", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#unmarkEntitiesFavorite(java.util.Collection)
     */
    @Override
    public void unmarkEntitiesFavorite ( Collection<EntityKey> es ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getVFS(es).begin(tx) ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            Set<ContentEntity> entities = new HashSet<>();
            for ( EntityKey id : es ) {
                VFSEntity e = v.load(id);
                if ( e == null ) {
                    throw new EntityNotFoundException();
                }
                this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
                ContentEntity entity = v.getOrCreateMappedEntity(e);
                entities.add(entity);
            }
            currentUser.getFavoriteEntities().removeAll(entities);
            em.persist(currentUser);
            em.flush();
            v.commit();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to unmark entities as favorite", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#unmarkSubjectsFavorite(java.util.Collection)
     */
    @Override
    public void unmarkSubjectsFavorite ( Collection<UUID> es ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            Set<Subject> subjects = new HashSet<>();
            for ( UUID id : es ) {
                Subject entity = em.find(Subject.class, id);
                subjects.add(entity);
            }
            currentUser.getFavoriteSubjects().removeAll(subjects);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to unmark subjects as favorite", e); //$NON-NLS-1$
        }
    }


    /**
     * @param hiddenEntities
     * @return
     */
    private Set<EntityKey> toEntityKeys ( EntityTransactionContext tx, Collection<? extends VFSEntity> entities ) {
        Set<EntityKey> ids = new HashSet<>();
        for ( VFSEntity e : entities ) {
            try {
                if ( e instanceof ContentEntity ) {
                    ids.add(ServiceUtil.unwrapEntity(tx, this.vfs, (ContentEntity) e).getEntityKey());
                }
                else {
                    ids.add(e.getEntityKey());
                }
            }
            catch ( FileshareException ex ) {
                log.error("Failed to get mapped entity " + e, ex); //$NON-NLS-1$
            }
        }
        return ids;
    }


    /**
     * @param hiddenEntities
     * @return
     */
    private static Set<UUID> toSubjectIds ( Collection<Subject> subjects ) {
        Set<UUID> ids = new HashSet<>();
        for ( Subject e : subjects ) {
            ids.add(e.getId());
        }
        return ids;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.FlaggingService#markMailFavorite(java.lang.String)
     */
    @Override
    public void markMailFavorite ( String addr ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            currentUser.getMailFavorites().add(addr);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Marking failed", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.FlaggingService#unmarkMailFavorite(java.lang.String)
     */
    @Override
    public void unmarkMailFavorite ( String addr ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            currentUser.getMailFavorites().remove(addr);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Marking failed", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#markLinksFavorite()
     */
    @Override
    public void markLinksFavorite () throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            currentUser.setLinksFavorite(true);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Marking failed", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#unmarkLinksFavorite()
     */
    @Override
    public void unmarkLinksFavorite () throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            currentUser.setLinksFavorite(false);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Marking failed", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.FlaggingService#markMailFavorite(java.lang.String)
     */
    @Override
    public void markMailHidden ( String addr ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            currentUser.getMailHidden().add(addr);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Marking failed", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.FlaggingService#unmarkMailFavorite(java.lang.String)
     */
    @Override
    public void unmarkMailHidden ( String addr ) throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            currentUser.getMailHidden().remove(addr);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Marking failed", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#markLinksFavorite()
     */
    @Override
    public void markLinksHidden () throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            currentUser.setLinksHidden(true);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Marking failed", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.FlaggingService#unmarkLinksFavorite()
     */
    @Override
    public void unmarkLinksHidden () throws FileshareException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return;
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            currentUser.setLinksHidden(false);
            em.persist(currentUser);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Marking failed", e); //$NON-NLS-1$
        }
    }

}
