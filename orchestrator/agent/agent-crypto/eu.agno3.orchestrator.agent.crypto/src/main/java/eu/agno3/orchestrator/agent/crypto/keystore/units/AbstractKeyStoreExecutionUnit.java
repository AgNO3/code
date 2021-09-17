/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.system.base.execution.Configurator;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;


/**
 * @author mbechler
 * @param <TResult>
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 *
 */
public abstract class AbstractKeyStoreExecutionUnit <TResult extends Result, TExecutionUnit extends ExecutionUnit<TResult, TExecutionUnit, TConfigurator>, TConfigurator extends Configurator<TResult, TExecutionUnit, TConfigurator>>
        extends AbstractExecutionUnit<TResult, TExecutionUnit, TConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 2325345583519797843L;
    private String keystoreName;


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    protected static KeystoresManager getKeystoresManager ( Context context ) throws ExecutionException {
        try {
            return context.getConfig().getService(KeystoresManager.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("KeystoresManager not available", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the keystoreName
     */
    public String getKeystoreName () {
        return this.keystoreName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( StringUtils.isEmpty(this.keystoreName) ) {
            throw new InvalidUnitConfigurationException("Key store name is required"); //$NON-NLS-1$
        }

        getKeystoresManager(context);
    }


    /**
     * @param keystoreName
     *            the keystoreName to set
     */
    protected void setKeystoreName ( String keystoreName ) {
        this.keystoreName = keystoreName;
    }


    /**
     * 
     */
    public AbstractKeyStoreExecutionUnit () {
        super();
    }

}