/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.CyclicMoveTargetException;
import eu.agno3.fileshare.exceptions.DisallowedMimeTypeException;
import eu.agno3.fileshare.exceptions.EntityExpirationInvalidException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InconsistentSecurityLabelException;
import eu.agno3.fileshare.exceptions.InvalidEntityException;
import eu.agno3.fileshare.exceptions.InvalidSecurityLabelException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.exceptions.StructureException;
import eu.agno3.fileshare.exceptions.UnsupportedOperationException;
import eu.agno3.fileshare.model.ChangeType;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.audit.EntityFileshareEvent;
import eu.agno3.fileshare.model.audit.MoveEntityFileshareEvent;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.EntityService;
import eu.agno3.fileshare.service.api.internal.BlockStorageService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.EntityServiceInternal;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.QuotaReservation;
import eu.agno3.fileshare.service.api.internal.QuotaServiceInternal;
import eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.audit.MoveEntityFileshareAuditBuilder;
import eu.agno3.fileshare.service.audit.MultiEntityFileshareAuditBuilder;
import eu.agno3.fileshare.service.audit.SingleEntityFileshareAuditBuilder;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.jmx.JMXSecurityUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    EntityServiceInternal.class, EntityService.class
} )
public class EntityServiceImpl implements EntityServiceInternal {

    /**
     * 
     */
    private static final String CHARSET = "charset"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(EntityServiceImpl.class);

    private DefaultServiceContext ctx;

    private BlockStorageService blockStore;

    private AccessControlService accessControl;

    private PolicyEvaluator policyEvaluator;

    private QuotaServiceInternal quota;

    private VFSServiceInternal vfs;

    private RecursiveModificationTimeTracker modTracker;


    @Reference
    protected synchronized void setPolicyEvaluator ( PolicyEvaluator pe ) {
        this.policyEvaluator = pe;
    }


    protected synchronized void unsetPolicyEvaluator ( PolicyEvaluator pe ) {
        if ( this.policyEvaluator == pe ) {
            this.policyEvaluator = null;
        }
    }


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
    protected synchronized void setBlockStorageService ( BlockStorageService bs ) {
        this.blockStore = bs;
    }


    protected synchronized void unsetBlockStorageService ( BlockStorageService bs ) {
        if ( this.blockStore == bs ) {
            this.blockStore = null;
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
    protected synchronized void setQuotaService ( QuotaServiceInternal qs ) {
        this.quota = qs;
    }


    protected synchronized void unsetQuotaService ( QuotaServiceInternal qs ) {
        if ( this.quota == qs ) {
            this.quota = null;
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


    @Reference
    protected synchronized void setRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        this.modTracker = rmt;
    }


    protected synchronized void unsetRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        if ( this.modTracker == rmt ) {
            this.modTracker = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.EntityService#parseEntityKey(java.lang.String)
     */
    @Override
    public EntityKey parseEntityKey ( String id ) {
        return this.vfs.parseEntityKey(id);
    }


    @Override
    public VFSEntity getEntity ( EntityKey id ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getVFS(id).begin(tx) ) {
            VFSEntity e = v.load(id);
            User currentUser = this.accessControl.getCurrentUser(tx);
            Grant g = this.accessControl.getTokenAuthGrant(v, e);

            if ( e == null ) {
                throw new EntityNotFoundException();
            }

            this.policyEvaluator.checkPolicy(v, e, WebUtils.getHttpRequest(SecurityUtils.getSubject()));

            if ( e instanceof ContainerEntity ) {
                this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
            }
            else {
                if ( !this.accessControl.hasAccess(v, e, GrantPermission.READ) ) {
                    this.accessControl.checkUserIsCreatorWithPerm(v, e, currentUser, g);
                }
            }

            return e.cloneShallow();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get entity", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws PolicyNotFulfilledException
     *
     * @see eu.agno3.fileshare.service.EntityService#getParent(eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public VFSContainerEntity getParent ( EntityKey entityKey ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly();
              VFSContext v = this.vfs.getVFS(entityKey).begin(tx) ) {
            VFSEntity e = v.load(entityKey);
            User currentUser = this.accessControl.getCurrentUser(tx);
            Grant g = this.accessControl.getTokenAuthGrant(v, e);

            if ( e == null ) {
                throw new EntityNotFoundException();
            }

            e = v.getParent(e);

            if ( e == null ) {
                return null;
            }

            this.policyEvaluator.checkPolicy(v, e, WebUtils.getHttpRequest(SecurityUtils.getSubject()));

            if ( e instanceof ContainerEntity ) {
                this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
            }
            else {
                if ( !this.accessControl.hasAccess(v, e, GrantPermission.READ) ) {
                    this.accessControl.checkUserIsCreatorWithPerm(v, e, currentUser, g);
                }
            }

            return (VFSContainerEntity) e.cloneShallow();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get parent", e); //$NON-NLS-1$
        }
    }


    @Override
    public Grant getGrant ( EntityKey entityId ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly();
              VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
            VFSEntity e = v.load(entityId);
            User currentUser = this.accessControl.getCurrentUser(tx);
            Grant g = this.accessControl.getTokenAuthGrant(v, e);

            if ( e == null ) {
                throw new EntityNotFoundException();
            }

            this.policyEvaluator.checkPolicy(v, e, WebUtils.getHttpRequest(SecurityUtils.getSubject()));

            if ( e instanceof ContainerEntity ) {
                this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
            }
            else {
                if ( !this.accessControl.hasAccess(v, e, GrantPermission.READ) ) {
                    this.accessControl.checkUserIsCreatorWithPerm(v, e, currentUser, g);
                }
            }

            if ( g == null ) {
                g = this.accessControl.getAnySubjectGrant(v, e);
            }

            if ( g != null ) {
                return g.cloneShallow();
            }
            return g;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get grant", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.EntityService#getEntities(java.util.List)
     */
    @Override
    public List<VFSEntity> getEntities ( List<EntityKey> ids ) throws FileshareException {
        try ( VFSContext v = this.vfs.getVFS(ids).begin(true) ) {
            List<VFSEntity> entities = new ArrayList<>();

            for ( EntityKey id : ids ) {
                VFSEntity e = v.load(id);

                if ( e == null ) {
                    throw new EntityNotFoundException("Entity not found " + id); //$NON-NLS-1$
                }

                this.accessControl.checkAccess(v, e, GrantPermission.READ);
                entities.add(e);
            }
            return entities;
        }
    }


    @Override
    public List<String> getFullPath ( EntityKey id ) throws FileshareException {
        try ( VFSContext v = this.vfs.getVFS(id).begin(true) ) {
            VFSEntity e = v.load(id);
            this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
            this.policyEvaluator.checkPolicy(v, e, WebUtils.getHttpRequest(SecurityUtils.getSubject()));

            List<String> pathSegments = getFullPath(v, e, true);
            v.commit();
            return pathSegments;
        }
    }


    /**
     * @param e
     * @return the full path to the entity
     * @throws FileshareException
     */
    @Override
    public List<String> getFullPath ( VFSContext v, VFSEntity e, boolean checkAccess ) throws FileshareException {
        List<String> pathSegments = new ArrayList<>();
        if ( StringUtils.isBlank(e.getLocalName()) ) {
            return pathSegments;
        }
        pathSegments.add(e.getLocalName());
        VFSEntity parent = v.getParent(e);

        while ( parent != null && ( !checkAccess || this.accessControl.hasAnyAccess(v, parent, GrantPermission.values()) ) ) {
            if ( !StringUtils.isBlank(parent.getLocalName()) ) {
                pathSegments.add(parent.getLocalName());
            }
            parent = v.getParent(parent);
        }

        Collections.reverse(pathSegments);
        return pathSegments;
    }


    @Override
    public List<VFSContainerEntity> getParents ( EntityKey id ) throws FileshareException {
        try ( VFSContext v = this.vfs.getVFS(id).begin(true) ) {
            VFSEntity e = v.load(id);
            this.accessControl.checkAnyAccess(v, e, GrantPermission.values());
            this.policyEvaluator.checkPolicy(v, e, WebUtils.getHttpRequest(SecurityUtils.getSubject()));

            List<VFSContainerEntity> pathSegments = new ArrayList<>();
            VFSContainerEntity parent = v.getParent(e);

            while ( parent != null && this.accessControl.hasAnyAccess(v, parent, GrantPermission.values()) ) {
                pathSegments.add(parent.cloneShallow(true));
                parent = v.getParent(parent);
            }

            Collections.reverse(pathSegments);
            return pathSegments;
        }
    }


    @Override
    public VFSEntity move ( EntityKey entityId, EntityKey targetId ) throws FileshareException {
        return move(Arrays.asList(entityId), Collections.EMPTY_MAP, targetId).get(0);

    }


    @Override
    public VFSEntity moveAndRename ( EntityKey entityId, EntityKey targetId, String newName ) throws FileshareException {
        return move(Arrays.asList(entityId), Collections.singletonMap(entityId, newName), targetId).get(0);
    }


    @Override
    public List<VFSEntity> move ( List<EntityKey> entities, Map<EntityKey, String> names, EntityKey targetId ) throws FileshareException {
        try ( AuditContext<MoveEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(MoveEntityFileshareAuditBuilder.class) ) {
            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                  VFSContext v = this.vfs.getVFS(targetId).begin(false) ) {
                audit.builder().access(this.accessControl).action(MoveEntityFileshareEvent.MOVE_ACTION);
                VFSContainerEntity target = v.load(targetId, VFSContainerEntity.class);
                audit.builder().target(target);
                User currentUser = this.accessControl.getCurrentUser(tx);
                Grant grant = this.accessControl.getTokenAuthGrant(v, target);
                this.accessControl.checkAccess(v, target, GrantPermission.UPLOAD);

                List<VFSEntity> toMove = getEntitiesToMove(v, entities, names, target, audit.builder());

                Map<VFSContainerEntity, Long> perParentSize = new HashMap<>();
                long totalSize = getPerParentSizes(v, toMove, perParentSize);

                for ( Entry<VFSContainerEntity, Long> toReserve : perParentSize.entrySet() ) {
                    this.quota.checkAndReserve(v, toReserve.getKey(), -toReserve.getValue(), 0);
                }
                List<VFSEntity> moved;
                try ( QuotaReservation qres = this.quota.checkAndReserve(v, target, totalSize, 0) ) {
                    moved = doMove(v, target, currentUser, grant, toMove, names, perParentSize);
                    qres.commit(v, target);
                }
                catch ( Exception e ) {
                    for ( Entry<VFSContainerEntity, Long> toReserve : perParentSize.entrySet() ) {
                        this.quota.undoReservation(v.getQuotaKey(toReserve.getKey()), -toReserve.getValue());
                    }
                    throw e;
                }
                v.commit();
                tx.commit();
                return moved;
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to move", e); //$NON-NLS-1$
            }

        }
    }


    /**
     * @param em
     * @param target
     * @param currentUser
     * @param grant
     * @param toMove
     * @param names
     * @param perParentSize
     * @return
     * @throws FileshareException
     */
    private List<VFSEntity> doMove ( VFSContext v, VFSContainerEntity target, User currentUser, Grant grant, List<VFSEntity> toMove,
            Map<EntityKey, String> names, Map<VFSContainerEntity, Long> perParentSize ) throws FileshareException {
        DateTime now = DateTime.now();
        List<VFSEntity> moved = new ArrayList<>();
        for ( VFSEntity entity : toMove ) {
            Grant targetGrant = this.accessControl.getTokenAuthGrant(v, entity);
            VFSContainerEntity oldParent = v.getParent(entity);

            v.trackChange(oldParent, entity, ChangeType.MOVE, now);
            v.removeChild(oldParent, entity);

            String newName = names.get(entity.getEntityKey());
            if ( newName != null ) {
                entity.setLocalName(newName);
            }
            ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, entity, grant);

            recursiveSetOwner(v, entity, target.getOwner());
            v.addChild(target, entity);

            ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, oldParent, targetGrant);
            v.saveNoFlush(oldParent, entity);
            long combinedSize = this.quota.getCombinedSize(v, entity);
            this.quota.commit(v, oldParent, -1 * combinedSize);
            perParentSize.put(oldParent, perParentSize.get(oldParent) - combinedSize);
            moved.add(entity);
        }

        ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, target, grant);
        v.save(target);
        return moved;
    }


    /**
     * @param entities
     * @param names
     * @param em
     * @param target
     * @param builder
     * @return
     * @throws FileshareException
     */
    private List<VFSEntity> getEntitiesToMove ( VFSContext v, List<EntityKey> entities, Map<EntityKey, String> names, VFSContainerEntity target,
            MoveEntityFileshareAuditBuilder builder ) throws FileshareException {
        List<VFSEntity> toMove = new LinkedList<>();
        for ( EntityKey entityId : entities ) {
            VFSEntity entity = v.load(entityId);
            this.accessControl.checkAccess(v, entity, GrantPermission.EDIT);
            VFSContainerEntity oldParent = v.getParent(entity);
            this.accessControl.checkAccess(v, oldParent, GrantPermission.EDIT);

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Moving %s to %s", entity, target)); //$NON-NLS-1$
            }

            if ( oldParent == null ) {
                throw new StructureException("Cannot move root directory"); //$NON-NLS-1$
            }
            checkMoveTarget(v, entity, target);

            String newName = names.get(entity.getEntityKey());
            if ( newName != null ) {
                ServiceUtil.checkFileName(newName);
                ServiceUtil.checkLocalNameConflict(v, newName, target);
            }
            else {
                ServiceUtil.checkLocalNameConflict(v, entity.getLocalName(), target);

            }
            builder.source(entity, oldParent, newName);

            toMove.add(entity);
        }
        return toMove;
    }


    /**
     * @param entity
     * @param owner
     */
    private void recursiveSetOwner ( VFSContext v, VFSEntity entity, Subject owner ) {
        entity.setOwner(owner);
        v.save(entity);
        if ( entity instanceof ContainerEntity ) {
            for ( ContentEntity child : ( (ContainerEntity) entity ).getElements() ) {
                recursiveSetOwner(v, child, owner);
            }
        }
    }


    /**
     * @param entity
     * @param target
     * @throws FileshareException
     */
    private static void checkMoveTarget ( VFSContext v, VFSEntity entity, VFSContainerEntity target ) throws FileshareException {
        VFSContainerEntity parent = target;
        while ( parent != null ) {
            if ( parent.equals(entity) ) {
                throw new CyclicMoveTargetException("Cannot move a directory to one of its subdirectories"); //$NON-NLS-1$
            }
            parent = v.getParent(parent);
        }
    }


    @Override
    public void setMimeType ( EntityKey entityId, String mimeType ) throws FileshareException {

        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.SET_MIMETYPE_ACTION);
            audit.builder().property("requestedMimeType", mimeType); //$NON-NLS-1$
            MediaType normalized;
            try {
                this.accessControl.checkPermission("entity:changeMimeType"); //$NON-NLS-1$

                if ( !this.ctx.getConfigurationProvider().getMimeTypePolicyConfiguration().isAllowMimeTypeChanges() ) {
                    throw new AccessDeniedException();
                }

                this.ctx.getConfigurationProvider().getMimeTypePolicyConfiguration().checkMimeType(mimeType, false);

                MediaType parsed = MediaType.parse(mimeType);
                if ( parsed == null ) {
                    throw new DisallowedMimeTypeException(mimeType, "Failed to parse media type"); //$NON-NLS-1$
                }
                normalized = MediaTypeRegistry.getDefaultRegistry().normalize(parsed);
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }

            audit.builder().property("normalizedMimeType", normalized.toString()); //$NON-NLS-1$

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                  VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
                VFSFileEntity entity = v.load(entityId, VFSFileEntity.class);
                audit.builder().entity(entity);
                audit.builder().parentEntity(v.getParent(entity));
                User currentUser = this.accessControl.getCurrentUser(tx);
                TokenGrant grant = this.accessControl.getTokenAuthGrant(v, entity);

                audit.builder().grant(grant);

                this.policyEvaluator.checkPolicy(v, entity, WebUtils.getHttpRequest(SecurityUtils.getSubject()));

                if ( !this.accessControl.hasAccess(v, entity, GrantPermission.EDIT) ) {
                    this.accessControl.checkUserIsCreatorWithPerm(v, entity, currentUser, grant, GrantPermission.EDIT_SELF);
                }

                if ( entity == null ) {
                    throw new EntityNotFoundException();
                }

                audit.builder().property("oldMimeType", mimeType); //$NON-NLS-1$

                entity.setContentType(normalized.toString());

                ServiceUtil.updateLastModified(this.modTracker, currentUser, DateTime.now(), v, entity, grant);

                if ( normalized.hasParameters() && !StringUtils.isBlank(normalized.getParameters().get(CHARSET)) ) {
                    entity.setContentEncoding(normalized.getParameters().get(CHARSET));
                }

                v.save(entity);
                v.commit();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set mime type", e); //$NON-NLS-1$
            }

        }
    }


    @Override
    public void setExpirationDate ( EntityKey id, DateTime expires ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            if ( expires != null ) {
                audit.builder().access(this.accessControl).action(EntityFileshareEvent.SET_EXPIRY_ACTION);
                audit.builder().property("requestedExpires", expires.toString()); //$NON-NLS-1$
            }
            else {
                audit.builder().access(this.accessControl).action(EntityFileshareEvent.UNSET_EXPIRY_ACTION);
            }

            try {
                this.accessControl.checkPermission("entity:changeExpirationDate"); //$NON-NLS-1$

                if ( expires != null && expires.isBeforeNow() ) {
                    throw new EntityExpirationInvalidException("Cannot set an expiration date in the past"); //$NON-NLS-1$
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                  VFSContext v = this.vfs.getVFS(id).begin(tx) ) {
                User currentUser = this.accessControl.getCurrentUser(tx);
                VFSFileEntity entity = v.load(id, VFSFileEntity.class);
                TokenGrant grant = this.accessControl.getTokenAuthGrant(v, entity);

                audit.builder().entity(entity);
                audit.builder().grant(grant);

                this.policyEvaluator.checkPolicy(v, entity, WebUtils.getHttpRequest(SecurityUtils.getSubject()));

                if ( !this.accessControl.hasAccess(v, entity, GrantPermission.EDIT) ) {
                    this.accessControl.checkUserIsCreatorWithPerm(v, entity, currentUser, grant, GrantPermission.EDIT_SELF);
                }

                if ( entity == null ) {
                    throw new EntityNotFoundException();
                }

                PolicyConfiguration policy = this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration()
                        .getPolicy(entity.getSecurityLabel().getLabel());

                if ( expires == null && policy.getMaximumExpirationDuration() != null ) {
                    throw new EntityExpirationInvalidException("Trying to unset expiration but a maximum is set"); //$NON-NLS-1$
                }

                if ( expires != null && policy.getMaximumExpirationDuration() != null ) {
                    DateTime maxExpiry = entity.getCreated().plus(policy.getMaximumExpirationDuration()).withTime(0, 0, 0, 0);
                    if ( expires.isAfter(maxExpiry) ) {
                        throw new EntityExpirationInvalidException("Trying to set a expiration date after the maximum allowed"); //$NON-NLS-1$
                    }
                }
                entity.setExpires(expires);
                ServiceUtil.updateLastModified(this.modTracker, currentUser, DateTime.now(), v, entity, grant);
                v.save(entity);
                v.commit();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set expiration date", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void setSecurityLabel ( EntityKey entityId, String label, boolean raise ) throws FileshareException {

        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.SET_SECURITY_LABEL_ACTION);
            audit.builder().property("newLabel", label); //$NON-NLS-1$

            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("entity:changeSecurityLabel"); //$NON-NLS-1$
                }

                if ( !this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().getDefinedLabels().contains(label) ) {
                    throw new InvalidSecurityLabelException(label, "Label is unknown"); //$NON-NLS-1$
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                  VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
                User currentUser = this.accessControl.getCurrentUser(tx);
                VFSEntity entity = v.load(entityId);
                TokenGrant grant = this.accessControl.getTokenAuthGrant(v, entity);

                audit.builder().entity(entity);
                audit.builder().grant(grant);

                if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission("manage:subjects:subjectRootSecurityLabel") ) { //$NON-NLS-1$
                    this.policyEvaluator.checkPolicy(v, entity, WebUtils.getHttpRequest(SecurityUtils.getSubject()));

                    if ( !this.accessControl.hasAccess(v, entity, GrantPermission.EDIT) ) {
                        this.accessControl.checkUserIsCreatorWithPerm(v, entity, currentUser, grant, GrantPermission.EDIT_SELF);
                    }
                }

                audit.builder().property("oldLabel", entity.getSecurityLabel() != null ? entity.getSecurityLabel().toString() : null); //$NON-NLS-1$

                SecurityLabel newLabel = ServiceUtil.getOrCreateSecurityLabel(tx, label);

                VFSContainerEntity parent = v.getParent(entity);
                audit.builder().parentEntity(parent);
                if ( parent != null && this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration()
                        .compareLabels(newLabel, parent.getSecurityLabel()) < 0 ) {
                    throw new InvalidSecurityLabelException(newLabel.getLabel(), "Label cannot be lower than the parent's"); //$NON-NLS-1$
                }

                if ( entity instanceof VFSContainerEntity ) {
                    Map<String, Collection<EntityKey>> invalidLabels = new HashMap<>();
                    recursiveEnsureLabelCompliance(v, currentUser, grant, entity, newLabel, invalidLabels, true, raise);
                    if ( !invalidLabels.isEmpty() ) {
                        InconsistentSecurityLabelException ex = new InconsistentSecurityLabelException(
                            newLabel.getLabel(),
                            "Must not contain elements with lower security labels"); //$NON-NLS-1$
                        ex.setBlockers(invalidLabels);
                        throw ex;
                    }
                }

                entity.setSecurityLabel(newLabel);
                ServiceUtil.updateLastModified(this.modTracker, currentUser, DateTime.now(), v, entity, grant);
                v.save(entity);
                v.commit();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set security label", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param entity
     * @param newLabel
     * @param raise
     * @throws FileshareException
     */
    private void recursiveEnsureLabelCompliance ( VFSContext v, User currentUser, Grant grant, VFSEntity entity, SecurityLabel newLabel,
            Map<String, Collection<EntityKey>> invalidLabels, boolean root, boolean raise ) throws FileshareException {
        if ( !root && this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().compareLabels(newLabel, entity.getSecurityLabel()) > 0 ) {

            if ( raise ) {
                entity.setSecurityLabel(newLabel);
                ServiceUtil.updateLastModified(this.modTracker, currentUser, DateTime.now(), v, entity, grant);
                v.save(entity);
            }
            else {
                String label = entity.getSecurityLabel().getLabel();
                Collection<EntityKey> invalid = invalidLabels.get(label);

                if ( invalid == null ) {
                    invalid = new LinkedList<>();
                    invalidLabels.put(label, invalid);
                }

                invalid.add(entity.getEntityKey());
            }
        }

        if ( entity instanceof VFSContainerEntity ) {
            VFSContainerEntity c = (VFSContainerEntity) entity;
            for ( VFSEntity child : v.getChildren(c) ) {
                recursiveEnsureLabelCompliance(v, currentUser, grant, child, newLabel, invalidLabels, false, raise);
            }
        }
    }


    @Override
    public void setSecurityLabelRecursive ( EntityKey id, String label, boolean force, Set<EntityKey> allowLower ) throws FileshareException {

        try ( AuditContext<MultiEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(MultiEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.SET_SECURITY_LABEL_RECURSIVE_ACTION);
            audit.builder().property("newLabel", label); //$NON-NLS-1$
            audit.builder().property("force", force); //$NON-NLS-1$
            audit.builder().property("allowLower", (Serializable) allowLower); //$NON-NLS-1$

            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("entity:changeSecurityLabel"); //$NON-NLS-1$
                }

                if ( !this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().getDefinedLabels().contains(label) ) {
                    throw new InvalidSecurityLabelException(label, "Label is unknown"); //$NON-NLS-1$
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                  VFSContext v = this.vfs.getVFS(id).begin(tx) ) {
                VFSEntity rootEntity = v.load(id);
                User currentUser = this.accessControl.getCurrentUser(tx);
                TokenGrant grant = this.accessControl.getTokenAuthGrant(v, rootEntity);

                audit.builder().entity(rootEntity);
                audit.builder().grant(grant);

                if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission("manage:subjects:subjectRootSecurityLabel") ) { //$NON-NLS-1$
                    this.policyEvaluator.checkPolicy(v, rootEntity, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
                    this.accessControl.checkOwner(v, rootEntity);
                }
                SecurityLabel newLabel = ServiceUtil.getOrCreateSecurityLabel(tx, label);

                VFSContainerEntity parent = v.getParent(rootEntity);
                if ( parent != null && this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration()
                        .compareLabels(newLabel, parent.getSecurityLabel()) < 0 ) {
                    throw new InvalidSecurityLabelException(newLabel.getLabel(), "Label cannot be lower than the parent's"); //$NON-NLS-1$
                }

                audit.builder().parentEntity(parent);

                recursiveSetLabelInternal(
                    v,
                    rootEntity,
                    newLabel,
                    DateTime.now(),
                    currentUser,
                    grant,
                    audit.builder(),
                    true,
                    force,
                    allowLower,
                    null);

                v.save();
                v.commit();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set security label recursively", e); //$NON-NLS-1$
            }
        }

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.EntityService#getChildrenSecurityLabels(eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public List<VFSEntity> getChildrenSecurityLabels ( EntityKey root ) throws FileshareException {

        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission("entity:changeSecurityLabel"); //$NON-NLS-1$
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getVFS(root).begin(tx) ) {
            VFSEntity rootEntity = v.load(root);

            if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission("manage:subjects:subjectRootSecurityLabel") ) { //$NON-NLS-1$
                this.policyEvaluator.checkPolicy(v, rootEntity, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
                this.accessControl.checkOwner(v, rootEntity);
            }

            if ( ! ( rootEntity instanceof VFSContainerEntity ) ) {
                throw new UnsupportedOperationException();
            }

            List<VFSEntity> inconsistencies = new LinkedList<>();
            collectInconsitencies(v, (VFSContainerEntity) rootEntity, rootEntity.getSecurityLabel(), inconsistencies, 1);
            tx.commit();
            return inconsistencies;
        }
        catch (
            EntityTransactionException |
            PersistenceException e ) {
            throw new FileshareException("Failed to get child security labels", e); //$NON-NLS-1$
        }

    }


    /**
     * @param e
     * @param reference
     * @param inconsistencies
     * @throws FileshareException
     */
    private void collectInconsitencies ( VFSContext v, VFSContainerEntity e, SecurityLabel reference, List<VFSEntity> inconsistencies,
            int explicitLevels ) throws FileshareException {

        for ( VFSEntity child : v.getChildren(e) ) {
            SecurityLabel label = child.getSecurityLabel();
            if ( explicitLevels > 0 || !Objects.equals(label, reference) ) {
                inconsistencies.add(child.cloneShallow(false));
            }

            if ( child instanceof VFSContainerEntity ) {
                VFSContainerEntity c = (VFSContainerEntity) child;
                collectInconsitencies(v, c, label, inconsistencies, explicitLevels - 1);
            }
        }
    }


    /**
     * @param entity
     * @param currentUser
     * @param grant
     * @param builder
     * @param allowLower
     * @param force
     * @throws FileshareException
     */
    private void recursiveSetLabelInternal ( VFSContext v, VFSEntity entity, SecurityLabel newLabel, DateTime now, User currentUser, Grant grant,
            MultiEntityFileshareAuditBuilder builder, boolean root, boolean force, Set<EntityKey> allowLower, SecurityLabel contextLabel )
            throws FileshareException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Setting label %s on %s", newLabel, entity)); //$NON-NLS-1$
        }

        builder.entity(entity);
        if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission("manage:subjects:userRootSecurityLabel") ) { //$NON-NLS-1$
            this.policyEvaluator.checkPolicy(v, entity, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
        }

        SecurityLabel recurseOn = contextLabel;
        SecurityLabel oldLabel = entity.getSecurityLabel();

        if ( !root && !force && ( recurseOn == null || !recurseOn.equals(oldLabel) ) && !allowLower.contains(entity.getEntityKey()) ) {
            int res = this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().compareLabels(newLabel, oldLabel);

            if ( res < 0 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(
                        String.format("Not setting label on %s old %s new %s (recurse %s)", entity.getLocalName(), oldLabel, newLabel, recurseOn)); //$NON-NLS-1$
                }
                return;
            }
        }
        else if ( !root && !force && allowLower.contains(entity.getEntityKey()) ) {
            recurseOn = oldLabel;
        }

        entity.setSecurityLabel(newLabel);
        ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, entity, grant);
        v.saveNoFlush(entity);

        if ( entity instanceof VFSContainerEntity ) {
            VFSContainerEntity c = (VFSContainerEntity) entity;
            for ( VFSEntity child : v.getChildren(c) ) {
                recursiveSetLabelInternal(v, child, newLabel, now, currentUser, grant, builder, false, force, allowLower, recurseOn);
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.EntityService#getPolicyViolation(java.lang.String, javax.servlet.ServletRequest)
     */
    @Override
    public PolicyViolation getPolicyViolation ( String label, ServletRequest req ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            PolicyViolation violation = this.policyEvaluator.isPolicyFulfilled(ServiceUtil.getOrCreateSecurityLabel(tx, label), req);
            tx.commit();
            return violation;
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    @Override
    public void delete ( EntityKey entityId ) throws FileshareException {
        this.delete(Arrays.asList(entityId));
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.EntityService#delete(java.util.Collection)
     */
    @Override
    public void delete ( Collection<EntityKey> entities ) throws FileshareException {
        try ( AuditContext<MultiEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(MultiEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.DELETE_OUTER_ACTION);
            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                  VFSContext v = this.vfs.getVFS(entities).begin(tx) ) {
                User currentUser = this.accessControl.getCurrentUser(tx);
                doDelete(tx, v, entities, currentUser, audit.builder());
                v.commit();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to delete entities", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param entities
     * @param em
     * @param builder
     * @throws FileshareException
     */
    private void doDelete ( EntityTransactionContext tx, VFSContext v, Collection<EntityKey> entities, User currentUser,
            MultiEntityFileshareAuditBuilder builder ) throws FileshareException {

        Set<VFSEntity> remove = new HashSet<>();

        for ( EntityKey entityId : entities ) {
            VFSEntity entity = v.load(entityId);
            Grant grant = this.accessControl.getTokenAuthGrant(v, entity);

            if ( !this.accessControl.hasAccess(v, entity, GrantPermission.EDIT) ) {
                this.accessControl.checkUserIsCreatorWithPerm(v, entity, currentUser, grant, GrantPermission.EDIT_SELF);
            }

            builder.entity(entity);

            if ( entity == null ) {
                throw new EntityNotFoundException("Could not find entity " + entityId); //$NON-NLS-1$
            }

            VFSContainerEntity parent = v.getParent(entity);

            if ( parent == null ) {
                throw new StructureException("Cannot remove root directory"); //$NON-NLS-1$
            }

            if ( !this.accessControl.hasAccess(v, parent, GrantPermission.EDIT) ) {
                this.accessControl.checkUserIsCreatorWithPerm(v, parent, currentUser, grant, GrantPermission.EDIT_SELF);
            }

            // deletion is only allowed if one of the following is true:
            // - the user is the owner
            // - the user has the edit permission on the parent
            // - the user has the upload permission and was the creator of the file
            if ( !this.accessControl.isOwner(v, entity) && !this.accessControl.hasAccess(v, entity, GrantPermission.EDIT) ) {
                this.accessControl.checkUserIsCreatorWithPermRecursive(v, entity, currentUser, grant, GrantPermission.EDIT_SELF);
            }

            addEntityToRemove(v, remove, entity);
        }

        doDelete(tx, v, currentUser, remove, false);
    }


    /**
     * @param remove
     * @param entity
     * @throws FileshareException
     */
    private void addEntityToRemove ( VFSContext v, Collection<VFSEntity> remove, VFSEntity entity ) throws FileshareException {
        if ( entity instanceof VFSContainerEntity ) {
            for ( VFSEntity e : v.getChildren((VFSContainerEntity) entity) ) {
                addEntityToRemove(v, remove, e);
            }
        }

        ContentEntity mapped = v.findMappedEntity(entity);
        if ( mapped != null ) {
            remove.add(mapped);
        }
    }


    @Override
    public void doDelete ( EntityTransactionContext tx, VFSContext v, User currentUser, Set<? extends VFSEntity> remove, boolean expired )
            throws FileshareException {

        Map<VFSContainerEntity, Long> perParentSize = new HashMap<>();
        getPerParentSizes(v, remove, perParentSize);
        Set<FileEntity> toRemoveContents = new HashSet<>();
        try {

            for ( Entry<VFSContainerEntity, Long> toReserve : perParentSize.entrySet() ) {
                this.quota.checkAndReserve(v, toReserve.getKey(), -toReserve.getValue(), 0);
            }

            DateTime now = DateTime.now();
            for ( VFSEntity entity : remove ) {
                doDeleteEntity(tx, v, currentUser, perParentSize, remove, toRemoveContents, now, entity, expired);
            }
            v.save();
        }
        catch ( Exception e ) {
            for ( Entry<VFSContainerEntity, Long> toRevert : perParentSize.entrySet() ) {
                this.quota.undoReservation(v.getQuotaKey(toRevert.getKey()), -1 * toRevert.getValue());
            }
            throw e;
        }

        for ( FileEntity toRemove : toRemoveContents ) {
            try {
                this.blockStore.removeContents(toRemove);
            }
            catch ( StorageException e ) {
                log.error("Failed to remove file contents", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param em
     * @param currentUser
     * @param perParentSize
     * @param remove
     * @param toRemoveContents
     * @param now
     * @param entity
     * @throws FileshareException
     */
    private void doDeleteEntity ( EntityTransactionContext tx, VFSContext v, User currentUser, Map<VFSContainerEntity, Long> perParentSize,
            Set<? extends VFSEntity> remove, Set<FileEntity> toRemoveContents, DateTime now, VFSEntity entity, boolean expired )
            throws FileshareException {
        if ( log.isDebugEnabled() ) {
            log.debug("Deleting " + entity); //$NON-NLS-1$
        }

        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(expired ? EntityFileshareEvent.EXPIRE_ACTION : EntityFileshareEvent.DELETE_ACTION);
            audit.builder().entity(entity);
            VFSContainerEntity parent = v.getParent(entity);
            audit.builder().parentEntity(parent);

            try {
                v.removeChild(parent, entity);
                TokenGrant grant = this.accessControl.getTokenAuthGrant(v, entity);
                ServiceUtil.updateLastModified(this.modTracker, currentUser, now, v, parent, grant);

                ContentEntity mapped = v.findMappedEntity(entity);

                if ( mapped != null ) {
                    dumpGrants(audit.builder(), mapped.getGrants());
                    if ( mapped instanceof FileEntity ) {
                        toRemoveContents.add((FileEntity) entity);
                    }
                }

                if ( !remove.contains(parent) ) {
                    v.trackChange(parent, entity, ChangeType.REMOVAL, now);
                    v.saveNoFlush(parent);
                }
                v.delete(entity);
                long combinedSize = this.quota.getCombinedSize(v, entity);
                if ( mapped != null ) {
                    v.removeMapped(mapped);
                }
                this.quota.commit(v, parent, -1 * combinedSize);
                perParentSize.put(parent, perParentSize.get(parent) - combinedSize);
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }
        }
    }


    /**
     * @param builder
     * @param grants
     */
    private static void dumpGrants ( SingleEntityFileshareAuditBuilder builder, Set<Grant> grants ) {
        LinkedList<String> grantInfo = new LinkedList<>();
        for ( Grant g : grants ) {
            StringBuilder info = new StringBuilder();

            if ( g instanceof SubjectGrant ) {
                Subject target = ( (SubjectGrant) g ).getTarget();
                if ( target instanceof User ) {
                    info.append("user:"); //$NON-NLS-1$
                    info.append( ( (User) target ).getPrincipal());
                }
                else if ( target instanceof Group ) {
                    info.append("group:"); //$NON-NLS-1$
                    info.append( ( (Group) target ).getName());
                }
                else {
                    throw new IllegalArgumentException();
                }

                info.append(":"); //$NON-NLS-1$
                info.append(target.getId());
            }
            else if ( g instanceof MailGrant ) {
                info.append("mail:"); //$NON-NLS-1$
                info.append( ( (MailGrant) g ).getMailAddress());
            }
            else if ( g instanceof TokenGrant ) {
                info.append("token:"); //$NON-NLS-1$
                info.append( ( (TokenGrant) g ).getIdentifier());
            }
            else {
                throw new IllegalArgumentException();
            }

            info.append('(');
            info.append(g.getId());
            info.append(')');

            info.append(" perms: "); //$NON-NLS-1$
            info.append(String.valueOf(GrantPermission.toInt(g.getPermissions())));
            grantInfo.add(info.toString());
        }
        builder.property("grants", grantInfo); //$NON-NLS-1$
    }


    /**
     * @param entities
     * @param perParentSize
     * @return
     * @throws FileshareException
     */
    private long getPerParentSizes ( VFSContext v, Collection<? extends VFSEntity> entities, Map<VFSContainerEntity, Long> perParentSize )
            throws FileshareException {
        long totalSize = 0;
        for ( VFSEntity e : entities ) {
            long combinedSize = this.quota.getCombinedSize(v, e);
            VFSContainerEntity parent = v.getParent(e);
            Long oldSize = perParentSize.get(parent);
            if ( oldSize == null ) {
                oldSize = 0L;
            }
            perParentSize.put(parent, oldSize + combinedSize);
            totalSize += combinedSize;
        }
        return totalSize;
    }


    @Override
    public FileEntity checkNameConflict ( EntityKey containerId, String filename ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getVFS(containerId).begin(tx) ) {
            VFSContainerEntity container = v.load(containerId, VFSContainerEntity.class);
            User currentUser = this.accessControl.getCurrentUser(tx);
            Grant g = this.accessControl.getTokenAuthGrant(v, container);

            Map<String, VFSEntity> elementNames;
            if ( !this.accessControl.hasAccess(v, container, GrantPermission.BROWSE) ) {
                elementNames = getDirectoryElementNamesWithCreator(v, container, currentUser, g);
            }
            else {
                elementNames = ServiceUtil.getDirectoryElementNames(v, container);
            }

            tx.commit();

            VFSEntity conflict = elementNames.get(filename);

            if ( conflict != null && ( this.accessControl.hasAccess(v, conflict, GrantPermission.EDIT)
                    || this.accessControl.isUserCreatorWithPerm(v, conflict, currentUser, g, GrantPermission.EDIT_SELF) ) ) {
                return (FileEntity) conflict;
            }

            return null;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to check name conflict", e); //$NON-NLS-1$
        }
    }


    @Override
    public VFSEntity rename ( EntityKey entityId, String newName ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.RENAME_ACTION);
            audit.builder().property("newName", newName); //$NON-NLS-1$
            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                  VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
                User currentUser = this.accessControl.getCurrentUser(tx);
                VFSEntity entity = v.load(entityId);
                TokenGrant grant = this.accessControl.getTokenAuthGrant(v, entity);
                VFSContainerEntity parent = v.getParent(entity);
                audit.builder().entity(entity);
                audit.builder().parentEntity(parent);
                audit.builder().grant(grant);

                this.policyEvaluator.checkPolicy(v, entity, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
                if ( !this.accessControl.hasAccess(v, entity, GrantPermission.EDIT) ) {
                    this.accessControl.checkUserIsCreatorWithPerm(v, entity, currentUser, grant, GrantPermission.EDIT_SELF);
                }
                if ( parent == null ) {
                    throw new InvalidEntityException("Cannot rename root"); //$NON-NLS-1$
                }

                if ( !this.accessControl.hasAccess(v, parent, GrantPermission.EDIT) ) {
                    this.accessControl.checkUserIsCreatorWithPerm(v, parent, currentUser, grant, GrantPermission.EDIT_SELF);
                }

                ServiceUtil.checkLocalNameConflict(v, newName, parent);
                ServiceUtil.checkFileName(newName);
                v.trackChange(parent, entity, ChangeType.RENAME);
                entity.setLocalName(newName);
                ServiceUtil.updateLastModified(this.modTracker, currentUser, DateTime.now(), v, entity, grant);
                v.save(entity);
                v.commit();
                tx.commit();
                return entity;
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to rename entity", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.service.EntityService#checkWriteAccess(eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public void checkWriteAccess ( EntityKey target ) throws FileshareException {
        if ( target == null ) {
            throw new EntityNotFoundException();
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly();
              VFSContext v = this.vfs.getVFS(target).begin(tx) ) {
            VFSEntity persistent = v.load(target);

            if ( persistent == null ) {
                throw new EntityNotFoundException();
            }

            if ( persistent instanceof VFSFileEntity ) {
                VFSContainerEntity parent = v.getParent(persistent);
                User currentUser = this.accessControl.getCurrentUser(tx);
                TokenGrant grant = this.accessControl.getTokenAuthGrant(v, persistent);

                if ( !parent.getAllowFileOverwrite() ) {
                    throw new AccessDeniedException("You are not allowed to overwrite this file"); //$NON-NLS-1$
                }

                if ( !this.accessControl.hasAccess(v, persistent, GrantPermission.EDIT) ) {
                    this.accessControl.checkUserIsCreatorWithPerm(v, persistent, currentUser, grant, GrantPermission.EDIT_SELF);
                }
            }
            else {
                this.accessControl.checkAccess(v, persistent, GrantPermission.UPLOAD);
            }
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to check write access", e); //$NON-NLS-1$
        }
    }


    /**
     * @param container
     * @param currentUser
     * @param g
     * @return
     * @throws FileshareException
     */
    private Map<String, VFSEntity> getDirectoryElementNamesWithCreator ( VFSContext v, VFSContainerEntity target, User currentUser, Grant g )
            throws FileshareException {
        Map<String, VFSEntity> elementNames = new HashMap<>();

        for ( VFSEntity e : v.getChildren(target) ) {
            if ( this.accessControl.isUserCreatorWithPerm(v, e, currentUser, g) ) {
                elementNames.put(e.getLocalName(), e);
            }
        }

        return elementNames;
    }

}
