/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 *
 */
public class PredicatedJobBuilderWrapper implements JobBuilder {

    private JobBuilder delegate;
    private Predicate predicate;


    /**
     * @param delegate
     * @param pred
     * 
     */
    public PredicatedJobBuilderWrapper ( JobBuilder delegate, Predicate pred ) {
        this.delegate = delegate;
        this.predicate = pred;
    }


    @Override
    public <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator add (
            Class<T> unitClass ) throws UnitInitializationFailedException {
        TConfigurator add = this.delegate.add(unitClass);
        add.runIf(this.predicate);
        return add;
    }


    @Override
    public <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator add (
            T unit ) throws UnitInitializationFailedException {
        TConfigurator add = this.delegate.add(unit);
        add.runIf(this.predicate);
        return add;
    }


    @Override
    public JobBuilder onFail () {
        return this.delegate.onFail();
    }


    @Override
    public @NonNull GroupJobBuilder beginGroup () {
        return this.delegate.beginGroup();
    }


    @Override
    public Job getJob () {
        return this.delegate.getJob();
    }


    @Override
    public void named ( String name ) {
        this.delegate.named(name);
    }


    @Override
    public void withFlag ( String flag ) {
        this.delegate.withFlag(flag);
    }


    @Override
    public <T extends SystemService> void withService ( Class<T> serviceClass, T service ) throws InvalidUnitConfigurationException {
        this.delegate.withService(serviceClass, service);
    }


    @Override
    public Map<Class<? extends SystemService>, SystemService> getServices () {
        return this.delegate.getServices();
    }

}
