/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.internal;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import liquibase.exception.LiquibaseException;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.sql.Alias;

import eu.agno3.runtime.db.schema.orm.hibernate.HibernateIndexingException;


/**
 * @author mbechler
 * 
 */
class HibernateTableIndexer {

    private static final Logger log = Logger.getLogger(HibernateTableIndexer.class);

    private Metadata config;

    private static final int PKNAMELENGTH = 63;
    private static final String PK = "PK"; //$NON-NLS-1$
    private static final Alias PK_ALIAS_15 = new Alias(15, PK);
    private static final Alias NEW_PK_ALIAS = new Alias(PKNAMELENGTH, PK);


    /**
     * 
     * @param config
     * 
     */
    public HibernateTableIndexer ( Metadata config ) {
        this.config = config;
    }


    /**
     * Run the indexing process for the given table
     * 
     * @param s
     * @param tbl
     * @param t
     * @param mapping
     * @param columnIndexer
     * @throws HibernateIndexingException
     */
    public void indexTable ( Schema s, org.hibernate.mapping.Table tbl, Table t, Mapping mapping, HibernateColumnIndexer columnIndexer )
            throws HibernateIndexingException {
        s.addDatabaseObject(t);
        String idCol = getIdColumn(tbl);
        columnIndexer.mapColumns(tbl, t, idCol);

        setupIndices(tbl, t);
        setupConstraints(tbl);
        setupUniqueKeys(tbl, t);
        setupPrimaryKey(tbl, t);
    }


    /**
     * @param snap
     * @param t
     * @throws LiquibaseException
     */
    public static void exportTable ( HibernateDatabaseSnapshot snap, Table t ) throws LiquibaseException {
        snap.include(t);

        if ( t.getPrimaryKey() != null ) {
            snap.include(t.getPrimaryKey());
        }

        for ( Column col : t.getColumns() ) {
            snap.include(col);
        }

        for ( UniqueConstraint uc : t.getUniqueConstraints() ) {
            snap.include(uc);
        }

        for ( Index i : t.getIndexes() ) {
            snap.include(i);
        }
    }


    /**
     * @param tbl
     * @param t
     */
    private static void setupPrimaryKey ( org.hibernate.mapping.Table tbl, Table t ) {
        if ( tbl.getPrimaryKey() != null ) {
            log.trace("+ adding primary key"); //$NON-NLS-1$ 
            t.setPrimaryKey(pkeyToPkey(t, tbl.getPrimaryKey()));
        }
    }


    /**
     * @param tbl
     * @param t
     */
    private static void setupUniqueKeys ( org.hibernate.mapping.Table tbl, Table t ) {
        Iterator<org.hibernate.mapping.UniqueKey> uniqueKeyIt = tbl.getUniqueKeyIterator();
        while ( uniqueKeyIt.hasNext() ) {
            org.hibernate.mapping.UniqueKey idx = uniqueKeyIt.next();
            UniqueConstraint u = setupUniqueConstraint(t, idx);
            t.getUniqueConstraints().add(u);
        }
    }


    /**
     * @param tbl
     */
    private static void setupConstraints ( org.hibernate.mapping.Table tbl ) {

        Iterator<String> constraintIt = tbl.getCheckConstraintsIterator();
        while ( constraintIt.hasNext() ) {
            String cons = constraintIt.next();
            if ( log.isTraceEnabled() ) {
                log.trace(" + found constraint " + cons); //$NON-NLS-1$
            }

        }
    }


    /**
     * @param tbl
     * @param t
     */
    private static void setupIndices ( org.hibernate.mapping.Table tbl, Table t ) {
        Iterator<org.hibernate.mapping.Index> indexIt = tbl.getIndexIterator();
        while ( indexIt.hasNext() ) {
            org.hibernate.mapping.Index idx = indexIt.next();
            Index i = setupIndex(t, idx);
            t.getIndexes().add(i);
        }
    }


    /**
     * @param tbl
     * @return
     */
    @SuppressWarnings ( "deprecation" )
    private String getIdColumn ( org.hibernate.mapping.Table tbl ) {
        String idCol = null;;
        if ( tbl.getIdentifierValue() != null
                && tbl.getIdentifierValue().isIdentityColumn(this.config.getIdentifierGeneratorFactory(), this.config.getDatabase().getDialect()) ) {
            org.hibernate.mapping.Column idColumn = tbl.getPrimaryKey().getColumns().get(0);
            if ( log.isTraceEnabled() ) {
                log.trace("has identity column " + idColumn.getName()); //$NON-NLS-1$
            }
            idCol = idColumn.getName();
        }
        return idCol;
    }


    /**
     * @param t
     * @param idx
     * @return
     */
    private static UniqueConstraint setupUniqueConstraint ( Table t, org.hibernate.mapping.UniqueKey idx ) {
        UniqueConstraint u = new UniqueConstraint();
        u.setSnapshotId(UUID.randomUUID().toString());
        u.setTable(t);
        u.setName(idx.getName());

        List<Column> columns = new ArrayList<>();
        Iterator<org.hibernate.mapping.Column> idxColumnIt = idx.getColumnIterator();
        while ( idxColumnIt.hasNext() ) {
            org.hibernate.mapping.Column idxCol = idxColumnIt.next();
            Column col = t.getColumn(idxCol.getName());
            u.getColumns().add(col);
            columns.add(col);
        }

        // Index backingIndex = new Index();
        // backingIndex.setTable(t);
        // backingIndex.setName(idx.getName());
        // backingIndex.getColumns().addAll(columns);
        // backingIndex.setUnique(true);
        //backingIndex.setName("UC_" + idx.getName().toUpperCase()); //$NON-NLS-1$
        // backingIndex.addAssociatedWith(Index.MARK_UNIQUE_CONSTRAINT);
        // u.setBackingIndex(backingIndex);
        // t.getIndexes().add(backingIndex);

        if ( log.isTraceEnabled() ) {
            log.trace("+ adding unique constraint on  " + u.getColumnNames()); //$NON-NLS-1$
        }
        return u;
    }


    /**
     * @param t
     * @param idx
     * @return
     */
    private static Index setupIndex ( Table t, org.hibernate.mapping.Index idx ) {
        Index i = new Index();
        i.setSnapshotId(UUID.randomUUID().toString());
        i.setTable(t);
        i.setName(idx.getName().toUpperCase());

        Iterator<org.hibernate.mapping.Column> idxColumnIt = idx.getColumnIterator();
        while ( idxColumnIt.hasNext() ) {
            org.hibernate.mapping.Column idxCol = idxColumnIt.next();
            i.addColumn(t.getColumn(idxCol.getName()));
        }

        i.setUnique(false);

        if ( log.isTraceEnabled() ) {
            log.trace("+ adding index on  " + i.getColumnNames()); //$NON-NLS-1$
        }
        return i;
    }


    private static PrimaryKey pkeyToPkey ( Table t, org.hibernate.mapping.PrimaryKey pkey ) {
        PrimaryKey res = new PrimaryKey();

        res.setSnapshotId(UUID.randomUUID().toString());
        res.setTable(t);

        String hbnPrimaryKeyName = makePrimaryKeyName(t, pkey);
        res.setName(hbnPrimaryKeyName);

        List<Column> columns = new ArrayList<>();

        Iterator<org.hibernate.mapping.Column> colIt = pkey.getColumnIterator();

        int pos = 0;

        while ( colIt.hasNext() ) {
            String columnName = colIt.next().getName();
            Column column = t.getColumn(columnName);
            res.addColumn(pos, column);
            columns.add(column);
            pos++;
        }

        // Index backingIndex = new Index();
        // backingIndex.setTable(t);
        // backingIndex.setName("IX_" + hbnPrimaryKeyName); //$NON-NLS-1$
        // backingIndex.getColumns().addAll(columns);
        // res.setBackingIndex(backingIndex);
        // backingIndex.setUnique(true);

        return res;
    }


    /**
     * @param t
     * @param pkey
     * @return
     */
    private static String makePrimaryKeyName ( Table t, org.hibernate.mapping.PrimaryKey pkey ) {
        String hbnPrimaryKeyName = pkey.getName();
        String tableName = t.getName();
        if ( hbnPrimaryKeyName != null && hbnPrimaryKeyName.length() == 15 && hbnPrimaryKeyName.equals(PK_ALIAS_15.toAliasString(tableName)) ) {
            log.debug("Hibernate primary key name is probably truncated. " + hbnPrimaryKeyName); //$NON-NLS-1$
            String newAlias = NEW_PK_ALIAS.toAliasString(tableName);
            int newAliasLength = newAlias.length();
            if ( newAliasLength > 15 ) {
                if ( newAliasLength == PKNAMELENGTH ) {
                    String suffix = "_" + //$NON-NLS-1$
                            Integer.toHexString(tableName.hashCode()).toUpperCase() + "_" + PK; //$NON-NLS-1$
                    hbnPrimaryKeyName = newAlias.substring(0, PKNAMELENGTH - suffix.length()) + suffix;
                }
                else {
                    hbnPrimaryKeyName = newAlias;
                }
                log.debug("Changing hibernate primary key name to " + hbnPrimaryKeyName); //$NON-NLS-1$
            }
        }
        return hbnPrimaryKeyName;
    }
}
