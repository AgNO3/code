/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.diff;


import java.util.Set;

import javax.sql.DataSource;

import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;


/**
 * @author mbechler
 * 
 */
public interface SchemaDiffService {

    /**
     * @param reference
     * @param target
     * @param roots
     * @return the difference between reference and target datasource
     * @throws SchemaDiffException
     */
    DiffResult diff ( DataSource reference, DataSource target, Set<DatabaseObject> roots ) throws SchemaDiffException;


    /**
     * 
     * 
     * @param reference
     * @param target
     * @param roots
     * @return the difference between reference and target database
     * @throws SchemaDiffException
     */
    DiffResult diff ( DataSource reference, Database target, Set<DatabaseObject> roots ) throws SchemaDiffException;


    /**
     * @param reference
     * @param target
     * @return the difference between reference and target database snapshot
     * @throws SchemaDiffException
     */
    DiffResult diff ( DataSource reference, DatabaseSnapshot target ) throws SchemaDiffException;


    /**
     * @param dsName
     * @param target
     * @return the difference between the schema described by available change log and an actual database snapshot
     * @throws SchemaDiffException
     */
    DiffResult diffToCurrentChangeSet ( String dsName, DatabaseSnapshot target ) throws SchemaDiffException;


    /**
     * @param dsName
     * @param target
     * @param roots
     * @return the difference between the schema described by available change log and an actual database instance
     * @throws SchemaDiffException
     */
    DiffResult diffToCurrentChangeSet ( String dsName, Database target, Set<DatabaseObject> roots ) throws SchemaDiffException;

}
