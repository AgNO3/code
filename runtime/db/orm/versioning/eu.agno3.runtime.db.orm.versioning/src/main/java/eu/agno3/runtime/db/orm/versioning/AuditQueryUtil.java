/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.versioning;


import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;


/**
 * @author mbechler
 * 
 */
public final class AuditQueryUtil {

    private AuditQueryUtil () {}


    /**
     * @param q
     * @param entityType
     * @param revisionInfoType
     * @return a properly typed resultset for this query
     */
    public static <TEntity, TRevisionEntity> List<AuditEntry<TEntity, TRevisionEntity>> fetchAuditEntries ( AuditQuery q, Class<TEntity> entityType,
            Class<TRevisionEntity> revisionInfoType ) {

        List<AuditEntry<TEntity, TRevisionEntity>> res = new ArrayList<>();

        for ( Object[] r : (List<Object[]>) q.getResultList() ) {
            if ( r.length != 3 || ! ( entityType.isAssignableFrom(r[ 0 ].getClass()) ) || ! ( revisionInfoType.isAssignableFrom(r[ 1 ].getClass()) )
                    || ! ( r[ 2 ] instanceof RevisionType ) ) {
                throw new IllegalArgumentException("The provided resultset has an unexpected form"); //$NON-NLS-1$
            }

            @SuppressWarnings ( "unchecked" )
            AuditEntry<TEntity, TRevisionEntity> entry = new AuditEntry<>((TEntity) r[ 0 ], (TRevisionEntity) r[ 1 ], (RevisionType) r[ 2 ]);

            res.add(entry);
        }

        return res;
    }
}
