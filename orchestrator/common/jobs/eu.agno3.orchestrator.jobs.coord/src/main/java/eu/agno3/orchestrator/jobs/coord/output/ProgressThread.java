/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.output;


import java.lang.Thread.UncaughtExceptionHandler;
import java.util.UUID;

import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.runtime.messaging.MessagingException;


class ProgressThread extends Thread implements UncaughtExceptionHandler {

    /**
     * 
     */
    private final OutputEventProducer outputEventProducer;

    private boolean shutdown = false;


    /**
     * @param outputEventProducer
     * @param jobId
     */
    public ProgressThread ( OutputEventProducer outputEventProducer, UUID jobId ) {
        super("Progress event publisher: " + jobId); //$NON-NLS-1$
        this.setUncaughtExceptionHandler(this);
        this.outputEventProducer = outputEventProducer;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException ( Thread t, Throwable e ) {
        OutputEventProducer.getLog().warn("Uncaugt exception in progress thread " + t.getName(), e); //$NON-NLS-1$
    }


    /**
     * 
     */
    public void shutdown () {
        OutputEventProducer.getLog().trace("Shutting down progess thread"); //$NON-NLS-1$
        this.shutdown = true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run () {
        OutputEventProducer.getLog().debug("Starting ProgressThread " + getName()); //$NON-NLS-1$
        try {
            while ( true ) {

                if ( this.shutdown ) {
                    OutputEventProducer.getLog().trace("Exiting main loop"); //$NON-NLS-1$
                    break;
                }

                Thread.sleep(OutputEventProducer.MIN_TIME_DELTA);

                if ( this.shutdown ) {
                    OutputEventProducer.getLog().trace("Exiting main loop"); //$NON-NLS-1$
                    break;
                }

                JobProgressInfo progress = fetchProgress();

                if ( progress != null ) {
                    publishProgressEvent(progress);
                }

                publishOutputQueue();
            }
        }
        catch ( InterruptedException e ) {
            OutputEventProducer.getLog().trace("ProgressThread interrupted:", e); //$NON-NLS-1$
            Thread.interrupted();
        }
        catch ( Exception e ) {
            OutputEventProducer.getLog().debug("ProgressThread exception:", e); //$NON-NLS-1$
        }

        // wait for connection to be reestablished
        long timeout = System.currentTimeMillis() + 5000;
        while ( !this.outputEventProducer.getOutputManager().isAvailable() && System.currentTimeMillis() < timeout ) {
            try {
                Thread.sleep(1000);
            }
            catch ( InterruptedException e ) {
                Thread.interrupted();
                OutputEventProducer.getLog().debug("Interrupted waiting for message client", e); //$NON-NLS-1$
            }
        }

        if ( !this.outputEventProducer.getOutputManager().isAvailable() ) {
            OutputEventProducer.getLog().debug("Timeout waiting for message client"); //$NON-NLS-1$
        }

        try {
            JobProgressInfo progress = fetchProgress();
            if ( progress != null ) {
                publishProgressEvent(progress);
            }
        }
        catch ( Exception e ) {
            OutputEventProducer.getLog().debug("Failed to send last progress", e); //$NON-NLS-1$
        }

        try {
            OutputEventProducer.getLog().debug("Publishing remaining output"); //$NON-NLS-1$
            publishOutputQueue();
        }
        catch ( InterruptedException e ) {
            OutputEventProducer.getLog().debug("Failed to send remaining queue", e); //$NON-NLS-1$
        }

        if ( !this.outputEventProducer.getOutputQueue().isEmpty() ) {
            OutputEventProducer.getLog()
                    .warn(String.format(
                        "Output queue is not empty on exit (%d/%d remain, disconnected: %s)", //$NON-NLS-1$
                        this.outputEventProducer.getOutputQueue().size(),
                        this.outputEventProducer.getProgressQueue().size(),
                        !this.outputEventProducer.getOutputManager().isAvailable()));
        }

        OutputEventProducer.getLog().debug("Exiting ProgressThread"); //$NON-NLS-1$
    }


    /**
     * @throws InterruptedException
     * 
     */
    private void publishOutputQueue () throws InterruptedException {
        OutputQueueEntry haveEof = null;
        OutputQueueEntry out;
        OutputQueueEntry combined = null;
        while ( ( out = fetchOutput() ) != null ) {
            if ( combined == null ) {
                combined = out;
            }
            else if ( out.isEof() ) {
                haveEof = out;
                if ( combined == out ) {
                    combined = null;
                }
            }
            else if ( combined.getOutputLevel() == out.getOutputLevel() ) {
                combined = combineBufferSegments(out, combined);
            }
            else {
                safePublishOutputEvent(combined);
                combined = out;
            }
        }

        if ( combined != null ) {
            safePublishOutputEvent(combined);
        }

        if ( haveEof != null ) {
            OutputEventProducer.getLog().debug("Sending EOF"); //$NON-NLS-1$
            safePublishOutputEvent(haveEof);
        }
        else {
            OutputEventProducer.getLog().debug("Do not have EOF"); //$NON-NLS-1$
        }
    }


    /**
     * @param combined
     * @throws InterruptedException
     */
    private void safePublishOutputEvent ( OutputQueueEntry combined ) throws InterruptedException {
        OutputEventProducer.getLog().trace("Publish output event"); //$NON-NLS-1$
        if ( !publishOutputEvent(combined) ) {
            OutputEventProducer.getLog().debug("Failed to publish output"); //$NON-NLS-1$
            this.outputEventProducer.getOutputQueue().push(combined);
        }
    }


    /**
     * @param out
     * @param combined
     * @return
     */
    private static OutputQueueEntry combineBufferSegments ( OutputQueueEntry out, OutputQueueEntry combined ) {
        if ( out.getOutputPosition() != combined.getOutputPosition() + out.getMsg().length() ) {
            OutputEventProducer.getLog().warn("Non successive outputs"); //$NON-NLS-1$
        }

        StringBuilder combinedText = new StringBuilder();
        combinedText.append(combined.getMsg());
        combinedText.append(out.getMsg());

        return new OutputQueueEntry(combinedText.toString(), combined.getOutputLevel(), out.getOutputPosition());
    }


    /**
     * @param out
     * @return
     * @throws InterruptedException
     */
    private boolean publishOutputEvent ( OutputQueueEntry out ) throws InterruptedException {
        boolean success = false;
        try {
            if ( OutputEventProducer.getLog().isTraceEnabled() ) {
                OutputEventProducer.getLog().trace(String.format(
                    "Publish output event %s len %d off %d", //$NON-NLS-1$
                    out.getOutputLevel(),
                    !out.isEof() ? out.getMsg().length() : 0,
                    out.getOutputPosition()));
            }

            if ( out.isEof() ) {
                OutputEventProducer.getLog().debug("Send EOF"); //$NON-NLS-1$
                success = this.outputEventProducer.getOutputManager().publishEof(this.outputEventProducer.getJob());
            }
            else {
                success = this.outputEventProducer.getOutputManager()
                        .publishOutputEvent(this.outputEventProducer.getJob(), out.getMsg(), out.getOutputLevel(), out.getOutputPosition());
            }
        }
        catch ( MessagingException e ) {
            OutputEventProducer.getLog().warn("Failed to publish output", e); //$NON-NLS-1$
        }
        return success;
    }


    private void publishProgressEvent ( JobProgressInfo progress ) throws InterruptedException {
        try {
            if ( OutputEventProducer.getLog().isTraceEnabled() ) {
                OutputEventProducer.getLog().trace(String.format(
                    "Publish progress event %s %.2f", //$NON-NLS-1$
                    progress.getState(),
                    progress.getProgress()));
            }
            this.outputEventProducer.getOutputManager().publishProgressEvent(this.outputEventProducer.getJob(), progress);
            this.outputEventProducer.updated(progress.getProgress());
        }
        catch ( MessagingException e ) {
            OutputEventProducer.getLog().warn("Failed to publish progress", e); //$NON-NLS-1$
        }
    }


    private JobProgressInfo fetchProgress () {
        JobProgressInfo progress;
        synchronized ( this.outputEventProducer.getProgressQueue() ) {
            if ( this.outputEventProducer.getProgressQueue().isEmpty() ) {
                return null;
            }
            progress = this.outputEventProducer.getProgressQueue().getLast();
            this.outputEventProducer.getProgressQueue().clear();
        }
        return progress;
    }


    private OutputQueueEntry fetchOutput () {
        return this.outputEventProducer.getOutputQueue().poll();
    }
}