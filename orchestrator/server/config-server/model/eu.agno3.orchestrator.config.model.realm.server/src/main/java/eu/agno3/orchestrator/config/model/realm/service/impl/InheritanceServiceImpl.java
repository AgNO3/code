/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

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
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyBuilder;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext;
import eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.ValueTypes;
import eu.agno3.orchestrator.config.model.realm.service.InheritanceService;
import eu.agno3.orchestrator.config.model.realm.service.InheritanceServiceDescriptor;
import eu.agno3.runtime.transaction.TransactionService;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    InheritanceService.class, InheritanceServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.InheritanceService",
    targetNamespace = InheritanceServiceDescriptor.NAMESPACE,
    serviceName = InheritanceServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/inheritance" )
public class InheritanceServiceImpl implements InheritanceService, SOAPWebService, InheritanceServerService {

    private static final Logger log = Logger.getLogger(InheritanceServiceImpl.class);
    private DefaultServerServiceContext sctx;
    private TransactionService tx;
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
    protected synchronized void setTransactionService ( TransactionService ts ) {
        this.tx = ts;
    }


    protected synchronized void unsetTransactionService ( TransactionService ts ) {
        if ( this.tx == ts ) {
            this.tx = null;
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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.InheritanceService#getInherited(eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "config:view:inherited" )
    public <@NonNull T extends ConfigurationObject> T getInherited ( @Nullable T obj, String rootType )
            throws ModelObjectNotFoundException, ModelServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Fetch inherited values for " + obj); //$NON-NLS-1$
        }
        EntityManager em = this.sctx.createConfigEM();
        try {
            AbstractConfigurationObject<@Nullable T> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:view:inherited"); //$NON-NLS-1$
            T inherited = getInherited(this.inheritanceUtil.makeProxyContext(
                em,
                persistent.getType(),
                persistent.getAnchor(),
                !StringUtils.isBlank(rootType) ? this.sctx.getObjectTypeRegistry().get(rootType).getObjectType() : null), persistent);
            return inherited;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch inherited values", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService#getInherited(eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext,
     *      eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public @NonNull <T extends ConfigurationObject> T getInherited ( @NonNull InheritanceProxyContext cc,
            @NonNull AbstractConfigurationObject<@Nullable T> persistent ) throws ModelServiceException {
        return (T) cc.getInheritanceProxyBuilder()
                .makeInheritanceProxy(cc.withValueTypes(EnumSet.of(ValueTypes.INHERITED, ValueTypes.DEFAULT)), persistent);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.InheritanceService#getEffective(eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      java.lang.String)
     */
    @Override
    @RequirePermissions ( "config:view:effective" )
    public <T extends ConfigurationObject> T getEffective ( @Nullable T obj, String rootType )
            throws ModelObjectNotFoundException, ModelServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Fetch effective values for " + obj); //$NON-NLS-1$
        }
        EntityManager em = this.sctx.createConfigEM();
        try {
            AbstractConfigurationObject<@Nullable T> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:view:effective"); //$NON-NLS-1$
            return getEffective(em, persistent, rootType != null ? this.sctx.getObjectTypeRegistry().get(rootType).getObjectType() : null);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch effective values", e); //$NON-NLS-1$
        }

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService#getEffective(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject, java.lang.Class)
     */
    @Override
    public @NonNull <T extends ConfigurationObject> T getEffective ( @NonNull EntityManager em,
            @NonNull AbstractConfigurationObject<@Nullable T> persistent, @Nullable Class<? extends ConfigurationObject> rootType )
            throws ModelServiceException {
        return getEffective(this.inheritanceUtil.makeProxyContext(em, persistent.getType(), persistent.getAnchor(), rootType), persistent);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService#getEffective(eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext,
     *      eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public @NonNull <T extends ConfigurationObject> T getEffective ( @NonNull InheritanceProxyContext cc,
            @NonNull AbstractConfigurationObject<@Nullable T> persistent ) throws ModelServiceException {
        return (@NonNull T) cc.getInheritanceProxyBuilder().makeInheritanceProxy(cc, PersistenceUtil.unproxyDeep(persistent));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.InheritanceService#getEligibleTemplates(eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "config:list:templates" )
    public List<ConfigurationObjectReference> getEligibleTemplates ( StructuralObject anchor, String objType, String filter )
            throws ModelObjectNotFoundException, ModelServiceException {
        if ( objType == null ) {
            throw new ModelServiceException();
        }
        EntityManager em = this.sctx.createConfigEM();
        AbstractStructuralObjectImpl persistentAnchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
        this.authz.checkAccess(persistentAnchor, "config:view:templates"); //$NON-NLS-1$
        List<ConfigurationObjectReference> clearedTemplates = new LinkedList<>();
        for ( ConfigurationObject template : getEligibleTemplates(em, persistentAnchor, objType) ) {
            clearedTemplates.add(new ConfigurationObjectReference(template));
        }
        return clearedTemplates;

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService#getEligibleTemplates(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl, java.lang.String)
     */
    @Override
    public @NonNull List<@NonNull ConfigurationObject> getEligibleTemplates ( @NonNull EntityManager em,
            @NonNull AbstractStructuralObjectImpl persistentAnchor, @NonNull String objType ) throws ModelServiceException {
        @SuppressWarnings ( "unchecked" )
        ConcreteObjectTypeDescriptor<ConfigurationObject, AbstractConfigurationObject<ConfigurationObject>> descriptor = (ConcreteObjectTypeDescriptor<ConfigurationObject, AbstractConfigurationObject<ConfigurationObject>>) this.sctx
                .getObjectTypeRegistry().get(objType);
        List<AbstractConfigurationObject<ConfigurationObject>> objs = this.objectPoolProvider
                .getObjectsInScopeByType(em, persistentAnchor, descriptor.getObjectType());
        List<@NonNull ConfigurationObject> res = new LinkedList<>();
        for ( AbstractConfigurationObject<ConfigurationObject> obj : objs ) {
            if ( obj != null && isUsableTemplate(obj) ) {
                res.add(obj);
            }
        }
        return res;
    }


    private boolean isUsableTemplate ( AbstractConfigurationObject<ConfigurationObject> obj ) {
        return this.authz.hasAccess(obj.getAnchor(), "config:view:template") //$NON-NLS-1$
                && ( obj.getDisplayName() != null || obj.getName() != null );
    }
}
