/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.sleep;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionInterruptedException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.progress.ProgressEventImpl;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 * 
 */
public class Sleep extends AbstractExecutionUnit<StatusOnlyResult, Sleep, SleepConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -4532627505676986329L;
    private int sleepTime = 0;


    /**
     * @param seconds
     */
    void setSleepTime ( int seconds ) {
        this.sleepTime = seconds;
    }


    /**
     * @return the sleep time
     */
    public int getSleepTime () {
        return this.sleepTime;
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

        long sleepMs = this.sleepTime * 10;

        context.getOutput().info(String.format("Sleeping for %d seconds", this.sleepTime)); //$NON-NLS-1$

        for ( int i = 0; i < 100; i++ ) {
            try {
                Thread.sleep(sleepMs);
            }
            catch ( InterruptedException e ) {
                throw new ExecutionInterruptedException("Sleep interrupted", e); //$NON-NLS-1$
            }
            context.publishEvent(new ProgressEventImpl(context, i));
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public SleepConfigurator createConfigurator () {
        return new SleepConfigurator(this);
    }

}
