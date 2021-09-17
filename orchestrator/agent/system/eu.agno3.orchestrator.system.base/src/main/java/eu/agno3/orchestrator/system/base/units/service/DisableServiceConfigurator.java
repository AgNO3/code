/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.service;


/**
 * @author mbechler
 * 
 */
public class DisableServiceConfigurator extends AbstractServiceConfigurator<DisableService, DisableServiceConfigurator> {

    /**
     * @param unit
     */
    protected DisableServiceConfigurator ( DisableService unit ) {
        super(unit);
    }


    /**
     * Disable service stop while disabling the service
     * 
     * @return this configurator
     */
    public DisableServiceConfigurator noStop () {
        this.getExecutionUnit().setImmediateStop(false);
        return this.self();
    }
}
