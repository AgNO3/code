/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.system.info.units;


import eu.agno3.orchestrator.agent.system.info.SystemInformationRefresher;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class RefreshSystemInformation
        extends AbstractExecutionUnit<StatusOnlyResult, RefreshSystemInformation, RefreshSystemInformationConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -630119073775684164L;
    private boolean rescanPartitions;
    private boolean ignoreError;


    /**
     * @param b
     */
    void setRescanPartitions ( boolean b ) {
        this.rescanPartitions = b;
    }


    /**
     * @return the rescanPartitions
     */
    public boolean isRescanPartitions () {
        return this.rescanPartitions;
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
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        try {
            context.getConfig().getService(SystemInformationRefresher.class);
        }
        catch ( NoSuchServiceException e ) {
            if ( isIgnoreError() ) {
                return new StatusOnlyResult(Status.SUCCESS);
            }
            throw new ExecutionException("System information service not found", e); //$NON-NLS-1$
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
            context.getOutput().info("Refreshing system information"); //$NON-NLS-1$
            context.getConfig().getService(SystemInformationRefresher.class).triggerRefresh(isRescanPartitions());
        }
        catch ( NoSuchServiceException e ) {
            if ( isIgnoreError() ) {
                return new StatusOnlyResult(Status.SUCCESS);
            }
            throw new ExecutionException("System information service not found", e); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            if ( isIgnoreError() ) {
                return new StatusOnlyResult(Status.SUCCESS);
            }
            throw e;
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public RefreshSystemInformationConfigurator createConfigurator () {
        return new RefreshSystemInformationConfigurator(this);
    }

}
