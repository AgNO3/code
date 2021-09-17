/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.units;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureTruststoreConfigurator extends AbstractConfigurator<StatusOnlyResult, EnsureTruststore, EnsureTruststoreConfigurator> {

    /**
     * @param unit
     */
    protected EnsureTruststoreConfigurator ( EnsureTruststore unit ) {
        super(unit);
    }


    /**
     * @param name
     * @return this configurator
     */
    public EnsureTruststoreConfigurator truststore ( String name ) {
        this.getExecutionUnit().setTruststore(name);
        return this.self();
    }
}
