/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 17, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryReference;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.InstanceStatus;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceStateServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceStatusCache;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.InstanceStateService;
import eu.agno3.orchestrator.config.model.realm.service.InstanceStateServiceDescriptor;
import eu.agno3.orchestrator.gui.connector.GuiNotificationEvent;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    InstanceStateService.class, InstanceStateServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.InstanceStateService",
    targetNamespace = InstanceStateServiceDescriptor.NAMESPACE,
    serviceName = InstanceStateServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/instanceState" )
public class InstanceStateServiceImpl implements InstanceStateService, InstanceStateServerService, SOAPWebService {

    private static final Logger log = Logger.getLogger(InstanceStateServiceImpl.class);

    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private ObjectAccessControl authz;
    private ResourceLibraryServerService resourceLibrary;
    private InheritanceServerService inheritanceService;

    private final ConcurrentHashMap<UUID, InstanceStatusCache> cache = new ConcurrentHashMap<>();


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
    protected synchronized void setResourceLibraryService ( ResourceLibraryServerService rlss ) {
        this.resourceLibrary = rlss;
    }


    protected synchronized void unsetResourceLibraryService ( ResourceLibraryServerService rlss ) {
        if ( this.resourceLibrary == rlss ) {
            this.resourceLibrary = null;
        }
    }


    @Reference
    protected synchronized void setInheritanceService ( InheritanceServerService iss ) {
        this.inheritanceService = iss;
    }


    protected synchronized void unsetInheritanceService ( InheritanceServerService iss ) {
        if ( this.inheritanceService == iss ) {
            this.inheritanceService = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.InstanceStateService#getState(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:view:state:INSTANCE" )
    public InstanceStatus getState ( InstanceStructuralObject inst ) throws ModelServiceException, ModelObjectNotFoundException {
        InstanceStatusCache cached = this.cache.get(inst.getId());

        @NonNull
        EntityManager em = this.sctx.createConfigEM();
        ConfigurationState compound = null;
        try {
            Set<@NonNull ResourceLibraryReference> referencedLibraries = new HashSet<>();
            Map<@NonNull UUID, @NonNull Set<@NonNull ResourceLibraryReference>> referencedLibrariesByService = new HashMap<>();
            InstanceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, inst);
            ImageTypeDescriptor desc = this.sctx.getImageTypeRegistry().getDescriptor(persistent.getImageType());
            for ( AbstractStructuralObjectImpl child : TreeUtil.getDirectChildren(em, AbstractStructuralObjectImpl.class, persistent) ) {
                compound = checkService(em, child, desc, compound, referencedLibraries, referencedLibrariesByService, cached);
            }

            if ( compound == null ) {
                compound = ConfigurationState.UNKNOWN;
            }
            compound = checkResourceLibraries(em, compound, persistent, referencedLibraries);
            InstanceStatusCache st = new InstanceStatusCache();
            st.setCompositeConfigurationState(compound);
            st.setLastUpdated(DateTime.now());
            st.getResourceLibraries().putAll(referencedLibrariesByService);
            this.cache.put(inst.getId(), st);
            return st;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch services", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param child
     * @param desc
     * @param compound
     * @param referencedLibrariesByService
     * @param cached
     * @return
     */
    private ConfigurationState checkService ( @NonNull EntityManager em, AbstractStructuralObjectImpl child, ImageTypeDescriptor desc,
            ConfigurationState compound, Set<@NonNull ResourceLibraryReference> referencedLibraries,
            Map<@NonNull UUID, @NonNull Set<@NonNull ResourceLibraryReference>> referencedLibrariesByService, InstanceStatusCache cached ) {
        if ( ! ( child instanceof ServiceStructuralObject ) ) {
            return compound;
        }

        if ( !this.authz.hasAccess(child, "structure:view:services:SERVICE") ) { //$NON-NLS-1$
            return compound;
        }

        ServiceStructuralObjectImpl ss = (ServiceStructuralObjectImpl) child;
        UUID serviceId = ss.getId();
        if ( serviceId == null ) {
            return compound;
        }

        @NonNull
        ConfigurationState cfgState = ss.getState();
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Service %s: state %s", ss, cfgState)); //$NON-NLS-1$
        }

        checkResourceLibraries(em, referencedLibraries, referencedLibrariesByService, cached, ss, serviceId, cfgState);

        EnumSet<ConfigurationState> prio = EnumSet.of(ConfigurationState.UNKNOWN, ConfigurationState.FAILED, ConfigurationState.UNCONFIGURED);
        if ( desc.getForcedServiceTypes().contains(ss.getServiceType()) && cfgState == ConfigurationState.UNCONFIGURED ) {
            // this is a required service, first priority is to configure this
            return ConfigurationState.UNCONFIGURED;
        }
        if ( desc.getForcedServiceTypes().contains(ss.getServiceType()) && cfgState == ConfigurationState.UNKNOWN ) {
            // this is a required service, first priority is to configure this
            return ConfigurationState.UNKNOWN;
        }
        else if ( compound != null && !prio.contains(compound) && !prio.contains(cfgState) ) {
            return mergeState(compound, cfgState);
        }
        else if ( compound != null && !prio.contains(cfgState) ) {
            // the states we want to give first level priority: UNKNOWN, FAILED, UNCONFIGURED
            return compound;
        }
        else {
            return cfgState;
        }
    }


    /**
     * @param em
     * @param referencedLibraries
     * @param referencedLibrariesByService
     * @param cached
     * @param ss
     * @param serviceId
     * @param cfgState
     */
    private void checkResourceLibraries ( EntityManager em, Set<@NonNull ResourceLibraryReference> referencedLibraries,
            Map<@NonNull UUID, @NonNull Set<@NonNull ResourceLibraryReference>> referencedLibrariesByService, InstanceStatusCache cached,
            ServiceStructuralObjectImpl ss, @NonNull UUID serviceId, ConfigurationState cfgState ) {
        // this is pretty expensive, so only do it if it might change the state
        // also cache the found library references, these can only change if the config was modified
        // once we got denormalized import/export data that might be much easier
        try {
            if ( cached != null && cached.getResourceLibraries().containsKey(serviceId) ) {
                Set<@NonNull ResourceLibraryReference> refLibs = cached.getResourceLibraries().get(serviceId);
                referencedLibraries.addAll(refLibs);
                referencedLibrariesByService.put(serviceId, refLibs);
            }
            else {
                ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> sdesc = this.sctx.getServiceTypeRegistry()
                        .getDescriptor(ss.getServiceType());

                ConfigurationInstance cfg = ss.getConfiguration();
                if ( cfgState == ConfigurationState.APPLIED && sdesc != null && cfg != null ) {

                    @SuppressWarnings ( {
                        "null", "unchecked"
                    } )
                    ConfigurationInstance effectiveConfig = this.inheritanceService.getEffective(
                        em,
                        PersistenceUtil.unproxyDeep((AbstractConfigurationObject<? extends ConfigurationInstance>) cfg),
                        cfg.getType());

                    @NonNull
                    Set<@NonNull ResourceLibraryReference> refLibs = sdesc.getReferencedResourceLibraries(effectiveConfig);
                    referencedLibraries.addAll(refLibs);
                    referencedLibrariesByService.put(serviceId, refLibs);
                }
            }
        }
        catch ( ModelServiceException e ) {
            log.warn("Failed to get effective configuration", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param compound
     * @param lastApplied
     * @param rlLastModified
     * @param persistent
     * @param referencedLibraries
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private ConfigurationState checkResourceLibraries ( @NonNull EntityManager em, @NonNull ConfigurationState compound,
            @NonNull InstanceStructuralObjectImpl persistent, Set<@NonNull ResourceLibraryReference> referencedLibraries )
                    throws ModelServiceException, ModelObjectNotFoundException {
        DateTime rlLastModified = null;
        for ( ResourceLibraryReference rl : referencedLibraries ) {
            try {
                String name = rl.getName();
                String type = rl.getType();
                if ( name == null || type == null ) {
                    continue;
                }
                @NonNull
                ResourceLibrary lib = this.resourceLibrary.getClosestByName(em, persistent, name, type);
                DateTime lm = this.resourceLibrary.getLastModified(em, lib);

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Found resource library %s last modified %s", name, lm)); //$NON-NLS-1$
                }

                if ( lm != null && ( rlLastModified == null || lm.isAfter(rlLastModified) ) ) {
                    rlLastModified = lm;
                }
            }
            catch (
                ModelServiceException |
                ModelObjectNotFoundException e ) {
                log.warn("Failed to get resource library last modified " + rl, e); //$NON-NLS-1$
            }
        }

        DateTime rlLastSync = persistent.getResourceLibraryLastSync();
        if ( rlLastModified != null && ( rlLastSync == null || rlLastModified.isAfter(rlLastSync) ) ) {
            log.debug(String.format(
                "Resource libraries have been modified, last modified %s last sync %s", //$NON-NLS-1$
                rlLastModified,
                rlLastSync));
            return mergeState(compound, ConfigurationState.UPDATE_AVAILABLE);
        }
        else if ( log.isDebugEnabled() ) {
            log.debug("Resource library last modified is " + rlLastModified); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Last sync timestamp is " + rlLastSync); //$NON-NLS-1$
            log.debug("Combined state is " + compound); //$NON-NLS-1$
        }
        return compound;
    }


    /**
     * @param compound
     * @param cfgState
     * @return
     */
    private static ConfigurationState mergeState ( @NonNull ConfigurationState compound, @NonNull ConfigurationState cfgState ) {
        if ( cfgState == ConfigurationState.APPLYING ) {
            return cfgState;
        }
        else if ( cfgState == ConfigurationState.UPDATE_AVAILABLE && compound != ConfigurationState.APPLYING ) {
            return cfgState;
        }
        else if ( cfgState == ConfigurationState.DEFAULTS_CHANGED && compound != ConfigurationState.APPLYING
                && compound != ConfigurationState.UPDATE_AVAILABLE ) {
            return cfgState;
        }
        else if ( cfgState == ConfigurationState.APPLIED && compound != ConfigurationState.DEFAULTS_CHANGED && compound != ConfigurationState.APPLYING
                && compound != ConfigurationState.UPDATE_AVAILABLE ) {
            return cfgState;
        }
        return compound;
    }


    @Override
    public void handleInstanceConfigApplied ( @NonNull InstanceStructuralObject instance ) {
        try {
            refreshInstanceStatus(instance);
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.warn("Failed to notify of instance state change", e); //$NON-NLS-1$
        }
    }


    /**
     * @param instance
     * @throws MessagingException
     * @throws InterruptedException
     */
    @Override
    public void refreshInstanceStatus ( @NonNull InstanceStructuralObject instance ) throws MessagingException, InterruptedException {
        this.cache.remove(instance.getId());
        String path = String.format("/instance/%s/status", instance.getId()); //$NON-NLS-1$
        if ( log.isDebugEnabled() ) {
            log.debug("Notifying instance state change " + path); //$NON-NLS-1$
        }
        this.sctx.getMessageClient().publishEvent(new GuiNotificationEvent(this.sctx.getMessageClient().getMessageSource(), path)); // $NON-NLS-1$
    }

}
