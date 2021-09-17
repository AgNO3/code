/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectConflictFault;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectReferentialIntegrityFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectMutable;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectState;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.service.StructuralObjectServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectServiceDescriptor;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    StructuralObjectService.class, StructuralObjectServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService",
    targetNamespace = StructuralObjectServiceDescriptor.NAMESPACE,
    serviceName = StructuralObjectServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/structure" )
public class StructuralObjectServiceImpl implements StructuralObjectService, StructuralObjectServerService, SOAPWebService {

    private static final String OBJECT_PERM_FORMAT = "structure:%s:%s"; //$NON-NLS-1$
    private static final String STRUCTURE_VIEW = "structure:view"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(StructuralObjectServiceImpl.class);
    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
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
    protected synchronized void setPersistenceUtil ( PersistenceUtil pu ) {
        this.persistenceUtil = pu;
    }


    protected synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil == pu ) {
            this.persistenceUtil = null;
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


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#fetchById(java.util.UUID)
     */
    @Override
    @RequirePermissions ( "structure:view:byId" )
    public StructuralObject fetchById ( UUID id ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( id == null ) {
            throw illegalParameters();
        }

        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        try {
            AbstractStructuralObjectImpl persistent = PersistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, id);
            this.authz.checkAccess(persistent, STRUCTURE_VIEW);
            deriveOverallState(em, persistent);
            return persistent;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch object by id " + id, e); //$NON-NLS-1$
        }
    }


    private static ModelServiceException illegalParameters () {
        return new ModelServiceException("Illegal parameters"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#refresh(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    @RequirePermissions ( "structure:view:refresh" )
    public <T extends StructuralObject> T refresh ( T refresh ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( refresh == null || refresh.getId() == null ) {
            throw illegalParameters();
        }

        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        try {
            AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, refresh);
            this.authz.checkAccess(persistent, STRUCTURE_VIEW);
            deriveOverallState(em, persistent);
            return (T) persistent;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch object", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#getStructureRoot()
     */
    @Override
    @RequirePermissions ( "structure:view:root:GROUP" )
    public StructuralObjectMutable getStructureRoot () throws ModelServiceException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        if ( em == null ) {
            throw new ModelServiceException();
        }
        return getStructureRoot(em);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.StructuralObjectServerService#getStructureRoot(javax.persistence.EntityManager)
     */
    @Override
    public @NonNull StructuralObjectMutable getStructureRoot ( @NonNull EntityManager em ) throws ModelServiceException {
        // TODO: return proper restricted root

        try {
            log.debug("Fetching root"); //$NON-NLS-1$
            Optional<GroupStructuralObjectImpl> root = TreeUtil.findRoot(em, GroupStructuralObjectImpl.class);

            if ( root.isPresent() ) {
                this.authz.checkAccess(root.get(), "structure:view:root:GROUP"); //$NON-NLS-1$
                GroupStructuralObjectImpl presRoot = root.get();
                if ( presRoot == null ) {
                    throw new ModelServiceException();
                }
                deriveOverallState(em, presRoot);
                return presRoot;
            }

            log.debug("Root does not yet exist, creating"); //$NON-NLS-1$
            GroupStructuralObjectImpl newRoot = new GroupStructuralObjectImpl();
            newRoot.setDisplayName("Root"); //$NON-NLS-1$
            TreeUtil.treeInsert(em, AbstractStructuralObjectImpl.class, null, newRoot);
            em.persist(newRoot);
            em.flush();
            em.refresh(newRoot);
            deriveOverallState(em, newRoot);
            return newRoot;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to get structural root", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectValidationException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#create(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:create" )
    public <T extends StructuralObject> T create ( StructuralObject parent, T toCreate ) throws ModelObjectNotFoundException, ModelServiceException,
            ModelObjectConflictException, ModelObjectReferentialIntegrityException, ModelObjectValidationException {
        EntityManager em = this.sctx.createConfigEM();
        this.checkObjectPermission("structure:create", parent); //$NON-NLS-1$

        if ( parent == null ) {
            throw new ModelServiceException();
        }

        return create(em, parent, toCreate, false);
    }


    protected <T extends StructuralObject> void checkObjectPermission ( String method, T object ) throws ModelServiceException {
        if ( this.authz.isDisabled() ) {
            return;
        }
        String type = this.getObjectPermissionType(object);
        String permission = String.format(OBJECT_PERM_FORMAT, method, type);
        SecurityUtils.getSubject().checkPermission(permission);
        this.authz.checkAccess(object, permission);
    }


    protected <T extends StructuralObject> boolean hasObjectPermission ( String method, T object ) throws ModelServiceException {
        if ( this.authz.isDisabled() ) {
            return true;
        }
        String type = this.getObjectPermissionType(object);
        String permission = String.format(OBJECT_PERM_FORMAT, method, type);
        return SecurityUtils.getSubject().isPermitted(permission) && this.authz.hasAccess(object, permission);
    }


    protected <T extends StructuralObject> String getObjectPermissionType ( T object ) throws ModelServiceException {
        if ( object == null ) {
            throw new ModelServiceException("Object is null"); //$NON-NLS-1$
        }
        return object.getType().name();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.StructuralObjectServerService#create(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject, boolean)
     */
    @Override
    public <@NonNull T extends StructuralObject> T create ( @NonNull EntityManager em, @NonNull StructuralObject parent, @Nullable T toCreate,
            boolean skipRequirements ) throws ModelServiceException, ModelObjectConflictException, ModelObjectNotFoundException,
                    ModelObjectReferentialIntegrityException, ModelObjectValidationException {
        return createInternal(em, parent, toCreate, false, skipRequirements);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.StructuralObjectServerService#createWithId(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject, boolean)
     */
    @Override
    public <@NonNull T extends StructuralObject> T createWithId ( @NonNull EntityManager em, @NonNull StructuralObject parent, @Nullable T toCreate,
            boolean skipRequirements ) throws ModelServiceException, ModelObjectConflictException, ModelObjectNotFoundException,
                    ModelObjectReferentialIntegrityException, ModelObjectValidationException {
        return createInternal(em, parent, toCreate, true, skipRequirements);
    }


    /**
     * @param em
     * @param parent
     * @param toCreate
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectConflictException
     * @throws ModelObjectValidationException
     */
    @SuppressWarnings ( "unchecked" )
    private <T extends StructuralObject> T createInternal ( EntityManager em, StructuralObject parent, @Nullable T toCreate, boolean allowSpecifiedId,
            boolean skipRequirements ) throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
                    ModelObjectConflictException, ModelObjectValidationException {
        if ( toCreate == null ) {
            throw new ModelServiceException();
        }

        AbstractStructuralObjectImpl newPersistent = (AbstractStructuralObjectImpl) toCreate;

        try {
            setParent(em, parent, newPersistent);
            checkRequirements(em, parent, newPersistent);
            if ( log.isDebugEnabled() ) {
                log.debug("Creating object " + newPersistent); //$NON-NLS-1$
            }
            if ( allowSpecifiedId ) {
                newPersistent.setVersion(null);
                em.persist(newPersistent);
            }
            else {
                em.persist(newPersistent);
            }
            if ( !skipRequirements ) {
                addRequirements(em, newPersistent, Collections.EMPTY_SET);
            }
            em.flush();
            em.refresh(newPersistent);
            this.validateObject(newPersistent);
            if ( log.isDebugEnabled() ) {
                log.info("Created object " + toCreate); //$NON-NLS-1$
            }
            this.deriveOverallState(em, newPersistent);
            return (T) newPersistent;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to persist object", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.StructuralObjectServerService#createRequirements(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject, java.util.Set)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <@NonNull T extends StructuralObject> T createRequirements ( @NonNull EntityManager em, @NonNull T instance,
            @NonNull Set<@NonNull String> ignoreTypes ) throws ModelObjectNotFoundException, ModelServiceException,
                    ModelObjectReferentialIntegrityException, ModelObjectValidationException {

        @NonNull
        AbstractStructuralObjectImpl pobj = PersistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, instance.getId());
        addRequirements(em, pobj, ignoreTypes);
        em.persist(pobj);
        em.flush();
        em.refresh(pobj);
        this.validateObject(pobj);
        return (T) pobj;
    }


    /**
     * @param em
     * @param newPersistent
     */
    protected void deriveOverallState ( EntityManager em, AbstractStructuralObjectImpl obj ) {
        if ( obj != null ) {
            obj.setOverallState(obj.getPersistentState());
        }
    }


    /**
     * @param em
     * @param parent
     * @param newPersistent
     * @param ignoreTypes
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectNotFoundException
     */
    private void addRequirements ( EntityManager em, AbstractStructuralObjectImpl newPersistent, Set<String> ignoreTypes )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException {

        List<AbstractStructuralObjectImpl> res = new ArrayList<>();
        if ( newPersistent instanceof InstanceStructuralObject ) {
            InstanceStructuralObject instance = (InstanceStructuralObject) newPersistent;

            Set<String> haveServiceTypes = new HashSet<>();
            for ( AbstractStructuralObjectImpl inst : TreeUtil.getChildren(em, AbstractStructuralObjectImpl.class, newPersistent) ) {
                if ( inst instanceof ServiceStructuralObject ) {
                    log.info("Found service " + inst); //$NON-NLS-1$
                    haveServiceTypes.add( ( (ServiceStructuralObject) inst ).getServiceType());
                }
            }

            haveServiceTypes.addAll(ignoreTypes);

            ImageTypeDescriptor descriptor = this.sctx.getImageTypeRegistry().getDescriptor(instance.getImageType());
            for ( String forcedServiceType : descriptor.getForcedServiceTypes() ) {
                if ( !haveServiceTypes.contains(forcedServiceType) ) {
                    log.info("Creating forced service " + forcedServiceType); //$NON-NLS-1$
                    ServiceStructuralObjectImpl forcedService = new ServiceStructuralObjectImpl();
                    forcedService.setServiceType(forcedServiceType);
                    res.add(forcedService);
                }
            }
        }

        if ( !res.isEmpty() ) {
            setParents(em, newPersistent, res);
            for ( AbstractStructuralObjectImpl obj : res ) {
                em.persist(obj);
            }
        }

    }


    /**
     * @param em
     * @param parent
     * @param newPersistent
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectConflictException
     */
    private void checkRequirements ( EntityManager em, StructuralObject parent, AbstractStructuralObjectImpl newPersistent )
            throws ModelServiceException, ModelObjectReferentialIntegrityException, ModelObjectConflictException {

        if ( newPersistent instanceof InstanceStructuralObject ) {
            InstanceStructuralObject inst = (InstanceStructuralObject) newPersistent;
            checkValidImageType(inst);
        }
        else if ( newPersistent instanceof ServiceStructuralObject ) {
            checkValidServiceType(em, parent, newPersistent);
        }
    }


    /**
     * @param parent
     * @param newPersistent
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     */
    private void checkValidServiceType ( EntityManager em, StructuralObject parent, AbstractStructuralObjectImpl newPersistent )
            throws ModelObjectReferentialIntegrityException, ModelServiceException, ModelObjectConflictException {
        ServiceStructuralObjectImpl service = (ServiceStructuralObjectImpl) newPersistent;

        ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> serviceDesc = this.sctx.getServiceTypeRegistry()
                .getDescriptor(service.getServiceType());

        if ( ! ( parent instanceof InstanceStructuralObject ) ) {
            throw new ModelObjectReferentialIntegrityException(
                "Parent must be an instance", //$NON-NLS-1$
                new ModelObjectReferentialIntegrityFault(ServiceStructuralObject.class, service.getId()));
        }
        InstanceStructuralObjectImpl instance = (InstanceStructuralObjectImpl) parent;
        checkImageType(service, instance);
        checkSingletonServices(em, serviceDesc, instance);

    }


    /**
     * @param em
     * @param serviceDesc
     * @param instance
     * @throws ModelObjectConflictException
     */
    private static void checkSingletonServices ( EntityManager em, ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> serviceDesc,
            InstanceStructuralObjectImpl instance ) throws ModelObjectConflictException {
        if ( !serviceDesc.isMultiInstance() ) {
            List<AbstractStructuralObjectImpl> childObjects = TreeUtil.getDirectChildren(em, AbstractStructuralObjectImpl.class, instance);

            for ( AbstractStructuralObjectImpl child : childObjects ) {
                if ( child instanceof ServiceStructuralObject
                        && serviceDesc.getTypeName().equals( ( (ServiceStructuralObject) child ).getServiceType()) ) {
                    throw new ModelObjectConflictException(String.format(
                        "Singleton service %s does already exist for the instance", //$NON-NLS-1$
                        serviceDesc.getTypeName()), new ModelObjectConflictFault(InstanceStructuralObject.class, instance.getId()));
                }
            }
        }
    }


    /**
     * @param service
     * @param instance
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     */
    private void checkImageType ( ServiceStructuralObjectImpl service, InstanceStructuralObjectImpl instance )
            throws ModelServiceException, ModelObjectReferentialIntegrityException {
        ImageTypeDescriptor imageDesc = this.sctx.getImageTypeRegistry().getDescriptor(instance.getImageType());
        if ( !imageDesc.getForcedServiceTypes().contains(service.getServiceType())
                && !imageDesc.getApplicableServiceTypes().contains(service.getServiceType()) ) {
            throw new ModelObjectReferentialIntegrityException(
                "The given service type is not applicable for the instance", //$NON-NLS-1$
                new ModelObjectReferentialIntegrityFault(ServiceStructuralObject.class, service.getId()));
        }
    }


    /**
     * @param parent
     * @param inst
     * @throws ModelServiceException
     */
    private void checkValidImageType ( InstanceStructuralObject inst ) throws ModelServiceException {
        this.sctx.getImageTypeRegistry().getDescriptor(inst.getImageType());
    }


    private void setParent ( EntityManager em, StructuralObject parent, AbstractStructuralObjectImpl obj )
            throws ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, ModelServiceException {
        AbstractStructuralObjectImpl parentPersistent = null;
        if ( parent != null ) {

            parentPersistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, parent, "Parent object does not exist"); //$NON-NLS-1$

            if ( !obj.getAllowedParents().contains(parentPersistent.getType()) ) {
                throw new ModelObjectReferentialIntegrityException(String.format(
                    "Cannot attach this object (%s) to the given parent (%s)", //$NON-NLS-1$
                    obj.getType(),
                    parentPersistent.getType()), new ModelObjectReferentialIntegrityFault(obj.getClass(), obj.getId()));
            }

        }

        TreeUtil.treeInsert(em, AbstractStructuralObjectImpl.class, parentPersistent, obj);
    }


    /**
     * @param res
     */
    private static void setParents ( EntityManager em, AbstractStructuralObjectImpl parent, List<AbstractStructuralObjectImpl> res ) {
        TreeUtil.treeMultiInsert(em, AbstractStructuralObjectImpl.class, parent, res);
    }


    @SuppressWarnings ( "unchecked" )
    private <T extends StructuralObject> void validateObject ( T object ) throws ModelObjectValidationException {
        Validator v = this.sctx.getValidatorFactory().getValidator();

        Set<ConstraintViolation<T>> res = v.validate(object);

        if ( !res.isEmpty() ) {
            throw new ModelObjectValidationException((Class<T>) object.getClass(), object.getId(), res);
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#update(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:modify" )
    public <T extends StructuralObject> T update ( T toUpdate )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException {
        this.checkObjectPermission("structure:modify", toUpdate); //$NON-NLS-1$
        EntityManager em = this.sctx.createConfigEM();
        return update(em, toUpdate);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.StructuralObjectServerService#setObjectState(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObjectState)
     */
    @Override
    public void setObjectState ( @NonNull EntityManager em, @NonNull InstanceStructuralObject instance, @NonNull StructuralObjectState state )
            throws ModelObjectNotFoundException, ModelServiceException {
        AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, instance);
        persistent.setPersistentState(state);
        em.persist(persistent);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.StructuralObjectServerService#update(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public @NonNull <T extends StructuralObject> T update ( @NonNull EntityManager em, @Nullable T toUpdate )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException {
        if ( toUpdate == null || toUpdate.getId() == null ) {
            throw new ModelServiceException("Object to update is null or has no identifier set"); //$NON-NLS-1$
        }

        AbstractStructuralObjectImpl newPersistent = (AbstractStructuralObjectImpl) toUpdate;

        try {
            AbstractStructuralObjectImpl old = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, toUpdate);
            if ( log.isDebugEnabled() ) {
                log.debug("Updating object " + newPersistent); //$NON-NLS-1$
            }
            copyPersistentProperties(newPersistent, old);
            this.validateObject(newPersistent);
            newPersistent = em.merge(newPersistent);
            em.flush();
            em.refresh(newPersistent);
            return toUpdate;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to update object", e); //$NON-NLS-1$
        }
    }


    private static void copyPersistentProperties ( AbstractStructuralObjectImpl newPersistent, AbstractStructuralObjectImpl old ) {
        newPersistent.setTreeNode(old.getTreeNode());
        newPersistent.setDefaultObjects(old.getDefaultObjects());
        newPersistent.setEnforcedObjects(old.getEnforcedObjects());
        newPersistent.setObjectPool(old.getObjectPool());
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#delete(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:delete" )
    public void delete ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException {

        this.checkObjectPermission("structure:delete", obj); //$NON-NLS-1$
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);
            if ( log.isDebugEnabled() ) {
                log.info("Deleting object " + persistent); //$NON-NLS-1$
            }

            Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistent);
            if ( !parent.isPresent() ) {
                throw new ModelServiceException("You cannot delete the root group"); //$NON-NLS-1$
            }

            TreeUtil.removeSubtree(em, AbstractStructuralObjectImpl.class, persistent);
            // this seems to be necessary, hibernate won't remove on real transaction commit
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to delete", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#fetchChildren(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( STRUCTURE_VIEW )
    public Set<StructuralObjectMutable> fetchChildren ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            Set<StructuralObjectMutable> children = new LinkedHashSet<>();
            AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);
            for ( AbstractStructuralObjectImpl child : TreeUtil.getDirectChildren(em, AbstractStructuralObjectImpl.class, persistent) ) {
                if ( this.hasObjectPermission(STRUCTURE_VIEW, child) ) {
                    deriveOverallState(em, child);
                    children.add(child);
                }
            }
            return children;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch children", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#fetchParent(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( STRUCTURE_VIEW )
    public StructuralObjectMutable fetchParent ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        try {
            AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);

            Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistent);

            if ( parent.isPresent() ) {
                AbstractStructuralObjectImpl parentObj = parent.get();
                this.checkObjectPermission(STRUCTURE_VIEW, parentObj);
                deriveOverallState(em, parent.get());
                return parent.get();
            }

            return null;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch parent", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#fetchAttachedObjects(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:view:objects" )
    public Set<ConfigurationObject> fetchAttachedObjects ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        this.checkObjectPermission("structure:view:objects", obj); //$NON-NLS-1$
        EntityManager em = this.sctx.createConfigEM();

        try {
            Set<ConfigurationObject> attached = new LinkedHashSet<>();

            for ( ConfigurationObject attObj : this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj).getObjectPool() ) {

                if ( ( (AbstractConfigurationObject<?>) attObj ).getOuterObject() == null ) {
                    attached.add(new ConfigurationObjectReference(attObj));
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found %d attached objects", attached.size())); //$NON-NLS-1$
            }
            return attached;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch attached objects", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#fetchDefaults(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:view:defaults" )
    public Set<ConfigurationObject> fetchDefaults ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        this.checkObjectPermission("structure:view:defaults", obj); //$NON-NLS-1$

        EntityManager em = this.sctx.createConfigEM();
        try {
            Set<ConfigurationObject> children = new LinkedHashSet<>();
            children.addAll(this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj).getDefaultObjects());
            return children;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch default objects", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#fetchEnforcements(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:view:enforcements" )
    public Set<ConfigurationObject> fetchEnforcements ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        this.checkObjectPermission("structure:view:enforcements", obj); //$NON-NLS-1$

        EntityManager em = this.sctx.createConfigEM();
        try {
            Set<ConfigurationObject> res = new LinkedHashSet<>();
            res.addAll(this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj).getEnforcedObjects());
            return res;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch enforced objects", e); //$NON-NLS-1$
        }
    }

}
