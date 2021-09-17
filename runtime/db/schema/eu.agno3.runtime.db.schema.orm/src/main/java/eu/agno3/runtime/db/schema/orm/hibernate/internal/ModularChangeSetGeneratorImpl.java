/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.internal;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.db.schema.orm.hibernate.ModularChangeSetGenerator;

import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ModularChangeSetGenerator.class
} )
public class ModularChangeSetGeneratorImpl implements ModularChangeSetGenerator {

    private static final Logger log = Logger.getLogger(ModularChangeSetGeneratorImpl.class);


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.orm.hibernate.ModularChangeSetGenerator#splitDiff(liquibase.diff.DiffResult)
     */
    @Override
    public Map<String, DiffResult> splitDiff ( DiffResult result ) {
        Map<String, DiffResult> res = new HashMap<>();
        Set<String> owners = new HashSet<>();

        Map<String, Map<DatabaseObject, ObjectDifferences>> changes = new HashMap<>();
        Map<String, List<DatabaseObject>> created = new HashMap<>();
        Map<String, List<DatabaseObject>> removed = new HashMap<>();

        changes.put(null, new HashMap<DatabaseObject, ObjectDifferences>());
        created.put(null, new ArrayList<DatabaseObject>());
        removed.put(null, new ArrayList<DatabaseObject>());

        processChanges(result, owners, changes);

        processMissing(result, owners, created);

        processUnexpected(result, owners, removed);

        res.put(null, makePerOwnerResult(result, null, changes, created, removed));

        for ( String owner : owners ) {
            res.put(owner, makePerOwnerResult(result, owner, changes, created, removed));
        }

        return res;
    }


    /**
     * @param result
     * @param owners
     * @param removed
     */
    private static void processUnexpected ( DiffResult result, Set<String> owners, Map<String, List<DatabaseObject>> removed ) {
        for ( DatabaseObject obj : result.getUnexpectedObjects() ) {

            String owner = getOwner(obj);
            if ( owner != null ) {
                owners.add(owner);
            }

            if ( !removed.containsKey(owner) ) {
                removed.put(owner, new ArrayList<DatabaseObject>());
            }

            removed.get(owner).add(obj);
        }
    }


    /**
     * @param result
     * @param owners
     * @param created
     */
    private static void processMissing ( DiffResult result, Set<String> owners, Map<String, List<DatabaseObject>> created ) {
        for ( DatabaseObject obj : result.getMissingObjects() ) {
            String owner = getOwner(obj);
            if ( owner != null ) {
                owners.add(owner);
            }

            if ( !created.containsKey(owner) ) {
                created.put(owner, new ArrayList<DatabaseObject>());
            }

            created.get(owner).add(obj);
        }
    }


    /**
     * @param result
     * @param owners
     * @param changes
     */
    private static void processChanges ( DiffResult result, Set<String> owners, Map<String, Map<DatabaseObject, ObjectDifferences>> changes ) {
        for ( Entry<DatabaseObject, ObjectDifferences> e : result.getChangedObjects().entrySet() ) {
            String owner = getOwner(e.getKey());
            if ( owner != null ) {
                owners.add(owner);
            }

            if ( !changes.containsKey(owner) ) {
                changes.put(owner, new HashMap<DatabaseObject, ObjectDifferences>());
            }

            changes.get(owner).put(e.getKey(), e.getValue());
        }
    }


    /**
     * @param obj
     * @return
     */
    private static String getOwner ( DatabaseObject obj ) {

        Relation t = null;

        if ( obj instanceof Catalog || obj instanceof Schema ) {
            return null;
        }
        else if ( obj instanceof Table ) {
            t = (Table) obj;
        }
        else if ( obj instanceof Column ) {
            t = ( (Column) obj ).getRelation();
        }
        else if ( obj instanceof ForeignKey ) {
            t = ( (ForeignKey) obj ).getForeignKeyTable();
        }
        else if ( obj instanceof PrimaryKey ) {
            t = ( (PrimaryKey) obj ).getTable();
        }
        else if ( obj instanceof Index ) {
            t = ( (Index) obj ).getTable();
        }
        else if ( obj instanceof UniqueConstraint ) {
            t = ( (UniqueConstraint) obj ).getTable();
        }
        else {
            log.error("Unhandled object type " + obj.getClass().getName()); //$NON-NLS-1$
        }

        return getOwnerAttribute(t);
    }


    /**
     * @param t
     * @return
     */
    private static String getOwnerAttribute ( Relation t ) {
        String owner;
        if ( t == null ) {
            owner = null;
        }
        else {
            owner = t.getAttribute("owner", String.class); //$NON-NLS-1$
        }

        return owner;
    }


    private static DiffResult makePerOwnerResult ( DiffResult orig, String owner, Map<String, Map<DatabaseObject, ObjectDifferences>> changes,
            Map<String, List<DatabaseObject>> created, Map<String, List<DatabaseObject>> removed ) {
        DiffResult ownerResult = new DiffResult(orig.getReferenceSnapshot(), orig.getComparisonSnapshot(), orig.getCompareControl());

        if ( changes.containsKey(owner) ) {
            for ( Entry<DatabaseObject, ObjectDifferences> e : changes.get(owner).entrySet() ) {
                ownerResult.addChangedObject(e.getKey(), e.getValue());
            }
        }

        if ( created.containsKey(owner) ) {
            for ( DatabaseObject obj : created.get(owner) ) {
                ownerResult.addMissingObject(obj);
            }
        }

        if ( removed.containsKey(owner) ) {
            for ( DatabaseObject obj : removed.get(owner) ) {
                ownerResult.addUnexpectedObject(obj);
            }
        }

        return ownerResult;
    }

}
