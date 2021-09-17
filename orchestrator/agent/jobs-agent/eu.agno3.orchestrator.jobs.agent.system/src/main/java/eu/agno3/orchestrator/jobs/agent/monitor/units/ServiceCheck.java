/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.01.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.monitor.units;


import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.jobs.agent.monitor.ServiceMonitor;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;


/**
 * @author mbechler
 *
 */
public class ServiceCheck extends AbstractExecutionUnit<StatusOnlyResult, ServiceCheck, ServiceCheckConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 4326026230609577081L;

    private StructuralObjectReference service;
    private long timeout = 30000;


    /**
     * @return the service
     */
    public StructuralObjectReference getService () {
        return this.service;
    }


    /**
     * @param service
     *            the service to set
     */
    void setService ( StructuralObjectReference service ) {
        this.service = service;
    }


    /**
     * @return the timeout
     */
    public long getTimeout () {
        return this.timeout;
    }


    /**
     * @param timeout
     *            the timeout to set
     */
    void setTimeout ( long timeout ) {
        this.timeout = timeout;
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
        if ( context.getConfig().isDryRun() ) {
            return new StatusOnlyResult(Status.SKIPPED);
        }
        try {
            ServiceMonitor sm = context.getConfig().getService(ServiceMonitor.class);
            context.getOutput().info(String.format("Checking service %s (%s)", this.service.getLocalType(), this.service.getId())); //$NON-NLS-1$
            ServiceRuntimeStatus status = sm.checkServiceActive(this.service, this.timeout);
            if ( status == ServiceRuntimeStatus.ACTIVE || status == ServiceRuntimeStatus.WARNING ) {
                return new StatusOnlyResult(Status.SUCCESS);
            }
            context.getOutput().error("Service status is " + status); //$NON-NLS-1$
            if ( context.getConfig().isDryRun() && ( status == ServiceRuntimeStatus.DISABLED || status == ServiceRuntimeStatus.UNKNOWN ) ) {
                return new StatusOnlyResult(Status.SUCCESS);
            }

            if ( status == ServiceRuntimeStatus.ERROR ) {
                context.getOutput().error("Service has errors"); //$NON-NLS-1$
            }

            return new StatusOnlyResult(Status.FAIL);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Failed to get service monitor", e); //$NON-NLS-1$
        }
        catch ( InterruptedException e ) {
            throw new ExecutionException("Interrupted while checking", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public ServiceCheckConfigurator createConfigurator () {
        return new ServiceCheckConfigurator(this);
    }

}
