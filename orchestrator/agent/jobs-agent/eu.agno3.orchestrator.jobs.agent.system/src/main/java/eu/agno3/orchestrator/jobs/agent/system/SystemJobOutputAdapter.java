/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.system.base.execution.output.Out;


/**
 * @author mbechler
 * 
 */
public class SystemJobOutputAdapter implements Out, AutoCloseable {

    private JobOutputHandler outHandler;


    /**
     * @param outHandler
     */
    public SystemJobOutputAdapter ( JobOutputHandler outHandler ) {
        this.outHandler = outHandler;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#debug(java.lang.String)
     */
    @Override
    public void debug ( String msg ) {
        // ignored
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#debug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void debug ( String msg, Throwable t ) {
        // ignored
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#info(java.lang.String)
     */
    @Override
    public void info ( String msg ) {
        this.outHandler.logLineInfo(msg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#info(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void info ( String msg, Throwable t ) {
        this.outHandler.logLineInfo(formatException(msg, t), t);
    }


    /**
     * @param msg
     * @param t
     * @return
     */
    private static String formatException ( String msg, Throwable t ) {
        return String.format("%s: exception %s: %s", msg, t.getClass().getName(), t.getMessage()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#error(java.lang.String)
     */
    @Override
    public void error ( String msg ) {
        this.outHandler.logLineError(msg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#error(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void error ( String msg, Throwable t ) {
        this.outHandler.logLineError(formatException(msg, t), t);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {
        this.outHandler.eof();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#getChild(java.lang.String)
     */
    @Override
    public Out getChild ( String name ) {
        return this;
    }

}
