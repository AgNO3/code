/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 29, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.component.msg;


import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.impl.EmptyMessage;


/**
 * @author mbechler
 *
 */
public class ServerShutdownEvent extends EmptyMessage<@NonNull ServerMessageSource> implements EventMessage<@NonNull ServerMessageSource> {

    private Collection<EventScope> scopes;


    /**
     * 
     */
    public ServerShutdownEvent () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public ServerShutdownEvent ( @NonNull ServerMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public ServerShutdownEvent ( @NonNull ServerMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public ServerShutdownEvent ( @NonNull ServerMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Collection<EventScope> getScopes () {
        return this.scopes;
    }


    /**
     * @param scopes
     *            the scopes to set
     */
    public void setScopes ( Collection<EventScope> scopes ) {
        this.scopes = scopes;
    }

}
