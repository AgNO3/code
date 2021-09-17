/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.diff;


import liquibase.diff.DiffResult;


/**
 * @author mbechler
 * 
 */
public interface DiffPostProcessor {

    /**
     * @return priority, higher priority processors are run first
     */
    int getPriority ();


    /**
     * @param r
     * @return a possibly modified instance of the result
     */
    DiffResult process ( DiffResult r );
}
