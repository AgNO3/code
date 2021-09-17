/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


/**
 * @author mbechler
 * 
 */
public interface ExecutorEventListener {

    /**
     * Called when an event occurs
     * 
     * @param ev
     */
    void onEvent ( ExecutorEvent ev );
}
