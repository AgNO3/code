/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;


/**
 * @author mbechler
 * 
 */
public class ExecConfigurator extends AbstractConfigurator<ExecResult, Exec, ExecConfigurator> {

    /**
     * @param exec
     */
    public ExecConfigurator ( Exec exec ) {
        super(exec);
    }


    /**
     * 
     * @param cmd
     * @return this configurator
     */
    public ExecConfigurator cmd ( Path cmd ) {
        this.getExecutionUnit().setExecutable(cmd);
        return this.self();
    }


    /**
     * 
     * @param cmd
     * @return this configurator
     */
    public ExecConfigurator cmd ( File cmd ) {
        return this.cmd(cmd.toPath());
    }


    /**
     * @param cmd
     * @return this configurator
     */
    public ExecConfigurator cmd ( String cmd ) {
        return this.cmd(Paths.get(cmd));
    }


    /**
     * @param args
     * @return this configurator
     */
    public ExecConfigurator args ( String... args ) {
        return this.args(Arrays.asList(args));
    }


    /**
     * @param args
     * @return this configurator
     */
    public ExecConfigurator args ( List<String> args ) {
        this.getExecutionUnit().setArgs(args);
        return this.self();
    }


    /**
     * @param env
     * @return this configurator
     */
    public ExecConfigurator env ( Environment env ) {
        this.getExecutionUnit().setEnv(env);
        return this.self();
    }


    /**
     * @param cwd
     * @return this configurator
     */
    public ExecConfigurator cwd ( Path cwd ) {
        this.getExecutionUnit().setCwd(cwd);
        return this.self();
    }


    /**
     * @param cwd
     * @return this configurator
     */
    public ExecConfigurator cwd ( File cwd ) {
        return this.cwd(cwd.toPath());
    }


    /**
     * @param cwd
     * @return this configurator
     */
    public ExecConfigurator cwd ( String cwd ) {
        return this.cwd(Paths.get(cwd));
    }


    /**
     * 
     * @return this configurator
     */
    public ExecConfigurator ignoreExitCode () {
        this.getExecutionUnit().setIgnoreExitValue(true);
        return this.self();
    }


    /**
     * @param code
     * @return this configurator
     */
    public ExecConfigurator expectExitCode ( int code ) {
        this.getExecutionUnit().setExpectExitValue(code);
        return this.self();
    }


    /**
     * 
     * @return this configurator
     */
    public ExecConfigurator stderrIsNotError () {
        this.getExecutionUnit().setStderrIsError(false);
        return this.self();
    }


    /**
     * 
     * @param stdout
     * @return this configurator
     */
    public ExecConfigurator stdout ( OutputHandler stdout ) {
        this.getExecutionUnit().setStdoutHandler(stdout);
        return this.self();
    }


    /**
     * 
     * @param stderr
     * @return this configurator
     */
    public ExecConfigurator stderr ( OutputHandler stderr ) {
        this.getExecutionUnit().setStderrHandler(stderr);
        return this.self();
    }


    /**
     * 
     * @param stdin
     * @return this configurator
     */
    public ExecConfigurator stdin ( InputProvider stdin ) {
        this.getExecutionUnit().setInputProvider(stdin);
        return this.self();
    }

}
