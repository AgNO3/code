/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import org.joda.time.DateTime;

import eu.agno3.orchestrator.agent.update.UpdateInstallation;
import eu.agno3.orchestrator.agent.update.UpdateTracker;
import eu.agno3.orchestrator.jobs.agent.backup.units.BackupResult;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.Predicate;
import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class TrackUpdate extends AbstractExecutionUnit<StatusOnlyResult, TrackUpdate, TrackUpdateConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -7977678449480517779L;

    private long updatedSequence;
    private String updatedStream;
    private DateTime updateTimestamp;

    private Predicate suggestRebootPredicate;

    private ResultReference<BackupResult> backupReference;


    /**
     * @return the updatedSequence
     */
    public long getUpdatedSequence () {
        return this.updatedSequence;
    }


    /**
     * @param updatedSequence
     *            the updatedSequence to set
     */
    void setUpdatedSequence ( long updatedSequence ) {
        this.updatedSequence = updatedSequence;
    }


    /**
     * @return the updatedStream
     */
    public String getUpdatedStream () {
        return this.updatedStream;
    }


    /**
     * @param updatedStream
     *            the updatedStream to set
     */
    void setUpdatedStream ( String updatedStream ) {
        this.updatedStream = updatedStream;
    }


    /**
     * @return the updateTimestamp
     */
    public DateTime getUpdateTimestamp () {
        return this.updateTimestamp;
    }


    /**
     * @param updateTimestamp
     *            the updateTimestamp to set
     */
    void setUpdateTimestamp ( DateTime updateTimestamp ) {
        this.updateTimestamp = updateTimestamp;
    }


    /**
     * @param predicate
     */
    void setSuggestRebootPredicate ( Predicate predicate ) {
        this.suggestRebootPredicate = predicate;
    }


    /**
     * @param context
     * @return the suggestRebootPredicate
     */
    public boolean isSuggestRebootPredicate ( Context context ) {
        if ( this.suggestRebootPredicate != null ) {
            return this.suggestRebootPredicate.evaluate(context);
        }
        return false;
    }


    /**
     * @param br
     */
    void setBackupReference ( ResultReference<BackupResult> br ) {
        this.backupReference = br;
    }


    /**
     * @return the backupReference
     */
    public ResultReference<BackupResult> getBackupReference () {
        return this.backupReference;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( this.updatedSequence <= 0 ) {
            throw new InvalidUnitConfigurationException("updatedSequence must be > 0"); //$NON-NLS-1$
        }

        if ( this.updatedStream == null ) {
            throw new InvalidUnitConfigurationException("updatedStream cannot be null"); //$NON-NLS-1$
        }

        if ( this.updateTimestamp == null ) {
            throw new InvalidUnitConfigurationException("updateTimestamp cannot be null"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        getUpdateTracker(context);
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     */
    private static UpdateTracker getUpdateTracker ( Context context ) throws ExecutionException {
        try {
            return context.getConfig().getService(UpdateTracker.class);
        }
        catch ( NoSuchServiceException e ) {
            throw new ExecutionException("Update tracker not found", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        UpdateInstallation inst = new UpdateInstallation();
        inst.setSequence(this.updatedSequence);
        inst.setStream(this.updatedStream);
        inst.setInstallDate(this.updateTimestamp);
        if ( this.backupReference != null ) {
            inst.setReferenceBackupId(context.fetchResult(this.backupReference).getBackupId());
        }
        getUpdateTracker(context).updated(inst, isSuggestRebootPredicate(context));
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public TrackUpdateConfigurator createConfigurator () {
        return new TrackUpdateConfigurator(this);
    }

}
