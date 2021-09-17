/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
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
public class SetActiveServiceConfiguration
        extends AbstractExecutionUnit<StatusOnlyResult, SetActiveServiceConfiguration, SetActiveServiceConfigurationConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 242035352320361967L;
    private ServiceStructuralObject service;
    private InstanceStructuralObject anchor;
    private ConfigurationInstance config;
    private boolean noNotify;


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
    public InstanceStructuralObject getAnchor () {
        return this.anchor;
    }


    /**
     * @param anchor
     *            the anchor to set
     */
    void setAnchor ( InstanceStructuralObject anchor ) {
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
     * @param b
     */
    void setNoNotify ( boolean b ) {
        this.noNotify = b;
    }


    /**
     * @return the notify
     */
    public boolean isNoNotify () {
        return this.noNotify;
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

        ConfigRepository cfgRepo;
        try {
            cfgRepo = context.getConfig().getService(ConfigRepository.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Failed to get ConfigRepository service", e); //$NON-NLS-1$
        }

        ConfigEventProducer eventProducer;
        try {
            eventProducer = context.getConfig().getService(ConfigEventProducer.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Failed to get ConfigEventProducer service", e); //$NON-NLS-1$
        }

        try {
            cfgRepo.setActiveConfiguration(this.service, this.config);
        }
        catch ( ConfigRepositoryException e ) {
            throw new ExecutionException("Failed to set active service configuration", e); //$NON-NLS-1$
        }

        if ( !this.noNotify ) {
            eventProducer.configApplied(getAnchor(), getService(), this.getConfig());
        }
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public SetActiveServiceConfigurationConfigurator createConfigurator () {
        return new SetActiveServiceConfigurationConfigurator(this);
    }

}
