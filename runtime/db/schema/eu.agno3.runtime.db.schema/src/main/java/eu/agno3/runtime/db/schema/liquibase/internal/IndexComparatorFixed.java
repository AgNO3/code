/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import java.util.Set;

import org.osgi.service.component.annotations.Component;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.core.IndexComparator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;


/**
 * @author mbechler
 * 
 */
@Component ( service = DatabaseObjectComparator.class )
public class IndexComparatorFixed extends IndexComparator {

    /**
     * 
     */
    private static final String ASSOCIATED_WITH = "associatedWith"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String GENERATED_PREFIX = "SQL"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.diff.compare.DatabaseObjectComparator#getPriority(java.lang.Class, liquibase.database.Database)
     */
    @Override
    public int getPriority ( Class<? extends DatabaseObject> obj, Database db ) {
        if ( obj.isAssignableFrom(Index.class) ) {
            return Integer.MAX_VALUE;
        }

        return -1;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.diff.compare.core.IndexComparator#isSameObject(liquibase.structure.DatabaseObject,
     *      liquibase.structure.DatabaseObject, liquibase.database.Database,
     *      liquibase.diff.compare.DatabaseObjectComparatorChain)
     */
    @Override
    public boolean isSameObject ( DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo,
            DatabaseObjectComparatorChain chain ) {

        if ( databaseObject1 instanceof Index && databaseObject2 instanceof Index ) {
            Index i1 = (Index) databaseObject1;
            Index i2 = (Index) databaseObject2;

            if ( needsWorkaround(i1, i2) ) {
                // workaround, this may be a generated index enforcing a constraint ... but we do not know
                return true;
            }

            if ( !nullSafeEquals(i1.isUnique(), i2.isUnique()) ) {
                return false;
            }
        }

        return super.isSameObject(databaseObject1, databaseObject2, accordingTo, chain);
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.diff.compare.core.IndexComparator#findDifferences(liquibase.structure.DatabaseObject,
     *      liquibase.structure.DatabaseObject, liquibase.database.Database, liquibase.diff.compare.CompareControl,
     *      liquibase.diff.compare.DatabaseObjectComparatorChain, java.util.Set)
     */
    @Override
    public ObjectDifferences findDifferences ( DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo,
            CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude ) {
        ObjectDifferences diff = super.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, chain, exclude);

        if ( databaseObject1 instanceof Index && databaseObject2 instanceof Index ) {
            Index i1 = (Index) databaseObject1;
            Index i2 = (Index) databaseObject2;

            if ( needsWorkaround(i1, i2) ) {
                diff.removeDifference("name"); //$NON-NLS-1$
                diff.removeDifference("unique"); //$NON-NLS-1$
                diff.removeDifference(ASSOCIATED_WITH);
            }

            if ( diff.isDifferent(ASSOCIATED_WITH) && diff.getDifferences().size() == 1 ) {
                diff.removeDifference(ASSOCIATED_WITH);
            }
        }

        return diff;
    }


    private static boolean needsWorkaround ( Index i1, Index i2 ) {
        if ( i1 == null || i2 == null ) {
            return false;
        }
        return !nullSafeEquals(i1.isUnique(), i2.isUnique()) && oneIsGenerated(i1, i2) && i1.getColumns().equals(i2.getColumns());
    }


    private static boolean oneIsGenerated ( Index i1, Index i2 ) {
        return ( i1.getName() != null && i2.getName() != null )
                && ( i1.getName().startsWith(GENERATED_PREFIX) ^ i2.getName().startsWith(GENERATED_PREFIX) );
    }


    /**
     * @param unique
     * @param unique2
     * @return
     */
    private static boolean nullSafeEquals ( Boolean a, Boolean b ) {
        if ( a == null && b == null ) {
            return true;
        }
        else if ( a == null || b == null ) {
            return false;
        }
        return a.equals(b);
    }

}
