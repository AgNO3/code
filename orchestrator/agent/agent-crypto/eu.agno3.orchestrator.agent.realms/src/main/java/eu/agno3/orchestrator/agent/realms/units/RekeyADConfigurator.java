/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 13, 2017 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class RekeyADConfigurator extends RealmConfigurator<StatusOnlyResult, RekeyAD, RekeyADConfigurator> {

    /**
     * @param unit
     */
    public RekeyADConfigurator ( RekeyAD unit ) {
        super(unit);
    }

}
