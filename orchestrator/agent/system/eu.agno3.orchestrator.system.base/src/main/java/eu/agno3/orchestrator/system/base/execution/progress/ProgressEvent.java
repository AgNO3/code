/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.progress;


import java.util.Map;

import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;


/**
 * @author mbechler
 * 
 */
public interface ProgressEvent extends ExecutorEvent {

    /**
     * @return the progress
     */
    float getProgress ();


    /**
     * @return the state context
     */
    Map<String, String> getStateContext ();


    /**
     * @return the state message
     */
    String getStateMessage ();

}
