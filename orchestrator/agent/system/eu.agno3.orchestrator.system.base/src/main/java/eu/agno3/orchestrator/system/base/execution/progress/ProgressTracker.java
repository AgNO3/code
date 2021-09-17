/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.progress;


import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.jobs.JobProgressInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.MessageContextEntry;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;
import eu.agno3.orchestrator.system.base.execution.ExecutorEventListener;
import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.events.AbstractJobEvent;
import eu.agno3.orchestrator.system.base.execution.events.AbstractPhaseEvent;
import eu.agno3.orchestrator.system.base.execution.events.AbstractUnitEvent;
import eu.agno3.orchestrator.system.base.execution.events.EnterPhaseEvent;
import eu.agno3.orchestrator.system.base.execution.events.EnterUnitEvent;
import eu.agno3.orchestrator.system.base.execution.events.StartJobEvent;


/**
 * @author mbechler
 * 
 */
public class ProgressTracker implements ExecutorEventListener {

    private static final Logger log = Logger.getLogger(ProgressTracker.class);

    private final ProgressEstimator estimator;

    private float minDelta = 0.01f;

    private Job currentJob;
    private Phase currentPhase;
    private int currentUnitPos;
    private float currentUnitProgress;
    private float currentTotalEstimate;


    /**
     * 
     */
    public ProgressTracker () {
        this.estimator = new WeightedProgressEstimator();
    }


    /**
     * @param estimator
     */
    public ProgressTracker ( ProgressEstimator estimator ) {
        this.estimator = estimator;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutorEventListener#onEvent(eu.agno3.orchestrator.system.base.execution.ExecutorEvent)
     */
    @Override
    public void onEvent ( ExecutorEvent ev ) {
        if ( ev instanceof StartJobEvent ) {
            startJob((AbstractJobEvent) ev);
        }
        if ( ev instanceof EnterPhaseEvent ) {
            enterPhase((AbstractPhaseEvent) ev);
        }
        else if ( ev instanceof EnterUnitEvent ) {
            enterUnit((AbstractUnitEvent) ev);
        }
        else if ( ev instanceof ProgressEvent ) {
            handleProgress((ProgressEvent) ev);
        }
        else {
            return;
        }

        if ( this.currentJob == null || this.currentPhase == null ) {
            return;
        }

        if ( updateEstimate() ) {
            Context ctx = ev.getContext();
            JobProgressInfoImpl info = new JobProgressInfoImpl();
            info.setLastUpdate(DateTime.now());
            info.setProgress(this.currentTotalEstimate * 100.0f);
            info.setState(JobState.RUNNING);
            info.setStateMessage(ctx.getStateMessage());
            info.setStateMessageContext(MessageContextEntry.fromMap(ctx.getStateContext()));
            ctx.publishEvent(new JobProgressStatusEvent(ctx, info));
        }
    }


    /**
     * 
     * @return whether an new estimate is available
     */
    protected boolean updateEstimate () {
        float estimate = this.estimator.estimateOverallProgress(this.currentJob, this.currentPhase, this.currentUnitPos, this.currentUnitProgress);
        if ( estimate - this.currentTotalEstimate < this.minDelta ) {
            return false;
        }

        this.currentTotalEstimate = estimate;

        if ( log.isTraceEnabled() ) {
            log.trace(String.format(
                "Phase %s Unit %d/%d Unit %5.1f%% Progress %5.1f%%", //$NON-NLS-1$
                this.currentPhase,
                this.currentUnitPos + 1,
                this.currentJob.getExecutionUnits().size(),
                this.currentUnitProgress * 100,
                estimate * 100));
        }

        return true;
    }


    /**
     * @param ev
     */
    protected void handleProgress ( ProgressEvent ev ) {
        this.currentUnitProgress = Math.max(0.0f, Math.min(1.0f, ( ev ).getProgress() / 100f));
    }


    /**
     * 
     */
    protected void enterUnit ( AbstractUnitEvent ev ) {
        this.currentUnitPos = Math.min(this.currentJob.getExecutionUnits().size() - 1, this.currentUnitPos + 1);
        this.currentUnitProgress = 0.0f;
    }


    /**
     * @param ev
     */
    protected void enterPhase ( AbstractPhaseEvent ev ) {
        this.currentPhase = ev.getPhase();
        this.currentUnitPos = 0;
        this.currentUnitProgress = 0.0f;
    }


    /**
     * @param ev
     */
    protected void startJob ( AbstractJobEvent ev ) {
        this.currentJob = ev.getJob();
        this.currentPhase = Phase.VALIDATE;
        this.currentUnitPos = 0;
        this.currentUnitProgress = 0.0f;
    }

}
