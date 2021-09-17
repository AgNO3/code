/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.units;


import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class RemoveTruststore extends AbstractTruststoreExcecutionUnit<StatusOnlyResult, RemoveTruststore, RemoveTruststoreConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 7832456535768146800L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);
        TruststoresManager service = getTruststoresManager(context);
        if ( !context.getConfig().isNoVerifyEnv() && ( !service.hasTrustStore(this.getTruststore()) || service.isReadOnly(this.getTruststore()) ) ) {
            throw new ExecutionException("Truststore does not exist " + this.getTruststore()); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
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
            context.getOutput().info("Removing truststore " + getTruststore()); //$NON-NLS-1$

            if ( !context.getConfig().isDryRun() ) {
                getTruststoresManager(context).deleteTrustStore(getTruststore());
            }
        }
        catch ( TruststoreManagerException e ) {
            throw new ExecutionException("Failed to remove truststore", e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public RemoveTruststoreConfigurator createConfigurator () {
        return new RemoveTruststoreConfigurator(this);
    }

}
