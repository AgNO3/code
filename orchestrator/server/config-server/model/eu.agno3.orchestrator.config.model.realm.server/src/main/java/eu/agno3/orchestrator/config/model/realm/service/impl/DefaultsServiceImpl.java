/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.DefaultsServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyBuilder;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext;
import eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.DefaultsService;
import eu.agno3.orchestrator.config.model.realm.service.DefaultsServiceDescriptor;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    DefaultsService.class, DefaultsServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.DefaultsService",
    targetNamespace = DefaultsServiceDescriptor.NAMESPACE,
    serviceName = DefaultsServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/defaults" )
public class DefaultsServiceImpl implements DefaultsService, SOAPWebService, DefaultsServerService {

    /**
     * 
     */
    private static final String DEFAULT_MODIFY = "default:modify"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(DefaultsServiceImpl.class);
    private DefaultServerServiceContext sctx;
    private InheritanceProxyBuilder inheritanceUtil;
    private PersistenceUtil persistenceUtil;
    private ObjectPoolProvider objectPoolProvider;
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
    protected synchronized void setInheritanceUtil ( InheritanceProxyBuilder iu ) {
        this.inheritanceUtil = iu;
    }


    protected synchronized void unsetInheritanceUtil ( InheritanceProxyBuilder iu ) {
        if ( this.inheritanceUtil == iu ) {
            this.inheritanceUtil = null;
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
    protected synchronized void setObjectPoolProvider ( ObjectPoolProvider opp ) {
        this.objectPoolProvider = opp;
    }


    protected synchronized void unsetObjectPoolProvider ( ObjectPoolProvider opp ) {
        if ( this.objectPoolProvider == opp ) {
            this.objectPoolProvider = null;
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


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService#fetchDefaults(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "default:list:local" )
    public Set<ConfigurationObject> fetchDefaults ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();

        try {
            AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);
            this.authz.checkAccess(persistent, "default:list:local"); //$NON-NLS-1$
            Set<ConfigurationObject> children = new LinkedHashSet<>();
            for ( ConfigurationObject def : persistent.getDefaultObjects() ) {
                children.add(new ConfigurationObjectReference(def));
            }
            return children;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch defaults listing", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.DefaultsService#getAppliedDefaults(eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "config:view:appliedDefaults" )
    public <T extends ConfigurationObject> @Nullable T getAppliedDefaults ( @Nullable T obj, String rootType )
            throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<@Nullable T> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:view:appliedDefaults"); //$NON-NLS-1$
            return getAppliedDefaults(
                this.inheritanceUtil.makeProxyContext(
                    em,
                    persistent.getType(),
                    persistent.getAnchor(),
                    !StringUtils.isBlank(rootType) ? this.sctx.getObjectTypeRegistry().get(rootType).getObjectType() : null),
                persistent);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to get applied defaults", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.DefaultsServerService#getAppliedDefaults(eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext,
     *      eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject)
     */
    @Override
    public @NonNull <T extends ConfigurationObject> T getAppliedDefaults ( @NonNull InheritanceProxyContext cc,
            @NonNull AbstractConfigurationObject<@Nullable T> persistent ) throws ModelServiceException {
        return cc.getInheritanceProxyBuilder().getDefaultProxy(cc, persistent.getType(), persistent.getAnchor());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.DefaultsService#getDefaultsFor(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "config:view:defaultsForType" )
    public ConfigurationObject getDefaultsFor ( StructuralObject obj, String objType, String rootType )
            throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        if ( objType == null ) {
            throw new ModelServiceException();
        }
        try {
            AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);
            this.authz.checkAccess(persistent, "config:view:defaultsForType"); //$NON-NLS-1$
            ConcreteObjectTypeDescriptor<?, ?> desc = this.sctx.getObjectTypeRegistry().getConcrete(objType);
            return getDefaultsFor(
                this.inheritanceUtil.makeProxyContext(
                    em,
                    desc.getObjectType(),
                    obj,
                    !StringUtils.isBlank(rootType) ? this.sctx.getObjectTypeRegistry().get(rootType).getObjectType() : null),
                persistent,
                objType);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch local default", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.DefaultsServerService#getDefaultsFor(eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl, java.lang.String)
     */
    @Override
    public @NonNull ConfigurationObject getDefaultsFor ( @NonNull InheritanceProxyContext cc, @NonNull AbstractStructuralObjectImpl obj,
            @NonNull String objType ) throws ModelServiceException {
        ConcreteObjectTypeDescriptor<?, ?> desc = this.sctx.getObjectTypeRegistry().getConcrete(objType);
        try {
            return this.inheritanceUtil.getDefaultProxy(cc, desc.getObjectType(), obj);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to materialize defaults", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.DefaultsService#setDefault(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( DEFAULT_MODIFY )
    public void setDefault ( StructuralObject anchor, ConfigurationObject object ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        try {
            AbstractStructuralObjectImpl persistentAnchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
            this.authz.checkAccess(persistentAnchor, DEFAULT_MODIFY);
            AbstractConfigurationObject<?> persistentConfig = this.persistenceUtil.fetch(em, object);

            if ( !this.objectPoolProvider.isInScope(persistentAnchor, persistentConfig) ) {
                throw new ModelServiceException("Template is not in scope of anchor"); //$NON-NLS-1$
            }

            AbstractConfigurationObject<?> defaultForType = this.objectPoolProvider
                    .getDefaultForType(em, persistentAnchor, persistentConfig.getType());
            if ( defaultForType != null ) {
                log.debug("There is already a default for this type, removing old"); //$NON-NLS-1$
                persistentAnchor.getDefaultObjects().remove(defaultForType);
                defaultForType.getDefaultFor().remove(persistentAnchor);
            }

            persistentAnchor.getDefaultObjects().add(persistentConfig);
            persistentConfig.getDefaultFor().add(persistentAnchor);

            em.persist(persistentAnchor);
            em.persist(persistentConfig);
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to set default", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.DefaultsService#unsetDefault(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( DEFAULT_MODIFY )
    public void unsetDefault ( StructuralObject anchor, ConfigurationObject object ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        try {
            AbstractStructuralObjectImpl persistentAnchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
            this.authz.checkAccess(persistentAnchor, DEFAULT_MODIFY);
            AbstractConfigurationObject<?> persistentConfig = this.persistenceUtil.fetch(em, object);

            persistentAnchor.getDefaultObjects().remove(persistentConfig);
            persistentConfig.getDefaultFor().remove(persistentAnchor);

            em.persist(persistentAnchor);
            em.persist(persistentConfig);
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to unset default", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.DefaultsService#fetchInheritedDefaults(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @SuppressWarnings ( "rawtypes" )
    @RequirePermissions ( "default:list:inherited" )
    @Override
    public Set<ConfigurationObject> fetchInheritedDefaults ( StructuralObject anchor ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        AbstractStructuralObjectImpl persistentAnchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
        this.authz.checkAccess(persistentAnchor, "default:list:inherited"); //$NON-NLS-1$

        List<AbstractConfigurationObject> inherited = this.objectPoolProvider.getInheritedDefaults(em, persistentAnchor);
        Set<ConfigurationObjectMutable> local = new HashSet<>(persistentAnchor.getDefaultObjects());
        return extractInheritedDefaults(inherited, local);
    }


    @SuppressWarnings ( "rawtypes" )
    private static Set<ConfigurationObject> extractInheritedDefaults ( List<AbstractConfigurationObject> inherited,
            Set<ConfigurationObjectMutable> local ) {
        // templates are sorted by tree depth, so the first one will be the applied one
        Set<Class<?>> foundTypes = new HashSet<>();
        Set<ConfigurationObject> res = new HashSet<>();
        for ( AbstractConfigurationObject<?> obj : inherited ) {
            if ( !foundTypes.contains(obj.getType()) ) {
                if ( !local.contains(obj) ) {
                    // only add if it is not local
                    res.add(new ConfigurationObjectReference(obj));
                }
                foundTypes.add(obj.getType());
            }
        }
        return res;
    }
}
