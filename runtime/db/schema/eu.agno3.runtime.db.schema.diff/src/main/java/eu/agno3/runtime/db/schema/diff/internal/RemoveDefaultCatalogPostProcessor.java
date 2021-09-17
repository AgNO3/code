/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.diff.internal;


import java.util.HashSet;
import java.util.Set;

import liquibase.diff.DiffResult;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.db.schema.diff.DiffPostProcessor;


/**
 * @author mbechler
 * 
 */
@Component ( service = DiffPostProcessor.class )
public class RemoveDefaultCatalogPostProcessor implements DiffPostProcessor {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.diff.DiffPostProcessor#getPriority()
     */
    @Override
    public int getPriority () {
        return 100;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.diff.DiffPostProcessor#process(liquibase.diff.DiffResult)
     */
    @Override
    public DiffResult process ( DiffResult r ) {
        removeDefaultCatalogObjects(r, r.getMissingObjects());
        removeDefaultCatalogObjects(r, r.getUnexpectedObjects());
        return r;
    }


    /**
     * @param r
     */
    private static void removeDefaultCatalogObjects ( DiffResult r, Set<? extends DatabaseObject> objs ) {
        Set<DatabaseObject> toRemove = new HashSet<>();

        for ( DatabaseObject obj : objs ) {
            if ( isDefaultCatalog(r, obj) ) {
                toRemove.add(obj);
            }
            if ( isDefaultSchema(r, obj) ) {
                toRemove.add(obj);
            }
        }

        for ( DatabaseObject remove : toRemove ) {
            objs.remove(remove);
        }
    }


    /**
     * @param r
     * @param cat
     * @return
     */
    private static boolean isDefaultCatalog ( DiffResult r, DatabaseObject obj ) {
        if ( ! ( obj instanceof Catalog ) ) {
            return false;
        }
        Catalog cat = (Catalog) obj;
        return cat.getName() == null || cat.getName().equals(r.getComparisonSnapshot().getDatabase().getDefaultCatalogName());
    }


    /**
     * @param r
     * @param s
     * @return
     */
    private static boolean isDefaultSchema ( DiffResult r, DatabaseObject obj ) {
        if ( ! ( obj instanceof Schema ) ) {
            return false;
        }

        Schema s = (Schema) obj;
        if ( s.getCatalogName() != null && !s.getCatalogName().equals(r.getComparisonSnapshot().getDatabase().getDefaultCatalogName()) ) {
            return false;
        }

        return s.getSchema().getName() == null || s.getSchema().getName().equals(r.getComparisonSnapshot().getDatabase().getDefaultSchemaName());
    }

}
