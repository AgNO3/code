/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

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
import eu.agno3.orchestrator.config.model.realm.server.service.EnforcementServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyBuilder;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext;
import eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.ValueTypes;
import eu.agno3.orchestrator.config.model.realm.service.EnforcementService;
import eu.agno3.orchestrator.config.model.realm.service.EnforcementServiceDescriptor;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    EnforcementService.class, EnforcementServerService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.EnforcementService",
    targetNamespace = EnforcementServiceDescriptor.NAMESPACE,
    serviceName = EnforcementServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/enforcement" )
public class EnforcementServiceImpl implements EnforcementService, SOAPWebService, EnforcementServerService {

    /**
     * 
     */
    private static final String ENFORCEMENT_MODIFY = "enforcement:modify"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(EnforcementServiceImpl.class);
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
     * @see eu.agno3.orchestrator.config.model.realm.service.EnforcementService#fetchEnforcements(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "enforcement:list:local" )
    public Set<ConfigurationObject> fetchEnforcements ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);
            this.authz.checkAccess(persistent, "enforcement:list:local"); //$NON-NLS-1$
            return fetchEnforcements(em, persistent);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch enforced objects", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.service.EnforcementServerService#fetchEnforcements(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl)
     */
    @Override
    public @NonNull Set<@NonNull ConfigurationObject> fetchEnforcements ( @NonNull EntityManager em,
            @NonNull AbstractStructuralObjectImpl persistent ) {
        Set<@NonNull ConfigurationObject> enforcements = new LinkedHashSet<>();
        for ( ConfigurationObject obj : persistent.getEnforcedObjects() ) {
            enforcements.add(new ConfigurationObjectReference(obj));
        }
        return enforcements;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.EnforcementService#getAppliedEnforcement(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:view:appliedEnforcement" )
    public <T extends ConfigurationObject> @Nullable T getAppliedEnforcement ( T obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<@Nullable T> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:view:appliedEnforcement"); //$NON-NLS-1$
            return getAppliedEnforcement(this.inheritanceUtil.makeProxyContext(em, persistent.getType(), persistent.getAnchor(), null), persistent);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch applied enforcements", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.EnforcementServerService#getAppliedEnforcement(eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext,
     *      eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConfigurationObject> @NonNull T getAppliedEnforcement ( @NonNull InheritanceProxyContext cc,
            @NonNull AbstractConfigurationObject<@Nullable T> persistent ) throws ModelServiceException {
        return (T) cc.getInheritanceProxyBuilder().makeInheritanceProxy(cc.withValueTypes(EnumSet.of(ValueTypes.ENFORCED)), persistent);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.EnforcementService#getEnforcementsFor(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "config:view:enforcementForType" )
    public ConfigurationObject getEnforcementsFor ( StructuralObject obj, String objType )
            throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        if ( objType == null ) {
            throw new ModelServiceException();
        }

        try {
            AbstractStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, obj);
            this.authz.checkAccess(persistent, "config:view:enforcementForType"); //$NON-NLS-1$
            ConcreteObjectTypeDescriptor<@Nullable ? extends ConfigurationObject, @Nullable ? extends AbstractConfigurationObject<?>> desc = this.sctx
                    .getObjectTypeRegistry().getConcrete(objType);
            return getEnforcementsFor(this.inheritanceUtil.makeProxyContext(em, desc.getObjectType(), obj, null), persistent, objType);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch local enforcment", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.EnforcementServerService#getEnforcementsFor(eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl, java.lang.String)
     */
    @Override
    public @NonNull ConfigurationObject getEnforcementsFor ( @NonNull InheritanceProxyContext cc, @NonNull AbstractStructuralObjectImpl persistent,
            @NonNull String objType ) throws ModelServiceException {

        ConcreteObjectTypeDescriptor<@Nullable ? extends ConfigurationObject, @Nullable ? extends AbstractConfigurationObject<?>> desc = this.sctx
                .getObjectTypeRegistry().getConcrete(objType);
        @NonNull
        Class<@Nullable ? extends ConfigurationObject> objectType = desc.getObjectType();
        return cc.getInheritanceProxyBuilder().getEnforcementProxy(cc, objectType, persistent);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.EnforcementService#setEnforcement(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( ENFORCEMENT_MODIFY )
    public void setEnforcement ( StructuralObject anchor, ConfigurationObject object ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractStructuralObjectImpl persistentAnchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
            this.authz.checkAccess(persistentAnchor, ENFORCEMENT_MODIFY);
            AbstractConfigurationObject<?> persistentConfig = this.persistenceUtil.fetch(em, object);

            if ( !this.objectPoolProvider.isInScope(persistentAnchor, persistentConfig) ) {
                throw new ModelServiceException("Template is not in scope of anchor"); //$NON-NLS-1$
            }

            AbstractConfigurationObject<?> enforcedForType = this.objectPoolProvider
                    .getDefaultForType(em, persistentAnchor, persistentConfig.getType());
            if ( enforcedForType != null ) {
                log.debug("There is already a default for this type, removing old"); //$NON-NLS-1$
                persistentAnchor.getEnforcedObjects().remove(enforcedForType);
                enforcedForType.getEnforcedFor().remove(persistentAnchor);
            }

            persistentAnchor.getEnforcedObjects().add(persistentConfig);
            persistentConfig.getEnforcedFor().add(persistentAnchor);

            em.persist(persistentAnchor);
            em.persist(persistentConfig);
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to set enforcement", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.EnforcementService#unsetEnforcement(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( ENFORCEMENT_MODIFY )
    public void unsetEnforcement ( StructuralObject anchor, ConfigurationObject object ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        try {
            AbstractStructuralObjectImpl persistentAnchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
            this.authz.checkAccess(persistentAnchor, ENFORCEMENT_MODIFY);
            AbstractConfigurationObject<?> persistentConfig = this.persistenceUtil.fetch(em, object);

            persistentAnchor.getEnforcedObjects().remove(persistentConfig);
            persistentConfig.getEnforcedFor().remove(persistentAnchor);

            em.persist(persistentAnchor);
            em.persist(persistentConfig);
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch defaults", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.EnforcementService#fetchInheritedEnforcements(eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @SuppressWarnings ( "rawtypes" )
    @RequirePermissions ( "enforcement:list:inherited" )
    @Override
    public Set<ConfigurationObject> fetchInheritedEnforcements ( StructuralObject anchor )
            throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();
        AbstractStructuralObjectImpl persistentAnchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
        this.authz.checkAccess(persistentAnchor, "enforcement:list:inherited"); //$NON-NLS-1$
        List<AbstractConfigurationObject> inherited = this.objectPoolProvider.getInheritedEnforcements(em, persistentAnchor);
        Set<ConfigurationObjectMutable> local = new HashSet<>(persistentAnchor.getEnforcedObjects());
        return extractInheritedEnforcements(inherited, local);
    }


    @SuppressWarnings ( "rawtypes" )
    private static Set<ConfigurationObject> extractInheritedEnforcements ( List<AbstractConfigurationObject> inherited,
            Set<ConfigurationObjectMutable> local ) {
        // templates are sorted by tree depth, so the first one will be the applied one
        Set<Class<?>> foundTypes = new HashSet<>();
        Set<ConfigurationObject> res = new HashSet<>();
        for ( ConfigurationObject obj : inherited ) {
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
