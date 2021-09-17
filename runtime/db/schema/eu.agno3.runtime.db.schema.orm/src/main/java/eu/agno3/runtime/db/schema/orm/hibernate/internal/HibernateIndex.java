/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.internal;


import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;


/**
 * @author mbechler
 */
class HibernateIndex {

    private static final Logger log = Logger.getLogger(HibernateIndex.class);

    private static final String UNKNOWN_TABLE = "Unknown table "; //$NON-NLS-1$
    private static final String UNKNOWN_SCHEMA = "Unknown schema "; //$NON-NLS-1$
    private static final String UNKNOWN_CATALOG = "Unknown catalog "; //$NON-NLS-1$

    private final Map<String, Catalog> catalogs = new HashMap<>();
    private final Map<Catalog, Map<String, Schema>> schemas = new HashMap<>();
    private final Map<Schema, Map<String, Table>> tables = new HashMap<>();
    private final Map<Schema, Map<String, Sequence>> sequences = new HashMap<>();


    /**
     * 
     */
    public HibernateIndex () {}


    /**
     * 
     * @param catalog
     * @return the catalog definition
     */
    public Catalog getCatalog ( String catalog ) {
        String catalogName = catalog;

        if ( catalogName != null ) {
            catalogName = catalogName.toUpperCase(Locale.ROOT);
        }

        if ( !this.catalogs.containsKey(catalogName) ) {
            throw new IllegalArgumentException(UNKNOWN_CATALOG + catalogName);
        }

        return this.catalogs.get(catalogName);
    }


    /**
     * 
     * @param catalog
     * @param schema
     * @return the schema definition
     */
    public Schema getSchema ( String catalog, String schema ) {
        String catalogName = catalog;
        String schemaName = schema;

        if ( catalogName != null ) {
            catalogName = catalogName.toUpperCase(Locale.ROOT);
        }

        if ( schemaName != null ) {
            schemaName = schemaName.toUpperCase(Locale.ROOT);
        }

        if ( !this.catalogs.containsKey(catalogName) ) {
            throw new IllegalArgumentException(UNKNOWN_CATALOG + catalogName);
        }

        Catalog c = this.catalogs.get(catalogName);

        if ( !this.schemas.get(c).containsKey(schemaName) ) {
            throw new IllegalArgumentException(UNKNOWN_SCHEMA + schemaName);
        }

        return this.schemas.get(c).get(schemaName);
    }


    /**
     * 
     * @param schema
     * @param name
     * @return the table definition
     */
    public Table getTable ( Schema schema, String name ) {
        if ( !this.tables.containsKey(schema) ) {
            throw new IllegalArgumentException(UNKNOWN_SCHEMA + schema);
        }

        if ( !this.tables.get(schema).containsKey(name) ) {
            throw new IllegalArgumentException(UNKNOWN_TABLE + name);
        }

        return this.tables.get(schema).get(name);
    }


    /**
     * @param defaultCatalog
     * @param defaultSchema
     */
    public void init ( Catalog defaultCatalog, Schema defaultSchema ) {
        String catalogName = defaultCatalog.getName() != null ? defaultCatalog.getName() : null;
        this.catalogs.put(catalogName, defaultCatalog);
        this.schemas.put(defaultCatalog, new HashMap<String, Schema>());
        this.schemas.get(defaultCatalog).put(defaultSchema.getName(), defaultSchema);

        this.tables.put(defaultSchema, new HashMap<String, Table>());
        this.sequences.put(defaultSchema, new HashMap<String, Sequence>());

    }


    /**
     * @param tbl
     * @param s
     * @return the new table
     */
    public Table setupTable ( org.hibernate.mapping.Table tbl, Schema s ) {
        Table t;

        if ( !this.tables.get(s).containsKey(tbl.getName()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Adding table " + tbl.getName()); //$NON-NLS-1$
            }
            t = new Table();
            t.setSnapshotId(UUID.randomUUID().toString());
            t.setSchema(s);
            t.setName(tbl.getName());
            t.setRemarks(tbl.getComment());
            this.tables.get(s).put(tbl.getName(), t);
        }
        else {
            t = this.tables.get(s).get(tbl.getName());
        }
        return t;
    }


    /**
     * 
     * @param name
     * @param s
     * @return the new sequence
     */
    public Sequence setupSequence ( String name, Schema s ) {
        Sequence t;

        if ( !this.sequences.get(s).containsKey(name) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Adding sequence " + name); //$NON-NLS-1$
            }
            t = new Sequence();
            t.setSnapshotId(UUID.randomUUID().toString());
            t.setSchema(s);
            t.setName(name);
            this.sequences.get(s).put(name, t);
        }
        else {
            t = this.sequences.get(s).get(name);
        }
        return t;
    }


    /**
     * @param tbl
     * @param c
     * @return the new schema
     */
    public Schema setupSchema ( org.hibernate.mapping.Table tbl, Catalog c ) {
        String schemaName = tbl.getSchema();
        if ( schemaName != null ) {
            schemaName = schemaName.toUpperCase(Locale.ROOT);
        }
        Schema s;
        if ( !this.schemas.get(c).containsKey(schemaName) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Adding schema %s:%s", c.getName(), schemaName)); //$NON-NLS-1$
            }
            s = new Schema(c, schemaName);
            s.setSnapshotId(UUID.randomUUID().toString());

            this.schemas.get(c).put(schemaName, s);
            this.tables.put(s, new HashMap<String, Table>());
            this.sequences.put(s, new HashMap<String, Sequence>());
        }
        else {
            s = this.schemas.get(c).get(schemaName);
        }
        return s;
    }


    /**
     * @param tbl
     * @return the new catalog
     */
    public Catalog setupCatalog ( org.hibernate.mapping.Table tbl ) {
        String catalogName = tbl.getCatalog();
        if ( catalogName != null ) {
            catalogName = catalogName.toUpperCase(Locale.ROOT);
        }
        Catalog c;
        if ( !this.catalogs.containsKey(catalogName) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Adding catalog " + catalogName); //$NON-NLS-1$
            }
            c = new Catalog(catalogName);
            c.setSnapshotId(UUID.randomUUID().toString());

            this.catalogs.put(catalogName, c);
            this.schemas.put(c, new HashMap<String, Schema>());
        }
        else {
            c = this.catalogs.get(catalogName);
        }
        return c;
    }


    /**
     * @param tbl
     * @return table liquibase table object for the given mapping table
     */
    public Table tableToTable ( org.hibernate.mapping.Table tbl ) {
        return this.getTable(this.getSchema(tbl.getCatalog(), tbl.getSchema()), tbl.getName());
    }


    /**
     * 
     * @return the defined catalogs
     */
    public Collection<Catalog> getCatalogs () {
        return this.catalogs.values();
    }


    /**
     * 
     * @param c
     * @return the catalogs defined schemas
     */
    public Collection<Schema> getSchemas ( Catalog c ) {
        return this.schemas.get(c).values();
    }


    /**
     * 
     * @param s
     * @return the schemas defined tables
     */
    public Collection<Table> getTables ( Schema s ) {
        return this.tables.get(s).values();
    }


    /**
     * 
     * @param s
     * @return the schemas defined tables
     */
    public Collection<Sequence> getSequences ( Schema s ) {
        return this.sequences.get(s).values();
    }
}