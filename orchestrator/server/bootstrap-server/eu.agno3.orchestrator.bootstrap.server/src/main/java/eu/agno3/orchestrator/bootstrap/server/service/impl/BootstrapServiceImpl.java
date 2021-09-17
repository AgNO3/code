/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.server.service.impl;


import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.bootstrap.BootstrapPlugin;
import eu.agno3.orchestrator.bootstrap.jobs.BootstrapCompleteJob;
import eu.agno3.orchestrator.bootstrap.server.service.BootstrapServerService;
import eu.agno3.orchestrator.bootstrap.service.BootstrapService;
import eu.agno3.orchestrator.bootstrap.service.BootstrapServiceDescriptor;
import eu.agno3.orchestrator.config.auth.PasswordPolicyConfig;
import eu.agno3.orchestrator.config.auth.PasswordPolicyConfigMutable;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.HostConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.HostIdentification;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.config.hostconfig.jobs.RebootJob;
import eu.agno3.orchestrator.config.hostconfig.jobs.SetAdminPasswordJob;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.jobs.ConfigApplyTrackingJob;
import eu.agno3.orchestrator.config.model.jobs.ResourceLibrarySynchronizationJob;
import eu.agno3.orchestrator.config.model.jobs.ResourceLibraryTrackingJob;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectState;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;
import eu.agno3.orchestrator.config.model.realm.server.service.DefaultRealmServicesContext;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfigurationImpl;
import eu.agno3.orchestrator.config.orchestrator.desc.OrchestratorServiceTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.jobs.OrchestratorConfigurationJob;
import eu.agno3.orchestrator.crypto.jobs.RegenerateInternalCertJob;
import eu.agno3.orchestrator.jobs.DefaultGroup;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobImpl;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.compound.CompoundJob;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.orchestrator.jobs.targets.AnyServerTarget;
import eu.agno3.orchestrator.jobs.targets.ServerTarget;
import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.orchestrator.types.net.name.HostOrAddress;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    BootstrapService.class, BootstrapServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.bootstrap.service.BootstrapService",
    targetNamespace = BootstrapServiceDescriptor.NAMESPACE,
    serviceName = BootstrapServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/bootstrap" )
public class BootstrapServiceImpl implements BootstrapService, BootstrapServerService, SOAPWebService {

    private static final String WEB = "web"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(BootstrapServiceImpl.class);

    private static final String LOCALHOST = "localhost"; //$NON-NLS-1$
    private static final String LOCALHOST_IP = "127.0.0.1"; //$NON-NLS-1$

    private DefaultServerServiceContext sctx;
    private ObjectAccessControl authz;
    private DefaultRealmServicesContext rctx;
    private PersistenceUtil persistenceUtil;
    private JobCoordinator jobCoordinator;

    private KeyStoreConfiguration webKeystoreConfig;

    private Set<BootstrapPlugin> plugins = new HashSet<>();

    private ResourceLibraryServerService resLibraryService;


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
    protected synchronized void setObjectAccessControl ( ObjectAccessControl oac ) {
        this.authz = oac;
    }


    protected synchronized void unsetObjectAccessControl ( ObjectAccessControl oac ) {
        if ( this.authz == oac ) {
            this.authz = null;
        }
    }


    @Reference
    protected synchronized void setDefaultRealmServicesContext ( DefaultRealmServicesContext ctx ) {
        this.rctx = ctx;
    }


    protected synchronized void unsetDefaultRealmServicesContext ( DefaultRealmServicesContext ctx ) {
        if ( this.rctx == ctx ) {
            this.rctx = null;
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
    protected synchronized void setJobCoordinator ( JobCoordinator jobCoord ) {
        this.jobCoordinator = jobCoord;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator jobCoord ) {
        if ( this.jobCoordinator == jobCoord ) {
            this.jobCoordinator = null;
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL, target = "(instanceId=web)" )
    protected synchronized void setWebKeystoreConfig ( KeyStoreConfiguration ks ) {
        this.webKeystoreConfig = ks;
    }


    protected synchronized void unsetWebKeystoreConfig ( KeyStoreConfiguration ks ) {
        if ( this.webKeystoreConfig == ks ) {
            this.webKeystoreConfig = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindBootstrapPlugin ( BootstrapPlugin bp ) {
        this.plugins.add(bp);
    }


    protected synchronized void unbindBootstrapPlugin ( BootstrapPlugin bp ) {
        this.plugins.remove(bp);
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


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.bootstrap.service.BootstrapService#getBootstrapContext()
     */
    @Override
    @RequirePermissions ( "bootstrap" )
    public BootstrapContext getBootstrapContext () throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        return getBootstrapContext(em);
    }


    /**
     * @param em
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private BootstrapContext getBootstrapContext ( @NonNull EntityManager em ) throws ModelServiceException, ModelObjectNotFoundException {
        try {
            InstanceStructuralObjectImpl bootstrapInstance = getBootstrapInstance(em);
            if ( bootstrapInstance == null ) {
                return null;
            }
            BootstrapContext ctx = createBootstrapContext(bootstrapInstance);
            ctx.setHostConfigService(getBootstrapHostConfigService(em, bootstrapInstance));
            ctx.setOrchConfigService(getBootstrapOrchestratorConfigService(em, bootstrapInstance));
            ctx.setInstance(bootstrapInstance);
            setupPassword(ctx, bootstrapInstance);
            for ( BootstrapPlugin bp : getApplicablePlugins(bootstrapInstance) ) {
                bp.setupContext(em, bootstrapInstance, ctx);
            }
            return ctx;
        }
        catch ( ModelObjectNotFoundException e ) {
            throw e;
        }
        catch (
            PersistenceException |
            ModelObjectException e ) {
            throw new ModelServiceException("Failed to get bootstrap context", e); //$NON-NLS-1$
        }
    }


    private Collection<BootstrapPlugin> getApplicablePlugins ( InstanceStructuralObject inst ) {
        Collection<BootstrapPlugin> plug = new LinkedList<>();
        for ( BootstrapPlugin bp : this.plugins ) {
            if ( bp.appliesTo(inst) ) {
                plug.add(bp);
            }
        }
        return plug;
    }


    /**
     * @param imageType
     * @return
     */
    private BootstrapContext createBootstrapContext ( InstanceStructuralObject bootstrapInstance ) {
        for ( BootstrapPlugin bp : this.plugins ) {
            if ( bp.isPrimary(bootstrapInstance) ) {
                return bp.createContext(bootstrapInstance);
            }
        }
        // default
        return new BootstrapContext();
    }


    /**
     * @param ctx
     * @param bootstrapInstance
     * @throws ModelServiceException
     */
    void setupPassword ( BootstrapContext ctx, InstanceStructuralObjectImpl bootstrapInstance ) throws ModelServiceException {
        Integer entropyLimit = this.sctx.getObjectTypeRegistry().getConcrete(PasswordPolicyConfig.class).getGlobalDefaults().getEntropyLowerLimit();
        if ( entropyLimit != null ) {
            ctx.setMinimumPasswordEntropy(entropyLimit);
        }

        if ( bootstrapInstance.getBootstrapPasswordEntropy() == null
                || bootstrapInstance.getBootstrapPasswordEntropy() < ctx.getMinimumPasswordEntropy() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Bootstrap password entropy is %d minimum is %d, forcing change", //$NON-NLS-1$
                    bootstrapInstance.getBootstrapPasswordEntropy(),
                    ctx.getMinimumPasswordEntropy()));
            }
            ctx.setRequirePasswordChange(true);
        }
    }


    /**
     * @param em
     * @param bootstrapInstance
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private @NonNull ServiceStructuralObjectImpl getBootstrapOrchestratorConfigService ( @NonNull EntityManager em,
            @NonNull InstanceStructuralObjectImpl bootstrapInstance ) throws ModelObjectNotFoundException, ModelServiceException {
        Set<ServiceStructuralObject> servicesOfType = this.rctx.getServiceService()
                .getServicesOfType(em, bootstrapInstance, OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE);

        if ( servicesOfType.size() != 1 ) {
            throw new ModelServiceException("Orchestrator config does not exist or is not unique"); //$NON-NLS-1$
        }

        ServiceStructuralObjectImpl first = (ServiceStructuralObjectImpl) servicesOfType.iterator().next();
        if ( first == null ) {
            throw new ModelServiceException("Orchestrator config is null"); //$NON-NLS-1$
        }
        return first;
    }


    /**
     * @param em
     * @param bootstrapInstance
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private @NonNull ServiceStructuralObjectImpl getBootstrapHostConfigService ( @NonNull EntityManager em,
            @NonNull InstanceStructuralObjectImpl bootstrapInstance ) throws ModelServiceException, ModelObjectNotFoundException {
        Set<ServiceStructuralObject> servicesOfType = this.rctx.getServiceService()
                .getServicesOfType(em, bootstrapInstance, HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE);

        if ( servicesOfType.size() != 1 ) {
            throw new ModelServiceException("Host config does not exist or is not unique"); //$NON-NLS-1$
        }

        ServiceStructuralObjectImpl first = (ServiceStructuralObjectImpl) servicesOfType.iterator().next();

        if ( first == null ) {
            throw new ModelServiceException("Service is null"); //$NON-NLS-1$
        }

        return first;
    }


    private static InstanceStructuralObjectImpl getBootstrapInstance ( @NonNull EntityManager em ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InstanceStructuralObjectImpl> q = cb.createQuery(InstanceStructuralObjectImpl.class);
        EntityType<InstanceStructuralObjectImpl> entity = em.getMetamodel().entity(InstanceStructuralObjectImpl.class);
        Root<InstanceStructuralObjectImpl> tbl = q.from(InstanceStructuralObjectImpl.class);
        q.where(cb.equal(tbl.get(entity.getSingularAttribute("persistentState", StructuralObjectState.class)), StructuralObjectState.BOOTSTRAPPING)); //$NON-NLS-1$
        TypedQuery<InstanceStructuralObjectImpl> qry = em.createQuery(q);
        qry.setMaxResults(1);
        List<InstanceStructuralObjectImpl> results = qry.getResultList();
        if ( results.isEmpty() ) {
            log.debug("No bootstrapping instance found"); //$NON-NLS-1$
            return null;
        }
        return results.get(0);
    }


    @Override
    @RequirePermissions ( "bootstrap" )
    public JobInfo completeBootstrap ( BootstrapContext ctx ) throws ModelObjectException, ModelObjectNotFoundException, ModelServiceException,
            ModelObjectConflictException, ModelObjectValidationException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException {

        HostConfiguration hc = ctx.getHostConfig();
        OrchestratorConfiguration oc = ctx.getOrchConfig();
        if ( hc == null || oc == null || ( ctx.getRequirePasswordChange() && StringUtils.isBlank(ctx.getChangeAdminPassword()) ) ) {
            throw new ModelServiceException("Missing parameters"); //$NON-NLS-1$
        }

        log.info("Completing bootstrap"); //$NON-NLS-1$

        EntityManager em = this.sctx.createConfigEM();
        return completeBootstrapInternal(ctx, hc, oc, em);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.bootstrap.service.BootstrapService#autoCompleteBootstrap()
     */
    @Override
    public JobInfo autoCompleteBootstrap () throws ModelObjectNotFoundException, ModelServiceException, ModelObjectConflictException,
            ModelObjectValidationException, ModelObjectReferentialIntegrityException, ModelObjectException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException {
        EntityManager em = this.sctx.createConfigEM();
        return completeBootstrapInternal(getBootstrapContext(em), null, null, em);
    }


    /**
     * @param ctx
     * @param hc
     * @param oc
     * @param em
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectValidationException
     * @throws ModelObjectConflictException
     * @throws ModelObjectException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    JobInfo completeBootstrapInternal ( BootstrapContext ctx, HostConfiguration hc, OrchestratorConfiguration oc, @NonNull EntityManager em )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectValidationException, ModelObjectConflictException,
            ModelObjectException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {
        InstanceStructuralObjectImpl pinst = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, ctx.getInstance());
        if ( pinst.getPersistentState() != StructuralObjectState.BOOTSTRAPPING ) {
            throw new ModelServiceException("Trying to bootstrap an instance that is not in state BOOTSTRAPPING"); //$NON-NLS-1$
        }

        ConfigUpdateInfo info = new ConfigUpdateInfo();
        ctx.setHostConfig(updateHostConfig(hc, em, pinst, info));
        ctx.setOrchConfig(updateOrchConfig(oc, em, pinst, info));

        String hostname = ctx.getHostConfig().getHostIdentification().getHostName();
        if ( !pinst.getDisplayName().equals(hostname) ) {
            pinst.setDisplayName(hostname);
            em.persist(pinst);
        }

        for ( BootstrapPlugin bp : getApplicablePlugins(pinst) ) {
            bp.completeContext(em, pinst, ctx, info);
        }

        try {
            CompoundJob job = this.makeBootstrapJob(pinst, em, ctx);
            return this.jobCoordinator.queueJob(job);
        }
        catch ( JobQueueException e ) {
            throw new ModelServiceException("Failed to queue bootstrap job", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.bootstrap.server.service.BootstrapServerService#afterApplyConfig(eu.agno3.orchestrator.bootstrap.BootstrapContext)
     */
    @Override
    public void afterApplyConfig ( BootstrapContext ctx ) throws ModelObjectNotFoundException, ModelServiceException {
        try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em = this.sctx.createConfigEM();
            InstanceStructuralObjectImpl persistentInstance = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, ctx.getInstance());

            if ( persistentInstance.getPersistentState() != StructuralObjectState.BOOTSTRAPPING ) {
                throw new ModelServiceException("Completion called for an instance that is not BOOTSTRAPPING"); //$NON-NLS-1$
            }

            persistentInstance.setPersistentState(StructuralObjectState.OK);
            em.persist(persistentInstance);
            em.flush();
            tx.commit();
        }
    }


    /**
     * @param adminPassword
     * @param instance
     * @param em
     * @param ctx
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    private CompoundJob makeBootstrapJob ( @NonNull InstanceStructuralObjectImpl instance, @NonNull EntityManager em, BootstrapContext ctx )
            throws ModelServiceException, ModelObjectException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {

        List<Float> weights = new ArrayList<>();
        List<Job> jobs = new ArrayList<>();

        boolean doSetPassword = !StringUtils.isBlank(ctx.getChangeAdminPassword());
        if ( doSetPassword ) {
            addPasswordSettingsJobs(instance, ctx.getChangeAdminPassword(), weights, jobs);
        }

        addConfigJobs(instance, em, ctx, weights, jobs, doSetPassword);

        for ( BootstrapPlugin bp : getApplicablePlugins(instance) ) {
            bp.contributeJobs(em, instance, ctx, weights, jobs);
        }

        addConfigAppliedJob(ctx, instance, weights, jobs);
        addBootstrapCompleteJob(ctx, weights, jobs);
        addRebootJob(instance, weights, jobs);

        CompoundJob j = new CompoundJob("Complete bootstrap process", jobs.toArray(new Job[] {})); //$NON-NLS-1$
        j.setJobGroup(new DefaultGroup());

        double sum = weights.stream().collect(Collectors.summingDouble(x -> x));
        j.setWeights(weights.stream().map(x -> (float) ( x / sum )).collect(Collectors.toList()));
        setupServerJob(j);
        return j;
    }


    /**
     * @param ctx
     * @param instance
     * @param weights
     * @param jobs
     * @throws ModelServiceException
     */
    private static void addConfigAppliedJob ( BootstrapContext ctx, @NonNull InstanceStructuralObjectImpl instance, List<Float> weights,
            List<Job> jobs ) throws ModelServiceException {
        ConfigApplyTrackingJob ctj = new ConfigApplyTrackingJob();
        ctj.setOwner(getUserPrincipal());
        ctj.setTarget(new AnyServerTarget());
        ctj.setAnchor(instance);
        jobs.add(ctj);
        weights.add(0.0f);
    }


    /**
     * @param instance
     * @param newHostConfig
     * @param newOrchConfig
     * @param weights
     * @param jobs
     * @throws ModelServiceException
     */
    private void addCertRegenerationJob ( InstanceStructuralObjectImpl instance, HostConfiguration newHostConfig,
            OrchestratorConfiguration newOrchConfig, List<Float> weights, List<Job> jobs ) throws ModelServiceException {

        @Nullable
        HostIdentification hostIdentification = newHostConfig.getHostIdentification();

        if ( hostIdentification == null ) {
            throw new ModelServiceException("No host identificiation present"); //$NON-NLS-1$
        }

        String primaryHostName = String.format(
            "%s.%s", //$NON-NLS-1$
            hostIdentification.getHostName(),
            hostIdentification.getDomainName());

        try {
            if ( checkCurrentCertificate(primaryHostName, newHostConfig) ) {
                return;
            }
        }
        catch ( GeneralSecurityException e ) {
            log.info("Failed to check keystore for certificate", e); //$NON-NLS-1$
        }

        RegenerateInternalCertJob regenerateInternalCertJob = new RegenerateInternalCertJob();
        regenerateInternalCertJob.setIncludeCA(true);
        regenerateInternalCertJob.setKeyStore(WEB); // $NON-NLS-1$
        regenerateInternalCertJob.setKeyAlias(WEB); // $NON-NLS-1$

        setWebSubject(regenerateInternalCertJob, primaryHostName);
        setWebBasicAttrs(regenerateInternalCertJob);
        HashSet<ASN1ObjectIdentifier> extendedKeyUsages = new HashSet<>();
        extendedKeyUsages.add(ASN1ObjectIdentifier.getInstance(KeyPurposeId.id_kp_serverAuth));
        regenerateInternalCertJob.setExtendedKeyUsages(extendedKeyUsages);
        regenerateInternalCertJob.setSanAddresses(getSANAddresses(newHostConfig, primaryHostName));
        setupAgentJob(regenerateInternalCertJob, instance);
        jobs.add(regenerateInternalCertJob);
        weights.add(0.1f);
    }


    /**
     * @param primaryHostName
     * @param hc
     * @return
     * @throws KeyStoreException
     * @throws CertificateParsingException
     * @throws CertificateNotYetValidException
     * @throws CertificateExpiredException
     * @throws ModelServiceException
     */
    private boolean checkCurrentCertificate ( String primaryHostName, HostConfiguration hc ) throws KeyStoreException, CertificateParsingException,
            CertificateExpiredException, CertificateNotYetValidException, ModelServiceException {
        KeyStoreConfiguration ksConfig = this.webKeystoreConfig;
        if ( ksConfig == null ) {
            log.debug("No keystore config"); //$NON-NLS-1$
            return false;
        }

        KeyStore ks = ksConfig.getKeyStore();
        if ( ks == null ) {
            log.debug("No keystore"); //$NON-NLS-1$
            return false;
        }
        if ( !ks.containsAlias(WEB) ) {
            log.debug("No key"); //$NON-NLS-1$
            return false;
        }

        Certificate certificate = ks.getCertificate(WEB);
        if ( ! ( certificate instanceof X509Certificate ) ) {
            log.debug("No certificate"); //$NON-NLS-1$
            return false;
        }
        X509Certificate cert = (X509Certificate) certificate;
        cert.checkValidity(DateTime.now().plusWeeks(2).toDate());
        X500Principal princ = cert.getSubjectX500Principal();
        X500Name x500Name = X500Name.getInstance(BCStyle.INSTANCE, princ.getEncoded());
        RDN[] rdNs = x500Name.getRDNs(BCStyle.CN);
        if ( rdNs == null || rdNs.length != 1 ) {
            log.debug("No CN"); //$NON-NLS-1$
            return false;
        }
        String certCNHostname = IETFUtils.valueToString(rdNs[ 0 ].getFirst().getValue());
        if ( !primaryHostName.equals(certCNHostname) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Hostname in certificate does not match " + certCNHostname); //$NON-NLS-1$
            }
            return false;
        }

        Collection<List<?>> sans = cert.getSubjectAlternativeNames();
        if ( sans == null ) {
            log.debug("SubjectAltNames not present"); //$NON-NLS-1$
            return false;
        }
        Set<HostOrAddress> sanAddresses = new HashSet<>();
        sanAddresses.add(new HostOrAddress(primaryHostName));
        addInterfaceAddresses(hc, sanAddresses);

        for ( List<?> gn : sans ) {
            Integer type = (Integer) gn.get(0);
            if ( type == 2 || type == 7 ) {
                // dNSName || iPAddress
                sanAddresses.remove(HostOrAddress.fromString((String) gn.get(1)));
            }
        }

        if ( !sanAddresses.isEmpty() ) {
            log.info("Certificate is missing subjectAltNames " + sanAddresses); //$NON-NLS-1$
            return false;
        }

        log.debug("Certificate already valid"); //$NON-NLS-1$
        return true;
    }


    /**
     * @param newHostConfig
     * @param primaryHostName
     * @return
     * @throws ModelServiceException
     */
    private static Set<HostOrAddress> getSANAddresses ( HostConfiguration newHostConfig, String primaryHostName ) throws ModelServiceException {
        Set<HostOrAddress> sanAddresses = new HashSet<>();
        sanAddresses.add(new HostOrAddress(AbstractIPAddress.parse(LOCALHOST_IP)));
        sanAddresses.add(new HostOrAddress(LOCALHOST));
        sanAddresses.add(new HostOrAddress(primaryHostName));
        addInterfaceAddresses(newHostConfig, sanAddresses);
        return sanAddresses;
    }


    /**
     * @param newHostConfig
     * @param sanAddresses
     * @throws ModelServiceException
     */
    private static void addInterfaceAddresses ( HostConfiguration newHostConfig, Set<HostOrAddress> sanAddresses ) throws ModelServiceException {
        @Nullable
        NetworkConfiguration networkConfiguration = newHostConfig.getNetworkConfiguration();

        if ( networkConfiguration == null ) {
            throw new ModelServiceException("No network configuration present"); //$NON-NLS-1$
        }

        for ( InterfaceEntry e : networkConfiguration.getInterfaceConfiguration().getInterfaces() ) {
            Set<NetworkSpecification> staticAddresses = e.getStaticAddresses();
            if ( staticAddresses == null ) {
                continue;
            }
            for ( NetworkSpecification networkSpec : staticAddresses ) {
                NetworkAddress ifAddress = networkSpec.getAddress();
                sanAddresses.add(new HostOrAddress(ifAddress));
            }
        }
    }


    /**
     * @param regenerateInternalCertJob
     */
    private static void setWebBasicAttrs ( RegenerateInternalCertJob regenerateInternalCertJob ) {
        regenerateInternalCertJob.setLifetime(Duration.standardDays(2 * 365));
        int keyUsage = KeyUsage.dataEncipherment | KeyUsage.digitalSignature | KeyUsage.keyAgreement | KeyUsage.keyEncipherment;
        regenerateInternalCertJob.setKeyUsage(keyUsage);
    }


    /**
     * @param regenerateInternalCertJob
     * @param primaryHostName
     */
    private static void setWebSubject ( RegenerateInternalCertJob regenerateInternalCertJob, String primaryHostName ) {
        X500NameBuilder nb = new X500NameBuilder(BCStyle.INSTANCE);
        nb.addRDN(BCStyle.CN, primaryHostName);
        nb.addRDN(BCStyle.OU, "Orchestrator WebGUI"); //$NON-NLS-1$
        nb.addRDN(BCStyle.O, "AgNO3 Orchestrator generated"); //$NON-NLS-1$
        regenerateInternalCertJob.setSubject(nb.build());
    }


    /**
     * @param ctx
     * @param weights
     * @param jobs
     * @throws ModelServiceException
     */
    private void addBootstrapCompleteJob ( BootstrapContext ctx, List<Float> weights, List<Job> jobs ) throws ModelServiceException {
        BootstrapCompleteJob bootstrapCompleteJob = new BootstrapCompleteJob();
        bootstrapCompleteJob.setContext(ctx);
        setupServerJob(bootstrapCompleteJob);
        jobs.add(bootstrapCompleteJob);
        weights.add(0.1f);
    }


    /**
     * @param instance
     * @param em
     * @param ctx
     * @param newHostConfig
     * @param newOrchConfig
     * @param weights
     * @param jobs
     * @param doSetPassword
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * @throws ModelObjectNotFoundException
     */
    private void addConfigJobs ( @NonNull InstanceStructuralObjectImpl instance, @NonNull EntityManager em, BootstrapContext ctx, List<Float> weights,
            List<Job> jobs, boolean doSetPassword ) throws ModelServiceException, ModelObjectNotFoundException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException {
        ServiceStructuralObject hostConfigService = ctx.getHostConfigService();
        HostConfiguration hc = ctx.getHostConfig();
        OrchestratorConfiguration oc = ctx.getOrchConfig();

        if ( hostConfigService == null || ! ( hc instanceof HostConfigurationImpl ) || ! ( oc instanceof OrchestratorConfigurationImpl ) ) {
            throw new ModelServiceException("Invalid parameters"); //$NON-NLS-1$
        }

        @NonNull
        HostConfigurationImpl hci = PersistenceUtil.unproxyDeep((HostConfigurationImpl) hc);
        @SuppressWarnings ( "null" )
        HostConfiguration effectiveHc = this.rctx.getInheritanceService().getEffective(em, hci, hci.getType());
        ServiceStructuralObjectImpl hcs = PersistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, hostConfigService.getId());

        @NonNull
        Set<@NonNull ResourceLibraryReference> libs = this.sctx.getServiceTypeRegistry().getDescriptor(HostConfiguration.class)
                .getReferencedResourceLibraries(effectiveHc);

        @NonNull
        List<@NonNull Job> resLibraryJobs = this.resLibraryService.makeSynchronizationJob(em, hcs, libs, true, getUserPrincipal());

        ResourceLibraryTrackingJob tj = new ResourceLibraryTrackingJob();
        tj.setOwner(getUserPrincipal());
        tj.setLastModified(combineLastModified(resLibraryJobs));
        tj.setDeadline(DateTime.now().plusMinutes(10));
        tj.setTarget(new AnyServerTarget());
        tj.setAnchor(instance);
        resLibraryJobs.add(tj);

        for ( Job j : resLibraryJobs ) {
            jobs.add(j);
            weights.add(0.05f);
        }

        HostConfigurationJob hcjob = makeHostConfigJob(instance, em, hostConfigService, hci, effectiveHc);
        jobs.add(hcjob);
        weights.add( ( doSetPassword ? 0.3f : 0.4f ) - ( 0.05f * resLibraryJobs.size() ));

        ServiceStructuralObject orchConfigService = ctx.getOrchConfigService();
        if ( orchConfigService == null ) {
            throw new ModelServiceException("Orchestrator service is null"); //$NON-NLS-1$
        }

        OrchestratorConfigurationJob orchjob = makeOrchConfigJob(instance, em, orchConfigService, (OrchestratorConfigurationImpl) oc);
        jobs.add(orchjob);
        weights.add(doSetPassword ? 0.2f : 0.3f);

        addCertRegenerationJob(instance, hcjob.getHostConfig(), orchjob.getOrchestratorConfig(), weights, jobs);

    }


    /**
     * @param instance
     * @param weights
     * @param jobs
     * @throws ModelServiceException
     */
    private static void addRebootJob ( InstanceStructuralObjectImpl instance, List<Float> weights, List<Job> jobs ) throws ModelServiceException {
        RebootJob rebootJob = new RebootJob();
        setupAgentJob(rebootJob, instance);
        jobs.add(rebootJob);
        weights.add(0.1f);
    }


    /**
     * @param instance
     * @param adminPassword
     * @param weights
     * @param jobs
     * @throws ModelServiceException
     */
    private void addPasswordSettingsJobs ( InstanceStructuralObjectImpl instance, String adminPassword, List<Float> weights, List<Job> jobs )
            throws ModelServiceException {
        SetAdminPasswordJob setAdminPasswordAgentJob = new SetAdminPasswordJob();
        setAdminPasswordAgentJob.setAdminPassword(adminPassword);
        setupAgentJob(setAdminPasswordAgentJob, instance);
        weights.add(0.1f);
        jobs.add(setAdminPasswordAgentJob);

        SetAdminPasswordJob setAdminPasswordServerJob = new SetAdminPasswordJob();
        setAdminPasswordServerJob.setAdminPassword(adminPassword);
        setupServerJob(setAdminPasswordServerJob);
        weights.add(0.1f);
        jobs.add(setAdminPasswordServerJob);
    }


    /**
     * @param instance
     * @param em
     * @param newHostConfig
     * @return
     * @throws ModelServiceException
     */
    private static HostConfigurationJob makeHostConfigJob ( @NonNull InstanceStructuralObjectImpl instance, @NonNull EntityManager em,
            @NonNull ServiceStructuralObject hcService, HostConfigurationImpl newHostConfig, HostConfiguration effectiveHostConfig )
            throws ModelServiceException {
        HostConfigurationJob hcjob = new HostConfigurationJob();
        hcjob.setHostConfig(effectiveHostConfig);
        hcjob.setAnchor(instance);
        hcjob.setInstanceId(instance.getId());
        hcjob.getApplyInfo().setForce(true);
        hcjob.setService(hcService);
        setupAgentJob(hcjob, instance);
        return hcjob;
    }


    /**
     * @param instance
     * @param em
     * @param newOrchConfig
     * @return
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "null" )
    private OrchestratorConfigurationJob makeOrchConfigJob ( InstanceStructuralObjectImpl instance, @NonNull EntityManager em,
            @NonNull ServiceStructuralObject orchService, @NonNull OrchestratorConfigurationImpl newOrchConfig ) throws ModelServiceException {
        OrchestratorConfigurationJob orchjob = new OrchestratorConfigurationJob();
        OrchestratorConfiguration effective = this.rctx.getInheritanceService()
                .getEffective(em, PersistenceUtil.unproxyDeep(newOrchConfig), newOrchConfig.getType());
        orchjob.setOrchestratorConfig(effective);
        orchjob.setInstanceId(instance.getId());
        orchjob.setAnchor(instance);
        orchjob.getApplyInfo().setForce(true);
        orchjob.setNoRestart(true);
        orchjob.setService(orchService);
        setupAgentJob(orchjob, instance);
        return orchjob;
    }


    /**
     * @param instance
     * @param hcjob
     * @throws ModelServiceException
     */
    private static void setupAgentJob ( JobImpl j, InstanceStructuralObjectImpl instance ) throws ModelServiceException {
        UUID agentId = instance.getAgentId();
        if ( agentId == null ) {
            throw new ModelServiceException("Agent id unknown"); //$NON-NLS-1$
        }
        j.setTarget(new AgentTarget(agentId));
        j.setOwner(getUserPrincipal());
    }


    /**
     * @param j
     * @throws ModelServiceException
     */
    private void setupServerJob ( JobImpl j ) throws ModelServiceException {
        j.setTarget(new ServerTarget(this.sctx.getServerConfig().getServerId()));
        j.setOwner(getUserPrincipal());
    }


    /**
     * @param orchConfig
     * @param em
     * @param persistentInstance
     * @param info
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectValidationException
     * @throws ModelObjectConflictException
     */
    private @NonNull OrchestratorConfigurationImpl updateOrchConfig ( OrchestratorConfiguration orchConfig, @NonNull EntityManager em,
            @NonNull InstanceStructuralObjectImpl persistentInstance, ConfigUpdateInfo info )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectValidationException, ModelObjectConflictException {

        ServiceStructuralObjectImpl bootstrapOrchestratorConfigService = getBootstrapOrchestratorConfigService(em, persistentInstance);
        em.refresh(bootstrapOrchestratorConfigService);

        if ( orchConfig == null ) {
            return (@NonNull OrchestratorConfigurationImpl) this.rctx.getServiceService()
                    .getServiceConfiguration(em, bootstrapOrchestratorConfigService);
        }

        if ( orchConfig instanceof OrchestratorConfigurationImpl ) {
            OrchestratorConfigurationImpl oc = (OrchestratorConfigurationImpl) orchConfig;
            PasswordPolicyConfigMutable pwPolicy = (PasswordPolicyConfigMutable) oc.getAuthenticationConfig().getAuthenticatorsConfig()
                    .getPasswordPolicy();
            // reset password limit to default. this is set during bootstrapping to prevent too many prompts.
            pwPolicy.setEntropyLowerLimit(null);
        }

        return this.rctx.getServiceService()
                .updateServiceConfiguration(em, bootstrapOrchestratorConfigService, (OrchestratorConfigurationImpl) orchConfig, info);
    }


    /**
     * @param hostConfig
     * @param em
     * @param persistentInstance
     * @param info
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     * @throws ModelObjectConflictException
     */
    private @NonNull HostConfigurationImpl updateHostConfig ( HostConfiguration hostConfig, @NonNull EntityManager em,
            @NonNull InstanceStructuralObjectImpl persistentInstance, ConfigUpdateInfo info )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException, ModelObjectConflictException {
        ServiceStructuralObjectImpl bootstrapHostConfigService = getBootstrapHostConfigService(em, persistentInstance);
        em.refresh(bootstrapHostConfigService);
        if ( hostConfig == null ) {
            return (@NonNull HostConfigurationImpl) this.rctx.getServiceService().getServiceConfiguration(em, bootstrapHostConfigService);
        }
        return this.rctx.getServiceService().updateServiceConfiguration(em, bootstrapHostConfigService, (HostConfigurationImpl) hostConfig, info);
    }


    /**
     * @return
     * @throws ModelServiceException
     */
    private static UserPrincipal getUserPrincipal () throws ModelServiceException {
        Subject subject = SecurityUtils.getSubject();
        if ( !subject.isAuthenticated() ) {
            return null;
        }
        Collection<UserPrincipal> ups = subject.getPrincipals().byType(UserPrincipal.class);

        if ( ups.size() != 1 ) {
            throw new ModelServiceException("Failed to determine user principal"); //$NON-NLS-1$
        }

        return ups.iterator().next();
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
}
