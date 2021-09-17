/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.Optional;
import java.util.UUID;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigurationEditContext;
import eu.agno3.orchestrator.config.model.realm.server.service.ConfigurationContextServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ConfigurationServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.DefaultsServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.EnforcementServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyBuilder;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext;
import eu.agno3.orchestrator.config.model.realm.server.util.ObjectReferenceUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationContextService;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationContextServiceDescriptor;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ConfigurationContextService.class, ConfigurationContextServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.ConfigurationContextService",
    targetNamespace = ConfigurationContextServiceDescriptor.NAMESPACE,
    serviceName = ConfigurationContextServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/config/context" )
public class ConfigurationContextServiceImpl implements ConfigurationContextService, SOAPWebService, ConfigurationContextServerService {

    private static final Logger log = Logger.getLogger(ConfigurationContextServiceImpl.class);

    private DefaultServerServiceContext sctx;
    private ConfigurationServerService configService;
    private DefaultsServerService defaultsService;
    private InheritanceServerService inheritanceService;
    private InheritanceProxyBuilder inheritanceUtil;
    private EnforcementServerService enforcementService;

    private Optional<@NonNull PersistenceUtil> persistenceUtil = Optional.empty();

    private ObjectAccessControl authz;


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
    protected synchronized void setConfigService ( ConfigurationServerService cs ) {
        this.configService = cs;
    }


    protected synchronized void unsetConfigService ( ConfigurationServerService cs ) {
        if ( this.configService == cs ) {
            this.configService = null;
        }
    }


    @Reference
    protected synchronized void setDefaultsService ( DefaultsServerService ds ) {
        this.defaultsService = ds;
    }


    protected synchronized void unsetDefaultsService ( DefaultsServerService ds ) {
        if ( this.defaultsService == ds ) {
            this.defaultsService = null;
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
    protected synchronized void setEnforcementService ( EnforcementServerService es ) {
        this.enforcementService = es;
    }


    protected synchronized void unsetEnforcementService ( EnforcementServerService es ) {
        if ( this.enforcementService == es ) {
            this.enforcementService = null;
        }
    }


    @Reference
    protected synchronized void setPersistenceUtil ( @NonNull PersistenceUtil pu ) {
        this.persistenceUtil = Optional.of(pu);
    }


    protected synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil.isPresent() && this.persistenceUtil.get() == pu ) {
            this.persistenceUtil = Optional.empty();
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


    @Override
    @RequirePermissions ( "config:createTemplate" )
    public ConfigurationEditContext<@NonNull ConfigurationObject, ConfigurationObject> newForEditing ( StructuralObject anchor, String objectType )
            throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        if ( objectType == null ) {
            throw new ModelServiceException();
        }
        this.authz.checkAccess(anchor, "config:createTemplate"); //$NON-NLS-1$
        try {
            @Nullable
            ConfigurationObject empty = this.configService.getEmpty(objectType);

            if ( empty == null ) {
                throw new ModelServiceException();
            }

            log.debug("Object type is " + empty.getClass()); //$NON-NLS-1$

            return getContextAtAnchor(
                this.inheritanceUtil.makeProxyContext(em, empty.getType(), anchor, null),
                this.persistenceUtil.get().fetch(em, AbstractStructuralObjectImpl.class, anchor),
                empty,
                ConfigurationState.UNCONFIGURED,
                false);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch anchor", e); //$NON-NLS-1$
        }
    }


    @Override
    public @NonNull ConfigurationEditContext<@NonNull ConfigurationObject, @NonNull ConfigurationObject> getContextAtAnchor (
            @NonNull InheritanceProxyContext cc, @NonNull AbstractStructuralObjectImpl anchor, @NonNull ConfigurationObject config,
            @NonNull ConfigurationState state, boolean inner ) throws ModelServiceException {
        ConfigurationEditContext<@NonNull ConfigurationObject, @NonNull ConfigurationObject> context = new ConfigurationEditContext<>();

        ObjectTypeName annot = config.getType().getAnnotation(ObjectTypeName.class);

        if ( annot == null ) {
            throw new ModelServiceException("Object type has no object type name annotation"); //$NON-NLS-1$
        }
        String value = annot.value();
        if ( value == null ) {
            throw new ModelServiceException("Object type has no object type name set"); //$NON-NLS-1$
        }

        try {
            context.setInner(inner);
            context.setConfigurationState(state);
            context.setCurrent(config);
            context.setInheritedValues(null);
            context.setStructuralDefaults(this.defaultsService.getDefaultsFor(cc, anchor, value));
            context.setEnforcedValues(this.enforcementService.getEnforcementsFor(cc, anchor, value));

        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch used by information", e); //$NON-NLS-1$
        }

        return context;
    }


    @Override
    @RequirePermissions ( "config:modify" )
    public ConfigurationEditContext<@NonNull ConfigurationObject, ConfigurationObject> getForEditing ( UUID id )
            throws ModelServiceException, ModelObjectNotFoundException {

        EntityManager em = this.sctx.createConfigEM();
        AbstractConfigurationObject<@Nullable ?> config;
        try {
            config = PersistenceUtil.fetch(em, AbstractConfigurationObject.class, id);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch config", e); //$NON-NLS-1$
        }

        this.authz.checkAccess(config.getAnchor(), "config:modify"); //$NON-NLS-1$

        return getContextForConfig(
            this.inheritanceUtil.makeProxyContext(em, config.getType(), config.getAnchor(), null),
            config,
            ConfigurationState.UPDATE_AVAILABLE);
    }


    @Override
    public @NonNull ConfigurationEditContext<@NonNull ConfigurationObject, @NonNull ConfigurationObject> getContextForConfig (
            @NonNull InheritanceProxyContext cc, @NonNull AbstractConfigurationObject<@Nullable ?> config, @NonNull ConfigurationState state )
                    throws ModelObjectNotFoundException, ModelServiceException {

        ConfigurationEditContext<@NonNull ConfigurationObject, @NonNull ConfigurationObject> context = new ConfigurationEditContext<>();

        try {
            context.setConfigurationState(state);
            long revision = PersistenceUtil.getMostRecentRevision(cc.getEntityManager());
            @NonNull
            PersistenceUtil pu = this.persistenceUtil.get();
            @NonNull
            AbstractConfigurationObject<?> cfg = pu.setRevisions(cc.getEntityManager(), (AbstractConfigurationObject<?>) config, revision);

            context.setInner(cfg.getOuterObject() != null);

            ObjectReferenceUtil.fillInMissing(cc.getEntityManager(), pu, cfg, this.sctx.getObjectTypeRegistry());
            context.setCurrent(cfg);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("State is %s version %d revision %d", state, config.getVersion(), revision)); //$NON-NLS-1$
            }
            long start = System.currentTimeMillis();

            ConfigurationObject inherited = this.inheritanceService.getInherited(cc, config);
            context.setInheritedValues(inherited);
            if ( log.isDebugEnabled() ) {
                log.debug("Fetching inherited took " + ( System.currentTimeMillis() - start )); //$NON-NLS-1$
                start = System.currentTimeMillis();
            }

            ConfigurationObject appliedDefaults = this.defaultsService.getAppliedDefaults(cc, config);

            context.setStructuralDefaults(appliedDefaults);
            if ( log.isDebugEnabled() ) {
                log.debug("Fetching defaults took " + ( System.currentTimeMillis() - start )); //$NON-NLS-1$
                start = System.currentTimeMillis();
            }

            ConfigurationObject appliedEnforcement = this.enforcementService.getAppliedEnforcement(cc, config);
            context.setEnforcedValues(appliedEnforcement);
            if ( log.isDebugEnabled() ) {
                log.debug("Fetching enforced took " + ( System.currentTimeMillis() - start )); //$NON-NLS-1$
            }

        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to obtain context for existing configuration", e); //$NON-NLS-1$
        }

        return context;
    }

}
