/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.Status;


/**
 * @author mbechler
 * 
 */
public class ExecResult implements Result {

    /**
     * 
     */
    private static final long serialVersionUID = 3727385171910317161L;
    private int expectExitCode = 0;
    private int actualExitCode;
    private boolean ignoreExitCode = false;


    /**
     * @param exitCode
     */
    public ExecResult ( int exitCode ) {
        this.actualExitCode = exitCode;
    }


    /**
     * @param exitCode
     * @param expectedExitCode
     */
    public ExecResult ( int exitCode, int expectedExitCode ) {
        this.actualExitCode = exitCode;
        this.expectExitCode = expectedExitCode;
    }


    /**
     * @param exitCode
     * @param ignoreExitCode
     */
    public ExecResult ( int exitCode, boolean ignoreExitCode ) {
        this.actualExitCode = exitCode;
        this.ignoreExitCode = ignoreExitCode;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Result#getStatus()
     */
    @Override
    public Status getStatus () {
        return this.failed() ? Status.FAIL : Status.SUCCESS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Result#suspended()
     */
    @Override
    public boolean suspended () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Result#failed()
     */
    @Override
    public boolean failed () {
        return !this.ignoreExitCode && this.actualExitCode != this.expectExitCode;
    }

}
