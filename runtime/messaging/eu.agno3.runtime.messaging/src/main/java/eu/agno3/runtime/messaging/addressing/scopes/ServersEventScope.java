/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.addressing.scopes;

import eu.agno3.runtime.messaging.addressing.EventScope;


/**
 * @author mbechler
 * 
 */
public class ServersEventScope extends BackendEventScope {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.scopes.BackendEventScope#getParent()
     */
    @Override
    public EventScope getParent () {
        return new BackendEventScope();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.scopes.BackendEventScope#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return "events-servers"; //$NON-NLS-1$
    }
}
