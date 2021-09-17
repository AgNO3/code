/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.internal;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import liquibase.exception.LiquibaseException;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.ForeignKeyConstraintType;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

import org.apache.log4j.Logger;
import org.hibernate.boot.Metadata;


/**
 * @author mbechler
 * 
 */
class HibernateForeignKeyIndexer {

    private static final Logger log = Logger.getLogger(HibernateForeignKeyIndexer.class);


    /**
     * 
     */
    public HibernateForeignKeyIndexer () {}


    /**
     * @param config
     * @param index
     * 
     */
    public void indexForeignKeys ( Metadata config, HibernateIndex index ) {
        // second pass for foreign keys
        for ( org.hibernate.mapping.Table tbl : config.collectTableMappings() ) {
            Table t = index.tableToTable(tbl);

            // foreign keys
            Iterator<org.hibernate.mapping.ForeignKey> fkeyIt = tbl.getForeignKeyIterator();
            while ( fkeyIt.hasNext() ) {
                org.hibernate.mapping.ForeignKey fk = fkeyIt.next();
                Table sourceTable = index.tableToTable(fk.getReferencedTable());
                ForeignKey k = setupForeignKey(t, fk, sourceTable);

                t.getOutgoingForeignKeys().add(k);
            }
        }
    }


    /**
     * @param snap
     * @param t
     * @throws LiquibaseException
     */
    public static void exportForeignKeys ( HibernateDatabaseSnapshot snap, Table t ) throws LiquibaseException {
        for ( ForeignKey fk : t.getOutgoingForeignKeys() ) {
            snap.include(fk);
        }
    }


    /**
     * @param t
     * @param fk
     * @param sourceTable
     * @return
     */
    private static ForeignKey setupForeignKey ( Table t, org.hibernate.mapping.ForeignKey fk, Table sourceTable ) {
        ForeignKey k = new ForeignKey();

        k.setName(fk.getName().toUpperCase());
        k.setPrimaryKeyTable(sourceTable);
        k.setForeignKeyTable(t);

        List<Column> sourceColumns = getFKSourceColumns(fk);
        List<Column> targetColumns = getFKTargetColumns(fk, sourceTable);

        setupFKProperties(fk, k);

        k.setSnapshotId(UUID.randomUUID().toString());
        k.setPrimaryKeyColumns(targetColumns);
        k.setForeignKeyColumns(sourceColumns);

        Index backingIndex = createFKBackingIndex(k, sourceColumns);

        k.setBackingIndex(backingIndex);

        if ( log.isTraceEnabled() ) {
            log.trace("Add foreign key " + k); //$NON-NLS-1$
        }

        return k;
    }


    /**
     * @param fk
     * @param k
     */
    private static void setupFKProperties ( org.hibernate.mapping.ForeignKey fk, ForeignKey k ) {
        k.setDeferrable(false);
        k.setInitiallyDeferred(false);

        if ( fk.isCascadeDeleteEnabled() ) {
            k.setDeleteRule(ForeignKeyConstraintType.importedKeyCascade);
        }
        else {
            k.setDeleteRule(ForeignKeyConstraintType.importedKeyNoAction);
        }

        k.setUpdateRule(ForeignKeyConstraintType.importedKeyNoAction);
    }


    /**
     * @param k
     * @param sourceColumns
     * @return
     */
    private static Index createFKBackingIndex ( ForeignKey k, List<Column> sourceColumns ) {
        Index backingIndex = new Index();
        backingIndex.setTable(k.getForeignKeyTable());
        backingIndex.getColumns().addAll(sourceColumns);
        backingIndex.setName("FK_" + k.getName()); //$NON-NLS-1$

        backingIndex.setUnique(false);
        return backingIndex;
    }


    /**
     * @param fk
     * @param sourceTable
     * @return
     */
    private static List<Column> getFKTargetColumns ( org.hibernate.mapping.ForeignKey fk, Table sourceTable ) {
        List<Column> targetColumns = new LinkedList<>();
        if ( fk.isReferenceToPrimaryKey() ) {
            targetColumns.addAll(sourceTable.getPrimaryKey().getColumns());
        }
        else {

            for ( org.hibernate.mapping.Column targetColumn : (Iterable<org.hibernate.mapping.Column>) fk.getReferencedColumns() ) {
                targetColumns.add(new Column(targetColumn.getName()));
            }
        }
        return targetColumns;
    }


    /**
     * @param fk
     * @return
     */
    private static List<Column> getFKSourceColumns ( org.hibernate.mapping.ForeignKey fk ) {
        List<Column> sourceColumns = new LinkedList<>();
        Iterator<org.hibernate.mapping.Column> sourceColIt = fk.getColumnIterator();

        while ( sourceColIt.hasNext() ) {
            sourceColumns.add(new Column(sourceColIt.next().getName()));
        }
        return sourceColumns;
    }

}
