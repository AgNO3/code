/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class RemoveKeystoreConfigurator extends AbstractConfigurator<StatusOnlyResult, RemoveKeystore, RemoveKeystoreConfigurator> {

    /**
     * @param unit
     */
    protected RemoveKeystoreConfigurator ( RemoveKeystore unit ) {
        super(unit);
    }


    /**
     * @param ksName
     * @return this configurator
     */
    public RemoveKeystoreConfigurator keystore ( String ksName ) {
        this.getExecutionUnit().setKeystoreName(ksName);
        return this.self();
    }

}
