/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema;


import java.util.Set;

import liquibase.changelog.ChangeLogIterator;


/**
 * @author mbechler
 * 
 */
public interface SchemaManager {

    /**
     * Ensures the schema is up to date, possibly performing updates
     * 
     * @throws SchemaException
     */
    void ensureUpToDate () throws SchemaException;


    /**
     * @return contexts used by this schema manager
     */
    Set<String> getContexts ();


    /**
     * @return an iterator over all unapplied changes
     * @throws SchemaException
     */
    ChangeLogIterator getUnappliedChanges () throws SchemaException;


    /**
     * @return the complete change log
     */
    ChangeLogIterator getAllChanges ();


    /**
     * Marks the current changelog as applied
     * 
     * @throws SchemaException
     */
    void setChangeLogApplied () throws SchemaException;

}
