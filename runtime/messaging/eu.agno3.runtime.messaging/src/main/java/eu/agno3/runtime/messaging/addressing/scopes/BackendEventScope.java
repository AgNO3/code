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
public class BackendEventScope extends GlobalEventScope {

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
        return "events-backend"; //$NON-NLS-1$
    }

}
