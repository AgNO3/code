/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;


/**
 * @author mbechler
 *
 */
public interface JobIterator extends Serializable {

    /**
     * @param p
     * @return the units to run for the phase
     */
    Iterator<ExecutionUnit<?, ?, ?>> iterate ( Phase p );


    /**
     * @param failedIn
     * @return an iterator to run on failure
     */
    JobIterator failure ( Phase failedIn );


    /**
     * @param toRollback
     * @return a reversed iterator containg only the given units
     */
    JobIterator reversedIterator ( Collection<ExecutionUnit<?, ?, ?>> toRollback );


    /**
     * @return the number of units in the regular execution flow
     */
    int size ();


    /**
     * @param unit
     */
    void add ( BaseExecutable unit );


    /**
     * @param unit
     */
    void addFailureUnit ( BaseExecutable unit );

}
