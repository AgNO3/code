/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 10, 2017 by mbechler
 */
package eu.agno3.orchestrator.system.agent;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class DisconnectAgentConnectionUnit
        extends AbstractExecutionUnit<StatusOnlyResult, DisconnectAgentConnectionUnit, DisconnectAgentConnectionConfigururator> {

    /**
     * 
     */
    private static final long serialVersionUID = -8000482789711908620L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        try {
            context.getConfig().getService(AgentConnectionService.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Agent connection service unavailable", e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        try {
            AgentConnectionService service = context.getConfig().getService(AgentConnectionService.class);
            service.disconnect();
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Agent connection service unavailable", e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public DisconnectAgentConnectionConfigururator createConfigurator () {
        return new DisconnectAgentConnectionConfigururator(this);
    }

}
