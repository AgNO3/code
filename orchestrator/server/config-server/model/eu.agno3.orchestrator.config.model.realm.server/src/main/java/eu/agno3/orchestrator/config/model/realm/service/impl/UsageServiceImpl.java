/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.UsageService;
import eu.agno3.orchestrator.config.model.realm.service.UsageServiceDescriptor;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    UsageService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.UsageService",
    targetNamespace = UsageServiceDescriptor.NAMESPACE,
    serviceName = UsageServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/usage" )
public class UsageServiceImpl implements UsageService, SOAPWebService {

    private static final Logger log = Logger.getLogger(UsageServiceImpl.class);
    private DefaultServerServiceContext sctx;
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


    private static Set<ConfigurationObject> toReferences ( Set<? extends ConfigurationObject> objs ) {
        Set<ConfigurationObject> refs = new HashSet<>();

        for ( ConfigurationObject o : objs ) {
            if ( o instanceof ConfigurationObjectReference ) {
                refs.add(o);
            }
            else {
                refs.add(new ConfigurationObjectReference(o));
            }
        }

        return refs;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getUsedBy(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:usedBy" )
    public Set<ConfigurationObject> getUsedBy ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            Set<ConfigurationObject> usedBy = new HashSet<>();
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:usedBy"); //$NON-NLS-1$
            addUsedBy(persistent, usedBy);
            return toReferences(usedBy);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch usedBy", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getUsedByClosure(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:usedByClosure" )
    public Set<ConfigurationObject> getUsedByClosure ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:usedByClosure"); //$NON-NLS-1$
            return toReferences(getUsedByClosure(persistent));
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch usedBy closure", e); //$NON-NLS-1$
        }
    }


    private Set<ConfigurationObject> getUsedByClosure ( AbstractConfigurationObject<ConfigurationObject> persistent ) {
        Set<ConfigurationObject> usedBy = new HashSet<>();
        Set<ConfigurationObject> localUsedBy = new HashSet<>();

        Deque<ConfigurationObject> q = new LinkedList<>();
        q.add(persistent);

        while ( !q.isEmpty() ) {
            ConfigurationObject cur = q.poll();
            localUsedBy.clear();
            addUsedBy((AbstractConfigurationObject<?>) cur, localUsedBy);
            q.addAll(localUsedBy);
            usedBy.addAll(localUsedBy);
        }

        return usedBy;
    }


    /**
     * @param fetch
     * @param usedBy
     */
    private void addUsedBy ( AbstractConfigurationObject<?> obj, Set<ConfigurationObject> usedBy ) {
        usedBy.addAll(obj.getUsedBy());
        usedBy.addAll(obj.getInheritedBy());

        for ( ConfigurationObject sub : obj.getSubObjects() ) {
            addUsedBy((AbstractConfigurationObject<?>) sub, usedBy);
        }

        usedBy.remove(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getUsedBy(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:uses" )
    public Set<ConfigurationObject> getUses ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            Set<ConfigurationObject> uses = new HashSet<>();
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:uses"); //$NON-NLS-1$
            addUses(persistent, uses);
            return toReferences(uses);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch uses", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getUsesClosure(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:usesClosure" )
    public Set<ConfigurationObject> getUsesClosure ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            Set<ConfigurationObject> uses = new HashSet<>();
            Set<ConfigurationObject> localUses = new HashSet<>();
            Deque<ConfigurationObject> q = new LinkedList<>();
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:usesClosure"); //$NON-NLS-1$
            q.add(persistent);

            while ( !q.isEmpty() ) {
                ConfigurationObject cur = q.poll();
                localUses.clear();
                addUses((AbstractConfigurationObject<?>) cur, localUses);
                q.addAll(localUses);
                uses.addAll(toReferences(localUses));
            }

            return uses;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch uses closure", e); //$NON-NLS-1$
        }
    }


    /**
     * @param obj
     * @param uses
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private void addUses ( AbstractConfigurationObject<?> obj, Set<ConfigurationObject> uses )
            throws ModelObjectNotFoundException, ModelServiceException {
        uses.addAll(obj.getUses());
        if ( obj.getInherits() != null ) {
            uses.add(obj.getInherits());
        }
        for ( ConfigurationObject sub : obj.getSubObjects() ) {
            uses.remove(sub);
            addUses((AbstractConfigurationObject<?>) sub, uses);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getInheritedBy(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:inheritedBy" )
    public Set<ConfigurationObject> getInheritedBy ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:inheritedBy"); //$NON-NLS-1$
            return toReferences(persistent.getInheritedBy());
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch inheritedBy", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getInheritedBy(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:inherits" )
    public Set<ConfigurationObject> getInherits ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            Set<ConfigurationObject> inherits = new HashSet<>();
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:inherits"); //$NON-NLS-1$
            addInherits(persistent, inherits);
            return toReferences(inherits);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch inherits", e); //$NON-NLS-1$
        }
    }


    /**
     * @param fetch
     * @param inherits
     */
    private void addInherits ( AbstractConfigurationObject<?> obj, Set<ConfigurationObject> inherits ) {
        if ( obj.getInherits() != null ) {
            inherits.add(obj.getInherits());
        }

        for ( ConfigurationObject sub : obj.getSubObjects() ) {
            addInherits((AbstractConfigurationObject<?>) sub, inherits);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getDefaultFor(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:defaultFor" )
    public Set<ConfigurationObject> getDefaultFor ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:defaultFor"); //$NON-NLS-1$
            return toReferences(this.objectPoolProvider.getDefaultAppliedToObjects(em, persistent));
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch default effects", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getDefaultForStructure(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:defaultForStructure" )
    public Set<? extends StructuralObject> getDefaultForStructure ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:defaultForStructure"); //$NON-NLS-1$
            return this.objectPoolProvider.getDefaultAppliedTo(em, persistent);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch structual default effects", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getEnforcedFor(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:enforcedFor" )
    public Set<ConfigurationObject> getEnforcedFor ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:enforcedFor"); //$NON-NLS-1$
            return toReferences(this.objectPoolProvider.getEnforcementAppliedToObjects(em, persistent));
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch enforcement effects", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getEnforcedForStructure(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:enforcedForStructure" )
    public Set<? extends StructuralObject> getEnforcedForStructure ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:enforcedForStructure"); //$NON-NLS-1$
            return this.objectPoolProvider.getEnforcedAppliedTo(em, persistent);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch structural enforcement effects", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getAffects(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:affects" )
    public Set<ConfigurationObject> getAffects ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:affects"); //$NON-NLS-1$
            return toReferences(getAffectsClosure(em, persistent));
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch effects", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.UsageService#getAffectsServices(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:usage:affectsServices" )
    public Set<ServiceStructuralObject> getAffectsServices ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:affectsServices"); //$NON-NLS-1$
            return extractServices(getAffectsClosure(em, persistent));
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch service effects", e); //$NON-NLS-1$
        }
    }


    /**
     * @param affectsClosure
     * @return
     */
    private static Set<ServiceStructuralObject> extractServices ( Set<@NonNull ConfigurationObject> affectsClosure ) {
        Set<@NonNull ServiceStructuralObject> res = new HashSet<>();

        for ( ConfigurationObject obj : affectsClosure ) {
            ConfigurationObject outer = PersistenceUtil.unproxy(getOutermostObject((AbstractConfigurationObject<@NonNull ?>) obj));

            if ( outer instanceof ConfigurationInstance ) {
                ServiceStructuralObject forService = ( (ConfigurationInstance) outer ).getForService();
                if ( forService != null ) {
                    res.add(forService);
                }
            }
        }

        return res;
    }


    /**
     * @param obj
     * @return
     */
    private static @NonNull AbstractConfigurationObject<@NonNull ?> getOutermostObject ( @NonNull AbstractConfigurationObject<@NonNull ?> obj ) {
        AbstractConfigurationObject<@NonNull ?> cur = obj;

        while ( cur.getOuterObject() != null ) {
            cur = (AbstractConfigurationObject<@NonNull ?>) cur.getOuterObject();
        }

        return cur;
    }


    private Set<@NonNull ConfigurationObject> getAffectsClosure ( EntityManager em,
            @NonNull AbstractConfigurationObject<ConfigurationObject> persistent ) throws ModelServiceException {
        Set<@NonNull ConfigurationObject> affected = new HashSet<>();
        Set<@NonNull ConfigurationObject> localAffected = new HashSet<>();

        Deque<@NonNull ConfigurationObject> q = new LinkedList<>();
        q.add(persistent);

        while ( !q.isEmpty() ) {
            ConfigurationObject cur = q.poll();
            localAffected.clear();
            addAffects(em, (AbstractConfigurationObject<?>) cur, localAffected);
            q.addAll(localAffected);
            affected.addAll(localAffected);
        }

        return affected;
    }


    /**
     * @param fetch
     * @param affected
     * @throws ModelServiceException
     */
    private void addAffects ( EntityManager em, AbstractConfigurationObject<?> obj, Set<@NonNull ConfigurationObject> affected )
            throws ModelServiceException {
        affected.addAll(obj.getUsedBy());
        affected.addAll(obj.getInheritedBy());

        for ( ConfigurationObject sub : obj.getSubObjects() ) {
            addUsedBy((AbstractConfigurationObject<?>) sub, affected);
        }

        affected.addAll(this.objectPoolProvider.getDefaultAppliedToObjects(em, obj));
        affected.addAll(this.objectPoolProvider.getEnforcementAppliedToObjects(em, obj));
        affected.remove(obj);
    }


    @Override
    @RequirePermissions ( "config:usage:affectedBy" )
    public Set<ConfigurationObject> getAffectedBy ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.createConfigEM();

        try {
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:usage:affectedBy"); //$NON-NLS-1$
            return toReferences(getAffectedByClosure(em, persistent));
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch inverse effects", e); //$NON-NLS-1$
        }
    }


    private Set<ConfigurationObject> getAffectedByClosure ( EntityManager em, AbstractConfigurationObject<ConfigurationObject> persistent )
            throws ModelServiceException {
        Set<ConfigurationObject> affected = new HashSet<>();
        Set<ConfigurationObject> localAffected = new HashSet<>();

        Deque<ConfigurationObject> q = new LinkedList<>();
        q.add(persistent);

        while ( !q.isEmpty() ) {
            ConfigurationObject cur = q.poll();
            localAffected.clear();
            addAffectedBy(em, (AbstractConfigurationObject<?>) cur, localAffected);
            q.addAll(localAffected);
            affected.addAll(localAffected);
        }

        return affected;
    }


    /**
     * @param fetch
     * @param affected
     * @throws ModelServiceException
     */
    private void addAffectedBy ( EntityManager em, AbstractConfigurationObject<?> obj, Set<ConfigurationObject> affected )
            throws ModelServiceException {

        affected.addAll(obj.getUses());
        if ( obj.getInherits() != null ) {
            affected.add(obj.getInherits());
        }
        else {
            AbstractConfigurationObject<?> def = this.objectPoolProvider.fetchInheritedDefault(em, obj.getType(), obj.getAnchor(), null);

            if ( def != null ) {
                affected.add(def);
            }
            else if ( log.isDebugEnabled() ) {
                log.debug("No default found for " + obj); //$NON-NLS-1$
            }
        }

        for ( ConfigurationObject sub : obj.getSubObjects() ) {
            affected.remove(sub);
            addAffectedBy(em, (AbstractConfigurationObject<?>) sub, affected);
        }

        AbstractConfigurationObject<?> enforced = this.objectPoolProvider.fetchInheritedEnforcement(em, obj.getType(), obj.getAnchor(), null);
        if ( enforced != null ) {
            affected.add(enforced);
        }
        else if ( log.isDebugEnabled() ) {
            log.debug("No enforcement found for " + obj); //$NON-NLS-1$
        }

        affected.remove(obj);
    }

}
