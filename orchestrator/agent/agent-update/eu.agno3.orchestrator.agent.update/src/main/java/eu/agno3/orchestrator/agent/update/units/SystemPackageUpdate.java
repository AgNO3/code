/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.service.Service;
import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.base.service.ServiceState;
import eu.agno3.orchestrator.system.base.service.ServiceSystem;
import eu.agno3.orchestrator.system.packagekit.PackageId;
import eu.agno3.orchestrator.system.packagekit.PackageKitException;
import eu.agno3.orchestrator.system.packagekit.PackageUpdate;
import eu.agno3.orchestrator.system.packagekit.SystemUpdateManager;
import eu.agno3.orchestrator.system.update.ServiceInstruction;
import eu.agno3.orchestrator.system.update.ServiceInstructionType;
import eu.agno3.orchestrator.system.update.SystemPackageTarget;


/**
 * @author mbechler
 *
 */
public class SystemPackageUpdate extends AbstractExecutionUnit<SystemPackageUpdateResult, SystemPackageUpdate, SystemPackageUpdateConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 3729681324704935027L;

    private static final Logger log = Logger.getLogger(SystemPackageUpdate.class);

    private Map<String, List<SystemPackageTarget>> originalTargets = new HashMap<>();
    private Map<String, List<SystemPackageTarget>> targets = new HashMap<>();

    private List<ServiceInstruction> beforeInstructions = new ArrayList<>();
    private List<ServiceInstruction> afterInstructions = new ArrayList<>();

    private Set<PackageId> realUpdates = new HashSet<>();

    private boolean suggestReboot;

    private String repository;

    private Set<String> alternativeRestartServices;

    private Set<String> doAlternativeRestart;


    /**
     * @param target
     */
    void addTarget ( SystemPackageTarget target ) {
        List<SystemPackageTarget> t = this.targets.get(target.getPackageName());
        if ( t == null ) {
            t = new ArrayList<>();
            this.targets.put(target.getPackageName(), t);
        }
        t.add(target);

        t = this.originalTargets.get(target.getPackageName());
        if ( t == null ) {
            t = new ArrayList<>();
            this.originalTargets.put(target.getPackageName(), t);
        }
        t.add(target);
    }


    /**
     * @return the targets
     */
    public Map<String, List<SystemPackageTarget>> getTargets () {
        return this.targets;
    }


    /**
     * @param repository
     */
    void setRepository ( String repository ) {
        this.repository = repository;
    }


    /**
     * @return the repository
     */
    public String getRepository () {
        return this.repository;
    }


    /**
     * @return the alternativeRestartServices
     */
    public Set<String> getAlternativeRestartServices () {
        return this.alternativeRestartServices;
    }


    /**
     * @param alternativeRestartServices
     *            the alternativeRestartServices to set
     */
    void setAlternativeRestartServices ( Set<String> alternativeRestartServices ) {
        this.alternativeRestartServices = alternativeRestartServices;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public SystemPackageUpdateResult prepare ( Context context ) throws ExecutionException {
        SystemUpdateManager sum = getSystemUpdateManager(context);

        context.getOutput().info("Preparing system updates"); //$NON-NLS-1$
        try {

            Set<PackageId> installedSoftware = context.getConfig().isNoVerifyEnv() ? Collections.EMPTY_SET
                    : sum.getInstalledSoftware(new SystemUpdateProgressBridge(context, 10, 0));

            Map<String, List<PackageId>> toUpdate = new HashMap<>();
            Map<String, List<PackageId>> installedByPackage = new HashMap<>();
            for ( PackageId c : installedSoftware ) {
                removeInstalled(context, installedByPackage, c);
            }

            for ( Entry<String, List<PackageId>> inst : installedByPackage.entrySet() ) {
                List<SystemPackageTarget> list = this.getTargets().get(inst.getKey());
                List<PackageId> targetIds = new ArrayList<>();
                for ( SystemPackageTarget t : list ) {
                    makeTargetIds(targetIds, t);
                }
                if ( !targetIds.isEmpty() ) {
                    toUpdate.put(inst.getKey(), targetIds);
                }
                else {
                    log.debug("Not found in target " + inst); //$NON-NLS-1$
                }
            }

            if ( toUpdate.isEmpty() ) {
                return new SystemPackageUpdateResult(Status.SKIPPED, false, Collections.EMPTY_SET);
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Collected updates " + toUpdate); //$NON-NLS-1$
            }

            String oldRepo = null;
            try {
                String repo = this.getRepository();
                if ( repo != null ) {
                    context.getOutput().info("Switching to repository " + repo); //$NON-NLS-1$
                    oldRepo = sum.switchRepository(repo);
                }

                Set<PackageUpdate> updates = sum.checkForUpdates(new SystemUpdateProgressBridge(context, 30, 10));
                this.realUpdates.clear();

                for ( PackageUpdate pu : updates ) {
                    collectUpdates(context, toUpdate, pu);
                }

                checkUnresolvable(toUpdate, installedByPackage);

                if ( log.isDebugEnabled() ) {
                    log.debug("Actual updates " + this.realUpdates); //$NON-NLS-1$
                }

                checkEffects(this.realUpdates);
                sum.prepareUpdates(this.realUpdates, new SystemUpdateProgressBridge(context, 60, 40));
            }
            catch ( PackageKitException e ) {
                // revert repository
                if ( oldRepo != null ) {
                    sum.switchRepository(oldRepo);
                }
                throw e;
            }
        }
        catch ( PackageKitException e ) {
            throw new ExecutionException("Failed to check for updates", e); //$NON-NLS-1$
        }

        return new SystemPackageUpdateResult(Status.SUCCESS, this.suggestReboot, this.doAlternativeRestart);
    }


    /**
     * @param targetIds
     * @param t
     */
    private static void makeTargetIds ( List<PackageId> targetIds, SystemPackageTarget t ) {
        for ( String version : t.getTargetVersions() ) {
            PackageId pid = new PackageId();
            pid.setPackageName(t.getPackageName());
            pid.setPackageRepo(t.getTargetRepository());
            pid.setPackageVersion(version);
            targetIds.add(pid);
        }
    }


    /**
     * @param realUpdates2
     */
    private void checkEffects ( Set<PackageId> upd ) {

        boolean reboot = false;
        List<ServiceInstruction> before = new ArrayList<>();
        List<ServiceInstruction> after = new ArrayList<>();

        for ( Entry<String, List<SystemPackageTarget>> entry : this.targets.entrySet() ) {
            for ( SystemPackageTarget systemPackageTarget : entry.getValue() ) {
                List<PackageId> targetIds = new ArrayList<>();
                makeTargetIds(targetIds, systemPackageTarget);
                boolean containsAny = false;
                for ( PackageId pid : targetIds ) {
                    if ( upd.contains(pid) ) {
                        containsAny = true;
                        break;
                    }
                }

                if ( !containsAny ) {
                    continue;
                }

                before.addAll(systemPackageTarget.getBeforeInstructions());
                after.addAll(systemPackageTarget.getAfterInstructions());
                reboot |= systemPackageTarget.getSuggestReboot() != null ? systemPackageTarget.getSuggestReboot() : false;
            }
        }

        this.doAlternativeRestart = new HashSet<>();
        this.doAlternativeRestart.addAll(filterServiceInstructions(before));
        this.doAlternativeRestart.addAll(filterServiceInstructions(after));

        this.suggestReboot = reboot;
        this.beforeInstructions = before;
        this.afterInstructions = after;
    }


    /**
     * @param insts
     * @return
     */
    private Set<String> filterServiceInstructions ( List<ServiceInstruction> insts ) {
        Set<String> found = new HashSet<>();
        for ( Iterator<ServiceInstruction> iterator = insts.iterator(); iterator.hasNext(); ) {
            ServiceInstruction serviceInstruction = iterator.next();

            if ( this.alternativeRestartServices.contains(serviceInstruction.getServiceName()) ) {
                found.add(serviceInstruction.getServiceName());
                iterator.remove();
            }
        }
        return found;
    }


    /**
     * @param toUpdate
     * @param installedByPackage
     * @throws ExecutionException
     */
    private static void checkUnresolvable ( Map<String, List<PackageId>> toUpdate, Map<String, List<PackageId>> installedByPackage )
            throws ExecutionException {
        if ( !toUpdate.isEmpty() ) {
            boolean notFound = false;
            for ( List<PackageId> missing : toUpdate.values() ) {
                if ( !missing.isEmpty() ) {
                    PackageId m = missing.iterator().next();
                    List<PackageId> list = installedByPackage.get(m.getPackageName());
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Missing %s, installed are %s", m, list)); //$NON-NLS-1$
                    }
                    notFound = true;
                }
            }

            if ( notFound ) {
                throw new ExecutionException("Updates cannot be fulfilled: " + toUpdate); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param context
     * @param toUpdate
     * @param pu
     */
    private void collectUpdates ( Context context, Map<String, List<PackageId>> toUpdate, PackageUpdate pu ) {
        String packageName = pu.getUpdate().getPackageName();
        List<PackageId> targetUpdate = toUpdate.get(packageName);

        if ( targetUpdate == null ) {
            context.getOutput().info("Ignoring found update as it is not contained in profile " + packageName); //$NON-NLS-1$
            return;
        }
        else if ( targetUpdate.isEmpty() ) {
            return;
        }

        log.debug("Found updates matching " + targetUpdate); //$NON-NLS-1$

        for ( PackageId pid : targetUpdate ) {
            PackageId cloned = new PackageId(pid);
            if ( StringUtils.isBlank(cloned.getPackageArch()) ) {
                cloned.setPackageArch(pu.getUpdate().getPackageArch());
            }
            this.realUpdates.add(cloned);
        }
        targetUpdate.clear();
    }


    /**
     * @param context
     * @param toUpdate
     * @param installedByPackage
     * @param matched
     * @param c
     */
    private void removeInstalled ( Context context, Map<String, List<PackageId>> installedByPackage, PackageId c ) {
        List<SystemPackageTarget> ts = getTargets().get(c.getPackageName());
        if ( ts == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Ignoring installed package as not part of target " + c.getPackageName()); //$NON-NLS-1$
            }
            return;
        }

        if ( ts.isEmpty() ) {
            return;
        }

        List<SystemPackageTarget> remove = new ArrayList<>();
        for ( SystemPackageTarget t : ts ) {
            if ( matches(c, t) ) {
                remove.add(t);
            }
        }

        if ( !remove.isEmpty() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Target is already installed, remove from target " + remove); //$NON-NLS-1$
            }
            ts.removeAll(remove);
            return;
        }

        List<PackageId> packageId = installedByPackage.get(c.getPackageName());
        if ( packageId == null ) {
            packageId = new ArrayList<>();
            installedByPackage.put(c.getPackageName(), packageId);
        }
        packageId.add(c);
    }


    /**
     * @param c
     * @param t
     * @return
     */
    private static boolean matches ( PackageId c, SystemPackageTarget t ) {

        if ( !c.getPackageName().equals(t.getPackageName()) ) {
            return false;
        }

        if ( !t.getTargetVersions().contains(c.getPackageVersion()) ) {
            return false;
        }

        if ( t.getTargetRepository() != null && !t.getTargetRepository().equals(c.getPackageRepo()) ) {
            return false;
        }

        return true;
    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    private static SystemUpdateManager getSystemUpdateManager ( Context context ) throws ExecutionException {
        try {
            return context.getConfig().getService(SystemUpdateManager.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Failed to get system update manager", e); //$NON-NLS-1$
        }
    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    private static ServiceSystem getServiceSystem ( Context context ) throws ExecutionException {
        try {
            return context.getConfig().getService(ServiceSystem.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Failed to get service system", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public SystemPackageUpdateResult execute ( Context context ) throws ExecutionException {
        if ( this.realUpdates.isEmpty() ) {
            return new SystemPackageUpdateResult(Status.SKIPPED, false, Collections.EMPTY_SET);
        }

        ServiceSystem service = getServiceSystem(context);
        List<ServiceInstruction> revertInstructions = new ArrayList<>();
        try {
            for ( ServiceInstruction serviceInstruction : this.beforeInstructions ) {
                handleInstruction(service, serviceInstruction, revertInstructions);
            }

            context.getOutput().info("Installing updates " + this.realUpdates); //$NON-NLS-1$
            if ( !context.getConfig().isDryRun() ) {
                SystemUpdateManager sum = getSystemUpdateManager(context);
                String oldRepo = null;
                try {
                    oldRepo = sum.switchRepository(this.getRepository());
                    sum.installUpdates(this.realUpdates, new SystemUpdateProgressBridge(context, 70, 10));
                }
                catch ( PackageKitException e ) {
                    // revert repository
                    if ( oldRepo != null ) {
                        sum.switchRepository(oldRepo);
                    }
                    throw e;
                }
            }

        }
        catch (
            PackageKitException |
            ServiceException e ) {
            revertServiceInstructions(service, revertInstructions);
            throw new ExecutionException("Failed to install updates", e); //$NON-NLS-1$
        }

        for ( ServiceInstruction serviceInstruction : this.afterInstructions ) {
            try {
                handleInstruction(service, serviceInstruction, null);
            }
            catch ( ServiceException e2 ) {
                throw new ExecutionException("Failed to run post update instructions", e2); //$NON-NLS-1$
            }
        }

        validateInstalled(context);

        return new SystemPackageUpdateResult(Status.SUCCESS, this.suggestReboot, this.doAlternativeRestart);
    }


    /**
     * @param context
     * @throws ExecutionException
     */
    private void validateInstalled ( Context context ) throws ExecutionException {
        try {
            Set<PackageId> installedSoftware = getSystemUpdateManager(context).getInstalledSoftware(new SystemUpdateProgressBridge(context, 10, 90));
            for ( PackageId pkg : installedSoftware ) {
                List<SystemPackageTarget> ts = this.originalTargets.get(pkg.getPackageName());
                if ( ts == null || ts.isEmpty() ) {
                    continue;
                }

                boolean found = false;
                for ( SystemPackageTarget t : ts ) {
                    if ( t.getTargetVersions().contains(pkg.getPackageVersion()) ) {
                        found = true;
                        break;
                    }
                }

                if ( !found ) {
                    String msg = String.format(
                        "Package validation failed, %s installed version %s is not in profile", //$NON-NLS-1$
                        pkg.getPackageName(),
                        pkg.getPackageVersion());
                    if ( context.getConfig().isDryRun() ) {
                        context.getOutput().error(msg);
                    }
                    else {
                        throw new ExecutionException(msg);
                    }
                }
            }
        }
        catch ( PackageKitException e ) {
            throw new ExecutionException("Failed to check package state", e); //$NON-NLS-1$
        }
    }


    /**
     * @param service
     * @param revertInstructions
     */
    private static void revertServiceInstructions ( ServiceSystem service, List<ServiceInstruction> revertInstructions ) {
        for ( ServiceInstruction revert : revertInstructions ) {
            try {
                handleInstruction(service, revert, null);
            }
            catch ( ServiceException e2 ) {
                log.error("Failed to revert service instruction", e2); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param service
     * @param serviceInstruction
     * @throws ServiceException
     */
    private static void handleInstruction ( ServiceSystem service, ServiceInstruction serviceInstruction,
            List<ServiceInstruction> revertInstructions ) throws ServiceException {
        Service s = service.getService(serviceInstruction.getServiceName());
        ServiceInstruction revert = new ServiceInstruction();
        revert.setServiceName(serviceInstruction.getServiceName());
        switch ( serviceInstruction.getType() ) {
        case RELOAD:
            s.reload();
            break;
        case RESTART:
            s.restart();
            break;
        case START:
            if ( s.getState() == ServiceState.INACTIVE ) {
                revert.setType(ServiceInstructionType.STOP);
            }
            s.start();
            break;
        case STOP:
            if ( s.getState() == ServiceState.ACTIVE ) {
                revert.setType(ServiceInstructionType.START);
            }
            s.stop();
            break;
        default:
            break;

        }
        if ( revertInstructions != null && revert.getType() != null ) {
            revertInstructions.add(revert);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public SystemPackageUpdateConfigurator createConfigurator () {
        return new SystemPackageUpdateConfigurator(this);
    }

}
