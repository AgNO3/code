/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.test.messages.msg;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.impl.EmptyMessage;


/**
 * @author mbechler
 * 
 */
public class TestEmptyMessage extends EmptyMessage<@NonNull MessageSource> implements EventMessage<@NonNull MessageSource> {

    /**
     */
    public TestEmptyMessage () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public TestEmptyMessage ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public TestEmptyMessage ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public TestEmptyMessage ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Set<EventScope> getScopes () {
        Set<EventScope> scopes = new HashSet<>();
        scopes.add(new GlobalEventScope());
        return scopes;
    }

}
