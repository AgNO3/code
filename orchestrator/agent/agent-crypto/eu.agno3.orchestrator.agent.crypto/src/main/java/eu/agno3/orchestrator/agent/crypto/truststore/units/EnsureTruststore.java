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
public class EnsureTruststore extends AbstractTruststoreExcecutionUnit<StatusOnlyResult, EnsureTruststore, EnsureTruststoreConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 7832456535768146800L;

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {

        if ( getTruststoresManager(context).hasTrustStore(getTruststore()) ) {
            return new StatusOnlyResult(Status.SKIPPED);
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
            TruststoresManager truststoresManager = getTruststoresManager(context);
            if ( truststoresManager.hasTrustStore(getTruststore()) ) {
                return new StatusOnlyResult(Status.SKIPPED);
            }

            context.getOutput().info("Creating truststore " + getTruststore()); //$NON-NLS-1$
            if ( !context.getConfig().isDryRun() ) {
                truststoresManager.createTrustStore(getTruststore());
            }
            return new StatusOnlyResult(Status.SUCCESS);
        }
        catch ( TruststoreManagerException e ) {
            throw new ExecutionException("Failed to create truststore", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public EnsureTruststoreConfigurator createConfigurator () {
        return new EnsureTruststoreConfigurator(this);
    }

}
