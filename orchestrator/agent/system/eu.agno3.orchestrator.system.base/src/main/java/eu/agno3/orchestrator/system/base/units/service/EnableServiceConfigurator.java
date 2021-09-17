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
public class EnableServiceConfigurator extends AbstractServiceConfigurator<EnableService, EnableServiceConfigurator> {

    /**
     * @param unit
     */
    protected EnableServiceConfigurator ( EnableService unit ) {
        super(unit);
    }


    /**
     * Disable service start while enabling the service
     * 
     * @return this configurator
     */
    public EnableServiceConfigurator noStart () {
        this.getExecutionUnit().setImmediateStart(false);
        return this.self();
    }
}
