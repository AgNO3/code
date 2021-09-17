/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.console;


import java.io.PrintStream;

import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;


/**
 * @author mbechler
 *
 */
public class ConsoleJobOutputHandler implements JobOutputHandler {

    private PrintStream console;


    /**
     * @param console
     */
    public ConsoleJobOutputHandler ( PrintStream console ) {
        this.console = console;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#eof()
     */
    @Override
    public void eof () {

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
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#setProgress(eu.agno3.orchestrator.jobs.JobProgressInfo)
     */
    @Override
    public void setProgress ( JobProgressInfo jobProgressInfo ) {
        // TODO Auto-generated method stub

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineInfo(java.lang.String)
     */
    @Override
    public void logLineInfo ( String msg ) {
        this.console.println(msg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineInfo(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void logLineInfo ( String msg, Throwable t ) {
        this.console.println(msg);
        t.printStackTrace(this.console);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineWarn(java.lang.String)
     */
    @Override
    public void logLineWarn ( String msg ) {
        this.console.println(msg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineWarn(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void logLineWarn ( String msg, Throwable t ) {
        this.console.println(msg);
        t.printStackTrace(this.console);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineError(java.lang.String)
     */
    @Override
    public void logLineError ( String msg ) {
        this.console.println(msg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logLineError(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void logLineError ( String msg, Throwable t ) {
        this.console.println(msg);
        t.printStackTrace(this.console);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobOutputHandler#logBuffer(eu.agno3.orchestrator.jobs.msg.JobOutputLevel,
     *      java.lang.String)
     */
    @Override
    public void logBuffer ( JobOutputLevel l, String buffer ) {
        this.console.print(buffer);
    }

}
