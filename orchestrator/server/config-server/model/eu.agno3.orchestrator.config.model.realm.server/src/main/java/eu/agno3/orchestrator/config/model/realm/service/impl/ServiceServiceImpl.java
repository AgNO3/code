/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebService;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;
import eu.agno3.orchestrator.config.model.realm.context.ConfigurationEditContext;
import eu.agno3.orchestrator.config.model.realm.server.service.ConfigurationContextServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ConfigurationServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceStateServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.StructuralObjectServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyBuilder;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.ServiceService;
import eu.agno3.orchestrator.config.model.realm.service.ServiceServiceDescriptor;
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
    ServiceService.class, ServiceServerService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.ServiceService",
    targetNamespace = ServiceServiceDescriptor.NAMESPACE,
    serviceName = ServiceServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/service" )
public class ServiceServiceImpl implements ServiceService, ServiceServerService, SOAPWebService {

    /**
     * 
     */
    private static final String CONFIG_MODIFY = "config:modify"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(ServiceServiceImpl.class);
    private DefaultServerServiceContext sctx;
    private ConfigurationServerService configService;
    private ConfigurationContextServerService contextService;
    private PersistenceUtil persistenceUtil;

    private InheritanceServerService inheritanceService;
    private ObjectAccessControl authz;
    private ResourceLibraryServerService resLibraryService;
    private InheritanceProxyBuilder inheritanceUtil;
    private StructuralObjectServerService structureService;
    private InstanceStateServerService instanceState;


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
    protected synchronized void setConfigurationService ( ConfigurationServerService cs ) {
        this.configService = cs;
    }


    protected synchronized void unsetConfigurationService ( ConfigurationServerService cs ) {
        if ( this.configService == cs ) {
            this.configService = null;
        }
    }


    @Reference
    protected synchronized void setContextService ( ConfigurationContextServerService cs ) {
        this.contextService = cs;
    }


    protected synchronized void unsetContextService ( ConfigurationContextServerService cs ) {
        if ( this.contextService == cs ) {
            this.contextService = null;
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
    protected synchronized void setInheritanceProxyBuilder ( InheritanceProxyBuilder ipb ) {
        this.inheritanceUtil = ipb;
    }


    protected synchronized void unsetInheritanceProxyBuilder ( InheritanceProxyBuilder ipb ) {
        if ( this.inheritanceUtil == ipb ) {
            this.inheritanceUtil = null;
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
    protected synchronized void setStructureService ( StructuralObjectServerService sos ) {
        this.structureService = sos;
    }


    protected synchronized void unsetStructureService ( StructuralObjectServerService sos ) {
        if ( this.structureService == sos ) {
            this.structureService = null;
        }
    }


    @Reference
    protected synchronized void setInstanceStateService ( InstanceStateServerService iss ) {
        this.instanceState = iss;
    }


    protected synchronized void unsetInstanceStateService ( InstanceStateServerService iss ) {
        if ( this.instanceState == iss ) {
            this.instanceState = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ServiceService#getServiceConfiguration(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    @RequirePermissions ( "config:view:service" )
    public ConfigurationObjectMutable getServiceConfiguration ( ServiceStructuralObject service )
            throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        ServiceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service);
        return getServiceConfiguration(em, persistent);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService#fetch(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    public @NonNull ServiceStructuralObject fetch ( @NonNull EntityManager em, @NonNull ServiceStructuralObject service )
            throws ModelObjectNotFoundException, ModelServiceException {
        return this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service);
    }


    /**
     * @param em
     * @param service
     * @return the service configuration
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @Override
    public @NonNull ConfigurationObjectMutable getServiceConfiguration ( @NonNull EntityManager em, @NonNull ServiceStructuralObjectImpl service )
            throws ModelObjectNotFoundException, ModelServiceException {
        this.authz.checkAccess(service, "config:view:service"); //$NON-NLS-1$
        ConfigurationInstance configuration = service.getConfiguration();
        if ( configuration != null ) {
            // otherwise we might end up with an untyped proxy
            configuration = PersistenceUtil.unproxy(configuration);
            return this.persistenceUtil.setRevisions(em, (AbstractConfigurationObject<?>) configuration, PersistenceUtil.getMostRecentRevision(em));
        }

        return this.createEmptyConfiguration(service);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ServiceService#getConfigState(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    @RequirePermissions ( "config:view:service" )
    public ConfigurationState getConfigState ( ServiceStructuralObject service ) throws ModelObjectNotFoundException, ModelServiceException {
        if ( service == null ) {
            throw new ModelServiceException();
        }
        EntityManager em = this.sctx.createConfigEM();
        ServiceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service);
        return persistent.getState();
    }


    /**
     * 
     * @param instance
     * @param serviceId
     * @return the service object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectReferentialIntegrityException
     */
    @Override
    public @NonNull ServiceStructuralObject getServiceById ( @NonNull InstanceStructuralObject instance, @NonNull UUID serviceId )
            throws ModelObjectNotFoundException, ModelServiceException, ModelObjectReferentialIntegrityException {

        @NonNull
        EntityManager em = this.sctx.createConfigEM();
        ServiceStructuralObjectImpl s = PersistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, serviceId);

        Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, s);
        if ( !parent.isPresent() || !parent.get().equals(instance) ) {
            throw new ModelObjectReferentialIntegrityException(InstanceStructuralObject.class, instance.getId());
        }

        return s;
    }


    @Override
    @RequirePermissions ( CONFIG_MODIFY )
    public <T extends ConfigurationObjectMutable> T updateServiceConfiguration ( ServiceStructuralObject service, T serviceConfig,
            ConfigUpdateInfo info )
                    throws ModelObjectNotFoundException, ModelServiceException, ModelObjectValidationException, ModelObjectConflictException {
        EntityManager em = this.sctx.createConfigEM();
        ServiceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service);
        this.authz.checkAccess(persistent, CONFIG_MODIFY);
        return updateServiceConfiguration(em, persistent, serviceConfig, info);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService#updateServiceConfiguration(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable,
     *      eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public @NonNull <T extends ConfigurationObjectMutable> T updateServiceConfiguration ( @NonNull EntityManager em,
            @NonNull ServiceStructuralObjectImpl persistent, @Nullable T serviceConfig, @Nullable ConfigUpdateInfo info )
                    throws ModelObjectNotFoundException, ModelServiceException, ModelObjectValidationException, ModelObjectConflictException {
        ServiceTypeDescriptor<?, ?> descriptor = this.sctx.getServiceTypeRegistry().getDescriptor(persistent.getServiceType());
        if ( serviceConfig == null || !descriptor.getConfigurationType().isAssignableFrom(serviceConfig.getClass()) ) {
            throw new ModelServiceException("The provided configuration is not compatible with " + descriptor.getConfigurationType().getName()); //$NON-NLS-1$
        }

        ConfigurationInstance cfg = persistent.getConfiguration();

        AbstractConfigurationObject<@NonNull T> configuration = null;
        if ( cfg != null ) {
            configuration = PersistenceUtil.unproxy((AbstractConfigurationObject<@NonNull T>) cfg);
        }

        T newServiceConfig = serviceConfig;
        if ( configuration == null ) {
            // easy, no config yet -> create config and set link
            if ( log.isDebugEnabled() ) {
                log.debug("No configuration found for service " + persistent); //$NON-NLS-1$
            }
            newServiceConfig = this.configService.create(em, persistent, serviceConfig, info);
            persistent.setConfiguration((AbstractConfigurationInstance<?>) newServiceConfig);
        }
        else {
            if ( !configuration.equals(serviceConfig) ) {
                // do not allow "switching" the configuration instance, if one exists it must be updated
                throw new ModelServiceException("Trying to apply a new configuration while already one exists"); //$NON-NLS-1$
            }

            newServiceConfig = this.configService.update(em, configuration, serviceConfig, info);
        }
        setConfigurationState(em, null, persistent, ConfigurationState.UPDATE_AVAILABLE, true);
        em.persist(persistent);
        em.flush();
        em.refresh(persistent);
        return newServiceConfig;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ServiceService#getApplicableServiceTypes(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "config:view:serviceTypes" )
    public Set<String> getApplicableServiceTypes ( InstanceStructuralObject instance ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        ImageTypeDescriptor descriptor = this.sctx.getImageTypeRegistry().getDescriptor(instance.getImageType());

        if ( descriptor == null ) {
            return Collections.EMPTY_SET;
        }

        Set<String> applicableTypes = new HashSet<>();
        applicableTypes.addAll(descriptor.getForcedServiceTypes());
        applicableTypes.addAll(descriptor.getApplicableServiceTypes());

        Set<ServiceStructuralObject> instanceServices = getServices(instance, em);
        Set<String> presentSingletonServices = new HashSet<>();

        for ( ServiceStructuralObject service : instanceServices ) {
            ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> serviceDesc = this.sctx.getServiceTypeRegistry()
                    .getDescriptor(service.getServiceType());

            if ( !serviceDesc.isMultiInstance() ) {
                presentSingletonServices.add(serviceDesc.getTypeName());
            }
        }

        applicableTypes.removeAll(presentSingletonServices);
        return applicableTypes;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ServiceService#getServices(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:view:services:INSTANCE" )
    public Set<ServiceStructuralObject> getServices ( InstanceStructuralObject instance ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        if ( instance == null ) {
            throw new ModelServiceException();
        }

        return getServices(instance, em);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService#getServices(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      javax.persistence.EntityManager)
     */
    @Override
    public @NonNull Set<@NonNull ServiceStructuralObject> getServices ( @NonNull InstanceStructuralObject instance, @NonNull EntityManager em )
            throws ModelObjectNotFoundException, ModelServiceException {
        Set<@NonNull ServiceStructuralObject> res = new HashSet<>();
        InstanceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance);

        for ( AbstractStructuralObjectImpl child : TreeUtil.getChildren(em, AbstractStructuralObjectImpl.class, persistent) ) {
            if ( child != null && isAccessibleServiceChild(child) ) {
                res.add((ServiceStructuralObject) child);
            }
        }

        return res;
    }


    @Override
    public @NonNull ServiceStructuralObjectImpl getOrCreateService ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl instance,
            @NonNull String serviceType, @Nullable UUID knownId ) throws ModelServiceException, ModelObjectNotFoundException,
                    ModelObjectConflictException, ModelObjectReferentialIntegrityException, ModelObjectValidationException {

        @NonNull
        InstanceStructuralObjectImpl pinst = PersistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance.getId());
        ServiceStructuralObjectImpl exists = null;
        try {
            if ( knownId != null ) {
                exists = PersistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, knownId);
            }
        }
        catch ( ModelObjectNotFoundException e ) {
            log.debug("Existing id not found", e); //$NON-NLS-1$
        }

        if ( exists != null && !TreeUtil.isAncestor(pinst, exists) ) {
            // service exists but is attached to another instance
            Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, exists);
            log.warn(String.format(
                "Found existing service %s, but it is not located on given instance %s but on %s", //$NON-NLS-1$
                exists,
                pinst,
                parent.isPresent() ? parent.get() : null));

            throw new ModelObjectConflictException(ServiceStructuralObjectImpl.class, exists.getId());
        }
        else if ( exists != null && serviceType.equals(exists.getServiceType()) ) {
            return exists;
        }

        Set<ServiceStructuralObject> existing = getServicesOfType(em, instance, serviceType);
        if ( !existing.isEmpty() ) {
            Iterator<ServiceStructuralObject> it = existing.iterator();
            ServiceStructuralObjectImpl found = (ServiceStructuralObjectImpl) it.next();

            if ( knownId == null && found != null ) {
                // no fixed id, use existing one
                return found;
            }

            if ( found != null && found.getId().equals(knownId) && !it.hasNext() ) {
                return found;
            }
            // a service of the type exists but does not match the request
            log.warn(String.format("Found existing service %s not matching %s [%s]", found, serviceType, knownId)); //$NON-NLS-1$
            throw new ModelObjectConflictException(ServiceStructuralObject.class, found != null ? found.getId() : null);
        }

        ServiceStructuralObjectImpl service = new ServiceStructuralObjectImpl();
        service.setId(knownId);
        service.setServiceType(serviceType);

        return this.structureService.createWithId(em, instance, service, false);
    }


    private boolean isAccessibleServiceChild ( AbstractStructuralObjectImpl child ) {
        return child instanceof ServiceStructuralObjectImpl && this.authz.hasAccess(child, "structure:view:services:INSTANCE"); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService#getServicesOfType(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl, java.lang.String)
     */
    @SuppressWarnings ( "null" )
    @Override
    public @NonNull Set<@NonNull ServiceStructuralObject> getServicesOfType ( @NonNull EntityManager em,
            @NonNull InstanceStructuralObjectImpl instance, @NonNull String serviceType ) throws ModelObjectNotFoundException, ModelServiceException {
        Set<@NonNull ServiceStructuralObject> res = new HashSet<>();
        for ( AbstractStructuralObjectImpl child : TreeUtil.getChildren(em, AbstractStructuralObjectImpl.class, instance) ) {
            if ( isAccessibleServiceChild(child) && serviceType.equals( ( (ServiceStructuralObject) child ).getServiceType()) ) {
                res.add((ServiceStructuralObject) child);
            }
        }

        return res;
    }


    @Override
    public @NonNull Map<@NonNull ServiceStructuralObject, @NonNull ConfigurationInstance> getEffectiveContextConfigs ( @NonNull EntityManager em,
            @NonNull AbstractStructuralObjectImpl persistentAnchor ) throws ModelServiceException, ModelObjectNotFoundException {

        Map<@NonNull ServiceStructuralObject, @NonNull ConfigurationInstance> contextConfigs = new HashMap<>();

        if ( ! ( persistentAnchor instanceof ServiceStructuralObjectImpl ) ) {
            return contextConfigs;
        }
        String serviceType = ( (ServiceStructuralObjectImpl) persistentAnchor ).getServiceType();
        ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> sdesc = this.sctx.getServiceTypeRegistry().getDescriptor(serviceType);

        Set<@NonNull String> contextServices = new HashSet<>();
        contextServices.addAll(sdesc.getRequiredServices());
        contextServices.addAll(sdesc.getUsedServices());

        InstanceStructuralObjectImpl contextInst = null;
        if ( persistentAnchor instanceof ServiceStructuralObjectImpl ) {
            @SuppressWarnings ( "null" )
            Optional<? extends AbstractStructuralObjectImpl> p = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistentAnchor);
            if ( p.isPresent() && p.get() instanceof InstanceStructuralObjectImpl ) {
                contextInst = (InstanceStructuralObjectImpl) p.get();
            }
        }

        if ( contextInst == null ) {
            return contextConfigs;
        }

        for ( AbstractStructuralObjectImpl child : TreeUtil.getChildren(em, AbstractStructuralObjectImpl.class, contextInst) ) {
            try {
                if ( child == null || !isAccessibleServiceChild(child) ) {
                    continue;
                }

                String cStype = ( (ServiceStructuralObject) child ).getServiceType();
                if ( contextServices.contains(cStype) ) {
                    ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> desc = this.sctx.getServiceTypeRegistry()
                            .getDescriptor(cStype);

                    @NonNull
                    AbstractConfigurationObject<?> sc = (@NonNull AbstractConfigurationObject<?>) getServiceConfiguration(
                        em,
                        (ServiceStructuralObjectImpl) child);

                    @NonNull
                    Class<ConfigurationInstance> ct = desc.getConfigurationType();
                    contextConfigs.put((ServiceStructuralObject) child, (ConfigurationInstance) this.inheritanceService.getEffective(em, sc, ct));

                }
            }
            catch ( Exception e ) {
                log.warn("Failed to get context service effective configuration", e); //$NON-NLS-1$
            }
        }
        return contextConfigs;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ServiceService#getServiceConfigurationLocation(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "config:view:service" )
    public ConfigurationObjectReference getServiceConfigurationLocation ( StructuralObject hostOrService, String serviceType )
            throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, hostOrService);
        this.authz.checkAccess(persistent, "config:view:service"); //$NON-NLS-1$

        ServiceTypeDescriptor<?, ?> descriptor = this.sctx.getServiceTypeRegistry().getDescriptor(serviceType);
        if ( descriptor == null || serviceType == null || descriptor.isMultiInstance() ) {
            throw new ModelServiceException("The service type is not a singleton " + serviceType); //$NON-NLS-1$
        }

        ServiceStructuralObject service;
        if ( persistent instanceof ServiceStructuralObject ) {

            if ( serviceType.equals( ( (ServiceStructuralObject) persistent ).getServiceType()) ) {
                service = (ServiceStructuralObject) persistent;
            }
            else {
                Optional<? extends AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistent);

                if ( !parent.isPresent() || ! ( parent.get() instanceof InstanceStructuralObjectImpl ) ) {
                    throw new ModelServiceException("Invalid service parent"); //$NON-NLS-1$
                }

                InstanceStructuralObjectImpl instance = (InstanceStructuralObjectImpl) parent.get();
                if ( instance == null ) {
                    throw new ModelServiceException("Invalid service parent"); //$NON-NLS-1$
                }

                Set<@NonNull ServiceStructuralObject> servicesOfType = this.getServicesOfType(em, instance, serviceType);

                if ( servicesOfType.size() != 1 ) {
                    throw new ModelServiceException("Not a singleton service"); //$NON-NLS-1$
                }

                service = servicesOfType.iterator().next();
            }

        }
        else if ( persistent instanceof InstanceStructuralObject ) {
            @NonNull
            Set<@NonNull ServiceStructuralObject> servicesOfType = this.getServicesOfType(em, (InstanceStructuralObjectImpl) persistent, serviceType);

            if ( servicesOfType.size() != 1 ) {
                throw new ModelServiceException("Not a singleton service"); //$NON-NLS-1$
            }

            service = servicesOfType.iterator().next();
        }
        else {
            throw new ModelServiceException("Not a service or instance " + hostOrService); //$NON-NLS-1$
        }

        if ( !this.authz.hasAccess(service, "config:view:service") ) { //$NON-NLS-1$
            return null;
        }

        AbstractConfigurationObject<@Nullable ?> configuration = (AbstractConfigurationObject<@Nullable ?>) service.getConfiguration();
        if ( configuration == null ) {
            return null;
        }

        return new ConfigurationObjectReference(configuration);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.ServiceService#getEditContext(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    @RequirePermissions ( CONFIG_MODIFY )
    public ConfigurationEditContext<ConfigurationObject, ConfigurationObject> getEditContext ( ServiceStructuralObject service )
            throws ModelObjectNotFoundException, ModelServiceException {

        long start = System.currentTimeMillis();
        EntityManager em = this.sctx.createConfigEM();
        ServiceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service);
        this.authz.checkAccess(persistent, CONFIG_MODIFY);

        AbstractConfigurationObject<@Nullable ?> configuration = (AbstractConfigurationObject<@Nullable ?>) persistent.getConfiguration();
        if ( configuration != null ) {
            configuration = PersistenceUtil.unproxy(configuration);
            this.persistenceUtil.setRevisions(em, configuration, PersistenceUtil.getMostRecentRevision(em));
            if ( log.isDebugEnabled() ) {
                log.debug("Fetching current config took " + ( System.currentTimeMillis() - start )); //$NON-NLS-1$
            }
            return this.contextService.getContextForConfig(
                this.inheritanceUtil.makeProxyContext(em, configuration.getType(), service, null),
                configuration,
                persistent.getState());
        }

        @NonNull
        AbstractConfigurationObject<?> empty = createEmptyConfiguration(persistent);
        return this.contextService.getContextAtAnchor(
            this.inheritanceUtil.makeProxyContext(em, empty.getType(), service, null),
            persistent,
            empty,
            ConfigurationState.UNCONFIGURED,
            false);
    }


    private @NonNull AbstractConfigurationObject<?> createEmptyConfiguration ( ServiceStructuralObjectImpl persistent ) throws ModelServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Returning empty configuration instance for service " + persistent); //$NON-NLS-1$
        }

        ServiceTypeDescriptor<?, ?> serviceDescriptor = this.sctx.getServiceTypeRegistry().getDescriptor(persistent.getServiceType());
        ConcreteObjectTypeDescriptor<?, ?> objectTypeDescriptor = this.sctx.getObjectTypeRegistry()
                .getConcrete(serviceDescriptor.getConfigurationType());

        return (AbstractConfigurationObject<@NonNull ?>) objectTypeDescriptor.newInstance();
    }


    @Override
    public void setConfigurationState ( @NonNull EntityManager em, @Nullable InstanceStructuralObjectImpl host,
            @NonNull ServiceStructuralObjectImpl persistent, @NonNull ConfigurationState state, boolean updateInstanceState ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Setting configuration state of %s to %s", persistent, state)); //$NON-NLS-1$
        }
        InstanceStructuralObjectImpl instance = host;
        if ( updateInstanceState && instance == null ) {
            Optional<? extends @Nullable AbstractStructuralObjectImpl> parent = TreeUtil
                    .getParent(em, AbstractStructuralObjectImpl.class, persistent);
            if ( parent.isPresent() && parent.get() instanceof InstanceStructuralObject ) {
                instance = (InstanceStructuralObjectImpl) parent.get();
            }
        }
        persistent.setState(state);
        try {
            if ( updateInstanceState && instance != null ) {
                this.instanceState.refreshInstanceStatus(instance);
            }
            this.sctx.getMessageClient().publishEvent(makeGUIConfigStateEvent(persistent, state));
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.warn("Failed to notify GUI of configuration state change", e); //$NON-NLS-1$
        }
    }


    private @NonNull GuiNotificationEvent makeGUIConfigStateEvent ( ServiceStructuralObject persistent, ConfigurationState state ) {
        String path = String.format("/service/%s/config_state", persistent.getId()); //$NON-NLS-1$
        return new GuiNotificationEvent(this.sctx.getMessageClient().getMessageSource(), path, state.name());
    }

}
