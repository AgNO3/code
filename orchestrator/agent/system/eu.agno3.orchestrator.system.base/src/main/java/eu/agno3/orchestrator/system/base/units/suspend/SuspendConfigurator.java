/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.suspend;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class SuspendConfigurator extends AbstractConfigurator<StatusOnlyResult, Suspend, SuspendConfigurator> {

    /**
     * @param unit
     */
    protected SuspendConfigurator ( Suspend unit ) {
        super(unit);
    }


    /**
     * @param i
     * @return this configurator
     */
    public SuspendConfigurator after ( int i ) {
        this.getExecutionUnit().setAfter(i);
        return this.self();
    }

}
