/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import org.osgi.service.component.annotations.Component;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.servicelocator.PrioritizedService;


/**
 * @author mbechler
 * 
 */
@Component ( service = LiquibaseDataType.class )
@DataTypeInfo ( name = "binary", aliases = {
    "java.sql.Types.BINARY"
}, minParameters = 1, maxParameters = 1, priority = PrioritizedService.PRIORITY_DEFAULT )
public class BinaryType extends liquibase.datatype.LiquibaseDataType {

    /**
     * {@inheritDoc}
     * 
     * @see liquibase.datatype.LiquibaseDataType#toDatabaseDataType(liquibase.database.Database)
     */
    @Override
    public DatabaseDataType toDatabaseDataType ( Database database ) {
        Object[] params = getParameters();
        if ( database instanceof DerbyDatabase ) {
            DatabaseDataType t = new BinaryDatabaseDataType("CHAR", params); //$NON-NLS-1$
            t.addAdditionalInformation(" FOR BIT DATA"); //$NON-NLS-1$
            return t;
        }
        if ( database instanceof PostgresDatabase ) {
            if ( params != null && params.length > 0 && params[ 0 ] != null && Integer.parseInt((String) params[ 0 ]) == 16 ) {
                return new DatabaseDataType("UUID"); //$NON-NLS-1$
            }
            return new DatabaseDataType("BYTEA"); //$NON-NLS-1$
        }
        return super.toDatabaseDataType(database);
    }

    /**
     * @author mbechler
     * 
     */
    public static class BinaryDatabaseDataType extends DatabaseDataType {

        private int size = 255;


        /**
         * @param type
         */
        public BinaryDatabaseDataType ( String type ) {
            super(type);
        }


        /**
         * @param type
         * @param params
         */
        public BinaryDatabaseDataType ( String type, Object... params ) {
            super(type, params);
            if ( params != null && params.length > 0 && params[ 0 ] != null ) {
                this.size = Integer.parseInt((String) params[ 0 ]);
            }
        }


        /**
         * {@inheritDoc}
         * 
         * @see liquibase.datatype.DatabaseDataType#toString()
         */
        @Override
        public String toString () {
            return String.format("CHAR (%d) FOR BIT DATA", this.size); //$NON-NLS-1$
        }

    }
}
