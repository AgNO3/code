/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.internal;


import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;

import eu.agno3.runtime.db.schema.orm.hibernate.HibernateIndexingException;


/**
 * @author mbechler
 * 
 */
public class HibernateDatabaseSnapshot extends DatabaseSnapshot {

    private HibernateIndexer indexer;


    /**
     * @param database
     * @throws LiquibaseException
     */
    public HibernateDatabaseSnapshot ( HibernateDatabase database ) throws LiquibaseException {
        super(new DatabaseObject[] {
            new Schema(new Catalog(null), null)
        }, database);
        this.indexer = database.getIndexer();
    }


    /**
     * 
     * @param catalog
     * @param schema
     * @throws HibernateIndexingException
     */
    public void includeSchema ( String catalog, String schema ) throws HibernateIndexingException {
        this.indexer.ensureHasRun();
        this.indexer.export(catalog, schema, this);
    }


    /**
     * 
     * @throws HibernateIndexingException
     */
    public void includeAll () throws HibernateIndexingException {
        this.indexer.ensureHasRun();
        this.indexer.exportAll(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.orm.ORMDatabaseSnapshot#include(T)
     */
    @SuppressWarnings ( "javadoc" )
    @Override
    public <T extends DatabaseObject> T include ( T arg0 ) throws DatabaseException, InvalidExampleException {
        return super.include(arg0);
    }
}
