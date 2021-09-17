/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.events;


import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;
import eu.agno3.orchestrator.system.base.execution.Result;


/**
 * @author mbechler
 * 
 */
public interface ResultEvent extends ExecutorEvent {

    /**
     * @return the result
     */
    Result getResult ();

}
