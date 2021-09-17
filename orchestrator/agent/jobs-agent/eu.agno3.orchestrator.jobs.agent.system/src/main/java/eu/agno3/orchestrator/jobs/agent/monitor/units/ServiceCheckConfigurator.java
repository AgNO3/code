/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.01.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.monitor.units;


import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class ServiceCheckConfigurator extends AbstractConfigurator<StatusOnlyResult, ServiceCheck, ServiceCheckConfigurator> {

    /**
     * @param unit
     */
    protected ServiceCheckConfigurator ( ServiceCheck unit ) {
        super(unit);
    }


    /**
     * 
     * @param ref
     * @return this configurator
     */
    public ServiceCheckConfigurator service ( StructuralObjectReference ref ) {
        this.getExecutionUnit().setService(ref);
        return this.self();
    }


    /**
     * 
     * @param secs
     * @return this configurator
     */
    public ServiceCheckConfigurator timeout ( long secs ) {
        this.getExecutionUnit().setTimeout(secs);
        return this.self();
    }


    /**
     * 
     * @param duration
     * @return this configurator
     */
    public ServiceCheckConfigurator timeout ( Duration duration ) {
        this.getExecutionUnit().setTimeout(duration.getMillis());
        return this.self();
    }

}
