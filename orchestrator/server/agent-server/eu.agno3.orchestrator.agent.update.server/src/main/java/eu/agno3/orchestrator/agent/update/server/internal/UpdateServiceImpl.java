/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.server.internal;


import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.agent.update.server.UpdateServiceInternal;
import eu.agno3.orchestrator.agent.update.server.data.AgentUpdateStateCache;
import eu.agno3.orchestrator.agent.update.server.data.DescriptorCache;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.gui.connector.GuiNotificationEvent;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.targets.AnyServerTarget;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.orchestrator.system.update.InstanceUpdateStatus;
import eu.agno3.orchestrator.system.update.UpdateDescriptor;
import eu.agno3.orchestrator.system.update.UpdateDescriptorParser;
import eu.agno3.orchestrator.system.update.UpdateState;
import eu.agno3.orchestrator.system.update.jobs.UpdateCheckJob;
import eu.agno3.orchestrator.system.update.jobs.UpdateInstallJob;
import eu.agno3.orchestrator.system.update.jobs.UpdateRevertJob;
import eu.agno3.orchestrator.system.update.msg.AgentUpdateStatusRequest;
import eu.agno3.orchestrator.system.update.msg.AgentUpdateStatusResponse;
import eu.agno3.orchestrator.system.update.service.AgentUpdateService;
import eu.agno3.orchestrator.system.update.service.AgentUpdateServiceDescriptor;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;
import eu.agno3.runtime.xml.XMLParserConfigurationException;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    AgentUpdateService.class, UpdateServiceInternal.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.system.update.service.AgentUpdateService",
    targetNamespace = AgentUpdateServiceDescriptor.NAMESPACE,
    serviceName = AgentUpdateServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/agent/update" )
public class UpdateServiceImpl implements AgentUpdateService, UpdateServiceInternal {

    private static final Logger log = Logger.getLogger(UpdateServiceImpl.class);

    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private ObjectAccessControl authz;

    private AgentServerService agentService;
    private UpdateDescriptorParser updateParser;

    private JobCoordinator jobCoordinator;

    private XmlMarshallingService xmlMarshaller;
    private XmlParserFactory xmlParser;

    private Duration minimumUpdateInterval;
    private Duration retainInterval;
    private Set<? extends String> alwaysCheckStreams;

    private MessagingClient<ServerMessageSource> msgClient;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.minimumUpdateInterval = ConfigUtil.parseDuration(ctx.getProperties(), "minimumUpdateInterval", Duration.standardMinutes(2)); //$NON-NLS-1$
        this.retainInterval = ConfigUtil.parseDuration(ctx.getProperties(), "retainInterval", Duration.standardDays(31 * 3)); //$NON-NLS-1$
        this.alwaysCheckStreams = ConfigUtil.parseStringSet(
            ctx.getProperties(),
            "alwaysCheckStream", //$NON-NLS-1$
            new HashSet<>(Arrays.asList("RELEASE"))); //$NON-NLS-1$
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
    protected synchronized void setUpdateParser ( UpdateDescriptorParser udp ) {
        this.updateParser = udp;
    }


    protected synchronized void unsetUpdateParser ( UpdateDescriptorParser udp ) {
        if ( this.updateParser == udp ) {
            this.updateParser = null;
        }
    }


    @Reference
    protected synchronized void setXmlMarshaller ( XmlMarshallingService xms ) {
        this.xmlMarshaller = xms;
    }


    protected synchronized void unsetXmlMarshaller ( XmlMarshallingService xms ) {
        if ( this.xmlMarshaller == xms ) {
            this.xmlMarshaller = null;
        }
    }


    @Reference
    protected synchronized void setXmlParser ( XmlParserFactory xpf ) {
        this.xmlParser = xpf;
    }


    protected synchronized void unsetXmlParser ( XmlParserFactory xpf ) {
        if ( this.xmlParser == xpf ) {
            this.xmlParser = null;
        }
    }


    @Reference
    protected synchronized void setAgentService ( AgentServerService as ) {
        this.agentService = as;
    }


    protected synchronized void unsetAgentService ( AgentServerService as ) {
        if ( this.agentService == as ) {
            this.agentService = null;
        }
    }


    @Reference
    protected synchronized void setJobCoordinator ( JobCoordinator coord ) {
        this.jobCoordinator = coord;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator coord ) {
        if ( this.jobCoordinator == coord ) {
            this.jobCoordinator = null;
        }
    }


    @Reference
    protected synchronized void setMsgClient ( MessagingClient<ServerMessageSource> mc ) {
        this.msgClient = mc;
    }


    protected synchronized void unsetMsgClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.msgClient == mc ) {
            this.msgClient = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.service.AgentUpdateService#checkForUpdates(java.util.Set)
     */
    @Override
    @RequirePermissions ( "update" )
    public JobInfo checkForUpdates ( Set<String> extraStreams ) throws ModelServiceException, ModelObjectNotFoundException,
            ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {
        UserPrincipal up = SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class);
        UpdateCheckJob updateCheckJob = createUpdateJob(extraStreams, up);
        try {
            return this.jobCoordinator.queueJob(updateCheckJob);
        }
        catch ( JobQueueException e ) {
            throw new ModelServiceException("Failed to queue job", e); //$NON-NLS-1$
        }
    }


    /**
     * @param extraStreams
     * @return an update check job instance
     */
    @Override
    public UpdateCheckJob createUpdateJob ( Set<String> extraStreams, UserPrincipal owner ) {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();

        TypedQuery<String> imTypesQuery = em.createQuery("SELECT DISTINCT s.imageType FROM InstanceStructuralObjectImpl s", String.class); //$NON-NLS-1$
        Set<String> imageTypes = new HashSet<>(imTypesQuery.getResultList());

        TypedQuery<String> streamsQuery = em.createQuery("SELECT DISTINCT s.releaseStream FROM InstanceStructuralObjectImpl s", String.class); //$NON-NLS-1$

        Set<String> streams = new HashSet<>();
        for ( String stream : streamsQuery.getResultList() ) {
            if ( stream != null ) {
                streams.add(stream);
            }
        }
        streams.addAll(this.alwaysCheckStreams);

        if ( extraStreams != null ) {
            streams.addAll(extraStreams);
        }

        DateTime minUpdate = DateTime.now().minus(this.minimumUpdateInterval);
        DateTime retainTime = DateTime.now().minus(this.retainInterval);

        UpdateCheckJob updateCheckJob = new UpdateCheckJob();
        updateCheckJob.setUpdateImageTypes(imageTypes);
        updateCheckJob.setUpdateStreams(streams);
        updateCheckJob.setRetainAfterTime(retainTime);
        updateCheckJob.setUpdateBeforeTime(minUpdate);
        updateCheckJob.setOwner(owner);
        updateCheckJob.setTarget(new AnyServerTarget());
        return updateCheckJob;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.service.AgentUpdateService#getUpdateStatus(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "update" )
    public InstanceUpdateStatus getUpdateStatus ( InstanceStructuralObject inst, String overrideStream )
            throws ModelObjectNotFoundException, ModelServiceException {
        InstanceUpdateStatus status = new InstanceUpdateStatus();
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        if ( inst == null ) {
            throw new ModelObjectNotFoundException(InstanceStructuralObject.class, null);
        }
        InstanceStructuralObject persistent = PersistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, inst.getId());
        this.authz.checkAccess(persistent, "update"); //$NON-NLS-1$
        return buildInstanceUpdateStatus(overrideStream, status, persistent, setupUpdateStatus(status, persistent));
    }


    /**
     * @param overrideStream
     * @param status
     * @param persistent
     * @param st
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private InstanceUpdateStatus buildInstanceUpdateStatus ( String overrideStream, InstanceUpdateStatus status, InstanceStructuralObject persistent,
            UpdateState st ) throws ModelServiceException, ModelObjectNotFoundException {
        if ( status.getCurrentStream() == null ) {
            status.setCurrentStream(persistent.getReleaseStream() != null ? persistent.getReleaseStream() : "RELEASE"); //$NON-NLS-1$
        }

        String realStream = !StringUtils.isBlank(overrideStream) ? overrideStream : status.getCurrentStream();

        if ( log.isDebugEnabled() ) {
            log.debug("Current stream is " + status.getCurrentStream()); //$NON-NLS-1$
            log.debug("Checked stream is " + realStream); //$NON-NLS-1$
            log.debug("Current sequence is " + status.getCurrentSequence()); //$NON-NLS-1$
        }

        UpdateDescriptor descriptor = getDescriptor(persistent.getImageType(), realStream, status.getCurrentSequence());
        if ( descriptor != null ) {
            status.setLatestDescriptor(descriptor);
            status.setDescriptorStream(realStream);
            status.setState(UpdateState.NEEDSUPDATE);
        }
        else if ( status.getCurrentSequence() != null ) {
            status.setState(UpdateState.UPTODATE);
        }
        else if ( st != null ) {
            status.setState(st);
        }
        else {
            status.setState(UpdateState.UNKNOWN);
        }

        return status;
    }


    /**
     * @param status
     * @param persistent
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private UpdateState setupUpdateStatus ( InstanceUpdateStatus status, InstanceStructuralObject persistent )
            throws ModelObjectNotFoundException, ModelServiceException {
        @Nullable
        AgentUpdateStatusResponse agentOnlineUpdateStatus = this.agentService.isAgentOnline(persistent) ? getAgentOnlineUpdateStatus(persistent)
                : null;
        if ( agentOnlineUpdateStatus != null ) {
            status.setCurrentSequence(agentOnlineUpdateStatus.getCurrentSequence());
            status.setCurrentStream(agentOnlineUpdateStatus.getCurrentStream());
            status.setCurrentInstallDate(agentOnlineUpdateStatus.getCurrentInstallDate());
            status.setRevertTimestamp(agentOnlineUpdateStatus.getRevertTimestamp());
            status.setRevertSequence(agentOnlineUpdateStatus.getRevertSequence());
            status.setRevertStream(agentOnlineUpdateStatus.getRevertStream());
            status.setRebootIndicated(agentOnlineUpdateStatus.getRebootIndicated());
        }
        else {
            EntityManager oem = this.sctx.getOrchestratorEMF().createEntityManager();
            if ( persistent.getAgentId() != null ) {
                AgentUpdateStateCache sc = oem.find(AgentUpdateStateCache.class, persistent.getAgentId());
                if ( sc != null ) {
                    status.setCurrentStream(sc.getCurrentStream());
                    status.setCurrentSequence(sc.getCurrentSequence());
                    status.setState(sc.getCurrentState());
                    status.setCurrentInstallDate(sc.getLastUpdated());
                    status.setRebootIndicated(sc.getRebootIndicated());
                    return sc.getCurrentState();
                }
            }
        }
        return null;
    }


    /**
     * @param persistent
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private @Nullable AgentUpdateStatusResponse getAgentOnlineUpdateStatus ( InstanceStructuralObject persistent )
            throws ModelObjectNotFoundException, ModelServiceException {
        try {
            @NonNull
            AgentMessageTarget messageTarget = this.agentService.getMessageTarget(persistent);
            try {
                AgentUpdateStatusRequest msg = new AgentUpdateStatusRequest(messageTarget);
                msg.setOrigin(this.msgClient.getMessageSource());
                @Nullable
                AgentUpdateStatusResponse resp = this.msgClient.sendMessage(msg);
                if ( resp != null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Agent has sequence %s stream %s", resp.getCurrentSequence(), resp.getCurrentStream())); //$NON-NLS-1$
                    }
                    return resp;
                }
            }
            catch (
                MessagingException |
                InterruptedException e ) {
                log.warn("Failed to get agent update status", e); //$NON-NLS-1$
            }
        }
        catch ( AgentDetachedException e ) {
            log.debug("Agent is detached", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param inst
     * @param payload
     */
    private void publishGUIEvent ( InstanceStructuralObject inst, String payload ) {
        String path = "/instance/" + inst.getId() + "/update_status"; //$NON-NLS-1$ //$NON-NLS-2$
        try {
            this.msgClient.publishEvent(new GuiNotificationEvent(this.msgClient.getMessageSource(), path, payload)); // $NON-NLS-1$
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.warn("Failed to publish update status event", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.server.UpdateServiceInternal#foundUpdates(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String)
     */
    @Override
    public void foundUpdates ( InstanceStructuralObject inst, String stream ) {
        if ( stream.equals(inst.getReleaseStream()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found updates for %s in %s", inst, stream)); //$NON-NLS-1$
            }

            try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
                EntityManager em = this.sctx.createOrchEM();
                if ( inst.getAgentId() != null ) {
                    AgentUpdateStateCache sc = em.find(AgentUpdateStateCache.class, inst.getAgentId());
                    if ( sc == null ) {
                        sc = new AgentUpdateStateCache();
                        sc.setAgentId(inst.getAgentId());
                    }
                    sc.setCurrentState(UpdateState.NEEDSUPDATE);
                    em.persist(sc);
                    em.flush();
                    tx.commit();
                }
            }
            catch ( ModelServiceException e ) {
                log.warn("Failed to update status cache", e); //$NON-NLS-1$
            }

            publishGUIEvent(inst, "UPDATE_AVAILABLE"); //$NON-NLS-1$
        }
        else {
            publishGUIEvent(inst, "REFRESH_UPDATES"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.server.UpdateServiceInternal#updated(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, long, boolean)
     */
    @Override
    public void updated ( InstanceStructuralObject inst, String updatedStream, long updatedSequence, boolean rebootIndicated ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Updated %s to %d (%s)", inst, updatedSequence, updatedStream)); //$NON-NLS-1$
        }

        try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em = this.sctx.createOrchEM();
            if ( inst.getAgentId() != null ) {
                AgentUpdateStateCache sc = em.find(AgentUpdateStateCache.class, inst.getAgentId());
                if ( sc == null ) {
                    sc = new AgentUpdateStateCache();
                    sc.setAgentId(inst.getAgentId());
                }
                sc.setCurrentState(UpdateState.UPTODATE);
                sc.setCurrentStream(updatedStream);
                sc.setCurrentSequence(updatedSequence);
                sc.setLastUpdated(DateTime.now());
                sc.setRebootIndicated(rebootIndicated);
                em.persist(sc);
                em.flush();
                tx.commit();
            }
        }
        catch ( ModelServiceException e ) {
            log.warn("Failed to update status cache", e); //$NON-NLS-1$
        }

        publishGUIEvent(inst, "UPDATED"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.server.UpdateServiceInternal#reverted(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, long)
     */
    @Override
    public void reverted ( @NonNull InstanceStructuralObject inst, String revertedToStream, long revertedToSequence ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Reverted %s to %d (%s)", inst, revertedToStream, revertedToSequence)); //$NON-NLS-1$
        }

        try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em = this.sctx.createOrchEM();
            if ( inst.getAgentId() != null ) {
                AgentUpdateStateCache sc = em.find(AgentUpdateStateCache.class, inst.getAgentId());
                if ( sc == null ) {
                    sc = new AgentUpdateStateCache();
                    sc.setAgentId(inst.getAgentId());
                }
                sc.setCurrentState(UpdateState.UPTODATE);
                sc.setCurrentStream(revertedToStream);
                sc.setCurrentSequence(revertedToSequence);
                sc.setLastUpdated(DateTime.now());
                sc.setRebootIndicated(false);
                em.persist(sc);
                em.flush();
                tx.commit();
            }
        }
        catch ( ModelServiceException e ) {
            log.warn("Failed to update status cache", e); //$NON-NLS-1$
        }

        publishGUIEvent(inst, "REVERTED"); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.service.AgentUpdateService#installUpdates(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, long)
     */
    @Override
    @RequirePermissions ( "update:install" )
    public JobInfo installUpdates ( InstanceStructuralObject inst, String stream, long sequence )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        InstanceStructuralObject persistent = PersistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, inst.getId());
        this.authz.checkAccess(persistent, "update:install"); //$NON-NLS-1$
        UpdateInstallJob j = new UpdateInstallJob();
        UpdateDescriptor descriptor = getDescriptor(persistent.getImageType(), stream, sequence - 1);
        if ( descriptor == null ) {
            throw new ModelServiceException("Incomplete descriptor for " + persistent.getImageType()); //$NON-NLS-1$
        }

        j.setDescriptorStream(stream);
        j.setDescriptor(descriptor);
        j.setOwner(SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class));
        return this.agentService.submitJob(persistent, j);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.service.AgentUpdateService#revert(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, long)
     */
    @Override
    @RequirePermissions ( "update:revert" )
    public JobInfo revert ( InstanceStructuralObject instance, String revertStream, long revertSequence )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        InstanceStructuralObject persistent = PersistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance.getId());
        this.authz.checkAccess(persistent, "update:revert"); //$NON-NLS-1$
        UpdateRevertJob j = new UpdateRevertJob();
        j.setRevertStream(revertStream);
        j.setRevertSequence(revertSequence);
        j.setOwner(SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class));
        return this.agentService.submitJob(persistent, j);
    }


    /**
     * @param imageType
     * @param stream
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private UpdateDescriptor getDescriptor ( String imageType, String stream, Long sequence )
            throws ModelServiceException, ModelObjectNotFoundException {

        EntityManager orchem = this.sctx.createOrchEM();
        TypedQuery<DescriptorCache> cacheQuery = orchem.createQuery(
            "SELECT d FROM DescriptorCache d WHERE " //$NON-NLS-1$
                    + "d.stream = :stream AND d.imageType = :imageType " + //$NON-NLS-1$
                    ( sequence != null ? "AND d.sequence > :sequence " : StringUtils.EMPTY ) + //$NON-NLS-1$
                    "ORDER BY d.sequence DESC", //$NON-NLS-1$
            DescriptorCache.class);

        cacheQuery.setParameter("stream", stream); //$NON-NLS-1$
        cacheQuery.setParameter("imageType", imageType); //$NON-NLS-1$
        if ( sequence != null ) {
            cacheQuery.setParameter("sequence", sequence); //$NON-NLS-1$
        }
        cacheQuery.setMaxResults(1);

        List<DescriptorCache> resultList = cacheQuery.getResultList();
        if ( resultList.isEmpty() ) {
            return null;
        }

        DescriptorCache latest = resultList.get(0);

        if ( latest.getData() == null ) {
            return null;
        }

        try {
            return this.xmlMarshaller
                    .unmarshall(UpdateDescriptor.class, this.xmlParser.createStreamReader(new ByteArrayInputStream(latest.getData())));
        }
        catch (
            XMLBindingException |
            XMLParserConfigurationException e ) {
            throw new ModelServiceException("Failed to decode update descriptor", e); //$NON-NLS-1$
        }

    }
}
