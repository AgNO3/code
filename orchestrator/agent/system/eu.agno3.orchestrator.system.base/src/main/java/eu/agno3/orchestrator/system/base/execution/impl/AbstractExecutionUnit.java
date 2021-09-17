/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.util.HashSet;
import java.util.Set;

import eu.agno3.orchestrator.system.base.execution.Configurator;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.Predicate;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.predicates.Predicates;


/**
 * @author mbechler
 * @param <TResult>
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 * 
 */
@SuppressWarnings ( "serial" )
public abstract class AbstractExecutionUnit <TResult extends Result, TExecutionUnit extends ExecutionUnit<TResult, TExecutionUnit, TConfigurator>, TConfigurator extends Configurator<TResult, TExecutionUnit, TConfigurator>>
        implements ExecutionUnit<TResult, TExecutionUnit, TConfigurator> {

    private Set<Predicate> predicates = new HashSet<>();


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#isSatisfied(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public boolean isSatisfied ( Context context ) {
        return Predicates.all(this.predicates).evaluate(context);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.BaseExecutable#unitCount()
     */
    @Override
    public int unitCount () {
        return 1;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#canRollback()
     */
    @Override
    public boolean canRollback () {
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#addPredicate(eu.agno3.orchestrator.system.base.execution.Predicate)
     */
    @Override
    public void addPredicate ( Predicate predicate ) {
        this.predicates.add(predicate);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#getResult()
     */
    @Override
    public ResultReference<TResult> getResult () {
        return new ResultReference<>(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ExecutionException
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#rollback(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void rollback ( Context context ) throws ExecutionException {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#cleanup(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void cleanup ( Context context ) throws ExecutionException {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#suspend(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void suspend ( Context context ) throws ExecutionException {
        this.cleanup(context);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#resume(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void resume ( Context context ) throws ExecutionException {
        this.prepare(context);
    }

}