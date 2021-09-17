/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionInterruptedException;


/**
 * @author mbechler
 * @param <TResult>
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 *            the configurator type for this execution unit
 */
public interface ExecutionUnit <TResult extends Result, TExecutionUnit extends ExecutionUnit<TResult, TExecutionUnit, TConfigurator>, TConfigurator extends Configurator<TResult, TExecutionUnit, TConfigurator>>
        extends BaseExecutable {

    /**
     * @param context
     * @return a predicate to check whether this execution unit is satisfied == may run
     */
    boolean isSatisfied ( Context context );


    /**
     * 
     * @return whether proper rollback is supported
     */
    boolean canRollback ();


    /**
     * @param predicate
     */
    void addPredicate ( Predicate predicate );


    /**
     * @param context
     * @throws ExecutionException
     */
    void validate ( Context context ) throws ExecutionException;


    /**
     * @param context
     * @return the result of the prepare phase
     * @throws ExecutionException
     * @throws ExecutionInterruptedException
     *             if an interrupt is caught
     */
    TResult prepare ( Context context ) throws ExecutionException;


    /**
     * @param context
     * @return the result of the execute phase
     * @throws ExecutionException
     * @throws ExecutionInterruptedException
     *             if an interrupt is caught
     */
    TResult execute ( Context context ) throws ExecutionException;


    /**
     * @param context
     * @throws ExecutionException
     */
    void rollback ( Context context ) throws ExecutionException;


    /**
     * @param context
     * @throws ExecutionException
     */
    void cleanup ( Context context ) throws ExecutionException;


    /**
     * @param context
     * @throws ExecutionException
     */
    void suspend ( Context context ) throws ExecutionException;


    /**
     * @param context
     * @throws ExecutionException
     */
    void resume ( Context context ) throws ExecutionException;


    /**
     * @return the configurator instance for this unit
     */
    TConfigurator createConfigurator ();


    /**
     * @return a result reference for this unit's result
     */
    ResultReference<TResult> getResult ();

}
