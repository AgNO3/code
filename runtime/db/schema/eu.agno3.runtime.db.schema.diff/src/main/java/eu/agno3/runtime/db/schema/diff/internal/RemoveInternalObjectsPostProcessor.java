/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.diff.internal;


import java.util.Map.Entry;

import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
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
public class RemoveInternalObjectsPostProcessor implements DiffPostProcessor {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.diff.DiffPostProcessor#getPriority()
     */
    @Override
    public int getPriority () {
        return Integer.MAX_VALUE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.diff.DiffPostProcessor#process(liquibase.diff.DiffResult)
     */
    @Override
    public DiffResult process ( DiffResult r ) {
        DiffResult modified = new DiffResult(r.getReferenceSnapshot(), r.getComparisonSnapshot(), r.getCompareControl());

        for ( Entry<DatabaseObject, ObjectDifferences> e : r.getChangedObjects().entrySet() ) {

            if ( isInternalObject(e.getKey()) ) {
                continue;
            }

            modified.addChangedObject(e.getKey(), e.getValue());
        }

        for ( DatabaseObject o : r.getMissingObjects() ) {

            if ( isInternalObject(o) ) {
                continue;
            }

            modified.addMissingObject(o);
        }

        for ( DatabaseObject o : r.getUnexpectedObjects() ) {

            if ( isInternalObject(o) ) {
                continue;
            }

            modified.addUnexpectedObject(o);
        }

        return modified;
    }


    /**
     * @param key
     * @return
     */
    private static boolean isInternalObject ( DatabaseObject obj ) {

        String catalogName = null;
        if ( obj instanceof Catalog ) {
            catalogName = ( (Catalog) obj ).getName();
        }
        else if ( obj instanceof Schema ) {
            catalogName = ( (Schema) obj ).getCatalogName();
        }
        else if ( obj.getSchema() != null ) {
            catalogName = obj.getSchema().getCatalogName();
        }

        return "APP_SCHEMA".equalsIgnoreCase(catalogName); //$NON-NLS-1$
    }
}
