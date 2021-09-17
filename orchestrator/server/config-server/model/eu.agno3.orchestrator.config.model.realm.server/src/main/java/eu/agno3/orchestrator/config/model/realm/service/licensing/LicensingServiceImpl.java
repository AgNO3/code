/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.licensing;


import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.agent.server.AgentLifecycleListener;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectModifiedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfo;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfoRequest;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfoResponse;
import eu.agno3.orchestrator.config.model.realm.license.LicenseSetRequest;
import eu.agno3.orchestrator.config.model.realm.license.LicenseStorage;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.LicensingService;
import eu.agno3.orchestrator.config.model.realm.service.LicensingServiceDescriptor;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.update.License;
import eu.agno3.runtime.update.LicenseParser;
import eu.agno3.runtime.update.LicensingException;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    LicensingService.class, SOAPWebService.class, AgentLifecycleListener.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.LicensingService",
    targetNamespace = LicensingServiceDescriptor.NAMESPACE,
    serviceName = LicensingServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/licensing" )
public class LicensingServiceImpl implements LicensingService, AgentLifecycleListener {

    private static final Logger log = Logger.getLogger(LicensingServiceImpl.class);

    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private ObjectAccessControl authz;
    private ObjectPoolProvider objectPoolProvider;

    private AgentServerService agentService;
    private MessagingClient<ServerMessageSource> messagingClient;
    private InstanceServerService instanceService;

    private LicenseParser licenseParser;

    private ImageTypeRegistry imageTypes;


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
    protected synchronized void setLicenseParser ( LicenseParser lp ) {
        this.licenseParser = lp;
    }


    protected synchronized void unsetLicenseParser ( LicenseParser lp ) {
        if ( this.licenseParser == lp ) {
            this.licenseParser = null;
        }
    }


    @Reference
    protected synchronized void setImageTypeRegistry ( ImageTypeRegistry reg ) {
        this.imageTypes = reg;
    }


    protected synchronized void unsetImageTypeRegistry ( ImageTypeRegistry reg ) {
        if ( this.imageTypes == reg ) {
            this.imageTypes = null;
        }
    }


    @Reference
    protected synchronized void setInstanceService ( InstanceServerService iss ) {
        this.instanceService = iss;
    }


    protected synchronized void unsetInstanceService ( InstanceServerService iss ) {
        if ( this.instanceService == iss ) {
            this.instanceService = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelObjectConflictException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.LicensingService#addLicense(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      javax.activation.DataHandler)
     */
    @Override
    @RequirePermissions ( "licensing:add" )
    public LicenseInfo addLicense ( StructuralObject anchor, DataHandler data )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectConflictException {

        @NonNull
        EntityManager em = this.sctx.createConfigEM();

        @NonNull
        AbstractStructuralObjectImpl panchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
        this.authz.checkAccess(panchor, "licensing:add"); //$NON-NLS-1$

        try {
            LicenseStorage ls = addLicense(em, panchor, data);
            return LicenseInfo.fromLicense(this.licenseParser.parseLicense(ls.getData()));
        }
        catch (
            LicensingException |
            IOException e ) {
            throw new ModelServiceException("Invalid license", e); //$NON-NLS-1$
        }

    }


    /**
     * @param data
     * @param em
     * @param panchor
     * @return
     * @throws LicensingException
     * @throws IOException
     * @throws ModelObjectConflictException
     */
    private LicenseStorage addLicense ( EntityManager em, AbstractStructuralObjectImpl panchor, DataHandler data )
            throws LicensingException, IOException, ModelObjectConflictException {
        License lic = this.licenseParser.parseLicense(data.getInputStream());

        LicenseStorage find = em.find(LicenseStorage.class, lic.getLicenseId());

        if ( find != null ) {
            throw new ModelObjectConflictException(LicenseStorage.class, lic.getLicenseId());
        }

        LicenseStorage ls = new LicenseStorage();
        ls.setId(lic.getLicenseId());
        ls.setAnchor(panchor);
        ls.setExpiration(lic.getExpirationDate());
        ls.setIssued(lic.getIssueDate());
        ls.setServiceTypes(lic.getServiceTypes());
        ls.setData(lic.getRawData());
        em.persist(ls);
        em.flush();
        em.refresh(ls);
        return ls;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.LicensingService#removeLicense(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "licensing:remove" )
    public void removeLicense ( StructuralObject anchor, UUID licenseId ) throws ModelObjectNotFoundException, ModelServiceException {
        @NonNull
        EntityManager em = this.sctx.createConfigEM();

        @NonNull
        AbstractStructuralObjectImpl panchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
        this.authz.checkAccess(panchor, "licensing:remove"); //$NON-NLS-1$

        LicenseStorage find = em.find(LicenseStorage.class, licenseId);
        if ( find == null ) {
            throw new ModelObjectNotFoundException(LicenseStorage.class, licenseId);
        }

        AbstractStructuralObjectImpl at = find.getAssignedTo();

        if ( at != null && ! ( at instanceof InstanceStructuralObjectImpl ) ) {
            throw new ModelServiceException();
        }

        InstanceStructuralObjectImpl assignedTo = (InstanceStructuralObjectImpl) at;

        if ( assignedTo != null ) {
            this.authz.checkAccess(assignedTo, "licensing:assign"); //$NON-NLS-1$
            find.setAssignedTo(null);
            assignedTo.setAssignedLicense(null);
            em.persist(assignedTo);
        }

        find.getAnchor().getLicensePool().remove(find);
        em.remove(find);
        em.flush();

        if ( assignedTo != null ) {
            log.debug("Removing license from instance"); //$NON-NLS-1$
            setInstanceLicense(assignedTo, null);
        }
    }


    /**
     * @param is
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws LicensingException
     */

    private @Nullable LicenseInfoResponse setInstanceLicense ( InstanceStructuralObjectImpl is, LicenseStorage lic )
            throws ModelObjectNotFoundException, ModelServiceException {
        try {
            @NonNull
            AgentMessageTarget agentTarget = this.agentService.ensureAgentOnline(is);
            LicenseSetRequest msg = new LicenseSetRequest(agentTarget, this.messagingClient.getMessageSource());
            if ( lic != null ) {
                try {
                    msg.setLicense(LicenseInfo.fromLicense(this.licenseParser.parseLicense(lic.getData())));
                }
                catch ( LicensingException e ) {
                    log.warn("License is invalid", e); //$NON-NLS-1$
                }
            }
            return this.messagingClient.sendMessage(msg);
        }
        catch (
            AgentDetachedException |
            AgentOfflineException |
            MessagingException |
            InterruptedException e ) {
            log.debug("Failed to revoke license, agent is detached", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.LicensingService#getLicense(java.util.UUID)
     */
    @Override
    @RequirePermissions ( "licensing:view" )
    public LicenseInfo getLicense ( UUID licenseId ) throws ModelObjectNotFoundException, ModelServiceException {
        @NonNull
        EntityManager em = this.sctx.createConfigEM();
        try {
            return getLicense(licenseId, em);
        }
        catch ( LicensingException e ) {
            throw new ModelServiceException("Failed to parse license", e); //$NON-NLS-1$
        }
    }


    /**
     * @param licenseId
     * @param em
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws LicensingException
     */
    private LicenseInfo getLicense ( UUID licenseId, EntityManager em )
            throws ModelObjectNotFoundException, ModelServiceException, LicensingException {
        LicenseStorage find = em.find(LicenseStorage.class, licenseId);

        if ( find == null ) {
            throw new ModelObjectNotFoundException(LicenseStorage.class, licenseId);
        }

        AbstractStructuralObjectImpl anchor = find.getAnchor();
        this.authz.checkAccess(anchor, "licensing:view"); //$NON-NLS-1$
        return LicenseInfo.fromLicense(this.licenseParser.parseLicense(find.getData()));
    }


    @Override
    @RequirePermissions ( "licensing:view" )
    public LicenseInfo getAssignedLicense ( InstanceStructuralObject to ) throws ModelObjectNotFoundException, ModelServiceException {
        @NonNull
        EntityManager em = this.sctx.createConfigEM();

        @NonNull
        InstanceStructuralObjectImpl pto = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, to);
        this.authz.checkAccess(pto, "licensing:view"); //$NON-NLS-1$

        LicenseStorage assignedLicense = pto.getAssignedLicense();
        if ( assignedLicense == null && pto.getDemoExpiration() != null ) {
            LicenseInfo i = new LicenseInfo();
            i.setExpirationDate(pto.getDemoExpiration());
            return i;
        }
        else if ( assignedLicense == null ) {
            return null;
        }
        try {
            return LicenseInfo.fromLicense(this.licenseParser.parseLicense(assignedLicense.getData()));
        }
        catch ( LicensingException e ) {
            throw new ModelServiceException("Failed to parse license", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.LicensingService#assignLicense(java.util.UUID,
     *      eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "licensing:assign" )
    public void assignLicense ( UUID licenseId, InstanceStructuralObject to ) throws ModelObjectNotFoundException, ModelServiceException {
        @NonNull
        EntityManager em = this.sctx.createConfigEM();

        LicenseStorage find = em.find(LicenseStorage.class, licenseId);

        if ( find == null ) {
            throw new ModelObjectNotFoundException(LicenseStorage.class, licenseId);
        }

        @NonNull
        InstanceStructuralObjectImpl pto = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, to);

        AbstractStructuralObjectImpl anchor = find.getAnchor();
        this.authz.checkAccess(anchor, "licensing:assign"); //$NON-NLS-1$
        this.authz.checkAccess(pto, "licensing:assign"); //$NON-NLS-1$

        AbstractStructuralObjectImpl from = find.getAssignedTo();
        if ( from != null ) {
            this.authz.checkAccess(from, "licensing:assign"); //$NON-NLS-1$
            if ( find.getAssignedTo().equals(to) ) {
                // already assigned
                return;
            }

            throw new ModelServiceException("Is already assigned to another host"); //$NON-NLS-1$
        }

        assignLicense(em, pto, find);
    }


    /**
     * @param licenseId
     * @param to
     * @param em
     * @param olic
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private void assignLicense ( EntityManager em, InstanceStructuralObjectImpl to, LicenseStorage olic )
            throws ModelObjectNotFoundException, ModelServiceException {
        InstanceStructuralObjectImpl inst = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, to);
        Set<String> requiredServiceTypes = this.imageTypes.getDescriptor(inst.getImageType()).getForcedServiceTypes();
        if ( !olic.getServiceTypes().containsAll(requiredServiceTypes) ) {
            throw new ModelServiceException("License is not valid for the instance"); //$NON-NLS-1$
        }

        if ( !TreeUtil.isAncestorOrSame(to, olic.getAnchor()) ) {
            throw new ModelServiceException("Cannot assign a license to a host stored in a different subtree"); //$NON-NLS-1$
        }

        setInstanceLicense(inst, olic);

        // message sending commits transaction, need to attach to the new one
        try ( TransactionContext tc = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em2 = this.sctx.createConfigEM();
            LicenseStorage lic = em2.find(LicenseStorage.class, olic.getId());
            InstanceStructuralObjectImpl pinst = this.persistenceUtil.fetch(em2, InstanceStructuralObjectImpl.class, to);

            if ( lic.getVersion() != olic.getVersion() || pinst.getVersion() != inst.getVersion() ) {
                new ModelObjectModifiedException(LicenseStorage.class, lic.getId());
            }

            LicenseStorage curAssign = pinst.getAssignedLicense();
            if ( curAssign != null && curAssign.getId() != lic.getId() ) {
                curAssign.setAssignedTo(null);
                em2.persist(curAssign);
                pinst.setAssignedLicense(null);
            }

            lic.setAssignedTo(pinst);
            pinst.setAssignedLicense(lic);
            em2.persist(lic);
            em2.persist(pinst);
            em2.flush();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.LicensingService#getLicensesAt(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      boolean)
     */
    @Override
    @RequirePermissions ( "licensing:list" )
    public Set<LicenseInfo> getLicensesAt ( StructuralObject anchor, boolean includeInherited )
            throws ModelObjectNotFoundException, ModelServiceException {
        @NonNull
        EntityManager em = this.sctx.createConfigEM();

        @NonNull
        AbstractStructuralObjectImpl panchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
        this.authz.checkAccess(panchor, "licensing:list"); //$NON-NLS-1$
        if ( !includeInherited ) {
            return wrap(panchor.getLicensePool());
        }
        return wrap(em.createQuery(getLicenseObjectsInScope(em, panchor)).getResultList());
    }


    private static <T extends AbstractConfigurationObject<?>> CriteriaQuery<LicenseStorage> getLicenseObjectsInScope ( EntityManager em,
            AbstractStructuralObjectImpl obj ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LicenseStorage> cq = cb.createQuery(LicenseStorage.class);
        Root<LicenseStorage> tbl = cq.from(LicenseStorage.class);
        EntityType<LicenseStorage> model = tbl.getModel();
        EntityType<AbstractStructuralObjectImpl> structModel = em.getMetamodel().entity(AbstractStructuralObjectImpl.class);
        Join<LicenseStorage, AbstractStructuralObjectImpl> struct = tbl
                .join(model.getSingularAttribute("anchor", AbstractStructuralObjectImpl.class)); //$NON-NLS-1$
        cq.where(TreeUtil.createAncestorQueryIncludingSelf(obj, em, struct, structModel));
        // order by descending depth => list will contain the sequence up to the root
        cq.orderBy(cb.desc(TreeUtil.getDepthPath(em, struct, structModel)));
        return cq;
    }


    /**
     * @param licensePool
     * @return
     */
    private Set<LicenseInfo> wrap ( Collection<LicenseStorage> licensePool ) {
        Set<LicenseInfo> ls = new HashSet<>();
        for ( LicenseStorage s : licensePool ) {
            try {
                ls.add(LicenseInfo.fromLicense(this.licenseParser.parseLicense(s.getData())));
            }
            catch ( LicensingException e ) {
                log.warn("Failed to parse license data", e); //$NON-NLS-1$
            }
        }
        return ls;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.LicensingService#getApplicableLicenses(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "licensing:list" )
    public Set<LicenseInfo> getApplicableLicenses ( InstanceStructuralObject anchor ) throws ModelObjectNotFoundException, ModelServiceException {
        @NonNull
        EntityManager em = this.sctx.createConfigEM();

        @NonNull
        InstanceStructuralObjectImpl panchor = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, anchor);
        this.authz.checkAccess(panchor, "licensing:list"); //$NON-NLS-1$
        Collection<?> requiredServiceTypes = this.imageTypes.getDescriptor(panchor.getImageType()).getForcedServiceTypes();
        List<LicenseStorage> resultList = em.createQuery(getApplicableLicenseObjectsInScope(em, panchor)).getResultList();
        Collection<LicenseStorage> filtered = new LinkedList<>();
        for ( LicenseStorage ls : resultList ) {
            if ( !ls.getServiceTypes().containsAll(requiredServiceTypes) ) {
                continue;
            }
            filtered.add(ls);
        }
        return wrap(filtered);
    }


    private static <T extends AbstractConfigurationObject<?>> CriteriaQuery<LicenseStorage> getApplicableLicenseObjectsInScope ( EntityManager em,
            AbstractStructuralObjectImpl obj ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LicenseStorage> cq = cb.createQuery(LicenseStorage.class);
        Root<LicenseStorage> tbl = cq.from(LicenseStorage.class);
        EntityType<LicenseStorage> model = tbl.getModel();
        EntityType<AbstractStructuralObjectImpl> structModel = em.getMetamodel().entity(AbstractStructuralObjectImpl.class);
        Join<LicenseStorage, AbstractStructuralObjectImpl> struct = tbl
                .join(model.getSingularAttribute("anchor", AbstractStructuralObjectImpl.class)); //$NON-NLS-1$

        cq.where(
            cb.and(
                TreeUtil.createAncestorQueryIncludingSelf(obj, em, struct, structModel),
                cb.isNull(tbl.get(model.getSingularAttribute("assignedTo", AbstractStructuralObjectImpl.class))), //$NON-NLS-1$
                cb.greaterThan(tbl.get(model.getSingularAttribute("expiration", DateTime.class)), DateTime.now()), //$NON-NLS-1$
                cb.lessThan(tbl.get(model.getSingularAttribute("issued", DateTime.class)), DateTime.now()))); //$NON-NLS-1$
        // order by descending depth => list will contain the sequence up to the root
        cq.orderBy(cb.desc(TreeUtil.getDepthPath(em, struct, structModel)));
        return cq;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#connected(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void connected ( AgentConfig c ) {
        if ( c == null ) {
            return;
        }
        UUID id = c.getId();
        if ( id == null ) {
            return;
        }

        AgentMessageTarget tgt = new AgentMessageTarget(id);
        LicenseInfoRequest linfo = new LicenseInfoRequest(tgt, this.messagingClient.getMessageSource());

        try {
            @Nullable
            LicenseInfoResponse lir = this.messagingClient.sendMessage(linfo);

            LicenseInfo li = null;
            if ( lir != null ) {
                li = lir.getInfo();
            }
            else {
                log.debug("No license received from agent"); //$NON-NLS-1$
            }

            try ( TransactionContext tc = this.sctx.getTransactionService().ensureTransacted() ) {
                @NonNull
                EntityManager em = this.sctx.createConfigEM();
                InstanceStructuralObjectImpl is = null;
                try {
                    is = this.instanceService.getInstanceForAgent(em, id);
                }
                catch ( ModelObjectNotFoundException e ) {
                    log.debug("No instance for agent", e); //$NON-NLS-1$
                }

                if ( li != null && li.getLicenseId() != null ) {
                    log.debug("Checking agent license"); //$NON-NLS-1$
                    handleAgentLicense(tgt, li, em, is);
                }
                else if ( is != null && is.getAssignedLicense() != null ) {
                    // has an assigned license but does not known about it
                    if ( log.isDebugEnabled() ) {
                        log.debug("Setting license on agent " + is.getDisplayName()); //$NON-NLS-1$
                    }
                    doSetLicense(tgt, LicenseInfo.fromLicense(this.licenseParser.parseLicense(is.getAssignedLicense().getData())));
                }
                else if ( is != null && li != null && li.getLicenseId() == null && li.getExpirationDate() != null ) {
                    // update expiration date
                    log.debug("Updating demo expiration"); //$NON-NLS-1$
                    is.setDemoExpiration(li.getExpirationDate());
                    em.persist(is);
                    em.flush();
                }
                else {
                    // ignore
                }
                tc.commit();
            }
        }
        catch (
            MessagingException |
            LicensingException |
            ModelServiceException |
            ModelObjectConflictException |
            ModelObjectNotFoundException |
            PersistenceException |
            IOException |
            InterruptedException e ) {
            log.warn("Failed to get agent license info", e); //$NON-NLS-1$
        }
    }


    /**
     * @param li
     * @param em
     * @param is
     * @throws LicensingException
     * @throws IOException
     * @throws ModelObjectConflictException
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws InterruptedException
     * @throws MessagingException
     */
    private void handleAgentLicense ( @NonNull AgentMessageTarget agent, LicenseInfo li, EntityManager em, InstanceStructuralObjectImpl is )
            throws LicensingException, IOException, ModelObjectConflictException, ModelObjectNotFoundException, ModelServiceException,
            MessagingException, InterruptedException {
        if ( log.isDebugEnabled() ) {
            log.debug("Agent is using license " + li.getLicenseId()); //$NON-NLS-1$
        }

        LicenseStorage knwn = em.find(LicenseStorage.class, li.getLicenseId());
        if ( knwn == null || knwn.getAssignedTo() == null ) {
            handleUnknownLicense(li, em, is, knwn);
        }
        else {
            handleKnownLicense(agent, is, knwn);
        }
    }


    /**
     * @param li
     * @param em
     * @param is
     * @param knwn
     * @throws LicensingException
     * @throws IOException
     * @throws ModelObjectConflictException
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private void handleUnknownLicense ( LicenseInfo li, EntityManager em, InstanceStructuralObjectImpl is, LicenseStorage knwn )
            throws LicensingException, IOException, ModelObjectConflictException, ModelObjectNotFoundException, ModelServiceException {
        if ( is != null && knwn == null ) {
            // license is unknown and we have a host
            log.debug("Importing license"); //$NON-NLS-1$
            LicenseStorage ls = addLicense(em, is, new DataHandler(new ByteArrayDataSource(li.getData(), "application/octect-stream"))); //$NON-NLS-1$
            assignLicense(em, is, ls);
        }
        else if ( is != null && knwn != null ) {
            // license is unassigned and we have a host
            if ( TreeUtil.isAncestorOrSame(is, knwn.getAnchor()) ) {
                log.debug("Assinging license"); //$NON-NLS-1$
                assignLicense(em, is, knwn);
            }
            else {
                log.debug("Cannot assign license"); //$NON-NLS-1$
            }
        }
        else {
            // license is unknown and we don't have a host, ignore
            log.debug("Don't have a host"); //$NON-NLS-1$
        }
    }


    /**
     * @param agent
     * @param is
     * @param knwn
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws MessagingException
     * @throws InterruptedException
     */
    private void handleKnownLicense ( @NonNull AgentMessageTarget agent, InstanceStructuralObjectImpl is, LicenseStorage knwn )
            throws ModelObjectNotFoundException, ModelServiceException, MessagingException, InterruptedException {
        // the license is known and assigned
        if ( Objects.equals(knwn.getAssignedTo(), is) ) {
            // and assigned to the host, ok
            log.debug("License is properly assigned"); //$NON-NLS-1$
        }
        else if ( is != null ) {
            // and assigned to another host, remove it from the host
            log.warn("Duplicate license, removing license from host " + is.getDisplayName()); //$NON-NLS-1$
            doSetLicense(agent, null);
        }
        else {
            log.warn("Duplicate license, removing license from agent " + agent.getAgentId()); //$NON-NLS-1$
            doSetLicense(agent, null);
        }
    }


    /**
     * @param agent
     * @param license
     * @throws MessagingException
     * @throws InterruptedException
     */
    private void doSetLicense ( @NonNull AgentMessageTarget agent, LicenseInfo license ) throws MessagingException, InterruptedException {
        LicenseSetRequest msg = new LicenseSetRequest(agent, this.messagingClient.getMessageSource());
        msg.setLicense(license);
        this.messagingClient.sendMessage(msg);
    }


    @Override
    public void connecting ( AgentConfig c ) {
        // ignore
    }


    @Override
    public void disconnecting ( AgentConfig c ) {
        // ignore
    }


    @Override
    public void failed ( AgentConfig c ) {
        // ignore
    }
}
