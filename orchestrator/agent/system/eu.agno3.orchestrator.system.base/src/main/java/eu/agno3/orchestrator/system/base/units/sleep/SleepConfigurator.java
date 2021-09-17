/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.sleep;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 * 
 */
public class SleepConfigurator extends AbstractConfigurator<StatusOnlyResult, Sleep, SleepConfigurator> {

    /**
     * @param unit
     */
    protected SleepConfigurator ( Sleep unit ) {
        super(unit);
    }


    /**
     * @param seconds
     * @return this configurator
     */
    public SleepConfigurator seconds ( int seconds ) {
        this.getExecutionUnit().setSleepTime(seconds);
        return this.self();
    }

}
