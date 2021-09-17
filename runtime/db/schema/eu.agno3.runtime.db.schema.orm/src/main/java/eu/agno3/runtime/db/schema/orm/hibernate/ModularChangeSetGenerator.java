/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate;


import java.util.Map;

import liquibase.diff.DiffResult;


/**
 * @author mbechler
 * 
 */
public interface ModularChangeSetGenerator {

    /**
     * Generates an database diff for each owner
     * 
     * @param result
     * @return a map of owner to his respective database diff, unassigned changed go to null key
     */
    Map<String, DiffResult> splitDiff ( DiffResult result );

}