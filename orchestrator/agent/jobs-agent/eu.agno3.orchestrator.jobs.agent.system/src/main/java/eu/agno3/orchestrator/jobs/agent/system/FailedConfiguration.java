/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.RequiredParameterMissingException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class FailedConfiguration extends AbstractExecutionUnit<StatusOnlyResult, FailedConfiguration, FailedConfigurationConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 242035352320361967L;
    private ServiceStructuralObject service;
    private StructuralObject anchor;
    private ConfigurationInstance config;


    /**
     * @return the service
     */
    public ServiceStructuralObject getService () {
        return this.service;
    }


    /**
     * @param service
     *            the service to set
     */
    void setService ( @NonNull ServiceStructuralObject service ) {
        this.service = service;
    }


    /**
     * @return the anchor
     */
    public StructuralObject getAnchor () {
        return this.anchor;
    }


    /**
     * @param anchor
     *            the anchor to set
     */
    void setAnchor ( StructuralObject anchor ) {
        this.anchor = anchor;
    }


    /**
     * @return the config
     */
    public ConfigurationInstance getConfig () {
        return this.config;
    }


    /**
     * @param config
     *            the config to set
     */
    void setConfig ( @NonNull ConfigurationInstance config ) {
        this.config = config;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( this.service == null ) {
            throw new RequiredParameterMissingException("Service is not set"); //$NON-NLS-1$
        }

        if ( this.config == null ) {
            throw new RequiredParameterMissingException("Config is not set"); //$NON-NLS-1$
        }

        try {
            context.getConfig().getService(ConfigRepository.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new InvalidUnitConfigurationException("No ConfigRepository service available", e); //$NON-NLS-1$
        }

        try {
            context.getConfig().getService(ConfigEventProducer.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new InvalidUnitConfigurationException("No ConfigEventProducer service available", e); //$NON-NLS-1$
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
        context.getOutput().info("Setting active configuration"); //$NON-NLS-1$

        ConfigEventProducer eventProducer;
        try {
            eventProducer = context.getConfig().getService(ConfigEventProducer.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Failed to get ConfigEventProducer service", e); //$NON-NLS-1$
        }

        eventProducer.configFailed(getAnchor(), getService(), this.getConfig());
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public FailedConfigurationConfigurator createConfigurator () {
        return new FailedConfigurationConfigurator(this);
    }

}
