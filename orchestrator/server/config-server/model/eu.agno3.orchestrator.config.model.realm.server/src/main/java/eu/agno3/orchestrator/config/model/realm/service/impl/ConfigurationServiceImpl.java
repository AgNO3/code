/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectConflictFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.db.versioning.RevisionEntity;
import eu.agno3.orchestrator.config.model.base.versioning.RevisionType;
import eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeTreeNode;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;
import eu.agno3.orchestrator.config.model.realm.server.service.ConfigurationServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.ModelObjectValidationUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.ObjectReferenceUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.RandomUUIDSettingsWalker;
import eu.agno3.orchestrator.config.model.realm.server.util.RecursiveReferenceVisitor;
import eu.agno3.orchestrator.config.model.realm.server.util.ReferenceWalker;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationServiceDescriptor;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ConfigurationService.class, ConfigurationServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.ConfigurationService",
    targetNamespace = ConfigurationServiceDescriptor.NAMESPACE,
    serviceName = ConfigurationServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/config" )
public class ConfigurationServiceImpl implements ConfigurationService, ConfigurationServerService, SOAPWebService {

    private static final Logger log = Logger.getLogger(ConfigurationServiceImpl.class);

    private DefaultServerServiceContext sctx;
    private Optional<@NonNull PersistenceUtil> persistenceUtil = Optional.empty();
    private ModelObjectValidationUtil objectValidator;

    private ObjectAccessControl authz;


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setPersistenceUtil ( @NonNull PersistenceUtil pu ) {
        this.persistenceUtil = Optional.of(pu);
    }


    protected synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil.equals(pu) ) {
            this.persistenceUtil = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setObjectValidator ( ModelObjectValidationUtil val ) {
        this.objectValidator = val;
    }


    protected synchronized void unsetObjectValidator ( ModelObjectValidationUtil val ) {
        if ( this.objectValidator == val ) {
            this.objectValidator = null;
        }
    }


    @Reference
    protected synchronized void setObjectAccessControl ( ObjectAccessControl oac ) {
        this.authz = oac;
    }


    protected synchronized void unsetObjectAccessControl ( ObjectAccessControl oac ) {
        if ( this.authz == oac ) {
            this.authz = null;
        }
    }


    private static ModelServiceException illegalParameters () {
        return new ModelServiceException("Illegal parameters"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#refresh(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    @RequirePermissions ( "config:view:refresh" )
    public <T extends ConfigurationObject> T refresh ( T refresh ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( refresh == null ) {
            throw illegalParameters();
        }

        EntityManager em = this.sctx.createConfigEM();
        try {
            AbstractConfigurationObject<T> persistent = this.persistenceUtil.get().fetch(em, refresh);
            this.authz.checkAccess(persistent.getAnchor(), "config:view"); //$NON-NLS-1$
            return (T) this.persistenceUtil.get().setRevisions(em, persistent, PersistenceUtil.getMostRecentRevision(em));
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to refresh object", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#fetchById(java.util.UUID)
     */
    @Override
    @RequirePermissions ( "config:view:byId" )
    public ConfigurationObject fetchById ( UUID id ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( id == null ) {
            throw illegalParameters();
        }

        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        if ( em == null ) {
            throw new ModelServiceException();
        }
        try {
            AbstractConfigurationObject<?> persistent = PersistenceUtil.fetch(em, AbstractConfigurationObject.class, id);
            this.authz.checkAccess(persistent.getAnchor(), "config:view"); //$NON-NLS-1$
            @NonNull
            PersistenceUtil pu = this.persistenceUtil.get();
            @NonNull
            AbstractConfigurationObject<?> cfg = pu.setRevisions(em, persistent, PersistenceUtil.getMostRecentRevision(em));
            ObjectReferenceUtil.fillInMissing(em, pu, cfg, this.sctx.getObjectTypeRegistry());
            return cfg;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch object", e); //$NON-NLS-1$
        }

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#create(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo)
     */
    @Override
    @RequirePermissions ( "config:createTemplate" )
    public <T extends ConfigurationObject> T create ( StructuralObject anchor, T create, @Nullable ConfigUpdateInfo info )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectValidationException {
        if ( anchor == null || create == null ) {
            throw illegalParameters();
        }

        EntityManager entityManager = getEntityManager();
        AbstractStructuralObjectImpl persistentAnchor = this.persistenceUtil.get()
                .fetch(entityManager, AbstractStructuralObjectImpl.class, anchor, "Failed to fetch anchor"); //$NON-NLS-1$

        this.authz.checkAccess(persistentAnchor, "config:createTemplate"); //$NON-NLS-1$

        return create(entityManager, persistentAnchor, create, info);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ConfigurationServerService#create(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public @NonNull <T extends ConfigurationObject> T create ( @NonNull EntityManager em, @NonNull AbstractStructuralObjectImpl anchor,
            @Nullable T create, @Nullable ConfigUpdateInfo info )
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException {
        if ( create == null ) {
            throw illegalParameters();
        }

        AuditReader ar = AuditReaderFactory.get(em);
        // javadoc says to expect a new API for this in 6
        @SuppressWarnings ( "deprecation" )
        RevisionEntity revision = ar.getCurrentRevision(RevisionEntity.class, true);
        revision.setOverrideRevisionType(RevisionType.ADD);

        AbstractConfigurationObject<T> persistentObj = (AbstractConfigurationObject<T>) create;
        try {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Creating configuration %s at anchor %s", persistentObj, anchor)); //$NON-NLS-1$
            }

            @NonNull
            PersistenceUtil pu = this.persistenceUtil.get();
            ObjectReferenceUtil.prepareObjectForPersist(em, pu, anchor, persistentObj);
            em.persist(persistentObj);
            em.persist(anchor);
            em.flush();
            em.refresh(persistentObj);
            em.refresh(anchor);
            doValidateObject(em, persistentObj, Collections.EMPTY_MAP);
            pu.setRevisions(em, persistentObj, revision.getRevision());
            if ( log.isDebugEnabled() ) {
                log.debug("Created object " + create); //$NON-NLS-1$
            }
            return (T) PersistenceUtil.unproxyDeep(persistentObj);
        }
        catch ( PersistenceException e ) {
            log.warn("Failed to create configuration", e); //$NON-NLS-1$
            throw new ModelServiceException("Failed to persist configuration object", e); //$NON-NLS-1$
        }
    }


    private @NonNull EntityManager getEntityManager () throws ModelServiceException {
        return this.sctx.createConfigEM();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelObjectConflictException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#update(eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo)
     */
    @Override
    @RequirePermissions ( "config:modify" )
    public <T extends ConfigurationObject> T update ( T update, ConfigUpdateInfo info )
            throws ModelServiceException, ModelObjectValidationException, ModelObjectNotFoundException, ModelObjectConflictException {

        if ( update == null ) {
            throw illegalParameters();
        }
        EntityManager em = getEntityManager();
        AbstractConfigurationObject<T> persistentObj = this.persistenceUtil.get().fetch(em, update);

        this.authz.checkAccess(persistentObj.getAnchor(), "config:modify"); //$NON-NLS-1$

        return update(em, persistentObj, update, info);

    }


    @SuppressWarnings ( "unchecked" )
    @Override
    public @NonNull <T extends ConfigurationObject> T update ( @NonNull EntityManager em, @NonNull AbstractConfigurationObject<T> persistentObj,
            @Nullable T update, @Nullable ConfigUpdateInfo info )
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException, ModelObjectConflictException {

        if ( update == null ) {
            throw new ModelServiceException();
        }

        AuditReader ar = AuditReaderFactory.get(em);
        // javadoc says to expect a new API for this in 6
        @SuppressWarnings ( "deprecation" )
        RevisionEntity revision = ar.getCurrentRevision(RevisionEntity.class, true);
        revision.setOverrideRevisionType(RevisionType.MODIFY);

        try {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Updating configuration %s", persistentObj)); //$NON-NLS-1$
                log.debug(String.format("Updating with %s", update)); //$NON-NLS-1$
            }
            @NonNull
            PersistenceUtil pu = this.persistenceUtil.get();
            ObjectReferenceUtil.prepareObjectForMerge(em, pu, persistentObj, update);
            em.merge(update);
            em.flush();
            em.refresh(persistentObj);
            doValidateObject(em, persistentObj, Collections.EMPTY_MAP);

            long revNo = revision.getRevision() > 0 ? revision.getRevision() : update.getRevision();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Updated object %s old revision %d new revision %d", //$NON-NLS-1$
                    persistentObj,
                    revNo,
                    update.getRevision()));
            }
            pu.setRevisions(em, persistentObj, revNo);
            return (T) PersistenceUtil.unproxyDeep(persistentObj);
        }
        catch ( PersistenceException e ) {
            log.warn("Failed to update configuration", e); //$NON-NLS-1$
            if ( e instanceof OptimisticLockException || e.getCause() instanceof OptimisticLockException ) {
                throw new ModelObjectConflictException(
                    "Object was concurrently modified", //$NON-NLS-1$
                    new ModelObjectConflictFault(update.getType(), update.getId()),
                    e);
            }

            throw new ModelServiceException("Failed to update configuration object", e); //$NON-NLS-1$
        }
    }


    private <T extends ConfigurationObject> void doValidateObject ( @NonNull EntityManager em, AbstractConfigurationObject<T> persistentObj,
            Map<ServiceStructuralObject, ConfigurationInstance> contextServices ) throws ModelServiceException, ModelObjectValidationException {
        try {
            this.objectValidator.validateObject(em, persistentObj, contextServices);
        }
        catch ( ModelObjectValidationException e ) {
            throw e;
        }
        catch ( ModelObjectException e ) {
            throw new ModelServiceException("Unexpected exception during validation", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#delete(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:delete" )
    public void delete ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectReferentialIntegrityException {

        if ( obj == null ) {
            throw illegalParameters();
        }

        EntityManager em = getEntityManager();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.get().fetch(em, obj);

            this.authz.checkAccess(persistent.getAnchor(), "config:delete"); //$NON-NLS-1$

            if ( !persistent.getUsedBy().isEmpty() ) {
                log.warn("Cannot delete, as object is still referenced"); //$NON-NLS-1$
                throw new ModelObjectReferentialIntegrityException(obj.getType(), obj.getId());
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Deleting object " + persistent); //$NON-NLS-1$
            }
            em.remove(persistent);
            // this seems to be necessary, hibernate won't remove on real transaction commit
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to delete configuration object", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#getAnchor(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:view:anchor" )
    public StructuralObject getAnchor ( ConfigurationObject obj ) throws ModelServiceException, ModelObjectNotFoundException {
        if ( obj == null ) {
            throw illegalParameters();
        }

        EntityManager em = getEntityManager();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.get().fetch(em, obj);
            AbstractStructuralObjectImpl anchor = persistent.getAnchor();

            if ( anchor == null ) {
                throw new ModelServiceException();
            }

            this.authz.checkAccess(anchor, "structure:view"); //$NON-NLS-1$
            return PersistenceUtil.unproxy(anchor);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch anchor", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#getUsedBy(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:view:usedBy" )
    public Set<ConfigurationObject> getUsedBy ( ConfigurationObject obj ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = getEntityManager();

        try {
            Set<ConfigurationObject> usedBy = new HashSet<>();
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.get().fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:view:usedBy"); //$NON-NLS-1$
            for ( ConfigurationObject used : persistent.getUsedBy() ) {
                usedBy.add(new ConfigurationObjectReference(used));
            }
            return usedBy;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch used by information", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#getUses(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:view:uses" )
    public Set<ConfigurationObject> getUses ( ConfigurationObject obj ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = getEntityManager();

        try {
            Set<ConfigurationObject> uses = new HashSet<>();
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.get().fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:view:uses"); //$NON-NLS-1$
            for ( ConfigurationObject used : persistent.getUses() ) {
                uses.add(new ConfigurationObjectReference(used));
            }
            return uses;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch uses information", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#getObjectTypes()
     */
    @Override
    @RequirePermissions ( "config:listObjectTypes" )
    public Set<String> getObjectTypes () throws ModelServiceException {
        return this.sctx.getObjectTypeRegistry().getObjectTypes();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#getObjectTypeTrees()
     */
    @Override
    @RequirePermissions ( "config:listObjectTypes" )
    public List<ObjectTypeTreeNode> getObjectTypeTrees () throws ModelServiceException {
        Set<ConcreteObjectTypeDescriptor<?, ?>> roots = this.sctx.getObjectTypeRegistry().getConcreteByParent(null);
        List<ObjectTypeTreeNode> res = new LinkedList<>();

        List<ConcreteObjectTypeDescriptor<?, ?>> sorted = new ArrayList<>(roots);
        Collections.sort(sorted, new ObjectTypeComparator());

        for ( ConcreteObjectTypeDescriptor<?, ?> root : sorted ) {
            if ( root.isHidden() ) {
                continue;
            }
            if ( log.isDebugEnabled() ) {
                log.debug("adding root type " + root.getObjectTypeName()); //$NON-NLS-1$
            }
            res.add(buildObjectTypeTree(root));
        }

        return res;
    }


    /**
     * @param root
     * @return
     * @throws ModelServiceException
     */
    private ObjectTypeTreeNode buildObjectTypeTree ( ConcreteObjectTypeDescriptor<?, ?> root ) throws ModelServiceException {

        ObjectTypeTreeNode tn = new ObjectTypeTreeNode(root.getObjectTypeName());
        Set<ConcreteObjectTypeDescriptor<?, ?>> children = this.sctx.getObjectTypeRegistry().getConcreteByParent(root.getObjectType());

        if ( children != null ) {
            List<ConcreteObjectTypeDescriptor<?, ?>> sorted = new ArrayList<>(children);
            Collections.sort(sorted, new ObjectTypeComparator());
            for ( ConcreteObjectTypeDescriptor<?, ?> child : sorted ) {
                if ( child.isHidden() ) {
                    continue;
                }
                tn.getChildren().add(buildObjectTypeTree(child));
            }
        }

        return tn;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#getApplicableTypes(java.lang.String)
     */
    @Override
    @RequirePermissions ( "config:listObjectTypes" )
    public Set<String> getApplicableTypes ( String objectType ) throws ModelServiceException {
        Set<String> res = new HashSet<>();
        for ( ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>> desc : this.sctx
                .getObjectTypeRegistry().getApplicableTypes(objectType) ) {
            res.add(desc.getObjectTypeName());
        }
        return res;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigurationService#getEmpty(java.lang.String)
     */
    @Override
    @RequirePermissions ( "config:view:defaults" )
    public ConfigurationObject getEmpty ( @Nullable String objectType ) throws ModelServiceException {
        AbstractConfigurationObject<?> obj = (AbstractConfigurationObject<?>) this.sctx.getObjectTypeRegistry().getConcrete(objectType).newInstance();
        try {
            ReferenceWalker.walk(null, obj, new RecursiveReferenceVisitor(new RandomUUIDSettingsWalker()));
        }
        catch ( ModelObjectException e ) {
            throw new ModelServiceException("Failed to set ids", e); //$NON-NLS-1$
        }
        return obj;

    }

}
