/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.base.tree.TreeNodeHolder;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectPoolProvider.class )
public class ObjectPoolProviderImpl implements ObjectPoolProvider {

    private static final String DEFAULT_FOR_RELATION = "defaultFor"; //$NON-NLS-1$
    private static final String ENFORCED_FOR_RELATION = "enforcedFor"; //$NON-NLS-1$

    private static final String ID = "id"; //$NON-NLS-1$
    private static final String ANCHOR = "anchor"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ObjectPoolProviderImpl.class);

    private ObjectTypeRegistry objectTypeRegistry;


    @Reference
    protected synchronized void setObjectTypeRegistry ( ObjectTypeRegistry otr ) {
        this.objectTypeRegistry = otr;
    }


    protected synchronized void unsetObjectTypeRegistry ( ObjectTypeRegistry otr ) {
        if ( this.objectTypeRegistry == otr ) {
            this.objectTypeRegistry = null;
        }
    }


    private <T extends ConfigurationObject> Class<? extends AbstractConfigurationObject<?>> getImplType ( Class<? extends T> type )
            throws ModelServiceException {
        return this.objectTypeRegistry.getConcrete(type).getImplementationType();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider#getAttachedObjectsByType(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl, java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConfigurationObject> List<AbstractConfigurationObject<T>> getAttachedObjectsByType ( EntityManager em,
            AbstractStructuralObjectImpl obj, Class<T> type ) throws ModelServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Loading objects of type %s at %s", type.getName(), obj.getId()); //$NON-NLS-1$
        }
        TypedQuery<? extends AbstractConfigurationObject<?>> q = em
                .createQuery(createObjectSingleRelationQuery(em, obj, this.getImplType(type), ANCHOR));
        return (List<AbstractConfigurationObject<T>>) q.getResultList();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider#getResourceLibraries(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl)
     */
    @SuppressWarnings ( "null" )
    @Override
    public @NonNull List<@NonNull ResourceLibrary> getResourceLibraries ( EntityManager em, AbstractStructuralObjectImpl obj ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ResourceLibrary> cq = cb.createQuery(ResourceLibrary.class);
        Root<ResourceLibrary> tbl = cq.from(ResourceLibrary.class);
        EntityType<ResourceLibrary> model = tbl.getModel();
        cq.where(cb.equal(tbl.get(model.getSingularAttribute(ANCHOR, AbstractStructuralObjectImpl.class)), obj));
        return em.createQuery(cq).getResultList();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider#getResourceLibrariesInScope(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl)
     */
    @Override
    public List<ResourceLibrary> getResourceLibrariesInScope ( EntityManager em, AbstractStructuralObjectImpl obj ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ResourceLibrary> cq = cb.createQuery(ResourceLibrary.class);
        Root<ResourceLibrary> tbl = cq.from(ResourceLibrary.class);
        EntityType<ResourceLibrary> model = tbl.getModel();
        EntityType<AbstractStructuralObjectImpl> structModel = em.getMetamodel().entity(AbstractStructuralObjectImpl.class);
        Join<ResourceLibrary, AbstractStructuralObjectImpl> struct = tbl.join(model.getSingularAttribute(ANCHOR, AbstractStructuralObjectImpl.class));
        cq.where(TreeUtil.createAncestorQueryIncludingSelf(obj, em, struct, structModel));

        // order by descending depth => list will contain the sequence up to the root
        cq.orderBy(cb.desc(TreeUtil.getDepthPath(em, struct, structModel)));
        return em.createQuery(cq).getResultList();
    }


    /**
     * @param em
     * @param obj
     * @param type
     * @return all objects which are of the given type and in the scope of the given obj
     * @throws ModelServiceException
     */
    @Override
    @SuppressWarnings ( "unchecked" )
    public <T extends ConfigurationObject> List<AbstractConfigurationObject<T>> getObjectsInScopeByType ( EntityManager em,
            AbstractStructuralObjectImpl obj, Class<T> type ) throws ModelServiceException {
        return (List<AbstractConfigurationObject<T>>) getAttachedObjectsInScope(em, obj, getImplType(type));
    }


    private static <T extends AbstractConfigurationObject<?>> List<T> getAttachedObjectsInScope ( EntityManager em, AbstractStructuralObjectImpl obj,
            Class<T> type ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> tbl = cq.from(type);
        EntityType<T> model = tbl.getModel();
        EntityType<AbstractStructuralObjectImpl> structModel = em.getMetamodel().entity(AbstractStructuralObjectImpl.class);
        Join<T, AbstractStructuralObjectImpl> struct = tbl.join(model.getSingularAttribute(ANCHOR, AbstractStructuralObjectImpl.class));
        cq.where(TreeUtil.createAncestorQueryIncludingSelf(obj, em, struct, structModel));

        // order by descending depth => list will contain the sequence up to the root
        cq.orderBy(cb.desc(TreeUtil.getDepthPath(em, struct, structModel)));

        return em.createQuery(cq).getResultList();
    }


    /**
     * @param base
     * @param toCheck
     * @return whether the object toCheck is in scope of base
     */
    @Override
    public boolean isInScope ( AbstractConfigurationObject<?> base, AbstractConfigurationObject<?> toCheck ) {
        return TreeUtil.isAncestorOrSame(toCheck.getAnchor(), base.getAnchor());
    }


    /**
     * @param base
     * @param toCheck
     * @return whether the object toCheck is in scope of base
     */
    @Override
    public boolean isInScope ( AbstractStructuralObjectImpl base, AbstractConfigurationObject<?> toCheck ) {
        return TreeUtil.isAncestorOrSame(toCheck.getAnchor(), base);
    }


    private static <T extends ConfigurationObject> CriteriaQuery<T> createObjectSingleRelationQuery ( EntityManager em,
            AbstractStructuralObjectImpl obj, Class<T> type, String relation ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> tbl = cq.from(type);
        EntityType<T> model = tbl.getModel();
        cq.where(cb.equal(tbl.get(model.getSingularAttribute(relation, AbstractStructuralObjectImpl.class)), obj));
        return cq;
    }


    private static <T extends ConfigurationObject> CriteriaQuery<T> createObjectMultiRelationQuery ( EntityManager em,
            AbstractStructuralObjectImpl obj, Class<T> type, String relation ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> tbl = cq.from(type);
        SetJoin<Object, Object> joinSet = tbl.joinSet(relation);
        return cq.where(cb.equal(joinSet.get(ID), obj.getId()));
    }


    /**
     * Param left, right
     * 
     * @param em
     * @param otr
     * @param type
     * @param at
     * @param relation
     * @return
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private <T extends ConfigurationObject> CriteriaQuery<T> createInheritedPoolRelationQuery ( EntityManager em, Class<? extends T> type,
            String relation ) throws ModelServiceException {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        @NonNull
        Class<? extends AbstractConfigurationObject<?>> implType = this.objectTypeRegistry.getConcrete(type).getImplementationType();
        CriteriaQuery<T> q = (CriteriaQuery<T>) cb.createQuery(implType);
        Root<T> from = (Root<T>) q.from(implType);

        SetJoin<Object, AbstractStructuralObjectImpl> joinSet = from.joinSet(relation);
        EntityType<AbstractStructuralObjectImpl> structModel = em.getMetamodel().entity(AbstractStructuralObjectImpl.class);
        Predicate ancestors = TreeUtil.createAncestorQuery(em, joinSet, structModel);
        q.where(ancestors);
        q.orderBy(cb.desc(TreeUtil.getDepthPath(em, joinSet, structModel)));
        return q;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider#getEnforcedForType(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl, java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConfigurationObject> AbstractConfigurationObject<T> getEnforcedForType ( EntityManager em, AbstractStructuralObjectImpl obj,
            Class<@Nullable T> type ) throws ModelServiceException {
        TypedQuery<? extends AbstractConfigurationObject<?>> q = em
                .createQuery(createObjectMultiRelationQuery(em, obj, this.getImplType(type), ENFORCED_FOR_RELATION));
        try {
            return (AbstractConfigurationObject<T>) q.getSingleResult();
        }
        catch ( NoResultException e ) {
            log.trace("No enforcement found", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider#getDefaultForType(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl, java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConfigurationObject> AbstractConfigurationObject<T> getDefaultForType ( EntityManager em, AbstractStructuralObjectImpl obj,
            Class<T> type ) throws ModelServiceException {
        TypedQuery<? extends AbstractConfigurationObject<?>> q = em
                .createQuery(createObjectMultiRelationQuery(em, obj, this.getImplType(type), DEFAULT_FOR_RELATION));
        try {
            return (AbstractConfigurationObject<T>) q.getSingleResult();
        }
        catch ( NoResultException e ) {
            log.trace("No default found", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider#fetchInheritedEnforcement(javax.persistence.EntityManager,
     *      java.lang.Class, eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl,
     *      eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public @Nullable <T extends ConfigurationObject> AbstractConfigurationObject<T> fetchInheritedEnforcement ( EntityManager em,
            Class<? extends @Nullable T> type, AbstractStructuralObjectImpl at, AbstractConfigurationObject<T> fallback )
                    throws ModelServiceException {
        TypedQuery<T> q = em.createQuery(createInheritedPoolRelationQuery(em, type, ENFORCED_FOR_RELATION));
        q.setParameter(TreeUtil.LEFT, at.getTreeNode().getLeft());
        q.setParameter(TreeUtil.RIGHT, at.getTreeNode().getRight());
        q.setMaxResults(1);
        List<AbstractConfigurationObject<T>> res = (List<AbstractConfigurationObject<T>>) q.getResultList();

        if ( res.isEmpty() ) {
            return fallback;
        }

        return res.get(0);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider#getAvailableEnforcementTypes(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public Set<Class<? extends ConfigurationObject>> getAvailableEnforcementTypes ( @NonNull EntityManager em, StructuralObject anchor ) {
        return getAvailablePoolTypes(em, ENFORCED_FOR_RELATION, (TreeNodeHolder) anchor);
    }


    /**
     * @param em
     * @param relation
     * @param anchor
     * @return
     */
    @SuppressWarnings ( "rawtypes" )
    private static Set<Class<? extends ConfigurationObject>> getAvailablePoolTypes ( EntityManager em, String relation, TreeNodeHolder anchor ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Fetching available types for " + relation); //$NON-NLS-1$
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Class> q = cb.createQuery(Class.class);
        Root<AbstractConfigurationObject> from = q.from(AbstractConfigurationObject.class);
        from.alias("e"); //$NON-NLS-1$
        q = q.select(from.type()).distinct(true);
        SetJoin<Object, AbstractStructuralObjectImpl> joinSet = from.joinSet(relation);
        EntityType<AbstractStructuralObjectImpl> structModel = em.getMetamodel().entity(AbstractStructuralObjectImpl.class);
        Predicate ancestors = TreeUtil.createAncestorQuery(em, joinSet, structModel);
        q.where(ancestors);
        TypedQuery<Class> cq = em.createQuery(q);
        cq.setParameter(TreeUtil.LEFT, anchor.getTreeNode().getLeft());
        cq.setParameter(TreeUtil.RIGHT, anchor.getTreeNode().getRight());
        List<Class> implTypes = cq.getResultList();
        Set<Class<? extends ConfigurationObject>> objTypes = new HashSet<>();
        for ( Class<?> implType : implTypes ) {
            objTypes.add(ObjectTypeUtil.findObjectType(implType));
        }
        return objTypes;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider#fetchInheritedDefault(javax.persistence.EntityManager,
     *      java.lang.Class, eu.agno3.orchestrator.config.model.realm.StructuralObject,
     *      eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public @Nullable <T extends ConfigurationObject> AbstractConfigurationObject<T> fetchInheritedDefault ( EntityManager em,
            Class<? extends @Nullable T> type, StructuralObject at, AbstractConfigurationObject<T> globalDefaults ) throws ModelServiceException {
        TypedQuery<T> dc = em.createQuery(createInheritedPoolRelationQuery(em, type, DEFAULT_FOR_RELATION));
        dc.setParameter(TreeUtil.LEFT, ( (TreeNodeHolder) at ).getTreeNode().getLeft());
        dc.setParameter(TreeUtil.RIGHT, ( (TreeNodeHolder) at ).getTreeNode().getRight());
        dc.setMaxResults(1);
        List<T> res = dc.getResultList();
        if ( res.isEmpty() ) {
            log.debug("Using global defaults"); //$NON-NLS-1$
            return globalDefaults;
        }

        log.debug("Using structural defaults"); //$NON-NLS-1$
        return (AbstractConfigurationObject<T>) res.get(0);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.ObjectPoolProvider#getAvailableDefaultTypes(javax.persistence.EntityManager,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    public Set<Class<? extends ConfigurationObject>> getAvailableDefaultTypes ( @NonNull EntityManager em, StructuralObject anchor ) {
        return getAvailablePoolTypes(em, DEFAULT_FOR_RELATION, (TreeNodeHolder) anchor);
    }


    @Override
    public Set<AbstractStructuralObjectImpl> getDefaultAppliedTo ( EntityManager em, AbstractConfigurationObject<?> def )
            throws ModelServiceException {
        return getRelationObjectAppliedTo(em, def, DEFAULT_FOR_RELATION, def.getDefaultFor());
    }


    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends AbstractConfigurationObject<?>> Set<T> getDefaultAppliedToObjects ( EntityManager em, T def ) throws ModelServiceException {
        return getRelationObjectAppliedToObjects(
            em,
            (Class<T>) this.objectTypeRegistry.getConcrete(def.getType()).getImplementationType(),
            DEFAULT_FOR_RELATION,
            def.getDefaultFor(),
            true);
    }


    @Override
    public Set<AbstractStructuralObjectImpl> getEnforcedAppliedTo ( EntityManager em, AbstractConfigurationObject<?> def )
            throws ModelServiceException {
        return getRelationObjectAppliedTo(em, def, ENFORCED_FOR_RELATION, def.getEnforcedFor());
    }


    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends AbstractConfigurationObject<?>> Set<T> getEnforcementAppliedToObjects ( EntityManager em, T enforcement )
            throws ModelServiceException {
        return getRelationObjectAppliedToObjects(
            em,
            (Class<T>) this.objectTypeRegistry.getConcrete(enforcement.getType()).getImplementationType(),
            ENFORCED_FOR_RELATION,
            enforcement.getEnforcedFor(),
            false);
    }


    private Set<AbstractStructuralObjectImpl> getRelationObjectAppliedTo ( EntityManager em, AbstractConfigurationObject<?> def, String relation,
            Set<StructuralObject> appliedTo ) throws ModelServiceException {
        Set<AbstractStructuralObjectImpl> res = new HashSet<>();
        for ( StructuralObject obj : appliedTo ) {
            AbstractStructuralObjectImpl root = (AbstractStructuralObjectImpl) obj;

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<AbstractStructuralObjectImpl> cq = cb.createQuery(AbstractStructuralObjectImpl.class);
            Root<AbstractStructuralObjectImpl> tbl = cq.from(AbstractStructuralObjectImpl.class);
            EntityType<AbstractStructuralObjectImpl> model = tbl.getModel();

            cq.where(
                createNonOverridenRelationshipQuery(
                    em,
                    this.objectTypeRegistry.getConcrete(def.getType()).getImplementationType(),
                    root,
                    cb,
                    tbl,
                    model,
                    relation));

            res.addAll(em.createQuery(cq).getResultList());
        }
        return res;
    }


    private static <T extends AbstractConfigurationObject<?>> Set<T> getRelationObjectAppliedToObjects ( EntityManager em, Class<T> implType,
            String relation, Set<StructuralObject> appliedTo, boolean nonInheritingOnly ) {
        Set<T> res = new HashSet<>();
        for ( StructuralObject obj : appliedTo ) {
            AbstractStructuralObjectImpl root = (AbstractStructuralObjectImpl) obj;
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(implType);
            Root<T> tbl = cq.from(implType);
            EntityType<T> model = tbl.getModel();
            Join<T, AbstractStructuralObjectImpl> struct = tbl.join(model.getSingularAttribute(ANCHOR, AbstractStructuralObjectImpl.class));
            EntityType<AbstractStructuralObjectImpl> structModel = em.getMetamodel().entity(AbstractStructuralObjectImpl.class);

            Predicate p = createNonOverridenRelationshipQuery(em, implType, root, cb, struct, structModel, relation);
            if ( nonInheritingOnly ) {
                p = cb.and(p, cb.isNull(tbl.get(model.getSingularAttribute("inherits", AbstractConfigurationObject.class)))); //$NON-NLS-1$
            }

            cq.where(p);
            res.addAll(em.createQuery(cq).getResultList());
        }
        return res;
    }


    private static Predicate createNonOverridenRelationshipQuery ( EntityManager em, Class<? extends AbstractConfigurationObject<?>> type,
            AbstractStructuralObjectImpl root, CriteriaBuilder cb, Path<AbstractStructuralObjectImpl> tbl,
            EntityType<AbstractStructuralObjectImpl> model, String relation ) {
        List<Tuple> relationshipOverrideRegions = getRelationshipOverrideRegions(em, root, type, relation);
        List<Predicate> excludePredicates = new LinkedList<>();

        for ( Tuple overrideRegion : relationshipOverrideRegions ) {
            excludePredicates.add(
                cb.not(
                    cb.and(
                        cb.gt(TreeUtil.getLeft(em, tbl, model), (Integer) overrideRegion.get(0)),
                        cb.lt(TreeUtil.getRight(em, tbl, model), (Integer) overrideRegion.get(1)))));
        }

        return cb.and(cb.and(excludePredicates.toArray(new Predicate[] {})), TreeUtil.createChildQuery(root, em, tbl, model));
    }


    private static <T extends AbstractConfigurationObject<?>> List<Tuple> getRelationshipOverrideRegions ( EntityManager em,
            AbstractStructuralObjectImpl root, Class<T> type, String relation ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<T> tbl = cq.from(type);

        SetJoin<AbstractConfigurationObject<?>, AbstractStructuralObjectImpl> joinSet = tbl.joinSet(relation);
        Path<AbstractStructuralObjectImpl> joinPath = (Path<AbstractStructuralObjectImpl>) joinSet.as(AbstractStructuralObjectImpl.class);

        EntityType<AbstractStructuralObjectImpl> structModel = em.getMetamodel().entity(AbstractStructuralObjectImpl.class);
        cq.where(TreeUtil.createChildQuery(root, em, joinPath, structModel));

        cq.multiselect(TreeUtil.getLeft(em, joinPath, structModel), TreeUtil.getRight(em, joinPath, structModel));
        return em.createQuery(cq).getResultList();
    }


    @Override
    @SuppressWarnings ( "rawtypes" )
    public List<AbstractConfigurationObject> getInheritedEnforcements ( EntityManager em, AbstractStructuralObjectImpl persistentAnchor ) {
        return getInheritedRelationObjects(em, persistentAnchor, ENFORCED_FOR_RELATION);
    }


    @Override
    @SuppressWarnings ( "rawtypes" )
    public List<AbstractConfigurationObject> getInheritedDefaults ( EntityManager em, AbstractStructuralObjectImpl persistentAnchor ) {
        return getInheritedRelationObjects(em, persistentAnchor, DEFAULT_FOR_RELATION);
    }


    @SuppressWarnings ( "rawtypes" )
    private static List<AbstractConfigurationObject> getInheritedRelationObjects ( EntityManager em, AbstractStructuralObjectImpl persistentAnchor,
            String relation ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AbstractConfigurationObject> cq = cb.createQuery(AbstractConfigurationObject.class);
        Root<AbstractConfigurationObject> tbl = cq.from(AbstractConfigurationObject.class);

        SetJoin<AbstractConfigurationObject<?>, AbstractStructuralObjectImpl> joinSet = tbl.joinSet(relation);
        Path<AbstractStructuralObjectImpl> joinPath = (Path<AbstractStructuralObjectImpl>) joinSet.as(AbstractStructuralObjectImpl.class);

        EntityType<AbstractStructuralObjectImpl> structModel = em.getMetamodel().entity(AbstractStructuralObjectImpl.class);
        cq.where(TreeUtil.createAncestorQueryIncludingSelf(persistentAnchor, em, joinPath, structModel));

        cq.orderBy(cb.desc(TreeUtil.getDepthPath(em, joinPath, structModel)));
        return em.createQuery(cq).getResultList();
    }

}
