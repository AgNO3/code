/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.update.RuntimeServiceUpdater;
import eu.agno3.orchestrator.agent.update.UpdateDescriptorGenerator;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.system.img.util.SystemImageUtil;
import eu.agno3.orchestrator.system.packagekit.PackageId;
import eu.agno3.orchestrator.system.packagekit.PackageKitException;
import eu.agno3.orchestrator.system.packagekit.PackageUpdate;
import eu.agno3.orchestrator.system.packagekit.SystemUpdateManager;
import eu.agno3.orchestrator.system.update.AbstractServiceUpdateUnit;
import eu.agno3.orchestrator.system.update.P2FeatureTarget;
import eu.agno3.orchestrator.system.update.P2UpdateUnit;
import eu.agno3.orchestrator.system.update.ServiceUpdateDescriptor;
import eu.agno3.orchestrator.system.update.SystemPackageTarget;
import eu.agno3.orchestrator.system.update.SystemUpdateUnit;
import eu.agno3.orchestrator.system.update.UpdateDescriptor;
import eu.agno3.orchestrator.system.update.UpdateException;
import eu.agno3.runtime.update.Feature;
import eu.agno3.runtime.update.FeatureUpdate;
import eu.agno3.runtime.update.LoggingProgressMonitor;


/**
 * @author mbechler
 *
 */
@Component ( service = UpdateDescriptorGenerator.class )
public class UpdateDescriptorGeneratorImpl implements UpdateDescriptorGenerator {

    private static final Logger log = Logger.getLogger(UpdateDescriptorGeneratorImpl.class);

    private RuntimeServiceUpdater serviceUpdater;
    private ConfigRepository configRepository;
    private ServiceTypeRegistry serviceTypeRegistry;
    private SystemUpdateManager systemUpdateManager;


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
    protected synchronized void setRuntimeServiceUpdater ( RuntimeServiceUpdater rsu ) {
        this.serviceUpdater = rsu;
    }


    protected synchronized void unsetRuntimeServiceUpdater ( RuntimeServiceUpdater rsu ) {
        if ( this.serviceUpdater == rsu ) {
            this.serviceUpdater = null;
        }
    }


    @Reference
    protected synchronized void setServiceTypeRegistry ( ServiceTypeRegistry reg ) {
        this.serviceTypeRegistry = reg;
    }


    protected synchronized void unsetServiceTypeRegistry ( ServiceTypeRegistry reg ) {
        if ( this.serviceTypeRegistry == reg ) {
            this.serviceTypeRegistry = null;
        }
    }


    @Reference
    protected synchronized void setSystemUpdateManager ( SystemUpdateManager sum ) {
        this.systemUpdateManager = sum;
    }


    protected synchronized void unsetSystemUpdateManager ( SystemUpdateManager sum ) {
        if ( this.systemUpdateManager == sum ) {
            this.systemUpdateManager = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.update.UpdateDescriptorGenerator#generateDescriptor(java.lang.String,
     *      java.lang.String, long)
     */
    @Override
    public UpdateDescriptor generateDescriptor ( String stream, String imageType, long sequence ) throws UpdateException {
        UpdateDescriptor desc = new UpdateDescriptor();
        desc.setReleaseDate(DateTime.now());
        if ( !StringUtils.isBlank(imageType) ) {
            desc.setImageType(imageType);
        }
        else {
            desc.setImageType(SystemImageUtil.getLocalImageType());
        }
        desc.setSequence(sequence);
        desc.setDescriptors(new ArrayList<>());

        try {
            for ( StructuralObjectReference service : getServicesToUpdate() ) {
                ServiceUpdateDescriptor sud = generateServiceUpdateDescriptor(service);
                if ( sud != null ) {
                    desc.getDescriptors().add(sud);
                }
            }
        }
        catch (
            ConfigRepositoryException |
            ServiceManagementException |
            eu.agno3.runtime.update.UpdateException |
            PackageKitException e ) {
            throw new UpdateException("Failed to generate update descriptor", e); //$NON-NLS-1$
        }

        return desc;
    }


    /**
     * @param service
     * @return
     * @throws eu.agno3.runtime.update.UpdateException
     * @throws ServiceManagementException
     * @throws PackageKitException
     */
    private ServiceUpdateDescriptor generateServiceUpdateDescriptor ( StructuralObjectReference service )
            throws ServiceManagementException, eu.agno3.runtime.update.UpdateException, PackageKitException {
        ServiceUpdateDescriptor desc = new ServiceUpdateDescriptor();
        desc.setServiceType(service.getLocalType());
        desc.setUnits(new ArrayList<>());

        if ( HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(service.getLocalType()) ) {
            addSystemPackageUpdates(desc.getUnits());
        }

        addServiceUpdates(service, desc.getUnits());
        if ( desc.getUnits().isEmpty() ) {
            return null;
        }
        return desc;
    }


    /**
     * @param service
     * @param list
     * @throws eu.agno3.runtime.update.UpdateException
     * @throws ServiceManagementException
     */
    private void addServiceUpdates ( StructuralObjectReference service, List<AbstractServiceUpdateUnit<?>> list )
            throws ServiceManagementException, eu.agno3.runtime.update.UpdateException {

        if ( log.isDebugEnabled() ) {
            log.debug("Checking service for updates " + service.getLocalType()); //$NON-NLS-1$
        }

        P2UpdateUnit u = new P2UpdateUnit();
        u.setTargets(new HashSet<>());

        for ( FeatureUpdate f : this.serviceUpdater.getAllUpdates(service, new LoggingProgressMonitor()) ) {
            Feature last = f.getPossibleUpdates().get(f.getPossibleUpdates().size() - 1);
            P2FeatureTarget t = new P2FeatureTarget();
            t.setFeatureId(last.getId());
            t.setFeatureVersion(last.getVersion());
            u.getTargets().add(t);
        }

        if ( !u.getTargets().isEmpty() ) {
            list.add(u);
        }
    }


    /**
     * @param units
     * @throws PackageKitException
     */
    private void addSystemPackageUpdates ( List<AbstractServiceUpdateUnit<?>> units ) throws PackageKitException {
        Set<PackageUpdate> checkForUpdates = this.systemUpdateManager.checkForUpdates(null);
        Set<PackageId> installed = this.systemUpdateManager.getInstalledSoftware(null);

        SystemUpdateUnit sysUp = new SystemUpdateUnit();
        sysUp.setTargetProfile(new HashSet<>());

        Map<String, List<PackageUpdate>> byName = new HashMap<>();
        for ( PackageUpdate u : checkForUpdates ) {
            List<PackageUpdate> pus = byName.get(u.getUpdate().getPackageName());
            if ( pus == null ) {
                pus = new ArrayList<>();
                byName.put(u.getUpdate().getPackageName(), pus);
            }
            pus.add(u);
        }

        for ( PackageId inst : installed ) {
            SystemPackageTarget p = new SystemPackageTarget();
            p.setPackageName(inst.getPackageName());
            if ( !byName.containsKey(inst.getPackageName()) ) {
                // package is not being updated
                p.setTargetVersions(Arrays.asList(inst.getPackageVersion()));
            }
            else {
                List<PackageUpdate> pus = byName.get(inst.getPackageName());
                List<String> versions = new ArrayList<>();
                for ( PackageUpdate pu : pus ) {
                    versions.add(pu.getUpdate().getPackageVersion());
                }
                p.setTargetVersions(versions);
                byName.remove(inst.getPackageName());
            }
            sysUp.getTargetProfile().add(p);
        }

        // add new packages
        for ( List<PackageUpdate> e : byName.values() ) {
            SystemPackageTarget p = new SystemPackageTarget();
            List<String> versions = new ArrayList<>();
            for ( PackageUpdate pu : e ) {
                p.setTargetRepository(pu.getUpdate().getPackageRepo());
                versions.add(pu.getUpdate().getPackageVersion());
            }
            p.setTargetVersions(versions);
            sysUp.getTargetProfile().add(p);
        }

        units.add(sysUp);
    }


    /**
     * @return
     * @throws ConfigRepositoryException
     */
    protected Collection<StructuralObjectReference> getServicesToUpdate () throws ConfigRepositoryException {
        Collection<StructuralObjectReference> services = new LinkedList<>(this.configRepository.getServiceReferences());
        Set<String> activeTypes = new HashSet<>();
        for ( StructuralObjectReference service : services ) {
            activeTypes.add(service.getLocalType());
        }

        Set<String> missing = new HashSet<>(this.serviceTypeRegistry.getServiceTypes());
        missing.removeAll(activeTypes);
        for ( String inactive : missing ) {
            StructuralObjectReference s = new StructuralObjectReferenceImpl(null, StructuralObjectType.SERVICE, inactive);
            services.add(s);
        }
        return services;
    }
}
