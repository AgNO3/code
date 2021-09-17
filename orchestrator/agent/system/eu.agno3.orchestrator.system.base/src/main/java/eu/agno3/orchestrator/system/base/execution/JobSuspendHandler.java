/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData;


/**
 * @author mbechler
 *
 */
public interface JobSuspendHandler {

    /**
     * @param suspend
     * @throws ExecutionException
     */
    void suspended ( JobSuspendData suspend ) throws ExecutionException;

}
