/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import eu.agno3.orchestrator.system.base.execution.output.Out;


/**
 * @author mbechler
 * 
 */
public interface Runner {

    /**
     * @return the executor for this context
     */
    Executor getExecutor ();


    /**
     * @return the execution unit factory for this context
     */
    ExecutionUnitFactory getExecutionUnitFactory ();


    /**
     * @return a new job builder
     */
    JobBuilder makeJobBuilder ();


    /**
     * @param job
     * @return the job result
     */
    Result run ( Job job );


    /**
     * @param job
     * @param out
     * @param cfg
     * @param suspendHandler
     * @return the job result
     */
    Result run ( Job job, Out out, ExecutionConfig cfg, JobSuspendHandler suspendHandler );


    /**
     * @param resume
     * @return the job result
     */
    Result resume ( SuspendData resume );


    /**
     * @param resume
     * @param out
     * @param cfg
     * @param suspendHandler
     * @return the job result
     */
    Result resume ( SuspendData resume, Out out, ExecutionConfig cfg, JobSuspendHandler suspendHandler );


    /**
     * @param results
     * @return a result resolver
     */
    ResultResolver makeResultResolver ( UnitResults results );


    /**
     * @param listener
     * 
     */
    void registerEventListener ( ExecutorEventListener listener );

}
