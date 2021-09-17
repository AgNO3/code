/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.service;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.service.Service;
import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.base.service.ServiceResult;
import eu.agno3.orchestrator.system.base.service.ServiceSystem;


/**
 * @author mbechler
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 * 
 */
@SuppressWarnings ( "serial" )
public abstract class AbstractServiceExecutionUnit <TExecutionUnit extends AbstractServiceExecutionUnit<TExecutionUnit, TConfigurator>, TConfigurator extends AbstractServiceConfigurator<TExecutionUnit, TConfigurator>>
        extends AbstractExecutionUnit<ServiceResult, TExecutionUnit, TConfigurator> {

    private String serviceName;
    private String instance;


    /**
     * @return the serviceName
     */
    public String getServiceName () {
        return this.serviceName;
    }


    /**
     * @param serviceName
     *            the serviceName to set
     */
    void setServiceName ( String serviceName ) {
        this.serviceName = serviceName;
    }


    /**
     * @return the instance
     */
    public String getInstance () {
        return this.instance;
    }


    /**
     * @param instance
     *            the instance to set
     */
    void setInstance ( String instance ) {
        this.instance = instance;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( context.getConfig().isNoVerifyEnv() ) {
            return;
        }

        getReferencedService(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public ServiceResult prepare ( Context context ) throws ExecutionException {

        if ( context.getConfig().isDryRun() ) {
            return new ServiceResult(Status.SUCCESS);
        }

        this.getReferencedService(context);
        return new ServiceResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public ServiceResult execute ( Context context ) throws ExecutionException {
        if ( context.getConfig().isDryRun() ) {
            return new ServiceResult(Status.SUCCESS);
        }

        boolean res = this.doWithService(context, this.getReferencedService(context));
        if ( res ) {
            return new ServiceResult(Status.SUCCESS);
        }

        return new ServiceResult(Status.SKIPPED);
    }


    /**
     * Called during execution with the appropritate service object when not in dry run mode
     * 
     * @param context
     * @param s
     * @return whether actual work was done (=> success result) or not ( => skipped result )
     */
    protected abstract boolean doWithService ( Context context, Service s ) throws ExecutionException;


    protected Service getReferencedService ( Context context ) throws ExecutionException {
        try {
            ServiceSystem init = context.getConfig().getService(ServiceSystem.class);

            if ( this.getInstance() != null ) {
                return init.getService(this.getServiceName(), this.getInstance());
            }

            return init.getService(this.getServiceName());
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("No service system is registered", e); //$NON-NLS-1$
        }
        catch ( ServiceException e ) {
            throw new ExecutionException("No such service exists", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     */
    protected String getServiceDescription () {
        if ( this.getInstance() != null ) {
            return String.format("%s/%s", this.getServiceName(), this.getInstance()); //$NON-NLS-1$
        }
        return this.getServiceName();
    }
}
