/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.internal.p2.engine.SimpleProfileRegistry;
import org.eclipse.equinox.internal.p2.garbagecollector.GarbageCollector;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.eclipse.equinox.internal.provisional.configurator.Configurator;
import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdmin;
import org.eclipse.equinox.p2.core.IAgentLocation;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.core.UIServices;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.engine.PhaseSetFactory;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.engine.query.UserVisibleRootQuery;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionedId;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProfileChangeOperation;
import org.eclipse.equinox.p2.operations.ProfileModificationJob;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UninstallOperation;
import org.eclipse.equinox.p2.operations.Update;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

import eu.agno3.runtime.update.Feature;
import eu.agno3.runtime.update.FeatureUpdate;
import eu.agno3.runtime.update.LoggingProgressMonitor;
import eu.agno3.runtime.update.UpdateConfiguration;
import eu.agno3.runtime.update.UpdateException;
import eu.agno3.runtime.update.UpdateManager;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "restriction" )
public class ProvisioningManagerImpl implements UpdateManager {

    private static final Logger log = Logger.getLogger(ProvisioningManagerImpl.class);

    private IProvisioningAgentProvider provAgentProvider;
    private Configurator configurator;
    private UpdateConfiguration config;
    private Transport transport;
    private FrameworkAdmin frameworkAdmin;


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {
        if ( this.provAgentProvider instanceof AgentProviderImpl ) {
            ( (AgentProviderImpl) this.provAgentProvider ).close();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener ( NotificationListener listener, NotificationFilter filter, Object handback ) {}


    /**
     * {@inheritDoc}
     *
     * @see javax.management.NotificationBroadcaster#getNotificationInfo()
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo () {
        return new MBeanNotificationInfo[] {};
    }


    @Override
    public void removeNotificationListener ( NotificationListener listener ) throws ListenerNotFoundException {}


    @Override
    public void removeNotificationListener ( NotificationListener listener, NotificationFilter filter, Object handback )
            throws ListenerNotFoundException {

    }


    /**
     * @param provAgentProvider
     * @param configurator
     * @param frameworkAdmin
     * @param transport
     * @param cfg
     */
    public ProvisioningManagerImpl ( IProvisioningAgentProvider provAgentProvider, Configurator configurator, FrameworkAdmin frameworkAdmin,
            Transport transport, UpdateConfiguration cfg ) {
        this.provAgentProvider = provAgentProvider;
        this.configurator = configurator;
        this.frameworkAdmin = frameworkAdmin;
        this.transport = transport;
        this.config = cfg;
    }


    protected static IMetadataRepositoryManager getRepositoryManager ( IProvisioningAgent agent ) {
        return new NoPreferencesRepositoryManager(agent);
    }


    private static IPlanner getPlanner ( IProvisioningAgent agent ) {
        IPlanner p = (IPlanner) agent.getService(IPlanner.class.getName());
        if ( p == null ) {
            throw new IllegalStateException("Provisioning system has not been initialized"); //$NON-NLS-1$
        }

        return p;

    }


    protected ClosableProvisioningAgent createP2Agent ( UpdateConfiguration cfg ) throws URISyntaxException, ProvisionException {
        if ( this.frameworkAdmin.getManipulator() == null ) {
            throw new ProvisionException("No manipulator available"); //$NON-NLS-1$
        }
        IProvisioningAgent agent = this.provAgentProvider.createAgent(cfg.getTargetArea());
        agent.registerService(Transport.SERVICE_NAME, this.transport);
        agent.registerService(UIServices.SERVICE_NAME, new UIServicesImpl());
        try {
            agent.registerService(
                IProfileRegistry.SERVICE_NAME,
                new ProfileRegistryWrapper(
                    agent,
                    SimpleProfileRegistry.getDefaultRegistryDirectory((IAgentLocation) agent.getService(IAgentLocation.SERVICE_NAME)),
                    getInstallLocation(cfg)));
        }
        catch ( IOException e ) {
            throw new ProvisionException("Install location does not exist", e); //$NON-NLS-1$
        }
        return new ClosableProvisioningAgent(agent);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UpdateException
     * 
     * @see eu.agno3.runtime.update.UpdateManager#listRepositories()
     */
    @Override
    public URI[] listRepositories () throws UpdateException {
        UpdateConfiguration cfg = this.config;
        try ( ClosableProvisioningAgent agent = createP2Agent(cfg) ) {
            loadRepositories(agent, cfg, new NullProgressMonitor(), false);
            return getRepositoryManager(agent).getKnownRepositories(IRepositoryManager.REPOSITORIES_ALL);
        }
        catch (
            ProvisionException |
            URISyntaxException e ) {
            log.warn("Failed to load repositories", e); //$NON-NLS-1$
            return new URI[] {};
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws UpdateException
     * 
     * @see eu.agno3.runtime.update.UpdateManager#listInstalledFeatures()
     */
    @Override
    public Set<Feature> listInstalledFeatures () throws UpdateException {
        UpdateConfiguration cfg = this.config;
        try ( ClosableProvisioningAgent agent = createP2Agent(cfg) ) {
            Iterable<IInstallableUnit> res = getInstalledFeatures(agent, cfg);
            Set<IInstallableUnit> installed = new HashSet<>();

            if ( res == null ) {
                return Collections.EMPTY_SET;
            }

            for ( IInstallableUnit iu : res ) {
                installed.add(iu);
            }
            return toFeatures(installed);
        }
        catch (
            ProvisionException |
            URISyntaxException e ) {
            log.warn("Failed to get installed features", e); //$NON-NLS-1$
            return Collections.EMPTY_SET;
        }
    }


    /**
     * @param ius
     * @return
     */
    private static Set<Feature> toFeatures ( Set<IInstallableUnit> ius ) {

        if ( ius == null ) {
            return null;
        }

        Set<Feature> features = new HashSet<>();
        for ( IInstallableUnit iu : ius ) {
            features.add(toFeature(iu));
        }

        return features;
    }


    /**
     * @param iu
     * @return
     */
    private static Feature toFeature ( IInstallableUnit iu ) {
        return new Feature(iu.getId(), iu.getVersion().toString());
    }


    IProfile getTargetProfile ( IProvisioningAgent agent, UpdateConfiguration cfg ) throws UpdateException {
        IProfileRegistry reg = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
        IProfile profile = reg.getProfile(cfg.getTargetProfile());

        if ( profile == null ) {
            if ( log.isDebugEnabled() ) {
                for ( IProfile prof : reg.getProfiles() ) {
                    log.debug("Available profile " + prof.getProfileId()); //$NON-NLS-1$
                }
            }
            try {
                throw new UpdateException(String.format("Target profile %s does not exist at %s", cfg.getTargetProfile(), cfg.getTargetArea())); //$NON-NLS-1$
            }
            catch ( URISyntaxException e ) {
                throw new UpdateException("Invalid target uri", e); //$NON-NLS-1$
            }
        }

        return profile;
    }


    private static String getInstallLocation ( UpdateConfiguration cfg ) throws URISyntaxException, IOException {
        return Paths.get(cfg.getTargetArea()).getParent().toRealPath().toString();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#getInstallableFeatures()
     */
    @Override
    public Set<Feature> getInstallableFeatures () throws UpdateException {
        UpdateConfiguration cfg = this.config;
        try ( ClosableProvisioningAgent agent = this.createP2Agent(cfg) ) {
            IQueryResult<IInstallableUnit> query = getRepositoryManager(agent)
                    .query(QueryUtil.createLatestQuery(QueryUtil.createIUGroupQuery()), null);

            Set<IInstallableUnit> installable = new HashSet<>();

            for ( IInstallableUnit iu : query ) {

                IQueryResult<IInstallableUnit> q = this.getTargetProfile(agent, cfg).query(QueryUtil.createIUQuery(iu.getId()), null);

                if ( q.iterator().hasNext() ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Already installed feature %s %s", iu.getId(), iu.getVersion())); //$NON-NLS-1$
                    }
                }
                else {
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Not installed feature %s %s", iu.getId(), iu.getVersion())); //$NON-NLS-1$
                    }
                    installable.add(iu);
                }
            }

            return toFeatures(installable);
        }
        catch (
            ProvisionException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to get installable features", wrapException(e)); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#installFeature(java.lang.String)
     */
    @Override
    public void installFeature ( String spec ) throws UpdateException {
        installFeature(spec, new LoggingProgressMonitor());
    }


    /**
     * 
     * @param spec
     * @param monitor
     * @throws UpdateException
     */
    @Override
    public void installFeature ( String spec, IProgressMonitor monitor ) throws UpdateException {
        UpdateConfiguration cfg = this.config;
        try ( ClosableProvisioningAgent agent = this.createP2Agent(cfg) ) {

            IQueryResult<IInstallableUnit> query = getRepositoryManager(agent).query(QueryUtil.createIUQuery(VersionedId.parse(spec)), null);

            if ( query.isEmpty() ) {
                throw new UpdateException("Requested IU not found " + spec); //$NON-NLS-1$
            }

            log.info("Installing " + query.iterator().next()); //$NON-NLS-1$

            this.installFeatures(agent, cfg, query.toSet(), monitor);
        }
        catch (
            ProvisionException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to install feature", wrapException(e)); //$NON-NLS-1$
        }
    }


    protected void installFeatures ( IProvisioningAgent agent, UpdateConfiguration cfg, Collection<IInstallableUnit> units, IProgressMonitor monitor )
            throws UpdateException {
        try {
            SubMonitor sm = SubMonitor.convert(monitor, "installing Features", 1500); //$NON-NLS-1$
            InstallOperation inst = createInstallOperation(agent, cfg, units);

            IStatus status = inst.resolveModal(sm.split(500));

            if ( log.isDebugEnabled() ) {
                log.debug("Status is " + status); //$NON-NLS-1$
            }

            if ( inst.getProfileChangeRequest() != null && log.isDebugEnabled() ) {
                log.debug("Change request is " + inst.getProfileChangeRequest()); //$NON-NLS-1$
            }

            if ( !checkStatus(inst, status) ) {
                throw new UpdateException("Cannot install the given units"); //$NON-NLS-1$
            }

            if ( !executeProvisioningJob(sm.split(1000), cfg, inst, false) ) {
                throw new UpdateException("Failed to install features"); //$NON-NLS-1$
            }
        }
        finally {
            monitor.done();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#removeFeature(java.lang.String)
     */
    @Override
    public void removeFeature ( String spec ) throws UpdateException {
        removeFeature(spec, new LoggingProgressMonitor());
    }


    /**
     * 
     * @param spec
     * @param monitor
     * @throws UpdateException
     */
    @Override
    public void removeFeature ( String spec, IProgressMonitor monitor ) throws UpdateException {
        UpdateConfiguration cfg = this.config;
        try ( ClosableProvisioningAgent agent = this.createP2Agent(cfg) ) {
            IQueryResult<IInstallableUnit> query = getRepositoryManager(agent).query(QueryUtil.createIUQuery(VersionedId.parse(spec)), null);

            if ( query.isEmpty() ) {
                throw new UpdateException("Requested IU not found " + spec); //$NON-NLS-1$
            }

            log.info("Installing " + query.iterator().next()); //$NON-NLS-1$

            this.removeFeatures(agent, this.config, query.toSet(), monitor);
        }
        catch (
            ProvisionException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to remove feature", wrapException(e)); //$NON-NLS-1$
        }
    }


    protected void removeFeatures ( IProvisioningAgent agent, UpdateConfiguration cfg, Collection<IInstallableUnit> units, IProgressMonitor monitor )
            throws UpdateException {
        try {
            SubMonitor sm = SubMonitor.convert(monitor, "removing features", 1000); //$NON-NLS-1$
            monitor.beginTask("removing features", 1000); //$NON-NLS-1$
            UninstallOperation uninst = createUninstallOperation(agent, cfg, units);

            IStatus status = uninst.resolveModal(sm.split(500));

            if ( !checkStatus(uninst, status) ) {
                throw new UpdateException("Cannot uninstall the given units"); //$NON-NLS-1$
            }

            executeProvisioningJob(sm, cfg, uninst, false);
        }
        finally {
            monitor.done();
        }
    }


    private IQueryResult<IInstallableUnit> getInstalledFeatures ( IProvisioningAgent agent, UpdateConfiguration cfg ) throws UpdateException {
        return this.getTargetProfile(agent, cfg).available(new UserVisibleRootQuery(), null);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#installAllUpdates(java.util.Set)
     */
    @Override
    public void installAllUpdates ( Set<URI> repositories ) throws UpdateException {
        installAllUpdates(repositories, new LoggingProgressMonitor());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#installUpdates(java.util.Set, java.util.Set)
     */
    @Override
    public void installUpdates ( Set<Feature> updates, Set<URI> repositories ) throws UpdateException {
        installUpdates(updates, repositories, new LoggingProgressMonitor());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#installAllUpdates(java.util.Set,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void installAllUpdates ( Set<URI> repositories, IProgressMonitor monitor ) throws UpdateException {
        installUpdates(null, repositories, monitor);
    }


    @Override
    public void installUpdates ( Set<Feature> selection, Set<URI> repositories, IProgressMonitor monitor ) throws UpdateException {
        UpdateConfiguration cfg = makeRepositoryConfig(repositories);
        try ( ClosableProvisioningAgent agent = createP2Agent(cfg) ) {
            SubMonitor sm = SubMonitor.convert(monitor, "installing updates", 1300); //$NON-NLS-1$
            UpdateOperation up = checkForUpdatesInternal(agent, cfg, null, sm.split(300));

            Set<Feature> realTargets = new HashSet<>();
            selectRealTargets(selection, cfg, agent, realTargets);

            if ( up == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("No updates found for " + this.config.getTargetArea()); //$NON-NLS-1$
                }
                return;
            }

            if ( selection != null ) {
                selectUpdates(realTargets, up);
            }

            dumpProvisionDetails(up.getProvisioningPlan());

            if ( !executeProvisioningJob(sm.split(1000), cfg, up, false) ) {
                throw new UpdateException("Failed to install updates"); //$NON-NLS-1$
            }
        }
        catch (
            ProvisionException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to install updates", wrapException(e)); //$NON-NLS-1$
        }
        finally {
            monitor.done();
        }
    }


    @Override
    public void doApplyUpdate () throws UpdateException {
        try {
            if ( this.configurator == null ) {
                throw new UpdateException("Not a local installation"); //$NON-NLS-1$
            }
            this.configurator.applyConfiguration();
        }
        catch ( IOException e ) {
            throw new UpdateException("Failed to apply bundle updates", e); //$NON-NLS-1$
        }
    }


    /**
     * @param provisioningPlan
     */
    private static void dumpProvisionDetails ( IProvisioningPlan provisioningPlan ) {

        if ( !log.isDebugEnabled() ) {
            return;
        }

        IQuery<IInstallableUnit> bundleQuery = QueryUtil.ALL_UNITS;
        Set<IInstallableUnit> additions = provisioningPlan.getAdditions().query(bundleQuery, new NullProgressMonitor()).toSet();
        Set<IInstallableUnit> removals = provisioningPlan.getRemovals().query(bundleQuery, new NullProgressMonitor()).toSet();

        MultiValuedMap<String, IInstallableUnit> additionsById = new HashSetValuedHashMap<>();
        MultiValuedMap<String, IInstallableUnit> removalsById = new HashSetValuedHashMap<>();

        for ( IInstallableUnit iu : additions ) {
            boolean bundle = false;
            for ( IArtifactKey artifact : iu.getArtifacts() ) {
                if ( "osgi.bundle".equals(artifact.getClassifier()) ) { //$NON-NLS-1$
                    bundle = true;
                    break;
                }
            }
            if ( bundle ) {
                additionsById.put(iu.getId(), iu);
            }
        }

        for ( IInstallableUnit iu : removals ) {
            boolean bundle = false;
            for ( IArtifactKey artifact : iu.getArtifacts() ) {
                if ( "osgi.bundle".equals(artifact.getClassifier()) ) { //$NON-NLS-1$
                    bundle = true;
                    break;
                }
            }
            if ( bundle ) {
                removalsById.put(iu.getId(), iu);
            }
        }

        Set<String> allIds = new HashSet<>(additionsById.keySet());
        allIds.addAll(removalsById.keySet());

        for ( String id : allIds ) {

            if ( additionsById.containsKey(id) && removalsById.containsKey(id) ) {
                log.debug(String.format("Update %s with %s", removalsById.get(id), additionsById.get(id))); //$NON-NLS-1$
            }
            else if ( additionsById.containsKey(id) ) {
                log.debug("Add " + id + additionsById.get(id)); //$NON-NLS-1$
            }
            else if ( removalsById.containsKey(id) ) {
                log.debug("Remove " + removalsById.get(id)); //$NON-NLS-1$
            }

        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#prepareUpdate(java.util.Set, java.util.Set)
     */
    @Override
    public boolean prepareUpdate ( Set<URI> repositories, Set<Feature> targets ) throws UpdateException {
        return prepareUpdate(repositories, targets, new LoggingProgressMonitor());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#prepareUpdate(java.util.Set, java.util.Set,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public boolean prepareUpdate ( Set<URI> repositories, Set<Feature> targets, IProgressMonitor monitor ) throws UpdateException {
        UpdateConfiguration cfg = makeRepositoryConfig(repositories);
        try ( ClosableProvisioningAgent p2Agent = createP2Agent(cfg) ) {
            SubMonitor sm = SubMonitor.convert(monitor, "preparing/downloading updates", 1300); //$NON-NLS-1$

            Set<Feature> realTargets = new HashSet<>();
            boolean allInstalled = selectRealTargets(targets, cfg, p2Agent, realTargets);

            UpdateOperation up = checkForUpdatesInternal(p2Agent, cfg, null, sm.split(300));

            if ( up == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("No updates found for " + this.config.getTargetArea()); //$NON-NLS-1$
                    log.debug("Requested are " + realTargets); //$NON-NLS-1$
                    log.debug("Repositories are " + repositories); //$NON-NLS-1$
                    for ( IInstallableUnit iInstallableUnit : getInstalledFeatures(p2Agent, this.config) ) {
                        log.debug("Installed " + iInstallableUnit); //$NON-NLS-1$
                    }
                }

                if ( !allInstalled ) {
                    throw new UpdateException("Requested updates are not available"); //$NON-NLS-1$
                }

                return false;
            }

            selectUpdates(realTargets, up);

            dumpProvisionDetails(up.getProvisioningPlan());

            if ( !executeProvisioningJob(sm, cfg, up, true) ) {
                throw new UpdateException("Failed to install updates"); //$NON-NLS-1$
            }

            return true;
        }
        catch (
            ProvisionException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to install updates", wrapException(e)); //$NON-NLS-1$
        }
        finally {
            monitor.done();
        }
    }


    /**
     * @param targets
     * @param cfg
     * @param p2Agent
     * @param realTargets
     * @return
     * @throws UpdateException
     */
    private boolean selectRealTargets ( Set<Feature> targets, UpdateConfiguration cfg, ClosableProvisioningAgent p2Agent, Set<Feature> realTargets )
            throws UpdateException {
        IQueryResult<IInstallableUnit> installedFeatures = getInstalledFeatures(p2Agent, cfg);
        Map<String, Version> installedIds = new HashMap<>();
        boolean allInstalled = true;
        for ( IInstallableUnit inst : installedFeatures ) {
            installedIds.put(inst.getId(), inst.getVersion());
        }

        for ( Feature f : targets ) {
            if ( installedIds.containsKey(f.getId()) ) {
                if ( !Version.create(f.getVersion()).equals(installedIds.get(f.getId())) ) {
                    allInstalled = false;
                    realTargets.add(f);
                }
                else {
                    log.debug("Already installed " + f); //$NON-NLS-1$
                }
            }
            else {
                log.debug("Requested feature is not installed " + f.getId()); //$NON-NLS-1$
            }
        }
        return allInstalled;
    }


    /**
     * @param repositories
     * @return
     */
    private UpdateConfiguration makeRepositoryConfig ( Set<URI> repositories ) {
        return new UpdateConfigurationWithRepositories(this.config, repositories);
    }


    /**
     * @param selection
     * @param up
     * @throws UpdateException
     */
    private static void selectUpdates ( Set<Feature> selection, UpdateOperation up ) throws UpdateException {
        Set<Feature> toSelect = new HashSet<>(selection);
        Set<Update> selected = new HashSet<>();

        for ( Update u : up.getPossibleUpdates() ) {
            log.debug("Possible update " + u); //$NON-NLS-1$
            Feature feature = toFeature(u.replacement);
            if ( toSelect.contains(feature) ) {
                selected.add(u);
                toSelect.remove(feature);
            }
        }

        up.setSelectedUpdates(selected.toArray(new Update[] {}));

        if ( !toSelect.isEmpty() ) {
            throw new UpdateException("Unresolvable updates " + toSelect); //$NON-NLS-1$
        }
    }


    /**
     * @param monitor
     * @param inst
     * @param downloadOnly
     * @throws UpdateException
     */
    private static boolean executeProvisioningJob ( SubMonitor monitor, UpdateConfiguration cfg, ProfileChangeOperation inst, boolean downloadOnly )
            throws UpdateException {
        IStatus status;
        ProvisioningJob provJob = inst.getProvisioningJob(new NullProgressMonitor());

        if ( provJob == null ) {
            log.warn("Could not get provisioning job"); //$NON-NLS-1$
            return false;
        }

        if ( provJob instanceof ProfileModificationJob ) {
            ProfileModificationJob pmj = (ProfileModificationJob) provJob;

            if ( downloadOnly ) {
                pmj.setPhaseSet(PhaseSetFactory.createPhaseSetIncluding(new String[] {
                    PhaseSetFactory.PHASE_CHECK_TRUST, PhaseSetFactory.PHASE_COLLECT
                }));
            }
        }

        try {

            if ( log.isDebugEnabled() ) {
                log.debug("Job is " + provJob); //$NON-NLS-1$
            }

            status = provJob.runModal(monitor);

            if ( !status.isOK() || status.getSeverity() == IStatus.ERROR ) {
                log.warn("Error while provisioning: " + status.getMessage(), status.getException()); //$NON-NLS-1$
                for ( IStatus chld : status.getChildren() ) {
                    log.warn(chld.getMessage(), chld.getException());
                }
                if ( status.getException() != null ) {
                    throw new UpdateException("Failed to execute provisioning job", status.getException()); //$NON-NLS-1$
                }
                return false;
            }

            return true;
        }
        finally {
            fixupPermissions(cfg);
        }
    }


    /**
     * @param cfg
     */
    private static void fixupPermissions ( UpdateConfiguration cfg ) {

        String repoPath;
        try {
            URI targetArea = cfg.getTargetArea();
            if ( !"file".equals(targetArea.getScheme()) ) { //$NON-NLS-1$
                log.error("Target is not a file repository " + targetArea); //$NON-NLS-1$
                return;
            }
            repoPath = targetArea.getPath();
        }
        catch ( URISyntaxException e ) {
            log.error("Failed to get install area", e); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Fixing permissions in " + repoPath); //$NON-NLS-1$
        }

        // repoPath is the p2 directory
        Path base = Paths.get(repoPath).resolve(".."); //$NON-NLS-1$
        if ( !Files.isDirectory(base) ) {
            log.error("Target area is not a directory"); //$NON-NLS-1$
            return;
        }

        String[] readableParts = new String[] {
            "features", //$NON-NLS-1$
            "plugins" //$NON-NLS-1$
        };

        String[] writeableParts = new String[] {
            "configuration" //$NON-NLS-1$
        };
        try {
            for ( String readablePart : readableParts ) {
                setPermissions(
                    base.resolve(readablePart),
                    null,
                    cfg.getGroup(),
                    PosixFilePermissions.fromString("rw-r-----"), //$NON-NLS-1$
                    PosixFilePermissions.fromString("rwxr-x---")); //$NON-NLS-1$
            }

            for ( String writeablePart : writeableParts ) {
                setPermissions(
                    base.resolve(writeablePart),
                    cfg.getOwner(),
                    cfg.getGroup(),
                    PosixFilePermissions.fromString("rw-r-----"), //$NON-NLS-1$
                    PosixFilePermissions.fromString("rwxr-x---")); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            log.error("Failed to set permissions", e); //$NON-NLS-1$
        }
    }


    /**
     * @param resolve
     * @param owner
     * @param group
     * @param filePerms
     * @param dirPerms
     * @throws IOException
     */
    private static void setPermissions ( Path resolve, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) throws IOException {
        if ( !Files.isDirectory(resolve, LinkOption.NOFOLLOW_LINKS) ) {
            return;
        }

        Files.walkFileTree(resolve, new PermissionSettingVisitor(owner, group, filePerms, dirPerms));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#runGarbageCollection()
     */
    @Override
    public void runGarbageCollection () throws UpdateException {
        UpdateConfiguration cfg = this.config;
        try ( ClosableProvisioningAgent agent = this.createP2Agent(cfg) ) {
            GarbageCollector gc = (GarbageCollector) agent.getService(GarbageCollector.SERVICE_NAME);
            gc.runGC(this.getTargetProfile(agent, cfg));
        }
        catch (
            ProvisionException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to run garbage collection", wrapException(e)); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LocalUpdateManagerMBean#checkForUpdates()
     */
    @Override
    public Set<FeatureUpdate> checkForUpdates () throws UpdateException {
        return this.checkForUpdates(new LoggingProgressMonitor());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateManager#checkForUpdates(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public Set<FeatureUpdate> checkForUpdates ( IProgressMonitor monitor ) throws UpdateException {
        UpdateConfiguration cfg = this.config;
        try ( ClosableProvisioningAgent agent = this.createP2Agent(cfg) ) {
            UpdateOperation up = this.checkForUpdatesInternal(agent, cfg, null, monitor);

            if ( up == null ) {
                log.debug("No updates available"); //$NON-NLS-1$
                return Collections.EMPTY_SET;
            }

            Iterator<IInstallableUnit> iter = this.getInstalledFeatures(agent, cfg).iterator();

            Set<FeatureUpdate> featureUpdates = new HashSet<>();
            while ( iter.hasNext() ) {
                IInstallableUnit iu = iter.next();
                IQueryResult<IInstallableUnit> replacements = getPlanner(agent).updatesFor(iu, up.getProvisioningContext(), null);

                Iterator<IInstallableUnit> iterator = replacements.iterator();
                if ( !iterator.hasNext() ) {
                    continue;
                }
                FeatureUpdate upd = new FeatureUpdate();
                upd.setOld(toFeature(iu));
                upd.setPossibleUpdates(new ArrayList<>());
                if ( log.isDebugEnabled() ) {
                    log.debug("Update available for Feature " + iu.getId()); //$NON-NLS-1$
                }

                while ( iterator.hasNext() ) {
                    upd.getPossibleUpdates().add(toFeature(iterator.next()));
                }

                featureUpdates.add(upd);
            }

            dumpProvisionDetails(up.getProvisioningPlan());

            return featureUpdates;
        }
        catch (
            ProvisionException |
            URISyntaxException e ) {
            throw new UpdateException("Failed to check for updates", wrapException(e)); //$NON-NLS-1$
        }
        finally {
            monitor.done();
        }
    }


    /**
     * @param e
     * @return
     */
    private static Throwable wrapException ( Exception e ) {
        // eclipse exceptions are not serializable ....
        if ( e instanceof CoreException ) {
            log.warn("Caught provisioning exception", e); //$NON-NLS-1$
            return new UpdateException(String.format("Provisioning exception %s: %s", e.getClass().getName(), e.getMessage())); //$NON-NLS-1$
        }

        return e;
    }


    /**
     * @param monitor
     * @return
     * @throws ProvisionException
     * @throws UpdateException
     */
    private UpdateOperation checkForUpdatesInternal ( IProvisioningAgent agent, UpdateConfiguration cfg, Collection<IInstallableUnit> requested,
            IProgressMonitor monitor ) throws UpdateException {
        try {
            SubMonitor sm = SubMonitor.convert(monitor, "checking for updates", 1000); //$NON-NLS-1$

            loadRepositories(agent, cfg, sm.split(500), true);
            UpdateOperation up = this.createUpdateOperation(agent, cfg, requested);
            IStatus status = up.resolveModal(sm.split(500));

            if ( log.isDebugEnabled() ) {
                log.debug("Status is " + status); //$NON-NLS-1$
            }

            if ( up.getProfileChangeRequest() != null && log.isDebugEnabled() ) {
                log.debug("Change request is " + up.getProfileChangeRequest()); //$NON-NLS-1$
            }

            if ( !checkStatus(up, status) ) {
                throw new UpdateException("Failed to resolve updates"); //$NON-NLS-1$
            }

            if ( status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE ) {
                return null;
            }

            logUpdates(up);
            return up;
        }
        catch ( ProvisionException e ) {
            throw new UpdateException("Could not resolve updates", wrapException(e)); //$NON-NLS-1$
        }
        finally {
            monitor.done();
        }
    }


    /**
     * @param up
     */
    private static void logUpdates ( UpdateOperation up ) {
        Update[] updates = up.getPossibleUpdates();

        if ( log.isDebugEnabled() ) {
            log.debug("Available updates:"); //$NON-NLS-1$
            for ( Update update : updates ) {
                IInstallableUnit oldIU = update.toUpdate;
                IInstallableUnit newIU = update.replacement;
                log.debug(String.format("Update %s-%s -> %s-%s", oldIU.getId(), oldIU.getVersion(), newIU.getId(), newIU.getVersion())); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param monitor
     * @param up
     * @param status
     */
    private static boolean checkStatus ( ProfileChangeOperation up, IStatus status ) {
        if ( status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE ) {
            return true;
        }

        if ( !status.isOK() || status.getSeverity() == IStatus.ERROR ) {

            log.warn("Error while checking for updates: " + status.getMessage(), status.getException()); //$NON-NLS-1$
            for ( IStatus chld : status.getChildren() ) {
                log.warn(chld.getMessage(), chld.getException());
            }
            if ( !up.getResolutionResult().isOK() || !up.hasResolved() ) {
                log.warn("Failed to resolve: " + up.getResolutionDetails()); //$NON-NLS-1$
            }

            return false;
        }

        return true;
    }


    /**
     * @return
     * @throws UpdateException
     */
    private UpdateOperation createUpdateOperation ( IProvisioningAgent agent, UpdateConfiguration cfg, Collection<IInstallableUnit> updates )
            throws UpdateException {
        ProvisioningSession ps = new ProvisioningSession(agent);
        ProvisioningContext ctx = createProvisioningContext(agent, cfg);

        Collection<IInstallableUnit> realUpdates;
        if ( updates == null ) {
            realUpdates = getInstalledFeatures(agent, cfg).toSet();
        }
        else {
            realUpdates = updates;
        }
        UpdateOperation up = new UpdateOperation(ps, realUpdates);
        up.setProvisioningContext(ctx);
        up.setProfileId(cfg.getTargetProfile());
        return up;
    }


    /**
     * @return
     * @throws UpdateException
     */
    private static InstallOperation createInstallOperation ( IProvisioningAgent agent, UpdateConfiguration cfg, Collection<IInstallableUnit> units )
            throws UpdateException {
        ProvisioningSession ps = new ProvisioningSession(agent);
        ProvisioningContext ctx = createProvisioningContext(agent, cfg);

        InstallOperation inst = new InstallOperation(ps, units);
        inst.setProvisioningContext(ctx);
        inst.setProfileId(cfg.getTargetProfile());
        return inst;
    }


    /**
     * @param cfg
     * @param agent
     * @return
     */
    private static ProvisioningContext createProvisioningContext ( IProvisioningAgent agent, UpdateConfiguration cfg ) {
        ProvisioningContext ctx = new ProvisioningContext(agent);
        ctx.setMetadataRepositories(cfg.getRepositories().toArray(new URI[] {}));
        ctx.setArtifactRepositories(cfg.getRepositories().toArray(new URI[] {}));
        return ctx;
    }


    private static UninstallOperation createUninstallOperation ( IProvisioningAgent agent, UpdateConfiguration cfg,
            Collection<IInstallableUnit> units ) {
        ProvisioningSession ps = new ProvisioningSession(agent);
        ProvisioningContext ctx = createProvisioningContext(agent, cfg);

        UninstallOperation inst = new UninstallOperation(ps, units);
        inst.setProvisioningContext(ctx);
        inst.setProfileId(cfg.getTargetProfile());
        return inst;
    }


    private static void loadRepositories ( IProvisioningAgent agent, UpdateConfiguration cfg, IProgressMonitor monitor, boolean refresh )
            throws ProvisionException {
        try {
            IMetadataRepositoryManager repositoryManager = getRepositoryManager(agent);
            SubMonitor sm = SubMonitor.convert(monitor, "loading repositories", cfg.getRepositories().size() * 100); //$NON-NLS-1$
            log.debug("Repositories:"); //$NON-NLS-1$
            for ( URI uri : cfg.getRepositories() ) {
                log.debug(uri);
                if ( !repositoryManager.contains(uri) ) {
                    repositoryManager.addRepository(uri);
                    repositoryManager.loadRepository(uri, sm.split(100));
                }
                else if ( refresh ) {
                    repositoryManager.refreshRepository(uri, sm.split(100));
                }
            }
        }
        finally {
            monitor.done();
        }
    }
}
