/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.server.tree;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.annotations.QueryHints;

import eu.agno3.orchestrator.config.model.base.tree.NestedSetNodeImpl;
import eu.agno3.orchestrator.config.model.base.tree.TreeNodeHolder;


/**
 * @author mbechler
 * 
 */
public final class TreeUtil {

    /**
     * 
     */
    private static final String INVALID_TREE_NODES = "Invalid tree nodes provided"; //$NON-NLS-1$
    private static final String TREE_NODE = "treeNode"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String RIGHT = "right"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String LEFT = "left"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String DEPTH = "depth"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(TreeUtil.class);


    /**
     * 
     */
    private TreeUtil () {}


    /**
     * @param em
     * @param treeType
     * @return the tree node
     */
    public static <T extends TreeNodeHolder> Optional<T> findRoot ( EntityManager em, Class<T> treeType ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> q = cb.createQuery(treeType);
        Root<T> from = q.from(treeType);
        // root always has left == 1
        q.where(cb.equal(getTreeNode(from).get(getTreeModel(em).getSingularAttribute(LEFT, Integer.class)), 1));

        try {
            @Nullable
            T singleResult = em.createQuery(q).setHint(QueryHints.CACHEABLE, true).getSingleResult();

            if ( singleResult == null ) {
                return Optional.empty();
            }

            return Optional.of(singleResult);
        }
        catch ( NoResultException e ) {
            log.trace("No tree root found", e); //$NON-NLS-1$
            return Optional.empty();
        }
    }


    private static EmbeddableType<NestedSetNodeImpl> getTreeModel ( EntityManager em ) {
        return em.getMetamodel().embeddable(NestedSetNodeImpl.class);
    }


    /**
     * 
     * @param em
     * @param from
     * @param model
     * @return path to the left value
     */
    public static <T extends TreeNodeHolder> Path<Integer> getLeft ( EntityManager em, Path<T> from, EntityType<T> model ) {
        return getPropertyPath(em, from, model, LEFT);
    }


    /**
     * 
     * @param em
     * @param from
     * @param model
     * @return path to the right value
     */
    public static <T extends TreeNodeHolder> Path<Integer> getRight ( EntityManager em, Path<T> from, EntityType<T> model ) {
        return getPropertyPath(em, from, model, RIGHT);
    }


    /**
     * 
     * @param em
     * @param from
     * @param model
     * @return the path to the depth value
     */
    public static <T extends TreeNodeHolder> Path<Integer> getDepthPath ( EntityManager em, Path<T> from, EntityType<T> model ) {
        return getPropertyPath(em, from, model, DEPTH);
    }


    private static <T extends TreeNodeHolder> Path<Integer> getPropertyPath ( EntityManager em, Path<T> from, EntityType<T> model, String prop ) {
        return getTreeNode(from, model).get(getTreeModel(em).getSingularAttribute(prop, Integer.class));
    }


    /**
     * @param from
     * @return the path the the tree node
     */
    public static <T extends TreeNodeHolder> Path<NestedSetNodeImpl> getTreeNode ( Root<T> from ) {
        return getTreeNode(from, from.getModel());
    }


    /**
     * 
     * @param from
     * @param model
     * @return the path the the tree node
     */
    public static <T extends TreeNodeHolder> Path<NestedSetNodeImpl> getTreeNode ( Path<T> from, EntityType<T> model ) {
        return from.get(model.getSingularAttribute(TREE_NODE, NestedSetNodeImpl.class));
    }


    /**
     * 
     * @param em
     * @param treeType
     * @param obj
     * @return all (transitive) children of obj
     */
    public static <T extends TreeNodeHolder> List<T> getChildren ( EntityManager em, Class<T> treeType, T obj ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> q = cb.createQuery(treeType);
        Root<T> from = q.from(treeType);

        // child iff parent.left + 1 <= n.left, n.right <= parent.right - 1
        q.where(cb.and(
            cb.between(
                getTreeNode(from).get(getTreeModel(em).getSingularAttribute(LEFT, Integer.class)),
                cb.parameter(Integer.class, LEFT),
                cb.parameter(Integer.class, RIGHT)),
            cb.between(
                getTreeNode(from).get(getTreeModel(em).getSingularAttribute(RIGHT, Integer.class)),
                cb.parameter(Integer.class, LEFT),
                cb.parameter(Integer.class, RIGHT))));

        return em.createQuery(q).setParameter(LEFT, obj.getTreeNode().getLeft() + 1).setParameter(RIGHT, obj.getTreeNode().getRight() - 1)
                .getResultList();
    }


    /**
     * 
     * @param em
     * @param treeType
     * @param obj
     * @return the direct children of obj
     */
    public static <T extends TreeNodeHolder> List<T> getDirectChildren ( EntityManager em, Class<T> treeType, T obj ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> q = cb.createQuery(treeType);
        Root<T> from = q.from(treeType);

        // direct child iff parent.left + 1 <= n.left, n.right <= parent.right - 1
        // and n.depth = parent.depth + 1
        q.where(cb.and(
            cb.between(
                getTreeNode(from).get(getTreeModel(em).getSingularAttribute(LEFT, Integer.class)),
                cb.parameter(Integer.class, LEFT),
                cb.parameter(Integer.class, RIGHT)),
            cb.between(
                getTreeNode(from).get(getTreeModel(em).getSingularAttribute(RIGHT, Integer.class)),
                cb.parameter(Integer.class, LEFT),
                cb.parameter(Integer.class, RIGHT)),
            cb.equal(getTreeNode(from).get(getTreeModel(em).getSingularAttribute(DEPTH, Integer.class)), cb.parameter(Integer.class, DEPTH))));

        return em.createQuery(q).setParameter(LEFT, obj.getTreeNode().getLeft() + 1).setParameter(RIGHT, obj.getTreeNode().getRight() - 1)
                .setParameter(DEPTH, obj.getTreeNode().getDepth() + 1).getResultList();
    }


    /**
     * 
     * @param em
     * @param treeType
     * @param obj
     * @return the ancestors of obj, ordered by descending depth
     */
    public static <T extends TreeNodeHolder> List<T> getAncestors ( EntityManager em, Class<T> treeType, T obj ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> q = cb.createQuery(treeType);
        Root<T> from = q.from(treeType);

        q.where(createAncestorQuery(em, from, from.getModel()));

        // order by descending depth => list will contain the sequence up to the root
        q.orderBy(cb.desc(getDepthPath(em, from, from.getModel())));

        return em.createQuery(q).setParameter(LEFT, obj.getTreeNode().getLeft()).setParameter(RIGHT, obj.getTreeNode().getRight()).getResultList();
    }


    /**
     * 
     * Query params left, right
     * 
     * @param em
     * @param path
     * @param model
     * @return a query that selects the ancestors (excluding self) of the given node
     */
    public static <T extends TreeNodeHolder> Predicate createAncestorQuery ( EntityManager em, Path<T> path, EntityType<T> model ) {
        Path<NestedSetNodeImpl> treePath = path.get(model.getSingularAttribute(TREE_NODE, NestedSetNodeImpl.class));
        EmbeddableType<NestedSetNodeImpl> treeModel = em.getMetamodel().embeddable(NestedSetNodeImpl.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Predicate ancestorQuery = cb.and(
            cb.lt(treePath.get(treeModel.getSingularAttribute(LEFT, Integer.class)), cb.parameter(Integer.class, LEFT)),
            cb.gt(treePath.get(treeModel.getSingularAttribute(RIGHT, Integer.class)), cb.parameter(Integer.class, RIGHT)));
        return ancestorQuery;
    }


    /**
     * 
     * @param obj
     * @param em
     * @param path
     * @param model
     * @return a query that selects the ancestors (including self) of the given node
     */
    public static <T extends TreeNodeHolder> Predicate createAncestorQueryIncludingSelf ( TreeNodeHolder obj, EntityManager em, Path<T> path,
            EntityType<T> model ) {
        Path<NestedSetNodeImpl> treePath = path.get(model.getSingularAttribute(TREE_NODE, NestedSetNodeImpl.class));
        EmbeddableType<NestedSetNodeImpl> treeModel = em.getMetamodel().embeddable(NestedSetNodeImpl.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        int l = obj.getTreeNode().getLeft();
        int r = obj.getTreeNode().getRight();
        Predicate ancestorQuery = cb.and(
            cb.le(treePath.get(treeModel.getSingularAttribute(LEFT, Integer.class)), l),
            cb.ge(treePath.get(treeModel.getSingularAttribute(RIGHT, Integer.class)), r));
        return ancestorQuery;
    }


    /**
     * 
     * @param obj
     * @param em
     * @param path
     * @param model
     * @return a query that selects the ancestors (including self) of the given node
     */
    public static <T extends TreeNodeHolder> Predicate createChildQuery ( TreeNodeHolder obj, EntityManager em, Path<T> path, EntityType<T> model ) {
        Path<NestedSetNodeImpl> treePath = path.get(model.getSingularAttribute(TREE_NODE, NestedSetNodeImpl.class));
        EmbeddableType<NestedSetNodeImpl> treeModel = em.getMetamodel().embeddable(NestedSetNodeImpl.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        int l = obj.getTreeNode().getLeft();
        int r = obj.getTreeNode().getRight();
        Predicate ancestorQuery = cb.and(
            cb.gt(treePath.get(treeModel.getSingularAttribute(LEFT, Integer.class)), l),
            cb.lt(treePath.get(treeModel.getSingularAttribute(RIGHT, Integer.class)), r));
        return ancestorQuery;
    }


    /**
     * 
     * @param em
     * @param treeType
     * @param node
     * @return the parent node
     */
    public static <T extends TreeNodeHolder> Optional<? extends T> getParent ( EntityManager em, Class<T> treeType, T node ) {

        if ( node == null || node.getTreeNode().getLeft() == 1 ) {
            // this is the root
            return Optional.empty();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> q = cb.createQuery(treeType);
        Root<T> from = q.from(treeType);

        // child iff parent.left + 1 <= n.left, n.right <= parent.right - 1
        int l = node.getTreeNode().getLeft();
        int r = node.getTreeNode().getRight();
        int d = node.getTreeNode().getDepth();
        q.where(
            cb.and(
                cb.lt(getTreeNode(from).get(getTreeModel(em).getSingularAttribute(LEFT, Integer.class)), l),
                cb.gt(getTreeNode(from).get(getTreeModel(em).getSingularAttribute(RIGHT, Integer.class)), r)),
            cb.equal(getTreeNode(from).get(getTreeModel(em).getSingularAttribute(DEPTH, Integer.class)), d - 1));

        T singleResult = em.createQuery(q).getSingleResult();

        if ( singleResult == null ) {
            return Optional.empty();
        }

        return Optional.of(singleResult);
    }


    /**
     * 
     * @param em
     * @param treeType
     * @param parent
     * @param toInsert
     */
    public static <T extends TreeNodeHolder> void treeInsert ( EntityManager em, Class<T> treeType, T parent, T toInsert ) {

        if ( parent == null ) {
            log.debug("Creating tree root"); //$NON-NLS-1$
            toInsert.getTreeNode().setDepth(0);
            toInsert.getTreeNode().setLeft(1);
            toInsert.getTreeNode().setRight(2);
            return;
        }

        treeMultiInsert(em, treeType, parent, Arrays.asList(toInsert));
    }


    /**
     * @param em
     * @param treeType
     * @param parent
     * @param objs
     */
    public static <T extends TreeNodeHolder> void treeMultiInsert ( EntityManager em, Class<T> treeType, T parent, List<T> objs ) {

        if ( parent == null ) {
            throw new IllegalArgumentException("Parent must not be null"); //$NON-NLS-1$
        }

        int newDepth = parent.getTreeNode().getDepth() + 1;
        int parentRight = parent.getTreeNode().getRight();

        int pos = parentRight;

        for ( T toInsert : objs ) {
            toInsert.getTreeNode().setDepth(newDepth);
            toInsert.getTreeNode().setLeft(pos);
            toInsert.getTreeNode().setRight(pos + 1);
            pos += 2;
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<T> qleft = cb.createCriteriaUpdate(treeType);
        CriteriaUpdate<T> qright = cb.createCriteriaUpdate(treeType);

        Root<T> leftTbl = qleft.from(treeType);
        Root<T> rightTbl = qright.from(treeType);

        SingularAttribute<? super NestedSetNodeImpl, Integer> leftAttr = getTreeModel(em).getSingularAttribute(LEFT, Integer.class);
        SingularAttribute<? super NestedSetNodeImpl, Integer> rightAttr = getTreeModel(em).getSingularAttribute(RIGHT, Integer.class);

        qleft.set(getTreeNode(leftTbl).get(leftAttr), cb.sum(getTreeNode(leftTbl).get(leftAttr), 2 * objs.size()));
        qleft.where(cb.ge(getTreeNode(leftTbl).get(leftAttr), parentRight));

        qright.set(getTreeNode(rightTbl).get(rightAttr), cb.sum(getTreeNode(rightTbl).get(rightAttr), 2 * objs.size()));
        qright.where(cb.ge(getTreeNode(rightTbl).get(rightAttr), parentRight));

        Query leftUpdate = em.createQuery(qleft);
        Query rightUpdate = em.createQuery(qright);

        leftUpdate.executeUpdate();
        rightUpdate.executeUpdate();
    }


    /**
     * @param em
     * @param treeType
     * @param persistent
     */
    public static <T extends TreeNodeHolder> void removeSubtree ( EntityManager em, Class<T> treeType, T persistent ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        int l = persistent.getTreeNode().getLeft();
        int r = persistent.getTreeNode().getRight();
        int width = r - l + 1;

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Removing %d nodes between %d and %d", width / 2, l, r)); //$NON-NLS-1$
        }

        CriteriaQuery<T> delete = cb.createQuery(treeType);
        CriteriaUpdate<T> qleft = cb.createCriteriaUpdate(treeType);
        CriteriaUpdate<T> qright = cb.createCriteriaUpdate(treeType);
        Root<T> deleteTbl = delete.from(treeType);
        Root<T> leftTbl = qleft.from(treeType);
        Root<T> rightTbl = qright.from(treeType);
        SingularAttribute<? super NestedSetNodeImpl, Integer> leftAttr = getTreeModel(em).getSingularAttribute(LEFT, Integer.class);
        SingularAttribute<? super NestedSetNodeImpl, Integer> rightAttr = getTreeModel(em).getSingularAttribute(RIGHT, Integer.class);

        delete.where(cb.and(cb.between(getTreeNode(deleteTbl).get(leftAttr), l, r), cb.between(getTreeNode(deleteTbl).get(rightAttr), l, r)));

        qleft.set(getTreeNode(leftTbl).get(leftAttr), cb.diff(getTreeNode(leftTbl).get(leftAttr), width));
        qleft.where(cb.gt(getTreeNode(leftTbl).get(leftAttr), r));

        qright.set(getTreeNode(rightTbl).get(rightAttr), cb.diff(getTreeNode(rightTbl).get(rightAttr), width));
        qright.where(cb.gt(getTreeNode(rightTbl).get(rightAttr), r));

        TypedQuery<T> deleteQuery = em.createQuery(delete);
        Query leftUpdate = em.createQuery(qleft);
        Query rightUpdate = em.createQuery(qright);

        // a delete query wont cascade, causing foreign key errors
        for ( T obj : deleteQuery.getResultList() ) {
            em.remove(obj);
        }

        leftUpdate.executeUpdate();
        rightUpdate.executeUpdate();

    }


    /**
     * 
     * @param base
     * @param check
     * @return whether base is an ancestor of check
     */
    public static boolean isAncestor ( TreeNodeHolder base, TreeNodeHolder check ) {
        if ( !isValidNode(base) || !isValidNode(check) ) {
            log.debug(INVALID_TREE_NODES);
            return false;
        }
        return base.getTreeNode().getLeft() < check.getTreeNode().getLeft() && base.getTreeNode().getRight() > check.getTreeNode().getRight();
    }


    /**
     * @param base
     * @param check
     * @return whether base in an ancestor of check or the same node
     */
    public static boolean isAncestorOrSame ( TreeNodeHolder base, TreeNodeHolder check ) {
        if ( !isValidNode(base) || !isValidNode(check) ) {
            log.debug(INVALID_TREE_NODES);
            return false;
        }
        return base.getTreeNode().getLeft() <= check.getTreeNode().getLeft() && base.getTreeNode().getRight() >= check.getTreeNode().getRight();
    }


    private static boolean isValidNode ( TreeNodeHolder node ) {
        return ! ( node == null || node.getTreeNode() == null );
    }

}
