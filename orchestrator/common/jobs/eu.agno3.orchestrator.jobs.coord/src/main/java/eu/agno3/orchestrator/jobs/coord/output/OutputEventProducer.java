/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.output;


import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.JobProgressInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;


/**
 * @author mbechler
 * 
 */
public class OutputEventProducer implements JobOutputHandler {

    /**
     * 
     */
    private static final int SHORT_TIMEOUT = 10000;
    private static final int LONG_TIMEOUT = 30000;

    private static final Logger log = Logger.getLogger(OutputEventProducer.class);

    private static final float MIN_DELTA = 1.0f;
    private static final float MAX_DELTA = 10.0f;
    static final long MIN_TIME_DELTA = 1000;

    private Job job;
    private OutputManager outputManager;
    private Deque<JobProgressInfo> progressQueue = new ConcurrentLinkedDeque<>();
    private Deque<OutputQueueEntry> outputQueue = new ConcurrentLinkedDeque<>();

    private float curProgress = 0.0f;
    private long lastUpdate = 0;

    private long infoPosition;
    private long warnPosition;
    private long errorPosition;

    private ProgressThread progressThread;
    private boolean eof;


    /**
     * @param j
     * @param outputManager
     */
    public OutputEventProducer ( Job j, OutputManager outputManager ) {
        this.job = j;
        this.outputManager = outputManager;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @return the job
     */
    Job getJob () {
        return this.job;
    }


    /**
     * @return the outputManager
     */
    OutputManager getOutputManager () {
        return this.outputManager;
    }


    /**
     * @return the progressQueue
     */
    Deque<JobProgressInfo> getProgressQueue () {
        return this.progressQueue;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#start()
     */
    @Override
    public void start () {
        this.progressThread = new ProgressThread(this, this.getJob().getJobId());
        this.progressThread.start();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#end()
     */
    @Override
    public void end () {
        if ( this.progressThread != null ) {

            synchronized ( this.progressQueue ) {
                JobProgressInfoImpl progressInfo = new JobProgressInfoImpl();
                progressInfo.setProgress(100.0f);
                progressInfo.setState(JobState.FINISHED);
                this.progressQueue.add(progressInfo);
            }
            eof();

            this.progressThread.shutdown();
            try {
                log.debug("Waiting for progress thread"); //$NON-NLS-1$
                try {
                    this.progressThread.join(SHORT_TIMEOUT);
                }
                catch ( InterruptedException e ) {
                    log.trace("Interrupted", e); //$NON-NLS-1$
                }

                this.progressThread.interrupt();

                if ( this.progressThread.isAlive() ) {
                    log.warn("Progress thread did not exit within 10s, interrupting again"); //$NON-NLS-1$
                    this.progressThread.interrupt();
                    this.progressThread.join(LONG_TIMEOUT);
                }

                log.debug("Progress thread joined"); //$NON-NLS-1$
            }
            catch ( InterruptedException e ) {
                log.warn("Thread join was interrupted:", e); //$NON-NLS-1$
            }
        }
        this.progressThread = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#eof()
     */
    @Override
    public synchronized void eof () {
        if ( !this.eof ) {
            this.eof = true;
            this.outputQueue.add(new OutputQueueEntry());
        }
        else {
            log.debug("Already EOF"); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#setProgress(eu.agno3.orchestrator.jobs.JobProgressInfo)
     */
    @Override
    public void setProgress ( JobProgressInfo info ) {

        float percent = info.getProgress();

        if ( log.isTraceEnabled() ) {
            log.trace("Progress @ " + percent); //$NON-NLS-1$
        }

        if ( ( percent - this.curProgress ) < MIN_DELTA ) {
            return;
        }

        if ( ( percent - this.curProgress ) < MAX_DELTA && isTooEarly() ) {
            return;
        }

        synchronized ( this.progressQueue ) {
            JobProgressInfoImpl progressInfo = new JobProgressInfoImpl();
            progressInfo.setProgress(percent);
            progressInfo.setState(info.getState());
            this.progressQueue.add(progressInfo);
        }

    }


    private boolean isTooEarly () {
        return this.lastUpdate != 0 && System.currentTimeMillis() < this.lastUpdate + MIN_TIME_DELTA;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logBuffer(eu.agno3.orchestrator.jobs.msg.JobOutputLevel,
     *      java.lang.String)
     */
    @Override
    public void logBuffer ( JobOutputLevel l, String buffer ) {
        String sanitizeMessage = sanitizeMessage(buffer, false);
        long pos;
        switch ( l ) {
        case ERROR:
            this.errorPosition += sanitizeMessage.length();
            pos = this.errorPosition;
            break;
        case INFO:
            this.infoPosition += sanitizeMessage.length();
            pos = this.infoPosition;
            break;
        case WARNING:
            this.warnPosition += sanitizeMessage.length();
            pos = this.warnPosition;
            break;
        default:
            throw new IllegalArgumentException();
        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Sending buffer with level %s position %d", l, pos)); //$NON-NLS-1$
        }
        this.outputQueue.add(new OutputQueueEntry(sanitizeMessage, l, pos));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineInfo(java.lang.String)
     */
    @Override
    public void logLineInfo ( String msg ) {
        getLog().debug(msg != null ? msg : StringUtils.EMPTY);
        String sanitizeMessage = sanitizeMessage(msg, true);
        if ( !sanitizeMessage.isEmpty() ) {
            this.infoPosition += sanitizeMessage.length();
            this.outputQueue.add(new OutputQueueEntry(sanitizeMessage, JobOutputLevel.INFO, this.infoPosition));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineInfo(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void logLineInfo ( String msg, Throwable t ) {
        getLog().debug(msg != null ? msg : StringUtils.EMPTY, t);
        String sanitizeMessage = sanitizeMessage(msg, true);
        if ( !sanitizeMessage.isEmpty() ) {
            this.infoPosition += sanitizeMessage.length();
            this.outputQueue.add(new OutputQueueEntry(sanitizeMessage, JobOutputLevel.INFO, this.infoPosition));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineWarn(java.lang.String)
     */
    @Override
    public void logLineWarn ( String msg, Throwable t ) {
        getLog().warn(msg != null ? msg : StringUtils.EMPTY, t);
        String sanitizeMessage = sanitizeMessage(msg, true);
        if ( !sanitizeMessage.isEmpty() ) {
            this.warnPosition += sanitizeMessage.length();
            this.outputQueue.add(new OutputQueueEntry(sanitizeMessage, JobOutputLevel.WARNING, this.warnPosition));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineWarn(java.lang.String)
     */
    @Override
    public void logLineWarn ( String msg ) {
        getLog().warn(msg != null ? msg : StringUtils.EMPTY);
        String sanitizeMessage = sanitizeMessage(msg, true);
        if ( !sanitizeMessage.isEmpty() ) {
            this.warnPosition += sanitizeMessage.length();
            this.outputQueue.add(new OutputQueueEntry(sanitizeMessage, JobOutputLevel.WARNING, this.warnPosition));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineError(java.lang.String)
     */
    @Override
    public void logLineError ( String msg, Throwable t ) {
        getLog().error(msg != null ? msg : StringUtils.EMPTY, t);
        String sanitizeMessage = sanitizeMessage(msg, true);
        if ( !sanitizeMessage.isEmpty() ) {
            this.errorPosition += sanitizeMessage.length();
            this.outputQueue.add(new OutputQueueEntry(sanitizeMessage, JobOutputLevel.ERROR, this.errorPosition));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineError(java.lang.String)
     */
    @Override
    public void logLineError ( String msg ) {
        getLog().error(msg != null ? msg : StringUtils.EMPTY);
        String sanitizeMessage = sanitizeMessage(msg, true);
        if ( !sanitizeMessage.isEmpty() ) {
            this.errorPosition += sanitizeMessage.length();
            this.outputQueue.add(new OutputQueueEntry(sanitizeMessage, JobOutputLevel.ERROR, this.errorPosition));
        }
    }


    /**
     * As per woodstox parser, strip/replace characters which are not valid in XML cdata
     * 
     * @param msg
     * @return
     */
    private static String sanitizeMessage ( String msg, boolean newline ) {
        if ( msg == null ) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for ( char c : msg.toCharArray() ) {
            if ( illegalCharacter(c) ) {
                continue;
            }

            if ( illegalWhitespaceCharacter(c) ) {
                sb.append(' ');
                continue;
            }

            sb.append(c);
        }

        if ( sb.length() == 0 ) {
            return StringUtils.EMPTY;
        }

        if ( newline && sb.charAt(sb.length() - 1) != '\n' ) {
            sb.append('\n');
        }
        return sb.toString();
    }


    private static boolean illegalCharacter ( char c ) {
        return c == 0 || c > 0x10FFFF || c >= 0xD800 && c <= 0xDFFF;
    }


    private static boolean illegalWhitespaceCharacter ( char c ) {
        return ( c != '\n' && c < ' ' ) || ( c >= 0x7F && c <= 0x9F );
    }


    /**
     * @param progress
     */
    protected void updated ( float progress ) {
        this.lastUpdate = System.currentTimeMillis();
        this.curProgress = progress;
    }


    /**
     * @return
     */
    protected Deque<OutputQueueEntry> getOutputQueue () {
        return this.outputQueue;
    }

}
