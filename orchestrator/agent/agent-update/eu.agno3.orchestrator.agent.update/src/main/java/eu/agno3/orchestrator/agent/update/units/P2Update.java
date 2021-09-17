/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import java.net.URI;
import java.util.Set;

import eu.agno3.orchestrator.agent.update.RuntimeServiceUpdater;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Predicate;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.update.Feature;
import eu.agno3.runtime.update.UpdateException;


/**
 * @author mbechler
 *
 */
public class P2Update extends AbstractExecutionUnit<StatusOnlyResult, P2Update, P2UpdateConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 3568030885000671844L;
    private StructuralObjectReference targetService;
    private Predicate forceOffline;
    private Set<URI> repositories;
    private Set<Feature> targets;
    private boolean noApply;
    private boolean shouldRun;


    /**
     * @return the target service
     */
    public StructuralObjectReference getTargetService () {
        return this.targetService;
    }


    /**
     * @param targetService
     *            the targetService to set
     */
    void setTargetService ( ServiceStructuralObject targetService ) {
        this.targetService = StructuralObjectReferenceImpl.fromObject(targetService);
    }


    /**
     * @param ctx
     * @return the forceOffline
     */
    public boolean isForceOffline ( Context ctx ) {
        if ( this.forceOffline == null ) {
            return false;
        }
        return this.forceOffline.evaluate(ctx);
    }


    /**
     * @param restartPredicate
     */
    void setForceOffline ( Predicate restartPredicate ) {
        this.forceOffline = restartPredicate;
    }


    /**
     * @return the repositories
     */
    public Set<URI> getRepositories () {
        return this.repositories;
    }


    /**
     * @param repositories
     */
    void setRepositories ( Set<URI> repositories ) {
        this.repositories = repositories;
    }


    /**
     * @return the targets
     */
    public Set<Feature> getTargets () {
        return this.targets;
    }


    /**
     * @param targets
     */
    void setTargets ( Set<Feature> targets ) {
        this.targets = targets;
    }


    /**
     * @param noApply
     */
    void setNoApply ( boolean noApply ) {
        this.noApply = noApply;
    }


    /**
     * @return the noApply
     */
    public boolean isNoApply () {
        return this.noApply;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        context.getOutput().info("Preparing update for service " + this.getTargetService().getLocalType()); //$NON-NLS-1$
        RuntimeServiceUpdater serviceUpdater = getRuntimeServiceUpdater(context);
        try {
            this.shouldRun = serviceUpdater
                    .prepareUpdate(this.getTargetService(), this.getRepositories(), this.getTargets(), new JobOutputProgressMonitor(context));
            if ( !this.shouldRun ) {
                context.getOutput().info("Nothing to be updated for service " + this.getTargetService().getLocalType()); //$NON-NLS-1$
            }
        }
        catch (
            ServiceManagementException |
            UpdateException e ) {
            throw new ExecutionException("Failed to validate updates", e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    private static RuntimeServiceUpdater getRuntimeServiceUpdater ( Context context ) throws ExecutionException {
        RuntimeServiceUpdater service;
        try {
            service = context.getConfig().getService(RuntimeServiceUpdater.class);

        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Failed to get update service", e); //$NON-NLS-1$
        }
        return service;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {

        if ( !this.shouldRun ) {
            return new StatusOnlyResult(Status.SKIPPED);
        }

        context.getOutput().info("Executing update for service " + this.getTargetService().getLocalType()); //$NON-NLS-1$
        RuntimeServiceUpdater serviceUpdater = getRuntimeServiceUpdater(context);

        try {
            ServiceManager service = context.getConfig().getService(ServiceManager.class);
            boolean stopped = false;

            try {
                boolean forceRestart = this.isForceOffline(context);
                if ( forceRestart ) {
                    stopped = true;
                    context.getOutput().info("Stopping service " + this.getTargetService().getLocalType()); //$NON-NLS-1$
                    service.stopService(this.getTargetService());
                }

                context.getOutput().info("Updating to " + this.getTargets()); //$NON-NLS-1$

                // if ( !context.getConfig().isDryRun() ) {
                serviceUpdater
                        .installUpdates(this.getTargetService(), this.getRepositories(), this.getTargets(), new JobOutputProgressMonitor(context));

                if ( !forceRestart ) {
                    try {
                        if ( !this.isNoApply() ) {
                            context.getOutput().info("Triggering runtime update for " + this.getTargetService().getLocalType()); //$NON-NLS-1$
                            serviceUpdater.doApplyUpdates(this.getTargetService());
                        }
                    }
                    catch ( UpdateException e ) {
                        context.getOutput().error("Live update failed, restarting service " + e.getMessage(), e); //$NON-NLS-1$
                        stopped = true;
                        service.stopService(this.getTargetService());
                    }
                }

                if ( !this.isNoApply() ) {
                    context.getOutput().info("Running garbage collection on " + this.getTargetService().getLocalType()); //$NON-NLS-1$
                    serviceUpdater.runGarbageCollection(this.getTargetService());
                }
                // }

            }
            finally {
                if ( stopped ) {
                    context.getOutput().info("Starting service " + this.getTargetService().getLocalType()); //$NON-NLS-1$
                    service.startService(this.getTargetService());
                }
            }
        }
        catch (
            ServiceManagementException |
            UpdateException |
            NoSuchServiceException e ) {
            throw new ExecutionException("Failed to install updates", e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public P2UpdateConfigurator createConfigurator () {
        return new P2UpdateConfigurator(this);
    }

}
