/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


/**
 * @author mbechler
 * @param <TResult>
 *            EU result type
 * @param <TExecutionUnit>
 *            EU type
 * @param <TConfigurator>
 *            EU configurator type
 * 
 */
public interface Configurator <TResult extends Result, TExecutionUnit extends ExecutionUnit<TResult, TExecutionUnit, TConfigurator>, TConfigurator extends Configurator<TResult, TExecutionUnit, TConfigurator>> {

    /**
     * Only run execution unit if the given condition is true
     * 
     * @param bool
     * @return this configurator
     */
    TConfigurator runIf ( boolean bool );


    /**
     * Skip the execution unit if the given condition is true
     * 
     * @param bool
     * @return this configurator
     */
    TConfigurator skipIf ( boolean bool );


    /**
     * Run only if the provided predicate is true
     * 
     * @param cond
     * @return this configurator
     */
    TConfigurator runIf ( Predicate cond );


    /**
     * Skip if the provided predicate is true
     * 
     * @param cond
     * @return this configurator
     */
    TConfigurator skipIf ( Predicate cond );


    /**
     * 
     * @return the execution unit instance configured by this configurator
     */
    TExecutionUnit getExecutionUnit ();
}
