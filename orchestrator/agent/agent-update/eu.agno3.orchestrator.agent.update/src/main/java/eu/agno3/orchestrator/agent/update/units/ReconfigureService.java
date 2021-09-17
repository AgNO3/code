/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.03.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import eu.agno3.orchestrator.agent.update.ServiceReconfigurator;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class ReconfigureService extends AbstractExecutionUnit<StatusOnlyResult, ReconfigureService, ServiceReconfigureConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 204836754390567430L;

    private StructuralObjectReference service;
    private boolean ignoreError;

    private boolean allowRestart;


    /**
     * @param service
     *            the service to set
     */
    void setService ( StructuralObjectReference service ) {
        this.service = service;
    }


    /**
     * @return the service
     */
    public StructuralObjectReference getService () {
        return this.service;
    }


    /**
     * @param ignoreError
     *            the ignoreError to set
     */
    void setIgnoreError ( boolean ignoreError ) {
        this.ignoreError = ignoreError;
    }


    /**
     * @return the ignoreError
     */
    public boolean isIgnoreError () {
        return this.ignoreError;
    }


    /**
     * @param allowRestart
     *            the allowRestart to set
     */
    void setAllowRestart ( boolean allowRestart ) {
        this.allowRestart = allowRestart;
    }


    /**
     * @return the allowRestart
     */
    public boolean isAllowRestart () {
        return this.allowRestart;
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
            throw new InvalidUnitConfigurationException("Service must be set"); //$NON-NLS-1$
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
        ServiceReconfigurator sr;
        try {
            sr = context.getConfig().getService(ServiceReconfigurator.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Failed to get reconfigurator", e); //$NON-NLS-1$
        }

        try {
            sr.reconfigure(context, isAllowRestart(), this.service);
            return new StatusOnlyResult(Status.SUCCESS);
        }
        catch (
            ModelServiceException |
            ConfigRepositoryException |
            JobBuilderException e ) {

            if ( isIgnoreError() ) {
                context.getOutput().info("Failed to reconfigure service", e); //$NON-NLS-1$
                return new StatusOnlyResult(Status.SUCCESS);
            }
            context.getOutput().error("Failed to reconfigure service", e); //$NON-NLS-1$
            throw new ExecutionException("Failed to reconfigure service", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public ServiceReconfigureConfigurator createConfigurator () {
        return new ServiceReconfigureConfigurator(this);
    }

}
