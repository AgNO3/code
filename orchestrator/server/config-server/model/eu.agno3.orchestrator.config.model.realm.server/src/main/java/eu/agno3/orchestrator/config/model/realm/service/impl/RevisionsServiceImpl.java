/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectNotFoundFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.db.versioning.RevisionEntity;
import eu.agno3.orchestrator.config.model.base.versioning.VersionInfo;
import eu.agno3.orchestrator.config.model.base.versioning.VersionInfoImpl;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.RevisionsService;
import eu.agno3.orchestrator.config.model.realm.service.RevisionsServiceDescriptor;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    RevisionsService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.RevisionsService",
    targetNamespace = RevisionsServiceDescriptor.NAMESPACE,
    serviceName = RevisionsServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/revisions" )
public class RevisionsServiceImpl implements RevisionsService, SOAPWebService {

    private DefaultServerServiceContext sctx;
    private InheritanceServerService inheritanceService;
    private PersistenceUtil persistenceUtil;
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
    protected synchronized void setInheritanceService ( InheritanceServerService iu ) {
        this.inheritanceService = iu;
    }


    protected synchronized void unsetInheritanceService ( InheritanceServerService iu ) {
        if ( this.inheritanceService == iu ) {
            this.inheritanceService = null;
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


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.RevisionsService#getRevisions(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    @RequirePermissions ( "config:view:revisions:list" )
    public List<VersionInfo> getRevisions ( ConfigurationObject obj ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();

        if ( obj == null ) {
            return Collections.EMPTY_LIST;
        }

        List<VersionInfo> res = new LinkedList<>();

        try {
            AuditReader ar = AuditReaderFactory.get(em);

            Set<Number> entityRevisions = new HashSet<>();
            AbstractConfigurationObject<ConfigurationObject> persistent = this.persistenceUtil.fetch(em, obj);
            this.authz.checkAccess(persistent.getAnchor(), "config:view:revisions:list"); //$NON-NLS-1$
            fetchEntityRevisions(persistent, ar, entityRevisions);
            Map<Number, RevisionEntity> revs = ar.findRevisions(RevisionEntity.class, new HashSet<>(entityRevisions));

            for ( Entry<Number, RevisionEntity> rev : revs.entrySet() ) {
                VersionInfoImpl info = new VersionInfoImpl();
                info.setRevisionNumber(rev.getValue().getRevision());
                info.setRevisionTime(rev.getValue().getRevisionTimestamp());
                info.setRevisionType(rev.getValue().getOverrideRevisionType());
                res.add(info);
            }

            Collections.sort(res, new Comparator<VersionInfo>() {

                @Override
                public int compare ( VersionInfo o1, VersionInfo o2 ) {
                    return Long.compare(o1.getRevisionNumber(), o2.getRevisionNumber());
                }
            });

            return res;

        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch revisions", e); //$NON-NLS-1$
        }
    }


    private void fetchEntityRevisions ( AbstractConfigurationObject<?> obj, AuditReader ar, Set<Number> entityRevisions )
            throws ModelServiceException {
        Class<?> entityClass = this.sctx.getObjectTypeRegistry().getConcrete(obj.getType()).getImplementationType();
        entityRevisions.addAll(ar.getRevisions(entityClass, obj.getId()));

        for ( ConfigurationObject inner : obj.getSubObjects() ) {

            fetchEntityRevisions((AbstractConfigurationObject<?>) inner, ar, entityRevisions);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.RevisionsService#getConfigAtRevision(eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      long)
     */

    @Override
    @SuppressWarnings ( "unchecked" )
    public <T extends ConfigurationObject> T getConfigAtRevision ( T obj, long revision ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();

        if ( obj == null ) {
            throw new ModelObjectNotFoundException(new ModelObjectNotFoundFault());
        }

        AbstractConfigurationObject<T> persistent = this.persistenceUtil.fetch(em, obj);
        this.authz.checkAccess(persistent.getAnchor(), "config:view:revisions:view"); //$NON-NLS-1$
        return (T) getConfigAtRevision(persistent, revision, em);
    }


    @SuppressWarnings ( "unchecked" )
    private <T extends ConfigurationObject> AbstractConfigurationObject<T> getConfigAtRevision ( @NonNull AbstractConfigurationObject<T> obj,
            long revision, EntityManager em ) throws ModelServiceException, ModelObjectNotFoundException {
        try {
            AuditReader ar = AuditReaderFactory.get(em);
            Class<?> entityClass = this.sctx.getObjectTypeRegistry().getConcrete(obj.getType()).getImplementationType();
            AbstractConfigurationObject<T> rev = (AbstractConfigurationObject<T>) ar.find(entityClass, obj.getId(), Long.valueOf(revision));

            if ( rev == null ) {
                throw new ModelObjectNotFoundException(obj.getType(), obj.getId());
            }

            return rev;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch config", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.RevisionsService#getEffectiveAtRevision(eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      long)
     */
    @SuppressWarnings ( "null" )
    @Override
    public <T extends ConfigurationObject> T getEffectiveAtRevision ( T obj, long revision )
            throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.createConfigEM();
        AbstractConfigurationObject<@Nullable T> persistent = this.persistenceUtil.fetch(em, obj);
        this.authz.checkAccess(persistent.getAnchor(), "config:view:revisions:viewEffective"); //$NON-NLS-1$
        AbstractConfigurationObject<@Nullable T> config = getConfigAtRevision(persistent, revision, em);

        if ( config == null ) {
            throw new ModelObjectNotFoundException(new ModelObjectNotFoundFault());
        }

        @NonNull
        Class<? extends ConfigurationObject> type = config.getType();
        return this.inheritanceService.getEffective(em, config, type);
    }
}
