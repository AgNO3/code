/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.resourcelibrary;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.jws.WebService;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectConflictFault;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectNotFoundFault;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectReferentialIntegrityFault;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectValidationFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryRegistry;
import eu.agno3.orchestrator.config.model.jobs.ResourceLibrarySynchronizationJob;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryEntry;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryException;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryFileInfo;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryListRequest;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryListResponse;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrarySynchronizeRequest;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryServiceDescriptor;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.targets.AnyServerTarget;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.CallErrorException;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ResourceLibraryService.class, ResourceLibraryServerService.class, SOAPWebService.class,
}, configurationPid = "resourceLibrary" )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService",
    targetNamespace = ResourceLibraryServiceDescriptor.NAMESPACE,
    serviceName = ResourceLibraryServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/resourceLibrary" )
public class ResourceLibraryServiceImpl implements ResourceLibraryService, ResourceLibraryServerService {

    private static final Logger log = Logger.getLogger(ResourceLibraryServiceImpl.class);
    private static final String DEFAULT_STORAGE_PATH = "/srv/orchserver/resourceLibraries/"; //$NON-NLS-1$
    private static final String DEFAULT_DEFAULT_PATH = "/usr/share/orchserver/libraryDefaults/"; //$NON-NLS-1$

    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private ObjectAccessControl authz;
    private ObjectPoolProvider objectPoolProvider;

    private Path resourceFileStore;
    private Path defaultPathStore;

    private AgentServerService agentService;
    private MessagingClient<ServerMessageSource> messagingClient;

    private ResourceLibraryRegistry resourceLibraryRegistry;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        Path storagePath = Paths.get(ConfigUtil.parseString(ctx.getProperties(), "storagePath", DEFAULT_STORAGE_PATH)); //$NON-NLS-1$

        if ( !Files.exists(storagePath) ) {
            try {
                Files.createDirectories(storagePath);
            }
            catch ( IOException e ) {
                log.error("Failed to create resource library storage directory", e); //$NON-NLS-1$
            }
        }

        if ( !Files.exists(storagePath) || !Files.isDirectory(storagePath) || !Files.isWritable(storagePath) ) {
            log.error("Invalid storage path"); //$NON-NLS-1$
            return;
        }

        try {
            this.resourceFileStore = storagePath.toRealPath();
        }
        catch ( IOException e ) {
            log.error("Could not canonicalize path", e); //$NON-NLS-1$
        }

        Path defaultPath = Paths.get(ConfigUtil.parseString(ctx.getProperties(), "defaultPath", DEFAULT_DEFAULT_PATH)); //$NON-NLS-1$

        if ( Files.exists(defaultPath) ) {
            try {
                this.defaultPathStore = defaultPath.toRealPath();
            }
            catch ( IOException e ) {
                log.error("Could not canonicalize path", e); //$NON-NLS-1$
            }
        }
    }


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


    @Reference
    protected synchronized void setObjectPoolProvider ( ObjectPoolProvider opp ) {
        this.objectPoolProvider = opp;
    }


    protected synchronized void unsetObjectPoolProvider ( ObjectPoolProvider opp ) {
        if ( this.objectPoolProvider == opp ) {
            this.objectPoolProvider = null;
        }
    }


    @Reference
    protected synchronized void setAgentService ( AgentServerService ass ) {
        this.agentService = ass;
    }


    protected synchronized void unsetAgentService ( AgentServerService ass ) {
        if ( this.agentService == ass ) {
            this.agentService = null;
        }
    }


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        this.messagingClient = mc;

    }


    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.messagingClient == mc ) {
            this.messagingClient = null;
        }
    }


    @Reference
    protected synchronized void setResourceLibraryRegistry ( ResourceLibraryRegistry reg ) {
        this.resourceLibraryRegistry = reg;
    }


    protected synchronized void unsetResourceLibraryRegistry ( ResourceLibraryRegistry reg ) {
        if ( this.resourceLibraryRegistry == reg ) {
            this.resourceLibraryRegistry = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#getById(java.util.UUID)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:read" )
    public ResourceLibrary getById ( UUID libraryId ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, libraryId);
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:read"); //$NON-NLS-1$
        return lib;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#getByName(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:read" )
    public ResourceLibrary getByName ( StructuralObject obj, String name, String type ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        @NonNull
        AbstractStructuralObjectImpl persistent = PersistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj.getId());
        return getClosestByNameInternal(persistent, name, type, em, false);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#getClosestByName(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:read" )
    public ResourceLibrary getClosestByName ( StructuralObject obj, String name, String type )
            throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        @NonNull
        AbstractStructuralObjectImpl persistent = PersistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj.getId());
        return getClosestByNameInternal(persistent, name, type, em, true);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService#getClosestByName(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl, java.lang.String, java.lang.String)
     */
    @Override
    public @NonNull ResourceLibrary getClosestByName ( @NonNull EntityManager em, @NonNull AbstractStructuralObjectImpl anchor, @NonNull String name,
            @NonNull String type ) throws ModelObjectNotFoundException, ModelServiceException {
        ResourceLibrary rl = getClosestByNameInternal(anchor, name, type, em, true);
        if ( rl == null ) {
            throw new ModelObjectNotFoundException(ResourceLibrary.class, null);
        }
        return rl;
    }


    /**
     * @param obj
     * @param name
     * @param type
     * @param em
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private ResourceLibrary getClosestByNameInternal ( AbstractStructuralObjectImpl persistent, String name, String type, EntityManager em,
            boolean recursive ) throws ModelObjectNotFoundException, ModelServiceException {
        this.authz.checkAccess(persistent, "resourceLibrary:read"); //$NON-NLS-1$
        TypedQuery<ResourceLibrary> q = em
                .createQuery("SELECT r FROM ResourceLibrary r WHERE anchor = :anchor AND type = :type AND name = :name", ResourceLibrary.class); //$NON-NLS-1$
        q.setParameter("anchor", persistent); //$NON-NLS-1$
        q.setParameter("type", type); //$NON-NLS-1$
        q.setParameter("name", name); //$NON-NLS-1$
        q.setMaxResults(1);
        List<ResourceLibrary> resultList = q.getResultList();
        if ( resultList.isEmpty() ) {
            if ( recursive ) {
                Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistent);
                if ( parent.isPresent() ) {
                    return getClosestByNameInternal(parent.get(), name, type, em, recursive);
                }
            }
            return null;
        }
        return resultList.get(0);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#getResourceLibraries(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:read" )
    public List<ResourceLibrary> getResourceLibraries ( StructuralObject obj ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        if ( obj == null ) {
            throw new ModelObjectNotFoundException(StructuralObject.class, null);
        }
        return getResourceLibraries(em, obj);
    }


    @Override
    public @NonNull List<@NonNull ResourceLibrary> getResourceLibraries ( @NonNull EntityManager em, @NonNull StructuralObject obj )
            throws ModelObjectNotFoundException, ModelServiceException {
        AbstractStructuralObjectImpl anchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);
        this.authz.checkAccess(anchor, "resourceLibrary:read"); //$NON-NLS-1$
        return this.objectPoolProvider.getResourceLibraries(em, anchor);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#getUsableResourceLibraries(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String, boolean)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:read" )
    public List<ResourceLibrary> getUsableResourceLibraries ( StructuralObject obj, String type, boolean excludeAnchor )
            throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        AbstractStructuralObjectImpl anchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);
        this.authz.checkAccess(anchor, "resourceLibrary:read"); //$NON-NLS-1$
        List<ResourceLibrary> inScope = this.objectPoolProvider.getResourceLibrariesInScope(em, anchor);
        List<ResourceLibrary> filtered = new ArrayList<>();
        Set<String> foundAliases = new HashSet<>();
        for ( ResourceLibrary rl : inScope ) {
            if ( excludeAnchor && rl.getAnchor().equals(obj) ) {
                continue;
            }
            if ( foundAliases.add(rl.getName()) ) {
                if ( type != null && !Objects.equals(type, rl.getType()) ) {
                    continue;
                }
                filtered.add(rl);
            }
        }
        return filtered;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelObjectReferentialIntegrityException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#delete(java.util.UUID)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:delete" )
    public void delete ( UUID libraryId ) throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException {
        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, libraryId);
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:delete"); //$NON-NLS-1$

        if ( !lib.getChildren().isEmpty() ) {
            throw new ModelObjectReferentialIntegrityException(ResourceLibrary.class, libraryId);
        }

        em.remove(lib);
        em.flush();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelObjectConflictException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#create(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.util.UUID, java.lang.String, java.lang.String, boolean)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:create" )
    public ResourceLibrary create ( StructuralObject obj, UUID parentId, String name, String type, boolean builtin ) throws ModelServiceException,
            ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, ModelObjectValidationException, ModelObjectConflictException {
        EntityManager em = this.sctx.createConfigEM();

        if ( obj == null ) {
            throw new ModelObjectNotFoundException(StructuralObject.class, null);
        }

        if ( name == null || type == null ) {
            throw new ModelServiceException();
        }

        return create(em, obj, parentId, name, type, builtin);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService#create(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject, java.util.UUID, java.lang.String,
     *      java.lang.String, boolean)
     */
    @Override
    public @NonNull ResourceLibrary create ( @NonNull EntityManager em, @NonNull StructuralObject obj, @Nullable UUID parentId, @NonNull String name,
            @NonNull String type, boolean builtin ) throws ModelObjectNotFoundException, ModelServiceException, ModelObjectValidationException,
                    ModelObjectReferentialIntegrityException, ModelObjectConflictException {
        AbstractStructuralObjectImpl anchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);
        this.authz.checkAccess(anchor, "resourceLibrary:create"); //$NON-NLS-1$

        if ( StringUtils.isBlank(name) || StringUtils.isBlank(type) ) {
            throw new ModelObjectValidationException("name and type are required", new ModelObjectValidationFault()); //$NON-NLS-1$
        }

        ResourceLibrary parent = null;
        if ( parentId != null ) {
            parent = PersistenceUtil.fetch(em, ResourceLibrary.class, parentId);

            AbstractStructuralObjectImpl parentAnchor = parent.getAnchor();

            if ( !TreeUtil.isAncestorOrSame(parentAnchor, anchor) ) {
                throw new ModelObjectReferentialIntegrityException("Parent must be at anchestor", new ModelObjectReferentialIntegrityFault()); //$NON-NLS-1$
            }
        }

        ResourceLibrary lib = new ResourceLibrary();
        lib.setLastModified(DateTime.now());
        lib.setAnchor(anchor);
        anchor.getResourceLibraries().add(lib);

        if ( parent != null ) {
            lib.setParent(parent);
            parent.getChildren().add(lib);
            em.persist(parent);
        }
        lib.setBuiltin(builtin);
        lib.setName(name);
        lib.setType(type);
        em.persist(lib);

        try {
            em.flush();
            em.refresh(lib);
            return lib;
        }
        catch ( PersistenceException e ) {
            if ( e.getCause() instanceof ConstraintViolationException ) {
                throw new ModelObjectConflictException(ResourceLibrary.class.getName(), name);
            }

            throw new ModelServiceException("Failed to create resource library", e); //$NON-NLS-1$
        }
    }


    private Path makeResourcePath ( ResourceLibrary lib ) throws ModelServiceException {
        if ( this.resourceFileStore == null ) {
            throw new ModelServiceException("Resource storage path is invalid"); //$NON-NLS-1$
        }

        return this.resourceFileStore.resolve(lib.getId().toString());
    }


    private Path makeDefaultPath ( String type, String name ) {
        if ( this.defaultPathStore == null ) {
            return null;
        }

        Path defPath = this.defaultPathStore.resolve(String.format("%s/%s/", type, name)); //$NON-NLS-1$

        if ( !Files.exists(defPath) || !Files.isReadable(defPath) ) {
            return null;
        }

        return defPath;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#getFiles(java.util.UUID)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:view" )
    public List<ResourceLibraryFileInfo> getFiles ( UUID resourceLibraryId ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, resourceLibraryId);
        return getFilesInternal(lib, false);
    }


    /**
     * @param resourceLibraryId
     * @param lib
     * @return
     * @throws ModelServiceException
     */
    private List<ResourceLibraryFileInfo> getFilesInternal ( ResourceLibrary lib, boolean inherited ) throws ModelServiceException {
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:view"); //$NON-NLS-1$
        return getFilesInternal(makeResourcePath(lib), lib.getId(), lib.getAnchor().getId(), lib.getName(), inherited, false);
    }


    /**
     * @param libPath
     * @return
     */
    private static List<ResourceLibraryFileInfo> getFilesInternal ( Path libPath, UUID libraryId, UUID anchorId, String libName, boolean inherited,
            boolean globalDefault ) {
        if ( !Files.exists(libPath) ) {
            return Collections.EMPTY_LIST;
        }

        final List<ResourceLibraryFileInfo> res = new ArrayList<>();
        try ( Stream<Path> list = Files.walk(libPath) ) {
            list.forEach(p -> {
                if ( libPath.equals(p) ) {
                    return;
                }
                Path subpath = p.subpath(libPath.getNameCount(), p.getNameCount());
                if ( globalDefault ) {
                    res.add(new ResourceLibraryFileInfo(subpath.toString(), true));
                }
                else {
                    res.add(new ResourceLibraryFileInfo(subpath.toString(), libraryId, anchorId, libName, inherited));
                }
            });
            return res;
        }
        catch ( IOException e ) {
            log.debug("Error enumerating files", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService#getLastModified(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary)
     */
    @Override
    public @Nullable DateTime getLastModified ( @NonNull EntityManager em, @NonNull ResourceLibrary rl ) {
        DateTime lm = rl.getLastModified();
        ResourceLibrary parent = rl.getParent();
        while ( parent != null ) {
            DateTime plm = parent.getLastModified();
            if ( plm != null && plm.isAfter(lm) ) {
                lm = plm;
            }

            parent = parent.getParent();
        }

        if ( this.defaultPathStore != null ) {
            Path defPath = this.defaultPathStore.resolve(String.format("%s/.lastModified", rl.getType())); //$NON-NLS-1$
            if ( Files.exists(defPath) && Files.isReadable(defPath) ) {
                try {
                    FileTime lastMod = Files.getLastModifiedTime(defPath);
                    if ( lastMod != null && lm.isBefore(lastMod.toMillis()) ) {
                        lm = new DateTime(lastMod.toMillis());
                    }
                }
                catch ( IOException e ) {
                    log.debug("Failed to get last modified time", e); //$NON-NLS-1$
                }
            }
        }
        return lm;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService#trackSynchronized(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      org.joda.time.DateTime)
     */
    @Override
    public void trackSynchronized ( @NonNull StructuralObject anchor, @NonNull DateTime lastMod )
            throws ModelServiceException, ModelObjectNotFoundException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Tracking resource library synchronization on %s, setting sync time %s", //$NON-NLS-1$
                anchor,
                lastMod));
        }
        try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em = this.sctx.createConfigEM();
            AbstractStructuralObjectImpl panchor = PersistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor.getId());
            if ( panchor.getResourceLibraryLastSync() == null || panchor.getResourceLibraryLastSync().isBefore(lastMod) ) {
                panchor.setResourceLibraryLastSync(lastMod);
                em.persist(panchor);
                em.flush();
            }
            tx.commit();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#getInheritedFiles(java.util.UUID)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:view" )
    public Set<ResourceLibraryFileInfo> getInheritedFiles ( UUID resourceLibraryId ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, resourceLibraryId);
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:view"); //$NON-NLS-1$

        Set<ResourceLibraryFileInfo> inheritedPaths = new HashSet<>();
        ResourceLibrary topMost = lib;
        ResourceLibrary parent = lib.getParent();
        while ( parent != null ) {
            inheritedPaths.addAll(getFilesInternal(parent, true));

            topMost = parent;
            parent = parent.getParent();
        }

        if ( this.resourceLibraryRegistry.getDescriptor(topMost.getType()).haveDefaultsFor(topMost.getName()) ) {
            Path defaultPath = makeDefaultPath(topMost.getType(), topMost.getName());
            if ( defaultPath != null ) {
                inheritedPaths.addAll(getFilesInternal(defaultPath, null, null, null, true, true));
            }
        }

        return inheritedPaths;
    }


    /**
     * @param em
     * @param lib
     * @throws ModelServiceException
     */
    private static void updateLastModified ( EntityManager em, ResourceLibrary lib ) throws ModelServiceException {
        try {
            lib.setLastModified(DateTime.now());
            em.persist(lib);
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#removeFile(java.util.UUID,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:edit:remove" )
    public void removeFile ( UUID resourceLibraryId, String path ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, resourceLibraryId);
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:edit:remove"); //$NON-NLS-1$

        try {
            Path rp = makeResourcePath(lib, path);
            if ( log.isDebugEnabled() ) {
                log.debug("Deleting " + rp); //$NON-NLS-1$
            }
            Files.deleteIfExists(rp);
            updateLastModified(em, lib);
        }
        catch ( IOException e ) {
            throw new ModelServiceException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#getFile(java.util.UUID,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:view" )
    public DataHandler getFile ( UUID resourceLibraryId, String path ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, resourceLibraryId);
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:view"); //$NON-NLS-1$

        try {
            return getFileInternal(path, lib);
        }
        catch ( IOException e ) {
            throw new ModelServiceException(e);
        }
    }


    /**
     * @param path
     * @param lib
     * @return
     * @throws ModelServiceException
     * @throws IOException
     */
    private DataHandler getFileInternal ( String path, ResourceLibrary lib ) throws ModelServiceException, IOException {
        Path resPath = makeResourcePath(lib, path);

        if ( Files.exists(resPath) ) {
            return new DataHandler(new FileDataSource(resPath.toFile()));
        }
        else if ( lib.getParent() != null ) {
            return getFileInternal(path, lib.getParent());
        }
        else if ( this.resourceLibraryRegistry.getDescriptor(lib.getType()).haveDefaultsFor(lib.getName()) ) {
            DataHandler h = getDefaultFileInternal(path, lib.getType(), lib.getName());
            if ( h != null ) {
                return h;
            }
        }

        return new DataHandler(new ByteArrayDataSource(new byte[0], "application/octet-stream")); //$NON-NLS-1$
    }


    /**
     * @param path
     * @param type
     * @param name
     * @return
     * @throws IOException
     */
    private DataHandler getDefaultFileInternal ( String path, String type, String name ) throws IOException {
        Path defaultPath = makeDefaultPath(type, name);

        if ( defaultPath == null ) {
            return null;
        }

        Path p = defaultPath.resolve(path);
        if ( !Files.exists(p) ) {
            return null;
        }
        if ( ! ( p.toAbsolutePath().startsWith(defaultPath.toAbsolutePath()) ) ) {
            throw new IOException("Directory traversal"); //$NON-NLS-1$
        }
        return new DataHandler(new FileDataSource(p.toFile()));
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelObjectConflictException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#putFile(java.util.UUID, boolean,
     *      java.lang.String, javax.activation.DataHandler)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:edit:modify" )
    public void putFile ( UUID resourceLibraryId, boolean create, String path, DataHandler data )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectConflictException {
        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, resourceLibraryId);
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:edit:modify"); //$NON-NLS-1$

        if ( path == null || data == null ) {
            throw new ModelServiceException();
        }

        putFile(em, lib, path, create, data);
    }


    /**
     * @param lib
     * @param path
     * @param create
     * @param data
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     */
    @Override
    public void putFile ( @NonNull EntityManager em, @NonNull ResourceLibrary lib, @NonNull String path, boolean create, @NonNull DataHandler data )
            throws ModelServiceException, ModelObjectConflictException {
        Path resPath;

        try {
            resPath = makeResourcePath(lib, path);

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Replacing with data contents %s", resPath)); //$NON-NLS-1$
            }

            if ( create && Files.exists(resPath) ) {
                throw new ModelObjectConflictException("File exists", new ModelObjectConflictFault()); //$NON-NLS-1$
            }

            try ( FileChannel ch = FileChannel
                    .open(resPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                  OutputStream os = Channels.newOutputStream(ch) ) {
                IOUtils.copy(data.getInputStream(), os);
            }
            updateLastModified(em, lib);
        }
        catch ( IOException e ) {
            throw new ModelServiceException(e);
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelObjectConflictException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#putEmptyFile(java.util.UUID,
     *      boolean, java.lang.String)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:edit:modify" )
    public void putEmptyFile ( UUID resourceLibraryId, boolean create, String path )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectConflictException {
        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, resourceLibraryId);
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:edit:modify"); //$NON-NLS-1$

        Path resPath;
        try {
            resPath = makeResourcePath(lib, path);

            if ( create && Files.exists(resPath) ) {
                throw new ModelObjectConflictException("File exists", new ModelObjectConflictFault()); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Replacing with empty contents " + resPath); //$NON-NLS-1$
            }

            try ( FileChannel ch = FileChannel
                    .open(resPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE) ) {

            }
            updateLastModified(em, lib);
        }
        catch ( IOException e ) {
            throw new ModelServiceException(e);
        }
    }


    @Override
    public @NonNull List<@NonNull Job> makeSynchronizationJob ( @NonNull EntityManager em, @NonNull ServiceStructuralObjectImpl actualCurrent,
            @NonNull Set<@NonNull ResourceLibraryReference> referencedResourceLibraryNames, boolean forceSync, @Nullable UserPrincipal owner )
                    throws ModelServiceException, ModelObjectNotFoundException, AgentCommunicationErrorException, AgentDetachedException,
                    AgentOfflineException {
        this.authz.checkAccess(actualCurrent, "config:apply"); //$NON-NLS-1$

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Synchronizing libraries %s for %s", referencedResourceLibraryNames, actualCurrent)); //$NON-NLS-1$
        }

        DateTime lastSync = actualCurrent.getResourceLibraryLastSync();
        List<ResourceLibrary> resourceLibrariesInScope = this.objectPoolProvider.getResourceLibrariesInScope(em, actualCurrent);
        Map<String, ResourceLibrary> matched = makeMatchedLibraries(referencedResourceLibraryNames, resourceLibrariesInScope);
        @NonNull
        List<@NonNull Job> jobs = new ArrayList<>();
        for ( ResourceLibraryReference synchronize : referencedResourceLibraryNames ) {
            ResourceLibrary lib = matched.get(synchronize.getName());

            if ( lib == null ) {
                throw new ModelServiceException();
            }

            @Nullable
            DateTime lm = getLastModified(em, lib);

            if ( !forceSync && lm != null && lastSync != null && ( lastSync.equals(lm) || lastSync.isAfter(lm) ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Library has not changed since last sync " + synchronize.getName()); //$NON-NLS-1$
                }
                continue;
            }
            else if ( !forceSync && log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Library %s last update %s last sync %s", //$NON-NLS-1$
                    synchronize.getName(),
                    lm,
                    lastSync));
            }

            ResourceLibrarySynchronizationJob j = new ResourceLibrarySynchronizationJob();
            j.setLibrary(lib);
            j.setHint(synchronize.getHint());
            j.setService(actualCurrent);
            j.setLastModified(lm);
            j.setOwner(owner);
            j.setTarget(new AnyServerTarget());
            jobs.add(j);
        }

        return jobs;
    }


    /**
     * @param referencedResourceLibraryNames
     * @param resourceLibrariesInScope
     * @return
     * @throws ModelObjectNotFoundException
     */
    private static Map<String, ResourceLibrary> makeMatchedLibraries ( Set<ResourceLibraryReference> referencedResourceLibraryNames,
            List<ResourceLibrary> resourceLibrariesInScope ) throws ModelObjectNotFoundException {
        Map<String, ResourceLibrary> matched = new HashMap<>();
        Map<String, String> expectType = new HashMap<>();
        Set<String> missing = new HashSet<>();
        for ( ResourceLibraryReference ref : referencedResourceLibraryNames ) {
            missing.add(ref.getName());
            expectType.put(ref.getName(), ref.getType());
        }

        for ( ResourceLibrary r : resourceLibrariesInScope ) {

            if ( matched.containsKey(r.getName()) ) {
                // the first match is the closest one
                continue;
            }

            String expType = expectType.get(r.getName());
            if ( expType != null && !expType.equals(r.getType()) ) {
                continue;
            }
            matched.put(r.getName(), r);
        }

        missing.removeAll(matched.keySet());
        if ( !missing.isEmpty() ) {
            throw new ModelObjectNotFoundException(
                "Missing resource libraries " + missing, //$NON-NLS-1$
                new ModelObjectNotFoundFault("resourceLibrary", missing.toString())); //$NON-NLS-1$
        }
        return matched;
    }


    @Override
    public void synchronizeServiceLibraries ( @NonNull ServiceStructuralObject service, @NonNull ResourceLibrary library, @Nullable String hint,
            @NonNull JobOutputHandler output ) throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException,
                    AgentOfflineException, AgentCommunicationErrorException {

        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, library.getId());

        ServiceStructuralObjectImpl persistentTarget = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service);
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:edit:modify"); //$NON-NLS-1$

        @Nullable
        DateTime lm = getLastModified(em, lib);

        if ( !StringUtils.isBlank(hint) ) {
            output.logLineInfo(String.format("Synchronizing library %s -> %s", library.getName(), hint)); //$NON-NLS-1$
        }
        else {
            output.logLineInfo("Synchronizing library " + library.getName()); //$NON-NLS-1$
        }
        synchronizeInternal(em, lib, hint, persistentTarget, output);
        if ( lm == null ) {
            lm = new DateTime();
        }
        trackSynchronized(service, lm);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService#synchronize(java.util.UUID,
     *      eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    @RequirePermissions ( "resourceLibrary:edit:modify" )
    public void synchronize ( UUID resourceLibraryId, ServiceStructuralObject target ) throws ModelServiceException, ModelObjectNotFoundException,
            AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {
        EntityManager em = this.sctx.createConfigEM();
        ResourceLibrary lib = PersistenceUtil.fetch(em, ResourceLibrary.class, resourceLibraryId);
        ServiceStructuralObjectImpl persistentTarget = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, target);
        this.authz.checkAccess(lib.getAnchor(), "resourceLibrary:edit:modify"); //$NON-NLS-1$

        synchronizeInternal(em, lib, null, persistentTarget, null);
    }


    /**
     * @param em
     * @param lib
     * @param persistentTarget
     * @param output
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     * @throws AgentCommunicationErrorException
     */
    private void synchronizeInternal ( EntityManager em, ResourceLibrary lib, String hint, ServiceStructuralObjectImpl persistentTarget,
            JobOutputHandler output ) throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentOfflineException,
                    AgentCommunicationErrorException {
        Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistentTarget);

        if ( !parent.isPresent() || parent.get().getType() != StructuralObjectType.INSTANCE ) {
            throw new ModelServiceException("Invalid service parent " + ( parent.isPresent() ? parent.get() : null )); //$NON-NLS-1$
        }

        AbstractStructuralObjectImpl entity = parent.get();

        if ( entity == null ) {
            throw new IllegalStateException();
        }

        @NonNull
        AgentMessageTarget messageTarget = this.agentService.ensureAgentOnline((InstanceStructuralObject) PersistenceUtil.unproxy(entity));

        List<ResourceLibraryEntry> onAgent;
        try {
            onAgent = getAgentEntries(messageTarget, persistentTarget, lib, hint);
        }
        catch ( ResourceLibraryException e ) {
            throw new ModelServiceException("Failed to synchronize library", e); //$NON-NLS-1$
        }
        Map<String, ResourceLibraryEntry> onAgentMap = makeFileMap(onAgent);
        if ( log.isDebugEnabled() ) {
            for ( Entry<String, ResourceLibraryEntry> entry : onAgentMap.entrySet() ) {
                log.debug(String.format("Agent %s: %s", entry.getKey(), entry.getValue())); //$NON-NLS-1$
            }
        }
        List<ResourceLibraryEntryInternal> inLibrary = flattenResourceLibrary(lib);
        Map<String, ResourceLibraryEntryInternal> inLibraryMap = makeFileMap(inLibrary);
        if ( log.isDebugEnabled() ) {
            for ( Entry<String, ResourceLibraryEntryInternal> entry : inLibraryMap.entrySet() ) {
                log.debug(String.format("Library %s: %s", entry.getKey(), entry.getValue())); //$NON-NLS-1$
            }
        }
        List<ResourceLibraryEntry> res = doDiffAndSynchronize(lib, hint, persistentTarget, messageTarget, onAgentMap, inLibraryMap);
        checkResult(inLibrary, inLibraryMap, res);
    }


    /**
     * @param lib
     * @param persistentTarget
     * @param messageTarget
     * @param onAgentMap
     * @param inLibraryMap
     * @return
     * @throws AgentCommunicationErrorException
     * @throws ModelServiceException
     */
    private List<ResourceLibraryEntry> doDiffAndSynchronize ( ResourceLibrary lib, String hint, ServiceStructuralObjectImpl persistentTarget,
            @NonNull AgentMessageTarget messageTarget, Map<String, ResourceLibraryEntry> onAgentMap,
            Map<String, ResourceLibraryEntryInternal> inLibraryMap ) throws AgentCommunicationErrorException, ModelServiceException {
        Set<String> missing = new HashSet<>(inLibraryMap.keySet());
        missing.removeAll(onAgentMap.keySet());

        Set<String> deleted = new HashSet<>(onAgentMap.keySet());
        deleted.removeAll(inLibraryMap.keySet());

        Set<String> common = new HashSet<>(inLibraryMap.keySet());
        common.retainAll(onAgentMap.keySet());
        Set<String> modified = new HashSet<>();
        Map<String, String> oldHashes = new HashMap<>();

        for ( String path : common ) {
            ResourceLibraryEntry agent = onAgentMap.get(path);
            ResourceLibraryEntry library = inLibraryMap.get(path);
            if ( !agent.getHash().equals(library.getHash()) ) {
                modified.add(path);
                oldHashes.put(path, agent.getHash());
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Missing " + missing); //$NON-NLS-1$
            log.debug("Deleted " + deleted); //$NON-NLS-1$
            log.debug("Modified " + modified); //$NON-NLS-1$
        }

        return doSynchronization(messageTarget, persistentTarget, lib, hint, inLibraryMap, onAgentMap, modified, oldHashes, missing, deleted);
    }


    /**
     * @param inLibrary
     * @param inLibraryMap
     * @param res
     * @throws ModelServiceException
     */
    private static void checkResult ( List<ResourceLibraryEntryInternal> inLibrary, Map<String, ResourceLibraryEntryInternal> inLibraryMap,
            List<ResourceLibraryEntry> res ) throws ModelServiceException {
        if ( res.size() != inLibrary.size() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Local: " + inLibrary); //$NON-NLS-1$
                log.debug("Remote: " + res); //$NON-NLS-1$
            }
            throw new ModelServiceException("Synchronization failed, not fully synchronized"); //$NON-NLS-1$
        }

        for ( ResourceLibraryEntry e : res ) {
            ResourceLibraryEntryInternal local = inLibraryMap.get(e.getPath());
            if ( local == null || !local.getHash().equals(e.getHash()) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("expected " + ( local != null ? local.getHash() : null )); //$NON-NLS-1$
                    log.debug("actual " + e.getHash()); //$NON-NLS-1$
                }
                throw new ModelServiceException("Synchronization failed, mismatch " + e.getPath()); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param messageTarget
     * @param persistentTarget
     * @param lib
     * @param inLibraryMap
     * @param modified
     * @param missing
     * @param deleted
     * @param oldHashes
     * @return
     * @throws AgentCommunicationErrorException
     * @throws ModelServiceException
     */
    private List<ResourceLibraryEntry> doSynchronization ( @NonNull AgentMessageTarget messageTarget, ServiceStructuralObject serviceTarget,
            ResourceLibrary lib, String hint, Map<String, ResourceLibraryEntryInternal> inLibraryMap, Map<String, ResourceLibraryEntry> remoteMap,
            Set<String> modified, Map<String, String> oldHashes, Set<String> missing, Set<String> deleted )
                    throws AgentCommunicationErrorException, ModelServiceException {

        @Nullable
        ResourceLibraryListResponse resp;
        try {

            resp = this.messagingClient.sendMessage(
                createSynchronizeRequest(messageTarget, serviceTarget, lib, hint, inLibraryMap, remoteMap, modified, oldHashes, missing, deleted));
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            throw new AgentCommunicationErrorException("Failure sending message", e); //$NON-NLS-1$
        }
        if ( resp == null ) {
            throw new AgentCommunicationErrorException("No response recieved"); //$NON-NLS-1$
        }
        return resp.getEntries();
    }


    /**
     * @param messageTarget
     * @param serviceTarget
     * @param lib
     * @param inLibraryMap
     * @param modified
     * @param missing
     * @param deleted
     * @param hint
     * @param oldHashes
     * @return
     * @throws ModelServiceException
     */
    private @NonNull ResourceLibrarySynchronizeRequest createSynchronizeRequest ( @NonNull AgentMessageTarget messageTarget,
            ServiceStructuralObject serviceTarget, ResourceLibrary lib, String hint, Map<String, ResourceLibraryEntryInternal> inLibraryMap,
            Map<String, ResourceLibraryEntry> remoteMap, Set<String> modified, Map<String, String> oldHashes, Set<String> missing,
            Set<String> deleted ) throws ModelServiceException {
        ResourceLibrarySynchronizeRequest msg = new ResourceLibrarySynchronizeRequest(messageTarget, this.messagingClient.getMessageSource());
        msg.setServiceTarget(serviceTarget);
        msg.setLibraryType(lib.getType());
        msg.setHint(hint);
        try {
            makeAdditions(inLibraryMap, missing, msg);
            makeUpdates(inLibraryMap, modified, oldHashes, msg);
            makeDeletions(remoteMap, deleted, msg);
        }
        catch ( IOException e ) {
            throw new ModelServiceException(e);
        }
        return msg;
    }


    /**
     * @param inLibraryMap
     * @param missing
     * @param msg
     * @throws IOException
     */
    private static void makeAdditions ( Map<String, ResourceLibraryEntryInternal> inLibraryMap, Set<String> missing,
            ResourceLibrarySynchronizeRequest msg ) throws IOException {
        Set<ResourceLibraryEntry> add = new HashSet<>();
        for ( String path : missing ) {
            ResourceLibraryEntryInternal e = inLibraryMap.get(path);
            e.setContent(Files.readAllBytes(e.getFile().toPath()));
            add.add(e);
        }
        msg.setAdd(add);
    }


    /**
     * @param inLibraryMap
     * @param modified
     * @param msg
     * @throws IOException
     */
    private static void makeUpdates ( Map<String, ResourceLibraryEntryInternal> inLibraryMap, Set<String> modified, Map<String, String> oldHashes,
            ResourceLibrarySynchronizeRequest msg ) throws IOException {
        Set<ResourceLibraryEntry> update = new HashSet<>();
        for ( String path : modified ) {
            ResourceLibraryEntryInternal e = inLibraryMap.get(path);
            e.setOldHash(oldHashes.get(path));
            e.setContent(Files.readAllBytes(e.getFile().toPath()));
            update.add(e);
        }
        msg.setUpdate(update);
    }


    /**
     * @param inLibraryMap
     * @param modified
     * @param msg
     */
    private static void makeDeletions ( Map<String, ResourceLibraryEntry> remoteMap, Set<String> modified, ResourceLibrarySynchronizeRequest msg ) {
        Set<ResourceLibraryEntry> delete = new HashSet<>();
        for ( String path : modified ) {
            delete.add(remoteMap.get(path));
        }
        msg.setDelete(delete);
    }


    /**
     * @param serviceTarget
     * @param target
     * @param lib
     * @return
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws MessagingException
     * @throws AgentOfflineException
     * @throws AgentCommunicationErrorException
     * @throws ResourceLibraryException
     */
    private List<ResourceLibraryEntry> getAgentEntries ( @NonNull AgentMessageTarget messageTarget, ServiceStructuralObjectImpl serviceTarget,
            ResourceLibrary lib, String hint ) throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException,
                    AgentOfflineException, AgentCommunicationErrorException, ResourceLibraryException {

        @Nullable
        ResourceLibraryListResponse resp;
        try {
            ResourceLibraryListRequest msg = new ResourceLibraryListRequest(messageTarget, this.messagingClient.getMessageSource());
            msg.setServiceTarget(serviceTarget);
            msg.setLibraryType(lib.getType());
            msg.setHint(hint);
            resp = this.messagingClient.sendMessage(msg);
        }
        catch ( CallErrorException e ) {
            DefaultXmlErrorResponseMessage result = e.getResult(DefaultXmlErrorResponseMessage.class);
            throw new ModelServiceException(result.getThrowable());
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            throw new AgentCommunicationErrorException("Failure sending message", e); //$NON-NLS-1$
        }
        if ( resp == null ) {
            throw new AgentCommunicationErrorException("No response recieved"); //$NON-NLS-1$
        }
        return resp.getEntries();
    }


    /**
     * @param onAgent
     * @return
     */
    private static <T extends ResourceLibraryEntry> Map<String, T> makeFileMap ( Collection<T> entries ) {
        Map<String, T> res = new HashMap<>();
        for ( T e : entries ) {
            res.put(e.getPath(), e);
        }
        return res;
    }


    /**
     * @param lib
     * @return
     * @throws ModelServiceException
     */
    private List<ResourceLibraryEntryInternal> flattenResourceLibrary ( ResourceLibrary lib ) throws ModelServiceException {
        Set<String> excludePaths = new HashSet<>();
        List<ResourceLibraryEntryInternal> res = new ArrayList<>();
        ResourceLibrary topMost = addLibraryEntries(lib, excludePaths, res);
        addDefaultEntries(topMost, excludePaths, res);
        return res;
    }


    /**
     * @param lib
     * @param excludePaths
     * @param res
     * @throws ModelServiceException
     */
    private void addDefaultEntries ( ResourceLibrary lib, Set<String> excludePaths, List<ResourceLibraryEntryInternal> res )
            throws ModelServiceException {
        if ( this.resourceLibraryRegistry.getDescriptor(lib.getType()).haveDefaultsFor(lib.getName()) ) {

            Path defaultPath = makeDefaultPath(lib.getType(), lib.getName());
            if ( defaultPath == null ) {
                return;
            }

            addLibraryEntries(lib, excludePaths, res, defaultPath);
        }
    }


    /**
     * @param lib
     * @param excludePaths
     * @param res
     * @param cur
     * @return
     * @throws ModelServiceException
     */
    private ResourceLibrary addLibraryEntries ( ResourceLibrary lib, Set<String> excludePaths, List<ResourceLibraryEntryInternal> res )
            throws ModelServiceException {
        ResourceLibrary cur = lib;
        while ( cur != null ) {
            Path libPath = makeResourcePath(cur);
            addLibraryEntries(cur, excludePaths, res, libPath);
            if ( cur.getParent() != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Follow parent %s @ %s", cur.getParent().getName(), cur.getParent().getAnchor())); //$NON-NLS-1$
                }
                cur = cur.getParent();
            }
            else {
                log.debug("Current is root"); //$NON-NLS-1$
                return cur;
            }
        }
        return cur;
    }


    /**
     * @param excludePaths
     * @param res
     * @param libPath
     * @param listFiles
     * @throws ModelServiceException
     */
    private static void addLibraryEntries ( ResourceLibrary lib, Set<String> excludePaths, List<ResourceLibraryEntryInternal> res, Path libPath )
            throws ModelServiceException {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Library %s @ %s (%s): %s", lib.getName(), lib.getAnchor(), lib.getId(), libPath)); //$NON-NLS-1$
        }

        if ( !Files.exists(libPath) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Does not exist " + libPath); //$NON-NLS-1$
            }
            return;
        }

        try ( Stream<Path> list = Files.walk(libPath) ) {
            list.forEach(p -> {
                Path lp = libPath;
                if ( lp.equals(p) ) {
                    return;
                }
                Path subpath = p.subpath(libPath.getNameCount(), p.getNameCount());
                String path = subpath.toString();
                if ( excludePaths.contains(path) ) {
                    if ( log.isTraceEnabled() ) {
                        log.debug("Excluded " + path); //$NON-NLS-1$
                    }
                    return;
                }

                excludePaths.add(path);
                try {
                    ResourceLibraryEntryInternal e = new ResourceLibraryEntryInternal();
                    e.setFile(p.toFile());
                    e.setPath(path);
                    e.setHash(makeFileHash(p));
                    res.add(e);
                }
                catch (
                    NoSuchAlgorithmException |
                    IOException e ) {
                    log.warn("Error reading file", e); //$NON-NLS-1$
                    return;
                }
                if ( log.isDebugEnabled() ) {
                    log.debug("Adding " + path); //$NON-NLS-1$
                }
            });
        }
        catch ( IOException e ) {
            log.debug("Error enumerating files", e); //$NON-NLS-1$
        }
    }


    /**
     * @param f
     * @return
     * @throws ModelServiceException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static String makeFileHash ( Path p ) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        MessageDigest dgst = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
        ByteBuffer buf = ByteBuffer.allocate(4096);
        try ( FileChannel fc = FileChannel.open(p, StandardOpenOption.READ) ) {
            while ( fc.read(buf) >= 0 ) {
                buf.flip();
                dgst.update(buf);
                buf.clear();
            }
        }
        return Hex.encodeHexString(dgst.digest());
    }


    /**
     * @param resPath
     * @return
     * @throws ModelServiceException
     * @throws IOException
     */
    private Path makeResourcePath ( ResourceLibrary lib, String resPath ) throws ModelServiceException, IOException {
        Path libDir = makeResourcePath(lib);
        if ( !Files.exists(libDir) ) {
            Files.createDirectories(libDir);
        }

        if ( StringUtils.isBlank(resPath) ) {
            throw new IOException("Path is empty"); //$NON-NLS-1$
        }

        Path p = libDir.resolve(resPath);
        if ( ! ( p.toAbsolutePath().startsWith(libDir.toAbsolutePath()) ) ) {
            throw new IOException("Directory traversal"); //$NON-NLS-1$
        }

        return p;
    }

}
