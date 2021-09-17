/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectNotFoundFault;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectReferentialIntegrityFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.events.ServiceConfigAppliedEvent;
import eu.agno3.orchestrator.config.model.events.ServiceConfigFailedEvent;
import eu.agno3.orchestrator.config.model.jobs.ConfigApplyJob;
import eu.agno3.orchestrator.config.model.jobs.ConfigApplyTrackingJob;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.model.jobs.ResourceLibrarySynchronizationJob;
import eu.agno3.orchestrator.config.model.jobs.ResourceLibraryTrackingJob;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectState;
import eu.agno3.orchestrator.config.model.realm.context.ConfigApplyContext;
import eu.agno3.orchestrator.config.model.realm.server.service.ConfigApplyServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.ModelObjectChallengeUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.OneOffUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.TopSort;
import eu.agno3.orchestrator.config.model.realm.server.util.TopSort.TopSortException;
import eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService;
import eu.agno3.orchestrator.config.model.realm.service.ConfigApplyServiceDescriptor;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.orchestrator.jobs.targets.AnyServerTarget;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ConfigApplyService.class, ConfigApplyServerService.class, SOAPWebService.class
}, configurationPid = "config.apply" )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService",
    targetNamespace = ConfigApplyServiceDescriptor.NAMESPACE,
    serviceName = ConfigApplyServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/applyConfig" )
public class ConfigApplyServiceImpl implements ConfigApplyService, ConfigApplyServerService, SOAPWebService {

    private static final Logger log = Logger.getLogger(ConfigApplyServiceImpl.class);

    private DefaultServerServiceContext sctx;
    private ServiceServerService serviceService;
    private ObjectAccessControl authz;
    private JobCoordinator jobCoordinator;
    private InheritanceServerService inheritanceService;
    private PersistenceUtil persistenceUtil;
    private ResourceLibraryServerService resLibraryService;
    private ModelObjectChallengeUtil challengeUtil;

    private Duration configJobTimeout = Duration.standardMinutes(10);


    @Activate
    @Modified
    protected synchronized void configure ( ComponentContext ctx ) {
        this.configJobTimeout = ConfigUtil.parseDuration(ctx.getProperties(), "configJobTimeout", Duration.standardMinutes(10)); //$NON-NLS-1$
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
    protected synchronized void setInheritanceService ( InheritanceServerService is ) {
        this.inheritanceService = is;
    }


    protected synchronized void unsetInheritanceService ( InheritanceServerService is ) {
        if ( this.inheritanceService == is ) {
            this.inheritanceService = null;
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
    protected synchronized void setResourceLibraryService ( ResourceLibraryServerService rls ) {
        this.resLibraryService = rls;
    }


    protected synchronized void unsetResourceLibraryService ( ResourceLibraryServerService rls ) {
        if ( this.resLibraryService == rls ) {
            this.resLibraryService = null;
        }
    }


    @Reference
    protected synchronized void setServiceService ( ServiceServerService ss ) {
        this.serviceService = ss;
    }


    protected synchronized void unsetServiceService ( ServiceServerService ss ) {
        if ( this.serviceService == ss ) {
            this.serviceService = null;
        }
    }


    @Reference
    protected synchronized void setJobCoordinator ( JobCoordinator jobCoord ) {
        this.jobCoordinator = jobCoord;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator jobCoord ) {
        if ( this.jobCoordinator == jobCoord ) {
            this.jobCoordinator = null;
        }
    }


    @Reference
    protected synchronized void setChallengeUtil ( ModelObjectChallengeUtil cu ) {
        this.challengeUtil = cu;
    }


    protected synchronized void unsetChallengeUtil ( ModelObjectChallengeUtil cu ) {
        if ( this.challengeUtil == cu ) {
            this.challengeUtil = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelObjectReferentialIntegrityException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService#preApplyServiceConfiguration(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.lang.Long)
     */
    @Override
    @RequirePermissions ( "config:apply" )
    public ConfigApplyContext preApplyServiceConfiguration ( ServiceStructuralObject service, Long revision )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException {
        EntityManager em = this.sctx.createConfigEM();

        if ( service == null ) {
            throw new ModelServiceException();
        }

        long latestRevision = PersistenceUtil.getMostRecentRevision(em);
        ServiceStructuralObjectImpl persistent = getPersistentService(em, service, revision);
        this.authz.checkAccess(persistent, "config:apply"); //$NON-NLS-1$

        if ( persistent == null ) {
            throw new ModelServiceException();
        }

        Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistent);

        if ( !parent.isPresent() || ! ( parent.get() instanceof InstanceStructuralObjectImpl ) ) {
            throw new ModelServiceException("Parent must be an instance"); //$NON-NLS-1$
        }

        InstanceStructuralObjectImpl instance = (InstanceStructuralObjectImpl) parent.get();

        if ( instance == null ) {
            throw new ModelServiceException();
        }

        ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> descriptor = this.sctx.getServiceTypeRegistry()
                .getDescriptor(service.getServiceType());
        validateServiceIntegrity(em, instance, descriptor, Collections.EMPTY_SET);

        AbstractConfigurationInstance<@Nullable ?> persistentConfig = (AbstractConfigurationInstance<@Nullable ?>) persistent.getConfiguration();
        if ( persistentConfig == null ) {
            throw new ModelServiceException("Service is unconfigured"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found service %s with state %s", service, service.getState())); //$NON-NLS-1$
        }

        persistentConfig.setRevision(revision != null ? revision : latestRevision);
        ConfigurationInstance effectiveConfig = this.inheritanceService.getEffective(em, PersistenceUtil.unproxyDeep(persistentConfig), null);

        ConfigApplyContext ap = new ConfigApplyContext();
        ap.setRevision(revision != null ? revision : latestRevision);
        ap.setChallenges(getChallenges(em, effectiveConfig));
        return ap;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService#applyServiceConfiguration(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      java.lang.Long, eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo)
     */
    @Override
    @RequirePermissions ( "config:apply" )
    public JobInfo applyServiceConfiguration ( ServiceStructuralObject service, Long revision, ConfigApplyInfo info )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException {
        EntityManager em = this.sctx.createConfigEM();

        if ( service == null ) {
            throw new ModelServiceException();
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Applying revision " + revision); //$NON-NLS-1$
        }

        return applyConfiguration(em, service, revision, info);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService#preApplyInstanceConfigurations(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.Long)
     */
    @Override
    @RequirePermissions ( "config:apply" )
    public ConfigApplyContext preApplyInstanceConfigurations ( InstanceStructuralObject instance, Long revision )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException {
        EntityManager em = this.sctx.createConfigEM();
        if ( instance == null ) {
            throw new ModelServiceException();
        }

        InstanceStructuralObjectImpl pinst = PersistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance.getId());

        UUID agentId = pinst.getAgentId();
        if ( agentId == null ) {
            throw new ModelServiceException("Agent is not registered for this instance"); //$NON-NLS-1$
        }

        List<AbstractStructuralObjectImpl> children = TreeUtil.getChildren(em, AbstractStructuralObjectImpl.class, pinst);
        Set<@NonNull ServiceStructuralObjectImpl> applying = new HashSet<>();
        Set<String> applyingServiceTypes = new HashSet<>();
        List<ConfigApplyChallenge> challenges = new ArrayList<>();
        long latestRevision = PersistenceUtil.getMostRecentRevision(em);

        for ( AbstractStructuralObjectImpl child : children ) {
            if ( ! ( child instanceof ServiceStructuralObjectImpl ) ) {
                continue;
            }

            if ( !this.authz.hasAccess(child, "config:apply") ) { //$NON-NLS-1$
                continue;
            }

            ServiceStructuralObjectImpl service = revision != null ? getPersistentService(em, (ServiceStructuralObject) child, revision)
                    : (ServiceStructuralObjectImpl) child;

            AbstractConfigurationInstance<@Nullable ?> persistentConfig = (AbstractConfigurationInstance<@Nullable ?>) service.getConfiguration();
            if ( persistentConfig == null ) {
                continue;
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found service %s with state %s", service, service.getState())); //$NON-NLS-1$
            }

            persistentConfig.setRevision(revision != null ? revision : latestRevision);
            ConfigurationInstance effectiveConfig = this.inheritanceService.getEffective(em, PersistenceUtil.unproxyDeep(persistentConfig), null);

            challenges.addAll(getChallenges(em, effectiveConfig));
        }

        for ( ServiceStructuralObjectImpl service : applying ) {
            ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> descriptor = this.sctx.getServiceTypeRegistry()
                    .getDescriptor(service.getServiceType());
            validateServiceIntegrity(em, pinst, descriptor, applyingServiceTypes);
        }

        ConfigApplyContext ap = new ConfigApplyContext();
        ap.setRevision(revision != null ? revision : latestRevision);
        ap.setChallenges(challenges);
        return ap;
    }


    /**
     * @param em
     * @param effectiveConfig
     * @return
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    private List<ConfigApplyChallenge> getChallenges ( EntityManager em, ConfigurationInstance effectiveConfig ) throws ModelServiceException {
        try {
            return this.challengeUtil.generateChallenges(effectiveConfig);
        }
        catch ( ModelObjectException e ) {
            throw new ModelServiceException("Failed to generate challenges", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService#applyInstanceConfigurations(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.Long, eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo)
     */
    @Override
    @RequirePermissions ( "config:apply" )
    public JobInfo applyInstanceConfigurations ( InstanceStructuralObject instance, Long revision, ConfigApplyInfo info )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException {
        EntityManager em = this.sctx.createConfigEM();
        if ( instance == null ) {
            throw new ModelServiceException();
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Applying all configs on " + instance); //$NON-NLS-1$
        }

        InstanceStructuralObjectImpl pinst = PersistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance.getId());

        UUID agentId = pinst.getAgentId();
        if ( agentId == null ) {
            throw new ModelServiceException("Agent is not registered for this instance"); //$NON-NLS-1$
        }

        long mostRecentRevision = PersistenceUtil.getMostRecentRevision(em);

        List<AbstractStructuralObjectImpl> children = TreeUtil.getChildren(em, AbstractStructuralObjectImpl.class, pinst);
        List<@NonNull Job> reslibraryJobs = new LinkedList<>();
        List<@NonNull ConfigurationJob> configJobs = new ArrayList<>();
        Set<@NonNull ServiceStructuralObjectImpl> applying = new HashSet<>();
        Set<String> applyingServiceTypes = new HashSet<>();
        Map<ServiceStructuralObjectImpl, ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance>> descriptors = new HashMap<>();

        for ( AbstractStructuralObjectImpl child : children ) {
            if ( ! ( child instanceof ServiceStructuralObjectImpl ) ) {
                continue;
            }

            if ( !this.authz.hasAccess(child, "config:apply") ) { //$NON-NLS-1$
                continue;
            }

            ServiceStructuralObjectImpl service = (ServiceStructuralObjectImpl) child;

            ServiceStructuralObjectImpl cfgService = revision != null ? getPersistentService(em, (ServiceStructuralObject) child, revision)
                    : (ServiceStructuralObjectImpl) child;

            AbstractConfigurationInstance<@Nullable ?> persistentConfig = (AbstractConfigurationInstance<@Nullable ?>) cfgService.getConfiguration();
            if ( persistentConfig == null ) {
                continue;
            }

            ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> sdesc = this.sctx.getServiceTypeRegistry()
                    .getDescriptor(service.getServiceType());

            descriptors.put(service, sdesc);

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found service %s with state %s", service, service.getState())); //$NON-NLS-1$
            }

            persistentConfig.setRevision(revision != null ? revision : mostRecentRevision);
            ConfigurationInstance effectiveConfig = this.inheritanceService.getEffective(em, PersistenceUtil.unproxyDeep(persistentConfig), null);

            reslibraryJobs.addAll(makeReslibrarySynchronizationJobs(sdesc, service, effectiveConfig, info, em));

            if ( !info.getForce() && service.getState() == ConfigurationState.APPLIED ) {
                // don't add config jobs for already applied configs unless forced
                continue;
            }

            ConfigurationJob cfgJob = makeConfigurationJob(pinst, service, pinst, agentId, sdesc, effectiveConfig, info);
            if ( cfgJob != null ) {
                applying.add(service);
                applyingServiceTypes.add(service.getServiceType());
                configJobs.add(cfgJob);
            }
        }

        try {
            configJobs = sortServices(configJobs, descriptors);
        }
        catch ( TopSortException e ) {
            throw new ModelServiceException("Cyclic services dependencies", e); //$NON-NLS-1$
        }

        ConfigApplyJob j = buildJobs(pinst, reslibraryJobs, configJobs);

        j.setName("Configure services"); //$NON-NLS-1$
        j.setOwner(getUserPrincipal());
        j.setDeadline(DateTime.now().plus(this.configJobTimeout));
        j.setTarget(new AnyServerTarget());
        j.setServices(new HashSet<>(descriptors.keySet()));

        for ( ServiceStructuralObjectImpl service : applying ) {
            ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> descriptor = this.sctx.getServiceTypeRegistry()
                    .getDescriptor(service.getServiceType());
            validateServiceIntegrity(em, pinst, descriptor, applyingServiceTypes);
            this.serviceService.setConfigurationState(em, pinst, service, ConfigurationState.APPLYING, false);
        }

        try {
            JobInfo ji = this.jobCoordinator.queueJob(j);

            if ( ji == null ) {
                throw new ModelServiceException("Queued job is null"); //$NON-NLS-1$
            }

            return ji;
        }
        catch ( JobQueueException e ) {
            throw new ModelServiceException("Failed to queue job", e); //$NON-NLS-1$
        }
    }


    /**
     * @param pinst
     * @param reslibraryJobs
     * @param configJobs
     * @return
     * @throws ModelServiceException
     */
    private ConfigApplyJob buildJobs ( InstanceStructuralObjectImpl pinst, List<@NonNull Job> reslibraryJobs,
            List<@NonNull ConfigurationJob> configJobs ) throws ModelServiceException {
        if ( !reslibraryJobs.isEmpty() ) {
            // add tracking job for setting the instance last sync
            log.debug("Adding reslibrary tracking job"); //$NON-NLS-1$
            ResourceLibraryTrackingJob tj = new ResourceLibraryTrackingJob();
            tj.setOwner(getUserPrincipal());
            tj.setLastModified(combineLastModified(reslibraryJobs));
            tj.setDeadline(DateTime.now().plusMinutes(10));
            tj.setTarget(new AnyServerTarget());
            tj.setAnchor(pinst);
            reslibraryJobs.add(tj);
        }

        ConfigApplyJob j = new ConfigApplyJob();
        j.setWeights(new ArrayList<>());
        j.getJobs().addAll(reslibraryJobs);
        int numLibrariesToSync = reslibraryJobs.size();
        int numConfigJobs = configJobs.size();
        for ( int i = 0; i < numLibrariesToSync; i++ ) {
            j.getWeights().add( ( numConfigJobs != 0 ? 0.3f : 1.0f ) / numLibrariesToSync);
        }

        j.getJobs().addAll(configJobs);

        for ( int i = 0; i < numConfigJobs; i++ ) {
            j.getWeights().add( ( numLibrariesToSync > 0 ? 0.7f : 1.0f ) / numConfigJobs);
        }

        ConfigApplyTrackingJob ctj = new ConfigApplyTrackingJob();
        ctj.setOwner(getUserPrincipal());
        ctj.setDeadline(DateTime.now().plus(this.configJobTimeout.multipliedBy(configJobs.size() + 1)));
        ctj.setTarget(new AnyServerTarget());
        ctj.setAnchor(pinst);
        j.getJobs().add(ctj);
        j.getWeights().add(0.0f);
        return j;
    }


    /**
     * @param reslibraryJobs
     * @return
     */
    private static DateTime combineLastModified ( List<@NonNull Job> reslibraryJobs ) {
        DateTime clm = null;
        for ( Job job : reslibraryJobs ) {
            if ( ! ( job instanceof ResourceLibrarySynchronizationJob ) ) {
                continue;
            }

            ResourceLibrarySynchronizationJob rlj = (ResourceLibrarySynchronizationJob) job;

            DateTime lm = rlj.getLastModified();
            if ( lm != null && ( clm == null || lm.isAfter(clm) ) ) {
                clm = lm;
            }
        }

        if ( clm == null ) {
            clm = DateTime.now();
        }

        return clm;
    }


    /**
     * @param configJobs
     * @param descriptors
     * @throws TopSortException
     */
    private static List<@NonNull ConfigurationJob> sortServices ( List<@NonNull ConfigurationJob> configJobs,
            Map<ServiceStructuralObjectImpl, ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance>> descriptors )
                    throws TopSortException {

        Map<String, Collection<@NonNull ConfigurationJob>> byServiceType = new HashMap<>();
        for ( ConfigurationJob j : configJobs ) {
            String stype = j.getService().getServiceType();
            Collection<ConfigurationJob> elems = byServiceType.get(stype);
            if ( elems == null ) {
                elems = new LinkedList<>();
                byServiceType.put(stype, elems);
            }
            elems.add(j);
        }

        return TopSort.topsort(configJobs, cj -> {
            ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> desc = descriptors.get(cj.getService());
            List<@NonNull ConfigurationJob> depends = new LinkedList<>();
            Set<@NonNull String> req = desc.getRequiredServices();
            for ( String reqsType : req ) {
                Collection<@NonNull ConfigurationJob> services = byServiceType.get(reqsType);
                if ( services != null ) {
                    depends.addAll(services);
                }
            }
            return depends;
        } , new Comparator<ConfigurationJob>() {

            @Override
            public int compare ( ConfigurationJob o1, ConfigurationJob o2 ) {
                // stabilize results
                return o1.getService().getId().compareTo(o2.getService().getId());
            }

        });
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ConfigApplyServerService#applyConfiguration(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject, java.lang.Long,
     *      eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo)
     */
    @Override
    public @NonNull JobInfo applyConfiguration ( @NonNull EntityManager em, @NonNull ServiceStructuralObject service, @Nullable Long revision,
            @Nullable ConfigApplyInfo info ) throws ModelObjectNotFoundException, ModelServiceException, ModelObjectReferentialIntegrityException,
                    AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {
        try {
            ServiceStructuralObjectImpl persistent = getPersistentService(em, service, revision);
            this.authz.checkAccess(persistent, "config:apply"); //$NON-NLS-1$

            if ( persistent == null ) {
                throw new ModelServiceException();
            }

            Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistent);

            if ( !parent.isPresent() || ! ( parent.get() instanceof InstanceStructuralObjectImpl ) ) {
                throw new ModelServiceException("Parent must be an instance"); //$NON-NLS-1$
            }

            InstanceStructuralObjectImpl instance = (InstanceStructuralObjectImpl) parent.get();

            if ( instance == null ) {
                throw new ModelServiceException();
            }

            UUID agentId = validateService(em, persistent, instance);
            return queueConfigJob(em, persistent, instance, agentId, revision, info);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to apply configuration", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param persistent
     * @param parent
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    protected @NonNull UUID validateService ( EntityManager em, ServiceStructuralObjectImpl persistent, InstanceStructuralObjectImpl instance )
            throws ModelServiceException, ModelObjectNotFoundException {

        UUID agentId = instance.getAgentId();

        if ( agentId == null ) {
            throw new ModelServiceException("Agent is not registered for this instance"); //$NON-NLS-1$
        }
        return agentId;
    }


    /**
     * @param em
     * @param service
     * @param revision
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    protected ServiceStructuralObjectImpl getPersistentService ( EntityManager em, ServiceStructuralObject service, Long revision )
            throws ModelObjectNotFoundException, ModelServiceException {
        ServiceStructuralObjectImpl persistent;
        AuditReader ar = AuditReaderFactory.get(em);

        if ( revision != null ) {
            persistent = ar.find(ServiceStructuralObjectImpl.class, service.getId(), revision);
        }
        else {
            persistent = PersistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service.getId());
        }

        if ( persistent == null ) {
            throw new ModelObjectNotFoundException(ServiceStructuralObject.class, service.getId());
        }
        return persistent;
    }


    private @NonNull JobInfo queueConfigJob ( @NonNull EntityManager em, @NonNull ServiceStructuralObjectImpl persistent,
            @NonNull InstanceStructuralObjectImpl instance, @NonNull UUID agentId, Long revision, ConfigApplyInfo info )
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
                    AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {

        ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> serviceDescriptor = this.sctx.getServiceTypeRegistry()
                .getDescriptor(persistent.getServiceType());

        @NonNull
        ServiceStructuralObjectImpl actualCurrent = checkApplyRequest(em, persistent, instance, revision, serviceDescriptor);

        AbstractConfigurationInstance<@Nullable ?> persistentConfig = (AbstractConfigurationInstance<@Nullable ?>) persistent.getConfiguration();

        if ( persistentConfig == null ) {
            throw new ModelObjectNotFoundException(new ModelObjectNotFoundFault());
        }

        persistentConfig.setRevision(revision);

        @NonNull
        AbstractConfigurationInstance<@Nullable ?> unproxied = PersistenceUtil.unproxyDeep(persistentConfig);
        ConfigurationInstance effectiveConfig = this.inheritanceService.getEffective(em, unproxied, null);

        ConfigApplyJob j = makeConfigApplyJob(em, persistent, instance, agentId, info, serviceDescriptor, actualCurrent, effectiveConfig);
        if ( log.isDebugEnabled() ) {
            log.debug("Apply revision " + effectiveConfig.getRevision()); //$NON-NLS-1$
        }

        return queueServiceConfigJobInternal(em, instance, actualCurrent, j);
    }


    /**
     * @param em
     * @param persistent
     * @param instance
     * @param agentId
     * @param info
     * @param serviceDescriptor
     * @param actualCurrent
     * @param effectiveConfig
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    private ConfigApplyJob makeConfigApplyJob ( @NonNull EntityManager em, @NonNull ServiceStructuralObjectImpl persistent,
            InstanceStructuralObjectImpl instance, @NonNull UUID agentId, ConfigApplyInfo info,
            ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> serviceDescriptor, @NonNull ServiceStructuralObjectImpl actualCurrent,
            @NonNull ConfigurationInstance effectiveConfig ) throws ModelServiceException, ModelObjectNotFoundException,
                    ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {
        ConfigApplyJob j = new ConfigApplyJob();
        @NonNull
        List<@NonNull Job> resLibraryJobs = makeReslibrarySynchronizationJobs(serviceDescriptor, actualCurrent, effectiveConfig, info, em);
        j.getJobs().addAll(resLibraryJobs);
        j.setWeights(new ArrayList<>());

        int numLibrariesToSync = resLibraryJobs.size();
        for ( int i = 0; i < numLibrariesToSync; i++ ) {
            j.getWeights().add(0.3f / numLibrariesToSync);
        }
        j.getWeights().add(numLibrariesToSync > 0 ? 0.7f : 1.0f);
        j.getJobs().add(makeConfigurationJob(persistent, persistent, instance, agentId, serviceDescriptor, effectiveConfig, info));
        j.setName("Configure service"); //$NON-NLS-1$
        j.setOwner(getUserPrincipal());
        j.setDeadline(DateTime.now().plus(this.configJobTimeout));
        j.setTarget(new AnyServerTarget());
        j.setServices(Collections.singleton(actualCurrent));
        return j;
    }


    /**
     * @param serviceDescriptor
     * @param actualCurrent
     * @param effectiveConfig
     * @param info
     * @param em
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    private @NonNull List<@NonNull Job> makeReslibrarySynchronizationJobs (
            ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> serviceDescriptor, @NonNull ServiceStructuralObjectImpl actualCurrent,
            @NonNull ConfigurationInstance effectiveConfig, ConfigApplyInfo info, @NonNull EntityManager em )
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
                    AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {
        @NonNull
        Set<@NonNull ResourceLibraryReference> libs = new HashSet<>(serviceDescriptor.getReferencedResourceLibraries(effectiveConfig));

        return this.resLibraryService.makeSynchronizationJob(em, actualCurrent, libs, info.getForce(), getUserPrincipal());
    }


    /**
     * @param em
     * @param actualCurrent
     * @param j
     * @return
     * @throws ModelServiceException
     */
    private @NonNull JobInfo queueServiceConfigJobInternal ( @NonNull EntityManager em, InstanceStructuralObjectImpl instance,
            @NonNull ServiceStructuralObjectImpl actualCurrent, ConfigApplyJob j ) throws ModelServiceException {
        try {
            this.serviceService.setConfigurationState(em, instance, actualCurrent, ConfigurationState.APPLYING, true);
            em.persist(actualCurrent);
            em.flush();
            JobInfo ji = this.jobCoordinator.queueJob(j);

            if ( ji == null ) {
                throw new ModelServiceException("Queued job is null"); //$NON-NLS-1$
            }

            return ji;
        }
        catch ( JobQueueException e ) {
            throw new ModelServiceException("Failed to queue configuration job", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param persistent
     * @param instance
     * @param revision
     * @param serviceDescriptor
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     */
    private @NonNull ServiceStructuralObjectImpl checkApplyRequest ( @NonNull EntityManager em, @NonNull ServiceStructuralObjectImpl persistent,
            @NonNull InstanceStructuralObjectImpl instance, Long revision,
            ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> serviceDescriptor )
                    throws ModelObjectNotFoundException, ModelServiceException, ModelObjectReferentialIntegrityException {
        validateServiceIntegrity(em, instance, serviceDescriptor, Collections.EMPTY_SET);

        ServiceStructuralObjectImpl actualCurrent = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, persistent);
        if ( log.isDebugEnabled() ) {
            log.debug("Currently applied revision " + actualCurrent.getAppliedRevision()); //$NON-NLS-1$
        }

        if ( revision != null && actualCurrent.getAppliedRevision() != null && actualCurrent.getAppliedRevision() > revision ) {
            throw new ModelServiceException("A newer configuration is already applied"); //$NON-NLS-1$
        }
        else if ( actualCurrent.getAppliedRevision() != null && actualCurrent.getAppliedRevision().equals(revision) ) {
            throw new ModelServiceException("Configuration is already applied"); //$NON-NLS-1$
        }
        return actualCurrent;
    }


    /**
     * @param persistent
     * @param instance
     * @param agentId
     * @param serviceDescriptor
     * @param effectiveConfig
     * @param info
     * @return
     * @throws ModelServiceException
     */
    private ConfigurationJob makeConfigurationJob ( @NonNull StructuralObject anchor, @NonNull ServiceStructuralObjectImpl persistent,
            InstanceStructuralObjectImpl instance, @NonNull UUID agentId,
            ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> serviceDescriptor, @NonNull ConfigurationInstance effectiveConfig,
            ConfigApplyInfo info ) throws ModelServiceException {
        ConfigurationJob j = serviceDescriptor.makeConfigurationJob(effectiveConfig);
        UUID id = instance.getId();
        if ( id == null ) {
            throw new ModelServiceException();
        }
        j.setInstanceId(id);
        j.setAnchor(anchor);
        j.setService(persistent);
        j.setApplyInfo(info);
        j.setOwner(getUserPrincipal());
        j.setDeadline(DateTime.now().plus(this.configJobTimeout));
        j.setTarget(new AgentTarget(agentId));
        return j;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ConfigApplyServerService#handleConfigFailed(eu.agno3.orchestrator.config.model.events.ServiceConfigFailedEvent)
     */
    @Override
    public void handleConfigFailed ( @NonNull ServiceConfigFailedEvent event ) {
        // server can produce these events too if it detects a job failure
        if ( ! ( event.getOrigin() instanceof AgentMessageSource ) && ! ( event.getOrigin() instanceof ServerMessageSource ) ) {
            log.error("Illegal config applied event"); //$NON-NLS-1$
            return;
        }
        MessageSource source = event.getOrigin();

        if ( log.isDebugEnabled() ) {
            log.debug("Recieved service config failed event from " + source); //$NON-NLS-1$
        }

        try {
            EntityManager em = this.sctx.createConfigEM();
            ServiceStructuralObjectImpl service = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, event.getService());
            this.serviceService.setConfigurationState(em, null, service, ConfigurationState.FAILED, true);
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException e ) {
            log.error("Failed to set config to failed state", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ConfigApplyServerService#handleConfigApplied(eu.agno3.orchestrator.config.model.events.ServiceConfigAppliedEvent)
     */
    @Override
    public void handleConfigApplied ( @NonNull ServiceConfigAppliedEvent event ) {

        if ( ! ( event.getOrigin() instanceof AgentMessageSource ) ) {
            log.error("Illegal config applied event"); //$NON-NLS-1$
            return;
        }
        AgentMessageSource source = (AgentMessageSource) event.getOrigin();

        if ( log.isDebugEnabled() ) {
            log.debug("Recieved service config applied event from " + source); //$NON-NLS-1$
        }

        try {
            EntityManager em = this.sctx.createConfigEM();

            handleConfigAppliedInternal(em, event, source, event.getAnchor() instanceof ServiceStructuralObject);
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException e ) {
            log.error("Failed to set appied config revision", e); //$NON-NLS-1$
        }

    }


    /**
     * @param event
     * @param source
     * @param em
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private void handleConfigAppliedInternal ( @NonNull EntityManager em, ServiceConfigAppliedEvent event, AgentMessageSource source,
            boolean updateInstanceState ) throws ModelObjectNotFoundException, ModelServiceException {
        ServiceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, event.getService());

        clearOneOffs(em, event, persistent);

        Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistent);

        if ( !parent.isPresent() || ! ( parent.get() instanceof InstanceStructuralObject ) ) {
            throw new ModelServiceException("Service parent is not an instance"); //$NON-NLS-1$
        }

        InstanceStructuralObjectImpl instance = (InstanceStructuralObjectImpl) parent.get();

        if ( !source.getAgentId().equals(instance.getAgentId()) ) {
            log.error("Recieved config applied event from wrong agent"); //$NON-NLS-1$
            return;
        }

        persistent.setLastApplied(DateTime.now());
        setAppliedConfigRevision(em, event, persistent, instance, updateInstanceState);
        em.persist(persistent);
        em.flush();
    }


    /**
     * @param em
     * @param event
     * @param persistent
     */
    private void clearOneOffs ( @NonNull EntityManager em, ServiceConfigAppliedEvent event, ServiceStructuralObjectImpl persistent ) {
        AbstractConfigurationInstance<@Nullable ?> configuration = (AbstractConfigurationInstance<@Nullable ?>) persistent.getConfiguration();
        if ( configuration == null ) {
            return;
        }
        try {
            configuration = PersistenceUtil.unproxyDeep(configuration);
            ConfigurationObject effective = this.inheritanceService.getEffective(em, configuration, null);
            if ( OneOffUtil.clear(effective) ) {
                em.persist(configuration);
                em.flush();
            }
        }
        catch (
            PersistenceException |
            ModelServiceException |
            ModelObjectException e ) {
            log.warn("Failed to clear one off flags", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ConfigApplyServerService#setAppliedRevision(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject, boolean, java.lang.Long)
     */
    @Override
    public void setAppliedRevision ( @NonNull EntityManager em, @NonNull ServiceStructuralObject service, boolean updateInstanceState,
            @Nullable Long appRev ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Setting applied revision to " + appRev); //$NON-NLS-1$
        }

        @NonNull
        ServiceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service);

        Long known = persistent.getAppliedRevision();
        long globalRev = PersistenceUtil.getMostRecentRevision(em);
        if ( appRev != null && appRev > globalRev ) {
            // machine has likely been reverted
            persistent.setAppliedRevision(null);
            this.serviceService.setConfigurationState(em, null, persistent, ConfigurationState.UNKNOWN, updateInstanceState);
        }
        else if ( appRev == null ) {
            persistent.setAppliedRevision(null);
            this.serviceService.setConfigurationState(em, null, persistent, ConfigurationState.UNCONFIGURED, updateInstanceState);
        }
        else if ( known != null ) {
            if ( appRev == known ) {
                return;
            }
            else if ( appRev > known ) {
                persistent.setAppliedRevision(appRev);
                this.serviceService.setConfigurationState(em, null, persistent, ConfigurationState.UNKNOWN, updateInstanceState);
            }
            else if ( known > appRev ) {
                persistent.setAppliedRevision(appRev);
                this.serviceService.setConfigurationState(em, null, persistent, ConfigurationState.UPDATE_AVAILABLE, updateInstanceState);
            }
        }
        else {
            // server does not have a applied revision
            persistent.setAppliedRevision(appRev);
            this.serviceService.setConfigurationState(em, null, persistent, ConfigurationState.UNKNOWN, updateInstanceState);
        }
        em.persist(persistent);
    }


    /**
     * @param event
     * @param persistent
     * @param instance
     */
    private void setAppliedConfigRevision ( @NonNull EntityManager em, ServiceConfigAppliedEvent event, ServiceStructuralObjectImpl persistent,
            InstanceStructuralObjectImpl instance, boolean updateInstanceState ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Setting applied revision to " + event.getRevision()); //$NON-NLS-1$
        }
        persistent.setAppliedRevision(event.getRevision());
        if ( persistent.getState() == ConfigurationState.APPLYING || instance.getPersistentState() == StructuralObjectState.BOOTSTRAPPING ) {
            this.serviceService.setConfigurationState(em, instance, persistent, ConfigurationState.APPLIED, updateInstanceState);
        }
        else if ( log.isDebugEnabled() ) {
            log.debug("Not setting to applied as state is " + persistent.getState()); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param instance
     * @param serviceDescriptor
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     */
    private void validateServiceIntegrity ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl instance,
            ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> serviceDescriptor, Set<String> applyingServiceTypes )
                    throws ModelObjectNotFoundException, ModelServiceException, ModelObjectReferentialIntegrityException {
        Set<ServiceStructuralObject> services = this.serviceService.getServices(instance, em);
        Set<String> haveServiceTypes = new HashSet<>(applyingServiceTypes);
        Set<String> requireServiceTypes = new HashSet<>(serviceDescriptor.getRequiredServices());

        for ( ServiceStructuralObject service : services ) {
            if ( service.getState() != null && ( service.getState() == ConfigurationState.APPLIED || service.getState() == ConfigurationState.APPLYING
                    || service.getState() == ConfigurationState.DEFAULTS_CHANGED || service.getState() == ConfigurationState.UPDATE_AVAILABLE ) ) {
                haveServiceTypes.add(service.getServiceType());
            }
        }

        requireServiceTypes.removeAll(haveServiceTypes);
        if ( !requireServiceTypes.isEmpty() ) {
            throw new ModelObjectReferentialIntegrityException(
                String.format(
                    "Services required for %s are not configured: %s", //$NON-NLS-1$
                    serviceDescriptor.getTypeName(),
                    StringUtils.join(requireServiceTypes, ',')),
                new ModelObjectReferentialIntegrityFault(InstanceStructuralObject.class, instance.getId()));
        }
    }


    /**
     * @return
     * @throws ModelServiceException
     */
    private static @NonNull UserPrincipal getUserPrincipal () throws ModelServiceException {
        UserPrincipal up = SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class);

        if ( up == null ) {
            throw new ModelServiceException("Failed to determine user principal"); //$NON-NLS-1$
        }

        return up;
    }
}
