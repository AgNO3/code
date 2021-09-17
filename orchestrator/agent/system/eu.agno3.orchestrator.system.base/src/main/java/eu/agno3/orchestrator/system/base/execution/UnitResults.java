/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.util.List;

import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;


/**
 * @author mbechler
 * 
 */
public interface UnitResults extends Result {

    /**
     * @return the list of unit that have been executed in this phase
     */
    List<ExecutionUnit<?, ?, ?>> getExecutedUnits ();


    /**
     * @param eu
     * @return the result for the given execution unit
     * @throws ResultReferenceException
     */
    <T extends Result> T getResult ( ExecutionUnit<T, ?, ?> eu ) throws ResultReferenceException;


    /**
     * @param eu
     * @param r
     */
    void add ( ExecutionUnit<?, ?, ?> eu, Result r );

}