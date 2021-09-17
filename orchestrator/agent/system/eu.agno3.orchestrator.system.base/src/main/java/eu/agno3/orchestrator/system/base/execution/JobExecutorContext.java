/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.util.Map;

import eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData;
import eu.agno3.orchestrator.system.base.execution.output.Out;


/**
 * @author mbechler
 * 
 */
public interface JobExecutorContext {

    /**
     * @return the current result resolver
     */
    ResultResolver getResultResolver ();


    /**
     * @param ctx
     * @param ev
     */
    void publishEvent ( ExecutorEvent ev );


    /**
     * @return the current result
     */
    Result getResult ();


    /**
     * @return the parent context
     */
    Context getContext ();


    /**
     * @return the output stream
     */
    Out getOutput ();


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
     * @param data
     * 
     */
    void save ( JobSuspendData data );


    /**
     * 
     * @param data
     */
    void restore ( JobSuspendData data );

}
