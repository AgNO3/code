/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.09.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service.units;


import java.util.HashSet;
import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
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
public class ReloadServiceConfig extends AbstractExecutionUnit<StatusOnlyResult, ReloadServiceConfig, ReloadServiceConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -5329627861396065356L;

    private Set<String> modifiedPids = new HashSet<>();
    private StructuralObjectReference service;
    private boolean forceRestart;


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
    void setService ( ServiceStructuralObject service ) {
        this.service = StructuralObjectReferenceImpl.fromObject(service);
    }


    /**
     * @return the modifiedPids
     */
    public Set<String> getModifiedPids () {
        return this.modifiedPids;
    }


    /**
     * @param modifiedPids
     *            the modifiedPids to set
     */
    void setModifiedPids ( Set<String> modifiedPids ) {
        this.modifiedPids = modifiedPids;
    }


    /**
     * @return the forceRestart
     */
    public boolean isForceRestart () {
        return this.forceRestart;
    }


    /**
     * @param forceRestart
     *            the forceRestart to set
     */
    void setForceRestart ( boolean forceRestart ) {
        this.forceRestart = forceRestart;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        getServiceManager(context);
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        RuntimeServiceManager rsm = getServiceManager(context);

        if ( getModifiedPids().isEmpty() ) {
            context.getOutput().info("Nothing modified, not reloading service"); //$NON-NLS-1$
            return new StatusOnlyResult(Status.SKIPPED);
        }

        try {
            ServiceRuntimeStatus runtimeStatus = rsm.getRuntimeStatus(null);

            if ( runtimeStatus == ServiceRuntimeStatus.DISABLED ) {
                context.getOutput().info("Service is disabled, skipped"); //$NON-NLS-1$
                return new StatusOnlyResult(Status.SKIPPED);
            }
            else if ( runtimeStatus == ServiceRuntimeStatus.ACTIVE || runtimeStatus == ServiceRuntimeStatus.TRANSIENT ) {

                if ( !this.forceRestart && rsm.isOnlineReconfigurable(getModifiedPids()) ) {
                    context.getOutput().info("Service is active, can reload"); //$NON-NLS-1$
                    context.getOutput().info("Reloading " + getModifiedPids()); //$NON-NLS-1$
                    try {
                        rsm.reconfigure(getModifiedPids());
                    }
                    catch ( ServiceManagementException ex ) {
                        context.getOutput().error("Cannot gracefully reload, restarting service", ex); //$NON-NLS-1$
                        rsm.restart(null);
                    }
                }
                else {
                    context.getOutput().info("Service is active, restart required for " + getService().getLocalType()); //$NON-NLS-1$
                    rsm.restart(null);
                }
            }
            else {
                context.getOutput().error("Service is not active, trying to restart " + getService().getLocalType()); //$NON-NLS-1$
                rsm.restart(null);
            }
        }
        catch ( ServiceManagementException e ) {
            throw new ExecutionException("Failed to reload service configuration for " + getService().getLocalType(), e); //$NON-NLS-1$
        }
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    private RuntimeServiceManager getServiceManager ( Context context ) throws ExecutionException {
        RuntimeServiceManager rsm;
        try {
            ServiceManager sm = context.getConfig().getService(ServiceManager.class);
            rsm = sm.getServiceManager(this.service, RuntimeServiceManager.class);
        }
        catch (
            NoSuchServiceException |
            ServiceManagementException e ) {
            throw new ExecutionException("Failed to get service manager", e); //$NON-NLS-1$
        }
        return rsm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public ReloadServiceConfigurator createConfigurator () {
        return new ReloadServiceConfigurator(this);
    }

}
