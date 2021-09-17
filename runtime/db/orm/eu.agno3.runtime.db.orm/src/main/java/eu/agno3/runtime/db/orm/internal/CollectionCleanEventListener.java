/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.08.2016 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.internal.DefaultMergeEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;


/**
 * 
 * 
 * On merge checks whether source and target collection do actually equal.
 * 
 * @author mbechler
 *
 */
@SuppressWarnings ( "rawtypes" )
class CollectionCleanEventListener extends DefaultMergeEventListener {

    /**
     * 
     */
    private static final long serialVersionUID = 4231556752760907237L;

    private static final Logger log = Logger.getLogger(CollectionCleanEventListener.class);


    /**
     * 
     */
    public CollectionCleanEventListener () {}


    @Override
    protected void copyValues ( EntityPersister persister, Object entity, Object target, SessionImplementor source, Map copyCache ) {
        List<PersistentCollection> cleanCollections = new ArrayList<>();
        Object[] origValues = persister.getPropertyValues(entity);
        Object[] targetValues = persister.getPropertyValues(target);
        Type[] types = persister.getPropertyTypes();

        for ( int i = 0; i < types.length; i++ ) {
            boolean collectionType = types[ i ].isCollectionType();
            if ( collectionType && targetValues[ i ] instanceof PersistentCollection ) {
                PersistentCollection targetValue = (PersistentCollection) targetValues[ i ];
                if ( origValues[ i ] instanceof Map ) {
                    checkMap((Map) origValues[ i ], targetValue, cleanCollections);
                }
                else {
                    checkCollection((Collection) origValues[ i ], targetValue, cleanCollections);
                }
            }
            else if ( collectionType && log.isDebugEnabled() ) {
                log.debug("Not a persistent collection " + types[ i ]); //$NON-NLS-1$
            }
        }

        super.copyValues(persister, entity, target, source, copyCache);

        for ( PersistentCollection col : cleanCollections ) {
            if ( col.isDirty() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Marking collection as clean " + col.getRole()); //$NON-NLS-1$
                }
                col.clearDirty();
            }
        }
    }


    /**
     * @param collection
     * @param targetValue
     * @param cleanCollections
     */
    private static void checkMap ( Map origValue, PersistentCollection targetValue, List<PersistentCollection> cleanCollections ) {
        Map target = targetValue instanceof Map ? (Map) targetValue.getValue() : null;

        if ( ( origValue == null || origValue.isEmpty() ) && ( target == null || target.isEmpty() ) ) {
            cleanCollections.add(targetValue);
            return;
        }
        else if ( origValue == null || target == null ) {
            return;
        }
        else if ( origValue.size() != target.size() ) {
            return;
        }
        else if ( CollectionUtils.isEqualCollection(origValue.entrySet(), target.entrySet()) ) {
            cleanCollections.add(targetValue);
        }
    }


    private static void checkCollection ( Collection origValue, PersistentCollection targetValue,
            Collection<PersistentCollection> cleanCollections ) {

        Collection target = targetValue instanceof Collection ? (Collection) targetValue.getValue() : null;

        if ( ( origValue == null || origValue.isEmpty() ) && ( target == null || target.isEmpty() ) ) {
            cleanCollections.add(targetValue);
            return;
        }
        else if ( origValue == null || target == null ) {
            return;
        }
        else if ( origValue.size() != target.size() ) {
            return;
        }
        else if ( CollectionUtils.isEqualCollection(origValue, target) ) {
            cleanCollections.add(targetValue);
        }
    }
}