/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 10, 2017 by mbechler
 */
package eu.agno3.orchestrator.system.agent;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class ReestablishAgentConnectionConfigururator
        extends AbstractConfigurator<StatusOnlyResult, ReestablishAgentConnection, ReestablishAgentConnectionConfigururator> {

    /**
     * @param unit
     */
    protected ReestablishAgentConnectionConfigururator ( ReestablishAgentConnection unit ) {
        super(unit);
    }

}
