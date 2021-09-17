/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.msg.addressing;


import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.scopes.BackendEventScope;


/**
 * @author mbechler
 * 
 */
public class AgentsEventScope extends BackendEventScope {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.EventScope#getParent()
     */
    @Override
    public EventScope getParent () {
        return new BackendEventScope();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return "events-agents"; //$NON-NLS-1$
    }
}
