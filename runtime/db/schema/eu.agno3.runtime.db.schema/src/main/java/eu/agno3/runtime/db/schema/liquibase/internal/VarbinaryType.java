/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.servicelocator.PrioritizedService;

import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 * 
 */
@Component ( service = LiquibaseDataType.class )
@DataTypeInfo ( name = "varbinary", aliases = {
    "java.sql.Types.VARBINARY"
}, minParameters = 1, maxParameters = 1, priority = PrioritizedService.PRIORITY_DATABASE )
public class VarbinaryType extends liquibase.datatype.core.BlobType {

    /**
     * {@inheritDoc}
     * 
     * @see liquibase.datatype.LiquibaseDataType#toDatabaseDataType(liquibase.database.Database)
     */
    @Override
    public DatabaseDataType toDatabaseDataType ( Database database ) {
        if ( database instanceof DerbyDatabase ) {
            DatabaseDataType t = new VarbinaryDatabaseDataType("VARCHAR", getParameters()); //$NON-NLS-1$
            t.addAdditionalInformation(" FOR BIT DATA"); //$NON-NLS-1$
            return t;
        }
        return super.toDatabaseDataType(database);
    }

    /**
     * @author mbechler
     * 
     */
    public static class VarbinaryDatabaseDataType extends DatabaseDataType {

        private int size = 255;


        /**
         * @param type
         */
        public VarbinaryDatabaseDataType ( String type ) {
            super(type);
        }


        /**
         * @param type
         * @param params
         */
        public VarbinaryDatabaseDataType ( String type, Object... params ) {
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
            return String.format("VARCHAR (%d) FOR BIT DATA", this.size); //$NON-NLS-1$
        }

    }
}
