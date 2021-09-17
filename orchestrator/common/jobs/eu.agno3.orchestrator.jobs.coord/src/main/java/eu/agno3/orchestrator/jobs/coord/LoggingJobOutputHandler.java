/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.10.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;


/**
 * @author mbechler
 * 
 */
public final class LoggingJobOutputHandler implements JobOutputHandler {

    private Logger logger;
    private boolean redirectInfoToDebug = false;


    /**
     * @param logger
     */
    public LoggingJobOutputHandler ( Logger logger ) {
        this(logger, false);
    }


    /**
     * 
     * @param logger
     * @param redirectInfoToDebug
     */
    public LoggingJobOutputHandler ( Logger logger, boolean redirectInfoToDebug ) {
        this.logger = logger;
        this.redirectInfoToDebug = redirectInfoToDebug;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#eof()
     */
    @Override
    public void eof () {}


    @Override
    public void setProgress ( JobProgressInfo info ) {
        if ( this.logger.isTraceEnabled() ) {
            this.logger.trace(String.format("Progress %.2f: %s", info.getProgress() * 100, info.getStateMessage())); //$NON-NLS-1$
        }
    }


    @Override
    public void logLineWarn ( String msg ) {
        this.logger.warn(msg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineWarn(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void logLineWarn ( String msg, Throwable t ) {
        this.logger.warn(msg, t);
    }


    @Override
    public void logLineInfo ( String msg ) {
        if ( this.redirectInfoToDebug ) {
            this.logger.debug(msg);
        }
        else {
            this.logger.info(msg);
        }
    }


    @Override
    public void logLineInfo ( String msg, Throwable t ) {
        if ( this.redirectInfoToDebug ) {
            this.logger.debug(msg, t);
        }
        else {
            this.logger.info(msg, t);
        }
    }


    @Override
    public void logLineError ( String msg ) {
        this.logger.error(msg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineError(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void logLineError ( String msg, Throwable t ) {
        this.logger.error(msg, t);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#start()
     */
    @Override
    public void start () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#end()
     */
    @Override
    public void end () {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logBuffer(eu.agno3.orchestrator.jobs.msg.JobOutputLevel,
     *      java.lang.String)
     */
    @Override
    public void logBuffer ( JobOutputLevel l, String buffer ) {
        switch ( l ) {
        case ERROR:
            this.logger.error(buffer);
            break;
        case INFO:
            if ( this.redirectInfoToDebug ) {
                this.logger.debug(buffer);
            }
            else {
                this.logger.info(buffer);
            }
            break;
        case WARNING:
            this.logger.warn(buffer);
            break;
        default:
            break;

        }
    }
}