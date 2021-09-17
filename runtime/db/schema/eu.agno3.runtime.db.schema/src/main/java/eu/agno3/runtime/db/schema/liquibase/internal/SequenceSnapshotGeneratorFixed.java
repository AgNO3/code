/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.11.2015 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import org.osgi.service.component.annotations.Component;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.jvm.SequenceSnapshotGenerator;
import liquibase.structure.core.Schema;


/**
 * @author mbechler
 *
 */
@Component ( service = SnapshotGenerator.class )
public class SequenceSnapshotGeneratorFixed extends SequenceSnapshotGenerator {

    /**
     * {@inheritDoc}
     *
     * @see liquibase.snapshot.jvm.SequenceSnapshotGenerator#getSelectSequenceSql(liquibase.structure.core.Schema,
     *      liquibase.database.Database)
     */
    @SuppressWarnings ( "nls" )
    @Override
    protected String getSelectSequenceSql ( Schema schema, Database database ) {
        if ( database instanceof LiquibaseDerbyDatabase ) {
            return "SELECT " + "  seq.SEQUENCENAME AS SEQUENCE_NAME " + "FROM " + "  SYS.SYSSEQUENCES seq, " + "  SYS.SYSSCHEMAS sch " + "WHERE "
                    + "  sch.SCHEMANAME = '" + new CatalogAndSchema(null, schema.getName()).customize(database).getCatalogName() + "' AND "
                    + "  sch.SCHEMAID = seq.SCHEMAID";
        }

        return super.getSelectSequenceSql(schema, database);
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.snapshot.jvm.JdbcSnapshotGenerator#replaces()
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public Class<? extends SnapshotGenerator>[] replaces () {
        return new Class[] {
            SequenceSnapshotGenerator.class
        };
    }

}
