/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.addressing.scopes;


import java.util.Objects;
import java.util.UUID;

import eu.agno3.runtime.messaging.addressing.EventScope;


/**
 * @author mbechler
 * 
 */
public class ServerEventScope extends ServersEventScope {

    private UUID serverId;


    /**
     * @param serverId
     */
    public ServerEventScope ( UUID serverId ) {
        this.serverId = serverId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.scopes.BackendEventScope#getParent()
     */
    @Override
    public EventScope getParent () {
        return new ServersEventScope();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.scopes.BackendEventScope#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return "events-server-" + this.serverId; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope#hashCode()
     */
    @Override
    public int hashCode () {
        return super.hashCode() + ( this.serverId != null ? 3 * this.serverId.hashCode() : 0 );
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        return super.equals(obj) && Objects.equals(this.serverId, ( (ServerEventScope) obj ).serverId);
    }
}
