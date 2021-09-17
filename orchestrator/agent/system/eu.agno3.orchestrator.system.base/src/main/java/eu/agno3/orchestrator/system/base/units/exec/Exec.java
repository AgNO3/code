/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionInterruptedException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.file.util.SerializablePath;


/**
 * @author mbechler
 * 
 */
public class Exec extends AbstractExecutionUnit<ExecResult, Exec, ExecConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 3526880922360331604L;
    private SerializablePath executable;
    private List<String> args;
    private Environment env;
    private SerializablePath cwd;

    private boolean ignoreExitValue;
    private boolean stderrIsError = true;
    private int expectExitValue;

    private transient ProcessBuilder pb;

    private InputProvider inputProvider = new EOFInputProvider();
    private OutputHandler stdoutHandler;
    private OutputHandler stderrHandler;


    /**
     * @return the executable
     */
    public Path getExecutable () {
        if ( this.executable == null ) {
            return null;
        }
        return this.executable.unwrap();
    }


    /**
     * @param executable
     *            the executable to set
     */
    void setExecutable ( Path executable ) {
        this.executable = SerializablePath.wrap(executable);
    }


    /**
     * @return the args
     */
    public List<String> getArgs () {
        if ( this.args == null ) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(this.args);
    }


    /**
     * @param args
     *            the args to set
     */
    void setArgs ( List<String> args ) {
        this.args = args;
    }


    /**
     * @return the env
     */
    public Environment getEnv () {
        return this.env;
    }


    /**
     * @param env
     *            the env to set
     */
    void setEnv ( Environment env ) {
        this.env = env;
    }


    /**
     * @return the ignoreExitValue
     */
    public boolean isIgnoreExitValue () {
        return this.ignoreExitValue;
    }


    /**
     * @param ignoreExitValue
     *            the ignoreExitValue to set
     */
    void setIgnoreExitValue ( boolean ignoreExitValue ) {
        this.ignoreExitValue = ignoreExitValue;
    }


    /**
     * @return the stdErrIsError
     */
    public boolean isStderrIsError () {
        return this.stderrIsError;
    }


    /**
     * @param stderrIsError
     *            the stderrIsError to set
     */
    void setStderrIsError ( boolean stderrIsError ) {
        this.stderrIsError = stderrIsError;
    }


    /**
     * @return the expectExitValue
     */
    public int getExpectExitValue () {
        return this.expectExitValue;
    }


    /**
     * @param expectExitValue
     *            the expectExitValue to set
     */
    void setExpectExitValue ( int expectExitValue ) {
        this.expectExitValue = expectExitValue;
    }


    /**
     * @return the cwd
     */
    public Path getCwd () {
        if ( this.cwd == null ) {
            return null;
        }
        return this.cwd.unwrap();
    }


    /**
     * @param cwd
     *            the cwd to set
     */
    void setCwd ( Path cwd ) {
        this.cwd = SerializablePath.wrap(cwd);
    }


    /**
     * @return the inputProvider
     */
    public InputProvider getInputProvider () {
        return this.inputProvider;
    }


    /**
     * @param inputProvider
     *            the inputProvider to set
     */
    void setInputProvider ( InputProvider inputProvider ) {
        this.inputProvider = inputProvider;
    }


    /**
     * @return the stdoutHandler
     */
    public OutputHandler getStdoutHandler () {
        return this.stdoutHandler;
    }


    /**
     * @param stdoutHandler
     *            the stdoutHandler to set
     */
    void setStdoutHandler ( OutputHandler stdoutHandler ) {
        this.stdoutHandler = stdoutHandler;
    }


    /**
     * @return the stderrHandler
     */
    public OutputHandler getStderrHandler () {
        return this.stderrHandler;
    }


    /**
     * @param stderrHandler
     *            the stderrHandler to set
     */
    void setStderrHandler ( OutputHandler stderrHandler ) {
        this.stderrHandler = stderrHandler;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( this.getExecutable() == null ) {
            throw new InvalidUnitConfigurationException("Executable must be set"); //$NON-NLS-1$
        }

        checkValidExecutable(context);

        if ( this.getCwd() != null && !Files.isDirectory(this.getCwd()) ) {
            throw new InvalidUnitConfigurationException("Working directory does not exist: " + this.getCwd()); //$NON-NLS-1$
        }
    }


    private void checkValidExecutable ( Context context ) throws InvalidUnitConfigurationException {
        if ( !context.getConfig().isNoVerifyEnv() && !Files.exists(this.getExecutable()) ) {
            throw new InvalidUnitConfigurationException("Executable does not exist: " + this.getExecutable()); //$NON-NLS-1$
        }

        if ( !context.getConfig().isNoVerifyEnv() && !Files.isExecutable(this.getExecutable()) ) {
            throw new InvalidUnitConfigurationException("Executable is not executable: " + this.getExecutable()); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public ExecResult prepare ( Context context ) throws ExecutionException {

        if ( context.getConfig().isDryRun() ) {
            return new ExecResult(this.expectExitValue);
        }

        List<String> cmdList = new ArrayList<>();
        cmdList.add(this.executable.toAbsolutePath().toString());
        if ( this.args != null ) {
            cmdList.addAll(this.args);
        }

        this.pb = makeProcessBuilder(cmdList);

        return new ExecResult(0, true);
    }


    private ProcessBuilder makeProcessBuilder ( List<String> cmdList ) {
        ProcessBuilder b = new ProcessBuilder(cmdList);

        if ( this.env != null ) {
            b.environment().clear();
            b.environment().putAll(this.env.getEnv());
        }

        if ( this.cwd != null ) {
            b.directory(this.cwd.toFile());
        }

        b.redirectInput(Redirect.PIPE);
        b.redirectOutput(Redirect.PIPE);
        b.redirectError(Redirect.PIPE);
        return b;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public ExecResult execute ( Context context ) throws ExecutionException {

        context.getOutput()
                .info(String.format(
                    "Running '%s' with argline '%s' in cwd '%s'", //$NON-NLS-1$
                    this.getExecutable(),
                    StringUtils.join(this.getArgs(), " "), //$NON-NLS-1$
                    getActualCwd()));

        if ( context.getConfig().isDryRun() ) {
            return new ExecResult(this.expectExitValue);
        }

        if ( this.pb == null ) {
            throw new ExecutionException("Process has not been set up correctly"); //$NON-NLS-1$
        }

        int exitCode = makePumpAndRun(context);

        if ( this.isIgnoreExitValue() ) {
            return new ExecResult(exitCode, true);
        }

        return new ExecResult(exitCode, this.getExpectExitValue());
    }


    /**
     * @param context
     * @return
     * @throws ExecutionException
     * @throws ExecutionInterruptedException
     */
    protected int makePumpAndRun ( Context context ) throws ExecutionException {
        IOPump pump = makeOutputPump(context);
        int exitCode = -1;
        try {
            exitCode = doRun(pump);
        }
        catch ( IOException e ) {
            throw new ExecutionException("Process failed to start:", e); //$NON-NLS-1$
        }
        catch ( InterruptedException e ) {
            throw new ExecutionInterruptedException("Interrupted while waiting for process", e); //$NON-NLS-1$
        }
        finally {
            pump.shutdown(true);
        }
        return exitCode;
    }


    private int doRun ( IOPump pump ) throws IOException, InterruptedException {
        int exitCode;
        Process p = this.pb.start();
        pump.connect(p);
        exitCode = p.waitFor();
        pump.shutdown(false);
        return exitCode;
    }


    private IOPump makeOutputPump ( Context context ) {
        OutputHandler stdout = this.getStdoutHandler();
        OutputHandler stderr = this.getStderrHandler();

        if ( stdout == null ) {
            stdout = new OutOutputHandler(context.getOutput().getChild("STDOUT"), false); //$NON-NLS-1$
        }

        if ( stderr == null ) {
            stderr = new OutOutputHandler(context.getOutput().getChild("STDERR"), this.isStderrIsError()); //$NON-NLS-1$
        }

        return new IOPump(this.getInputProvider(), stdout, stderr);
    }


    private Path getActualCwd () {
        return this.cwd != null ? this.cwd : Paths.get(".").toAbsolutePath(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public ExecConfigurator createConfigurator () {
        return new ExecConfigurator(this);
    }

}
