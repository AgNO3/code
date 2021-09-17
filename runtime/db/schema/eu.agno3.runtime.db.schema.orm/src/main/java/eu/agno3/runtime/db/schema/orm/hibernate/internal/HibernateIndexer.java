/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.internal;


import java.util.Collection;

import liquibase.exception.LiquibaseException;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;

import org.apache.log4j.Logger;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.dialect.Dialect;

import eu.agno3.runtime.db.schema.SchemaException;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateIndexingException;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategy;
import eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategyFactory;


/**
 * @author mbechler
 * 
 */
public class HibernateIndexer {

    private static final Logger log = Logger.getLogger(HibernateIndexer.class);

    private boolean hasRun = false;
    private Metadata config;

    private HibernateIndex index = new HibernateIndex();
    private HibernateDatabase db;
    private HibernateOwnershipStrategy ownershipStrategy;


    /**
     * @param db
     * @param ownershipFactory
     */
    public HibernateIndexer ( HibernateDatabase db, HibernateOwnershipStrategyFactory ownershipFactory ) {
        this.db = db;
        this.config = db.getConfiguration();
        this.ownershipStrategy = ownershipFactory.createStrategy(this.config);
    }


    /**
     * 
     * @throws HibernateIndexingException
     */
    public void ensureHasRun () throws HibernateIndexingException {
        if ( !this.hasRun ) {
            this.index();
        }
    }


    /**
     * @throws SchemaException
     * 
     */
    private void index () throws HibernateIndexingException {
        this.hasRun = true;

        Collection<org.hibernate.mapping.Table> tables = this.config.collectTableMappings();

        Catalog defaultCatalog = new Catalog(this.db.getDefaultCatalogName());
        Schema defaultSchema = new Schema(defaultCatalog, this.db.getDefaultSchemaName());
        this.index.init(defaultCatalog, defaultSchema);

        Dialect dialect = this.db.getDialect();
        indexTables(tables, dialect);
        indexForeignKeys();
        indexSequences();
    }


    /**
     * 
     */
    private void indexForeignKeys () {
        HibernateForeignKeyIndexer foreignKeyIndexer = new HibernateForeignKeyIndexer();
        foreignKeyIndexer.indexForeignKeys(this.config, this.index);
    }


    /**
     * @param tables
     * @param dialect
     * @param tableIndexer
     * @throws HibernateIndexingException
     */
    private void indexTables ( Collection<org.hibernate.mapping.Table> tables, Dialect dialect ) throws HibernateIndexingException {
        HibernateTableIndexer tableIndexer = new HibernateTableIndexer(this.config);
        for ( org.hibernate.mapping.Table tbl : tables ) {

            if ( !tbl.isPhysicalTable() ) {
                continue;
            }

            Catalog c = this.index.setupCatalog(tbl);
            Schema s = this.index.setupSchema(tbl, c);
            Table t = this.index.setupTable(tbl, s);
            setupTableOwner(tbl, t);

            tableIndexer.indexTable(s, tbl, t, this.config, new HibernateColumnIndexer(dialect, this.config));
        }
    }


    /**
     * 
     */
    private void indexSequences () {
        for ( Namespace namespace : this.config.getDatabase().getNamespaces() ) {
            for ( org.hibernate.boot.model.relational.Sequence sequence : namespace.getSequences() ) {
                Identifier catalogName = sequence.getName().getCatalogName();
                Identifier schemaName = sequence.getName().getSchemaName();
                Schema schema = this.index.getSchema(catalogName != null ? catalogName.getText() : null, schemaName != null ? schemaName.getText()
                        : null);
                this.index.setupSequence(sequence.getName().getObjectName().getText(), schema);
            }
        }
    }


    /**
     * @param tbl
     * @param t
     * @return
     */
    private String setupTableOwner ( org.hibernate.mapping.Table tbl, Table t ) {
        String owningBundle = this.ownershipStrategy.getOwner(tbl);
        if ( log.isTraceEnabled() ) {
            log.trace("+ owner: " + owningBundle); //$NON-NLS-1$
        }
        t.setAttribute("owner", owningBundle); //$NON-NLS-1$
        return owningBundle;
    }


    /**
     * 
     * @param catalog
     * @param schema
     * @param snap
     * @throws HibernateIndexingException
     */
    public void export ( String catalog, String schema, HibernateDatabaseSnapshot snap ) throws HibernateIndexingException {
        log.debug(String.format("Exporting %s:%s to database snapshot", catalog, schema)); //$NON-NLS-1$
        Catalog c = this.index.getCatalog(catalog);
        Schema s = this.index.getSchema(catalog, schema);

        try {
            snap.include(c);
            snap.include(s);

            for ( Table t : this.index.getTables(s) ) {
                HibernateTableIndexer.exportTable(snap, t);
                HibernateForeignKeyIndexer.exportForeignKeys(snap, t);
            }

            for ( Sequence seq : this.index.getSequences(s) ) {
                snap.include(seq);
            }

            log.debug("Export done."); //$NON-NLS-1$
        }
        catch ( LiquibaseException e ) {
            throw new HibernateIndexingException(String.format("Failed to export %s:%s", catalog, schema), e); //$NON-NLS-1$
        }

    }


    /**
     * 
     * @param snap
     * @throws HibernateIndexingException
     */
    public void exportAll ( HibernateDatabaseSnapshot snap ) throws HibernateIndexingException {
        for ( Catalog c : this.index.getCatalogs() ) {
            for ( Schema s : this.index.getSchemas(c) ) {
                this.export(c.getName(), s.getName(), snap);
            }
        }
    }
}
