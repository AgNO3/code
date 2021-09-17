/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.04.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import java.util.Set;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.core.ColumnComparator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;

import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 * 
 */
@Component ( service = DatabaseObjectComparator.class )
public class ColumnComparatorFixed extends ColumnComparator {

    private static final DatabaseFunction GENERATED_BY_DEFAULT = new DatabaseFunction("GENERATED_BY_DEFAULT"); //$NON-NLS-1$
    private static final String DEFAULT_VALUE = "defaultValue"; //$NON-NLS-1$
    private static final String TYPE = "type"; //$NON-NLS-1$
    private static final String NAME = "name"; //$NON-NLS-1$


    @Override
    public int getPriority ( Class<? extends DatabaseObject> objectType, Database database ) {
        if ( Column.class.isAssignableFrom(objectType) ) {
            return 1000;
        }
        return PRIORITY_NONE;
    }


    @Override
    public ObjectDifferences findDifferences ( DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo,
            CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude ) {
        exclude.add(NAME);
        exclude.add(TYPE);
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclude);

        Column c1 = (Column) databaseObject1;
        Column c2 = (Column) databaseObject2;

        if ( differences.isDifferent(DEFAULT_VALUE)
                && ( GENERATED_BY_DEFAULT.equals(c1.getDefaultValue()) || GENERATED_BY_DEFAULT.equals(c2.getDefaultValue()) ) ) {
            differences.removeDifference(DEFAULT_VALUE);
        }

        differences.removeDifference(NAME);
        differences.removeDifference(TYPE);

        differences.compare(
            NAME,
            databaseObject1,
            databaseObject2,
            new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        differences.compare(TYPE, databaseObject1, databaseObject2, new FixedDataTypeComparator(accordingTo));

        return differences;
    }

    private class FixedDataTypeComparator extends ObjectDifferences.DataTypeCompareFunction {

        private static final String JAVA_SQL_TYPES_VARBINARY = "java.sql.Types.VARBINARY"; //$NON-NLS-1$
        private static final String JAVA_SQL_TYPES_BINARY = "java.sql.Types.BINARY"; //$NON-NLS-1$
        private static final String VARCHAR_FOR_BIT_DATA = "VARCHAR () FOR BIT DATA"; //$NON-NLS-1$
        private static final String CHAR_FOR_BIT_DATA = "CHAR () FOR BIT DATA"; //$NON-NLS-1$


        /**
         * @param accordingTo
         */
        public FixedDataTypeComparator ( Database accordingTo ) {
            super(accordingTo);
        }


        /**
         * {@inheritDoc}
         * 
         * @see liquibase.diff.ObjectDifferences.DataTypeCompareFunction#areEqual(java.lang.Object, java.lang.Object)
         */
        @Override
        public boolean areEqual ( Object referenceValue, Object compareToValue ) {

            if ( referenceValue == null && compareToValue == null ) {
                return true;
            }
            if ( referenceValue == null || compareToValue == null ) {
                return false;
            }

            DataType referenceType = (DataType) referenceValue;
            DataType compareToType = (DataType) compareToValue;

            if ( needsVarbinaryFix(referenceType, compareToType) || needsBinaryFix(referenceType, compareToType) ) {
                return referenceType.getColumnSize().equals(compareToType.getColumnSize());
            }

            return super.areEqual(referenceValue, compareToValue);
        }


        private boolean needsVarbinaryFix ( DataType referenceType, DataType compareToType ) {
            return VARCHAR_FOR_BIT_DATA.equalsIgnoreCase(referenceType.getTypeName())
                    && JAVA_SQL_TYPES_VARBINARY.equalsIgnoreCase(compareToType.getTypeName())
                    || ( VARCHAR_FOR_BIT_DATA.equalsIgnoreCase(compareToType.getTypeName()) && JAVA_SQL_TYPES_VARBINARY
                            .equalsIgnoreCase(referenceType.getTypeName()) );
        }


        private boolean needsBinaryFix ( DataType referenceType, DataType compareToType ) {
            return CHAR_FOR_BIT_DATA.equalsIgnoreCase(referenceType.getTypeName())
                    && JAVA_SQL_TYPES_BINARY.equalsIgnoreCase(compareToType.getTypeName())
                    || ( CHAR_FOR_BIT_DATA.equalsIgnoreCase(compareToType.getTypeName()) && JAVA_SQL_TYPES_BINARY.equalsIgnoreCase(referenceType
                            .getTypeName()) );
        }
    }

}
