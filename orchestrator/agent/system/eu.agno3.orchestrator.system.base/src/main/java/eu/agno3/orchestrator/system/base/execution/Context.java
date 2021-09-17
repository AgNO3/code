/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.util.Iterator;
import java.util.Map;

import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.output.Out;


/**
 * @author mbechler
 * 
 */
public interface Context extends ResultResolver {

    /**
     * @param execContext
     */
    void pushExecutorContext ( JobExecutorContext execContext );


    /**
     * @return the pop'ed context
     * 
     */
    JobExecutorContext popExecutorContext ();


    /**
     * @return the current executor context
     */
    JobExecutorContext getExecutorContext ();


    /**
     * 
     * @param ev
     */
    void publishEvent ( ExecutorEvent ev );


    /**
     * @return current outcome
     */
    Result getResult ();


    /**
     * @return the associated runner
     */
    Runner getRunner ();


    /**
     * 
     * @return a logger
     */
    Out getOutput ();


    /**
     * 
     * @return the configuration for this run
     */
    ExecutionConfig getConfig ();


    /**
     * @param l
     */
    void registerEventListener ( ExecutorEventListener l );


    /**
     * @param ev
     */
    void notifyListeners ( ExecutorEvent ev );


    /**
     * @return a logger for jobs
     */
    Out getJobOutput ();


    /**
     * 
     * @return the state message
     */
    String getStateMessage ();


    /**
     * 
     * @param s
     *            the state message
     */
    void setStateMessage ( String s );


    /**
     * 
     * @return the current state context
     */
    Map<String, String> getStateContext ();


    /**
     * 
     * @param key
     * @param val
     */
    void addStateContext ( String key, String val );


    /**
     * @param flag
     * @return whether the flag is set
     */
    boolean getFlag ( String flag );


    /**
     * @return whether suspending a running job is supported
     */
    boolean canSuspend ();


    /**
     * @param after
     * @param p
     * @param cur
     * @return the units to run before suspending
     * @throws ExecutionException
     * 
     */
    Iterator<ExecutionUnit<?, ?, ?>> suspend ( int after, Iterator<ExecutionUnit<?, ?, ?>> cur ) throws ExecutionException;


    /**
     * @param data
     * @return the remaining units for the current phase
     */
    Iterator<ExecutionUnit<?, ?, ?>> restore ( SuspendData data );

}
