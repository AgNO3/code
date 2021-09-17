/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.update.ServiceReconfigurator;
import eu.agno3.orchestrator.agent.update.UpdateJobFactory;
import eu.agno3.orchestrator.agent.update.units.P2Update;
import eu.agno3.orchestrator.agent.update.units.ReconfigureService;
import eu.agno3.orchestrator.agent.update.units.SystemPackageUpdate;
import eu.agno3.orchestrator.agent.update.units.SystemPackageUpdateConfigurator;
import eu.agno3.orchestrator.agent.update.units.SystemPackageUpdateResult;
import eu.agno3.orchestrator.agent.update.units.TrackUpdate;
import eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemException;
import eu.agno3.orchestrator.config.hostconfig.agent.BaseSystemIntegration;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.backup.units.BackupResult;
import eu.agno3.orchestrator.jobs.agent.backup.units.CreateBackup;
import eu.agno3.orchestrator.jobs.agent.monitor.units.ServiceCheck;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.agent.ReestablishAgentConnection;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.Predicate;
import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.base.units.log.Log;
import eu.agno3.orchestrator.system.base.units.service.RestartService;
import eu.agno3.orchestrator.system.base.units.service.StartService;
import eu.agno3.orchestrator.system.base.units.service.StopService;
import eu.agno3.orchestrator.system.base.units.suspend.Suspend;
import eu.agno3.orchestrator.system.img.util.SystemImageUtil;
import eu.agno3.orchestrator.system.update.AbstractServiceUpdateUnit;
import eu.agno3.orchestrator.system.update.P2FeatureTarget;
import eu.agno3.orchestrator.system.update.P2UpdateUnit;
import eu.agno3.orchestrator.system.update.ServiceUpdateDescriptor;
import eu.agno3.orchestrator.system.update.SystemPackageTarget;
import eu.agno3.orchestrator.system.update.SystemUpdateUnit;
import eu.agno3.orchestrator.system.update.UpdateDescriptor;
import eu.agno3.runtime.update.Feature;


/**
 * @author mbechler
 *
 */
@Component ( service = UpdateJobFactory.class )
public class UpdateJobFactoryImpl implements UpdateJobFactory {

    private static final Logger log = Logger.getLogger(UpdateJobFactoryImpl.class);

    private static final boolean TESTING = false;

    private ConfigRepository configRepository;

    private BaseSystemIntegration systemIntegration;

    private ServiceReconfigurator serviceReconfigurator;

    private ServiceManager serviceManager;


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository repo ) {
        this.configRepository = repo;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository repo ) {
        if ( this.configRepository == repo ) {
            this.configRepository = null;
        }
    }


    @Reference
    protected synchronized void setServiceReconfigurator ( ServiceReconfigurator sr ) {
        this.serviceReconfigurator = sr;
    }


    protected synchronized void unsetServiceReconfigurator ( ServiceReconfigurator sr ) {
        if ( this.serviceReconfigurator == sr ) {
            this.serviceReconfigurator = null;
        }
    }


    @Reference
    protected synchronized void setSystemIntegration ( BaseSystemIntegration bsi ) {
        this.systemIntegration = bsi;
    }


    protected synchronized void unsetSystemIntegration ( BaseSystemIntegration bsi ) {
        if ( this.systemIntegration == bsi ) {
            this.systemIntegration = null;
        }
    }


    @Reference
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.UpdateJobFactory#getReconfigurationClassLoaders(eu.agno3.orchestrator.system.update.UpdateDescriptor)
     */
    @Override
    public Collection<ClassLoader> getReconfigurationClassLoaders ( UpdateDescriptor updateDescriptor ) {
        Set<ClassLoader> cls = new HashSet<>();
        try {
            Map<ServiceStructuralObject, ServiceUpdateDescriptor> services = getServicesToUpdate(updateDescriptor);

            for ( Entry<ServiceStructuralObject, ServiceUpdateDescriptor> e : services.entrySet() ) {
                ClassLoader unitClassLoader = this.serviceReconfigurator.getUnitClassLoader(e.getKey().getServiceType());
                if ( unitClassLoader != null ) {
                    cls.add(unitClassLoader);
                }
            }

            return cls;
        }
        catch (
            ConfigRepositoryException |
            JobBuilderException e ) {
            return Collections.EMPTY_SET;
        }

    }


    @Override
    public void buildJob ( @NonNull JobBuilder b, boolean allowReboot, String stream, UpdateDescriptor desc ) throws JobBuilderException {
        try {
            b.onFail().add(Log.class).error("Update failed, triggering recovery (reboot)"); //$NON-NLS-1$
            if ( !TESTING ) {
                b.onFail().add(Exec.class).cmd("/usr/share/agno3-bootloader/quickrevert.sh").ignoreExitCode(); //$NON-NLS-1$
                this.systemIntegration.reboot(b.onFail(), 4);
            }
        }
        catch (
            UnitInitializationFailedException |
            BaseSystemException e ) {
            throw new JobBuilderException("Failed to add failsafe reboot", e); //$NON-NLS-1$
        }

        String localImage = SystemImageUtil.getLocalImageType();

        if ( localImage != null && !localImage.equals(desc.getImageType()) ) {
            throw new JobBuilderException("Update descriptor is not applicable for this appliance"); //$NON-NLS-1$
        }

        Map<ServiceStructuralObject, ServiceUpdateDescriptor> services;
        try {
            services = getServicesToUpdate(desc);
        }
        catch ( ConfigRepositoryException e ) {
            throw new JobBuilderException("Failed to enumerate services", e); //$NON-NLS-1$
        }

        SystemUpdateUnit sysUpdates = null;
        P2UpdateUnit agentUpdate = null;
        ServiceStructuralObject agentService = null;
        Map<ServiceStructuralObject, P2UpdateUnit> serviceUpdates = new HashMap<>();
        Map<ServiceStructuralObject, String> altRestartServices = new HashMap<>();

        for ( Entry<ServiceStructuralObject, ServiceUpdateDescriptor> e : services.entrySet() ) {
            List<AbstractServiceUpdateUnit<?>> units = e.getValue().getUnits();
            if ( units == null ) {
                continue;
            }
            for ( AbstractServiceUpdateUnit<?> u : units ) {

                if ( u instanceof P2UpdateUnit ) {
                    try {
                        StructuralObjectReference ref = StructuralObjectReferenceImpl.fromObject(e.getKey());
                        if ( this.serviceManager.hasServiceManagerFor(ref) ) {
                            RuntimeServiceManager sm = this.serviceManager.getServiceManager(ref, RuntimeServiceManager.class);
                            String sName = sm.getSystemServiceName();
                            if ( sName != null ) {
                                altRestartServices.put(e.getKey(), sName);
                            }
                        }
                    }
                    catch ( ServiceManagementException e1 ) {
                        throw new JobBuilderException("Failed to get service manager for " + e.getKey()); //$NON-NLS-1$
                    }
                }

                if ( u instanceof P2UpdateUnit && HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(e.getValue().getServiceType()) ) {
                    if ( agentUpdate != null ) {
                        agentUpdate.merge((P2UpdateUnit) u);
                    }
                    else {
                        agentUpdate = (P2UpdateUnit) u;
                    }

                    agentService = e.getKey();
                }
                else if ( u instanceof P2UpdateUnit ) {
                    P2UpdateUnit existing = serviceUpdates.get(e.getKey());
                    if ( existing != null ) {
                        existing.merge((P2UpdateUnit) u);
                    }
                    else {
                        existing = (P2UpdateUnit) u;
                    }
                    serviceUpdates.put(e.getKey(), existing);
                }
                else if ( u instanceof SystemUpdateUnit ) {
                    if ( sysUpdates != null ) {
                        sysUpdates = sysUpdates.merge((SystemUpdateUnit) u);
                    }
                    else {
                        sysUpdates = (SystemUpdateUnit) u;
                    }
                }
            }
        }

        // setup failsafe
        setupFailsafe(b);

        ResultReference<BackupResult> br;
        try {
            br = b.add(CreateBackup.class).getExecutionUnit().getResult();
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to add backup", e); //$NON-NLS-1$
        }

        ResultReference<SystemPackageUpdateResult> sysUpd = null;
        if ( sysUpdates != null ) {
            sysUpd = addSystemUpdates(b, sysUpdates, altRestartServices.values());
        }

        Map<ServiceStructuralObject, ResultReference<StatusOnlyResult>> serviceResults = new HashMap<>();

        addAgentUpdate(b, serviceResults, agentService, agentUpdate, altRestartServices, agentUpdate != null && agentService != null, sysUpd);

        // update services
        for ( Entry<ServiceStructuralObject, P2UpdateUnit> e : serviceUpdates.entrySet() ) {
            addServiceUpdate(b, serviceResults, e.getKey(), e.getValue(), altRestartServices, sysUpd);
        }

        // reconfigure services
        try {
            addServiceReconfigurations(b, services, serviceResults);
        }
        catch (
            ConfigRepositoryException |
            ModelServiceException |
            UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to add service reconfigurations", e); //$NON-NLS-1$
        }

        try {
            b.add(ReestablishAgentConnection.class);
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to add ReestablishAgentConnectionUnit", e); //$NON-NLS-1$
        }

        // check and clear failsafe
        trackUpdate(b, allowReboot, stream, desc, sysUpd, br);

        if ( !TESTING ) {
            try {
                b.add(Exec.class).cmd("/usr/bin/grub-editenv") //$NON-NLS-1$
                        .args(
                            "/boot/grub/grubenv", //$NON-NLS-1$
                            "unset", //$NON-NLS-1$
                            "systemfail"); //$NON-NLS-1$
            }
            catch ( UnitInitializationFailedException e1 ) {
                throw new JobBuilderException("Failed to add flag clearing", e1); //$NON-NLS-1$
            }
        }
        addRebootUnit(b, allowReboot, sysUpd);
    }


    /**
     * @param b
     * @throws JobBuilderException
     * 
     */
    private static void setupFailsafe ( @NonNull JobBuilder b ) throws JobBuilderException {
        try {
            if ( !TESTING ) {
                b.add(StartService.class).service("makefailsafe"); //$NON-NLS-1$
                b.add(Exec.class).cmd("/usr/bin/grub-editenv") //$NON-NLS-1$
                        .args("/boot/grub/grubenv", "set", "systemfail=1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to setup failsafe", e); //$NON-NLS-1$
        }
    }


    /**
     * @param b
     * @param allowReboot
     * @param stream
     * @param desc
     * @param sysUpd
     * @param br
     * @throws JobBuilderException
     */
    private static void trackUpdate ( JobBuilder b, boolean allowReboot, String stream, UpdateDescriptor desc,
            ResultReference<SystemPackageUpdateResult> sysUpd, ResultReference<BackupResult> br ) throws JobBuilderException {
        try {
            b.add(TrackUpdate.class).sequence(desc.getSequence()).stream(stream).backupRef(br).suggestReboot(context -> {
                try {
                    return !allowReboot && context.fetchResult(sysUpd).isRebootSuggested();
                }
                catch ( ResultReferenceException e ) {
                    log.warn("Failed to get result", e); //$NON-NLS-1$
                    return false;
                }
            });
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to add track update", e); //$NON-NLS-1$
        }
    }


    /**
     * @param b
     * @param services
     * @param serviceResults
     * @throws ConfigRepositoryException
     * @throws ModelServiceException
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     */
    private void addServiceReconfigurations ( @NonNull JobBuilder b, Map<ServiceStructuralObject, ServiceUpdateDescriptor> services,
            Map<ServiceStructuralObject, ResultReference<StatusOnlyResult>> serviceResults )
                    throws ConfigRepositoryException, ModelServiceException, JobBuilderException, UnitInitializationFailedException {
        for ( Entry<ServiceStructuralObject, ServiceUpdateDescriptor> e : services.entrySet() ) {
            try {
                for ( ServiceStructuralObject sos : this.configRepository.getServicesByType(e.getKey().getServiceType()) ) {
                    b.add(ReconfigureService.class).service(sos).allowRestart().ignoreError().runIf(context -> {
                        try {
                            ResultReference<StatusOnlyResult> updated = serviceResults.get(sos);
                            if ( updated == null ) {
                                return false;
                            }

                            return context.fetchResult(updated).getStatus() == Status.SUCCESS;
                        }
                        catch ( ResultReferenceException ex ) {
                            log.warn("Failed to get result", ex); //$NON-NLS-1$
                            return false;
                        }
                    });
                }
            }
            catch ( Exception ex ) {
                // allows recovery from incompatible service configuration, should not happen but is bad if it does
                b.add(Log.class).error("Failed to add service reconfiguration, you will have to re-apply the config manually"); //$NON-NLS-1$
                log.error("Failed to add service reconfiguration, you will have to re-apply the config manually for " + e.getKey().getServiceType()); //$NON-NLS-1$
                log.debug("Service reconfiguration failed " + e.getKey().getServiceType(), e); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param b
     * @param systemUpdateRef
     * @throws JobBuilderException
     */
    private void addRebootUnit ( JobBuilder b, boolean allowReboot, ResultReference<SystemPackageUpdateResult> systemUpdateRef )
            throws JobBuilderException {
        if ( systemUpdateRef != null ) {
            final ResultReference<SystemPackageUpdateResult> ref = systemUpdateRef;
            try {
                this.systemIntegration.reboot(b, 10).runIf(context -> {
                    try {
                        return allowReboot && context.fetchResult(ref).isRebootSuggested();
                    }
                    catch ( ResultReferenceException e ) {
                        log.warn("Failed to get result", e); //$NON-NLS-1$
                        return false;
                    }
                });
            }
            catch (
                UnitInitializationFailedException |
                BaseSystemException e ) {
                throw new JobBuilderException("Failed to add reboot", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param b
     * @param sysUpdates
     * @param altRestartServices
     * @return
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     */
    private static ResultReference<SystemPackageUpdateResult> addSystemUpdates ( @NonNull JobBuilder b, SystemUpdateUnit sysUpdates,
            Collection<String> altRestartServices ) throws JobBuilderException {
        try {
            SystemPackageUpdateConfigurator c = b.add(SystemPackageUpdate.class);
            c.repository(sysUpdates.getRepository());
            c.altRestartServices(altRestartServices);
            for ( SystemPackageTarget systemPackageTarget : sysUpdates.getTargetProfile() ) {
                c.target(systemPackageTarget);
            }
            return c.getExecutionUnit().getResult();
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to create system update unit", e); //$NON-NLS-1$
        }
    }


    /**
     * @param b
     * @param serviceResults
     * @param key
     * @param value
     * @param altRestartServices
     * @param sysUpd
     * @param needUpdate
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     */
    private static void addAgentUpdate ( @NonNull JobBuilder b, Map<ServiceStructuralObject, ResultReference<StatusOnlyResult>> serviceResults,
            ServiceStructuralObject key, P2UpdateUnit value, Map<ServiceStructuralObject, String> altRestartServices, boolean needUpdate,
            ResultReference<SystemPackageUpdateResult> sysUpd ) throws JobBuilderException {

        String sysService = altRestartServices.get(key);
        Set<P2FeatureTarget> targets = value.getTargets();
        Set<Feature> target = new HashSet<>();
        for ( P2FeatureTarget ft : targets ) {
            target.add(new Feature(ft.getFeatureId() + ".feature.group", ft.getFeatureVersion())); //$NON-NLS-1$
        }

        try {
            final ResultReference<StatusOnlyResult> agentUpdateRes = b.add(P2Update.class).service(key).runIf(needUpdate)
                    .noApply(value.getForceOffline()).repositories(value.getRepositories()).targets(target).getExecutionUnit().getResult();
            serviceResults.put(key, agentUpdateRes);
            Predicate restartPredicate = context -> {
                try {
                    return ( sysUpd != null && context.fetchResult(sysUpd).isRequireRestart(sysService) )
                            || ( value.getForceOffline() && context.fetchResult(agentUpdateRes).getStatus() == Status.SUCCESS );
                }
                catch ( ResultReferenceException e ) {
                    log.warn("Failed to get result", e); //$NON-NLS-1$
                    return false;
                }
            };

            // service triggering a reboot if not stopped in time
            b.add(StartService.class).service("orchagent-failsafe").runIf(restartPredicate); //$NON-NLS-1$
            b.add(Suspend.class).after(1).runIf(restartPredicate);
            b.add(RestartService.class).service("orchagent").noWait().runIf(restartPredicate); //$NON-NLS-1$

            b.add(ServiceCheck.class).service(StructuralObjectReferenceImpl.fromObject(key)).timeout(Duration.standardSeconds(10));
            // this will stop the failsafe timeout if we regain execution here
            b.add(StopService.class).service("orchagent-failsafe").runIf(restartPredicate); //$NON-NLS-1$
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to create update unit", e); //$NON-NLS-1$
        }

    }


    /**
     * @param b
     * @param serviceResults
     * @param key
     * @param value
     * @param altRestartServices
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     */
    private static void addServiceUpdate ( @NonNull JobBuilder b, Map<ServiceStructuralObject, ResultReference<StatusOnlyResult>> serviceResults,
            ServiceStructuralObject key, P2UpdateUnit value, Map<ServiceStructuralObject, String> altRestartServices,
            ResultReference<SystemPackageUpdateResult> sysUpd ) throws JobBuilderException {

        String sysService = altRestartServices.get(key);
        Set<P2FeatureTarget> targets = value.getTargets();
        Set<Feature> target = new HashSet<>();
        for ( P2FeatureTarget ft : targets ) {
            target.add(new Feature(ft.getFeatureId() + ".feature.group", ft.getFeatureVersion())); //$NON-NLS-1$
        }

        try {
            Predicate restartPredicate = context -> {
                try {
                    return ( sysUpd != null && context.fetchResult(sysUpd).isRequireRestart(sysService) ) || value.getForceOffline();
                }
                catch ( ResultReferenceException e ) {
                    log.warn("Failed to get result", e); //$NON-NLS-1$
                    return false;
                }
            };
            ResultReference<StatusOnlyResult> result = b.add(P2Update.class).service(key).forceOffline(restartPredicate)
                    .repositories(value.getRepositories()).targets(target).getExecutionUnit().getResult();
            serviceResults.put(key, result);
        }
        catch ( UnitInitializationFailedException e ) {
            throw new JobBuilderException("Failed to create update unit", e); //$NON-NLS-1$
        }

    }


    /**
     * @return
     * @throws ConfigRepositoryException
     * @throws JobBuilderException
     */
    protected Map<ServiceStructuralObject, ServiceUpdateDescriptor> getServicesToUpdate ( UpdateDescriptor desc )
            throws ConfigRepositoryException, JobBuilderException {
        if ( desc == null ) {
            throw new JobBuilderException("Descriptor is null"); //$NON-NLS-1$
        }
        Map<ServiceStructuralObject, ServiceUpdateDescriptor> services = new LinkedHashMap<>();
        for ( ServiceUpdateDescriptor serviceUpdateDescriptor : desc.getDescriptors() ) {
            ServiceStructuralObject service = this.configRepository.getSingletonServiceByType(serviceUpdateDescriptor.getServiceType());

            if ( service == null ) {
                ServiceStructuralObjectImpl s = new ServiceStructuralObjectImpl();
                s.setServiceType(serviceUpdateDescriptor.getServiceType());
                service = s;
            }
            services.put(service, serviceUpdateDescriptor);
        }
        return services;
    }

}
