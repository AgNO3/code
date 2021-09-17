/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.units;


import java.security.cert.X509Certificate;
import java.util.Set;

import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class SynchronizeTruststore extends
        AbstractTruststoreExcecutionUnit<StatusOnlyResult, SynchronizeTruststore, SynchronizeTruststoreConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 7832456535768146800L;

    private Set<X509Certificate> trustAnchors;


    /**
     * @return the trustAnchors
     */
    public Set<X509Certificate> getTrustAnchors () {
        return this.trustAnchors;
    }


    /**
     * @param trustAnchors
     *            the trustAnchors to set
     */
    void setTrustAnchors ( Set<X509Certificate> trustAnchors ) {
        this.trustAnchors = trustAnchors;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( this.getTrustAnchors() == null ) {
            throw new InvalidParameterException("Trust anchors must be set"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        // TODO: backup truststore?
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
            context.getOutput().info("Synchronizing trust store " + getTruststore()); //$NON-NLS-1$
            TruststoresManager truststoresManager = getTruststoresManager(context);
            if ( !context.getConfig().isDryRun() || truststoresManager.hasTrustStore(getTruststore()) ) {
                TruststoreManager trustStoreManager = truststoresManager.getTrustStoreManager(getTruststore());
                trustStoreManager.setCertificates(this.getTrustAnchors());
            }
            return new StatusOnlyResult(Status.SUCCESS);
        }
        catch ( TruststoreManagerException e ) {
            throw new ExecutionException("Failed to synchronize truststore", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public SynchronizeTruststoreConfigurator createConfigurator () {
        return new SynchronizeTruststoreConfigurator(this);
    }

}
