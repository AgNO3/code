/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.units;


import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.system.base.execution.Configurator;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;


/**
 * @author mbechler
 * @param <TResult>
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 *
 */
public abstract class AbstractTruststoreExcecutionUnit <TResult extends Result, TExecutionUnit extends ExecutionUnit<TResult, TExecutionUnit, TConfigurator>, TConfigurator extends Configurator<TResult, TExecutionUnit, TConfigurator>>
        extends AbstractExecutionUnit<TResult, TExecutionUnit, TConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -8307593239818685032L;


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    protected static TruststoresManager getTruststoresManager ( Context context ) throws ExecutionException {
        TruststoresManager service;
        try {
            service = context.getConfig().getService(TruststoresManager.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("TruststoresManager unavailable", e); //$NON-NLS-1$
        }
        return service;
    }


    private String truststore;


    /**
     * 
     */
    public AbstractTruststoreExcecutionUnit () {
        super();
    }


    /**
     * @return the truststore
     */
    public String getTruststore () {
        return this.truststore;
    }


    /**
     * @param truststore
     *            the truststore to set
     */
    public void setTruststore ( String truststore ) {
        this.truststore = truststore;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);
    
        if ( this.getTruststore() == null ) {
            throw new InvalidParameterException("Truststore must be set"); //$NON-NLS-1$
        }
    
        getTruststoresManager(context);
    }

}