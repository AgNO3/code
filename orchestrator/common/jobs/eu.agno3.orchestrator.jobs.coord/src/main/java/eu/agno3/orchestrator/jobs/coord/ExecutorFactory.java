/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import eu.agno3.orchestrator.jobs.Job;


/**
 * @author mbechler
 * 
 */
public interface ExecutorFactory {

    /**
     * 
     * @param prov
     * @param jst
     * @param runnableFactory
     * @return a new executor instance
     */
    Executor makeExecutor ( JobForExecutionProvider prov, JobStateTracker jst );


    /**
     * @param j
     * @return whether the runnable is available
     */
    boolean canRun ( Job j );
}
