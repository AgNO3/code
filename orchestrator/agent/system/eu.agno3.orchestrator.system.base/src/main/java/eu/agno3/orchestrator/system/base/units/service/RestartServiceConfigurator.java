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
public class RestartServiceConfigurator extends AbstractServiceConfigurator<RestartService, RestartServiceConfigurator> {

    /**
     * @param unit
     */
    protected RestartServiceConfigurator ( RestartService unit ) {
        super(unit);
    }


    /**
     * @return this configurator
     */
    public RestartServiceConfigurator noWait () {
        this.getExecutionUnit().setNoWait(true);
        return this.self();
    }

}
