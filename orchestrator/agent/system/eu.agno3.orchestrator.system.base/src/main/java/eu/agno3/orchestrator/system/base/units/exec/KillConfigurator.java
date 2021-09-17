/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class KillConfigurator extends AbstractConfigurator<StatusOnlyResult, Kill, KillConfigurator> {

    /**
     * @param unit
     */
    protected KillConfigurator ( Kill unit ) {
        super(unit);
    }


    /**
     * 
     * @param pid
     * @return this configurator
     */
    public KillConfigurator pid ( int pid ) {
        getExecutionUnit().setPid(pid);
        return self();
    }


    /**
     * 
     * @param signal
     * @return this configurator
     */
    public KillConfigurator signal ( int signal ) {
        getExecutionUnit().setSignal(signal);
        return self();
    }


    /**
     * 
     * @return this configurator
     */
    public KillConfigurator waitForExit () {
        getExecutionUnit().setWaitForExit(true);
        return self();
    }


    /**
     * 
     * @param timeout
     * @return this configurator
     */
    public KillConfigurator waitForExit ( long timeout ) {
        getExecutionUnit().setWaitForExit(true);
        getExecutionUnit().setWaitTimeout(timeout);
        return self();
    }


    /**
     * 
     * @return this configurator
     */
    public KillConfigurator failOnError () {
        getExecutionUnit().setIgnoreError(false);
        return self();
    }

}
