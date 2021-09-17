/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import eu.agno3.orchestrator.system.base.execution.Configurator;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.Predicate;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.predicates.Predicates;


/**
 * @author mbechler
 * 
 * @param <TResult>
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 */
public abstract class AbstractConfigurator <TResult extends Result, TExecutionUnit extends ExecutionUnit<TResult, TExecutionUnit, TConfigurator>, TConfigurator extends Configurator<TResult, TExecutionUnit, TConfigurator>>
        implements Configurator<TResult, TExecutionUnit, TConfigurator> {

    private final TExecutionUnit unit;


    /**
     * @param unit
     *            Unit to configure
     * 
     */
    protected AbstractConfigurator ( TExecutionUnit unit ) {
        super();
        this.unit = unit;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Configurator#runIf(boolean)
     */
    @Override
    public TConfigurator runIf ( boolean bool ) {
        this.skipIf(!bool);
        return this.self();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Configurator#skipIf(boolean)
     */
    @Override
    public TConfigurator skipIf ( boolean bool ) {
        if ( bool ) {
            this.unit.addPredicate(Predicates.no());
        }
        this.unit.addPredicate(Predicates.yes());
        return this.self();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Configurator#runIf(eu.agno3.orchestrator.system.base.execution.Predicate)
     */
    @Override
    public TConfigurator runIf ( Predicate cond ) {
        this.unit.addPredicate(cond);
        return this.self();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Configurator#skipIf(eu.agno3.orchestrator.system.base.execution.Predicate)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public TConfigurator skipIf ( Predicate cond ) {
        this.unit.addPredicate(Predicates.not(cond));
        return (TConfigurator) this;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Configurator#getExecutionUnit()
     */
    @Override
    public TExecutionUnit getExecutionUnit () {
        return this.unit;
    }


    @SuppressWarnings ( "unchecked" )
    protected final TConfigurator self () {
        return (TConfigurator) this;
    }
}