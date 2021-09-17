/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.output.Out;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class Kill extends AbstractExecutionUnit<StatusOnlyResult, Kill, KillConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 3529461443065233817L;

    private static final Logger log = Logger.getLogger(Kill.class);

    /**
     * 
     */
    private static final String EXECUTABLE = "/bin/kill"; //$NON-NLS-1$

    /**
     * 
     */
    public static final int NONE = 0;

    /**
     * 
     */
    public static final int HUP = 1;

    /**
     * 
     */
    public static final int INT = 2;

    /**
     * 
     */
    public static final int KILL = 9;

    /**
     * 
     */
    public static final int USR1 = 10;

    /**
     * 
     */
    public static final int USR2 = 12;

    /**
     * 
     */
    public static final int TERM = 15;

    private int pid = -1;
    private int signal = TERM;

    private boolean waitForExit = false;
    private long waitTimeout = 5000;
    private boolean ignoreError = true;


    /**
     * @return the pid
     */
    public int getPid () {
        return this.pid;
    }


    /**
     * @param pid
     *            the pid to set
     */
    void setPid ( int pid ) {
        this.pid = pid;
    }


    /**
     * @return the signal
     */
    public int getSignal () {
        return this.signal;
    }


    /**
     * @param signal
     *            the signal to set
     */
    void setSignal ( int signal ) {
        this.signal = signal;
    }


    /**
     * @return the wait
     */
    public boolean isWaitForExit () {
        return this.waitForExit;
    }


    /**
     * @param waitForExit
     *            the wait to set
     */
    void setWaitForExit ( boolean waitForExit ) {
        this.waitForExit = waitForExit;
    }


    /**
     * @return the waitTimeout
     */
    public long getWaitTimeout () {
        return this.waitTimeout;
    }


    /**
     * @param waitTimeout
     *            the waitTimeout to set
     */
    void setWaitTimeout ( long waitTimeout ) {
        this.waitTimeout = waitTimeout;
    }


    /**
     * @return the ignoreError
     */
    public boolean isIgnoreError () {
        return this.ignoreError;
    }


    /**
     * @param ignoreError
     *            the ignoreError to set
     */
    void setIgnoreError ( boolean ignoreError ) {
        this.ignoreError = ignoreError;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( getPid() < 0 ) {
            throw new InvalidUnitConfigurationException("PID is required"); //$NON-NLS-1$
        }

        if ( !Files.isExecutable(Paths.get(EXECUTABLE)) ) {
            throw new ExecutionException(EXECUTABLE + " is not executable"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        context.getOutput().info(String.format("Killing process %d with signal %d", getPid(), getSignal())); //$NON-NLS-1$

        if ( context.getConfig().isDryRun() ) {
            return new StatusOnlyResult(Status.SUCCESS);
        }

        if ( !runKill(context.getOutput(), getSignal(), getWaitTimeout()) ) {
            if ( isIgnoreError() ) {
                return new StatusOnlyResult(Status.SUCCESS);
            }
            return new StatusOnlyResult(Status.FAIL);
        }

        if ( isWaitForExit() ) {
            return waitForExit(context);
        }
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @param context
     */
    private StatusOnlyResult waitForExit ( Context context ) {
        long timeout = System.currentTimeMillis() + getWaitTimeout();
        try {
            while ( System.currentTimeMillis() < timeout ) {
                if ( !runKill(context.getOutput(), NONE, timeout - System.currentTimeMillis()) ) {
                    if ( System.currentTimeMillis() >= timeout ) {
                        log.warn("Timeout waiting for process to die"); //$NON-NLS-1$
                        return new StatusOnlyResult(Status.FAIL);
                    }
                    return new StatusOnlyResult(Status.SUCCESS);
                }
                Thread.sleep(500);
            }
        }
        catch ( InterruptedException e ) {
            log.warn("Interrupted waiting for process to die", e); //$NON-NLS-1$
            return new StatusOnlyResult(Status.FAIL);
        }

        return new StatusOnlyResult(Status.FAIL);
    }


    private boolean runKill ( Out o, int sig, long timeout ) {
        File devnull = new File("/dev/null"); //$NON-NLS-1$
        ProcessBuilder pb = new ProcessBuilder(EXECUTABLE, String.format("-%d", sig), String.valueOf(getPid())); //$NON-NLS-1$
        pb.environment().clear();
        pb.redirectError(devnull);
        pb.redirectOutput(devnull);
        try {
            Process proc = pb.start();
            proc.getInputStream().close();
            try {

                if ( !proc.waitFor(timeout, TimeUnit.MILLISECONDS) ) {
                    o.error("Timeout waiting for kill"); //$NON-NLS-1$
                    proc.destroyForcibly();
                    return false;
                }
                return proc.exitValue() == 0;
            }
            catch ( InterruptedException e ) {
                o.error("Timeout waiting for kill", e); //$NON-NLS-1$
                proc.destroyForcibly();
                return false;
            }
        }
        catch ( IOException e ) {
            o.error("Failure running kill command", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public KillConfigurator createConfigurator () {
        return new KillConfigurator(this);
    }

}
