/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class RemoveKeystore extends AbstractKeyStoreExecutionUnit<StatusOnlyResult, RemoveKeystore, RemoveKeystoreConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 4390514366313537716L;


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
        KeystoresManager keystoresManager = getKeystoresManager(context);
        if ( !context.getConfig().isDryRun() && keystoresManager.hasKeyStore(getKeystoreName()) ) {
            context.getOutput().info("Removing keystore " + getKeystoreName()); //$NON-NLS-1$
            try {
                keystoresManager.deleteKeyStore(this.getKeystoreName());
            }
            catch ( KeystoreManagerException e ) {
                throw new ExecutionException("Failed to remove keystore", e); //$NON-NLS-1$
            }
            return new StatusOnlyResult(Status.SUCCESS);
        }
        return new StatusOnlyResult(Status.SKIPPED);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public RemoveKeystoreConfigurator createConfigurator () {
        return new RemoveKeystoreConfigurator(this);
    }

}
