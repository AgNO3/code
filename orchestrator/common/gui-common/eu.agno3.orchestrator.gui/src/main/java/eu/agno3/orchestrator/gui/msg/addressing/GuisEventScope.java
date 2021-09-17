/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.msg.addressing;


import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.scopes.BackendEventScope;
import eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope;


/**
 * @author mbechler
 * 
 */
public class GuisEventScope extends BackendEventScope {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.EventScope#getParent()
     */
    @Override
    public EventScope getParent () {
        return new GlobalEventScope();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return "events-guis"; //$NON-NLS-1$
    }
}
