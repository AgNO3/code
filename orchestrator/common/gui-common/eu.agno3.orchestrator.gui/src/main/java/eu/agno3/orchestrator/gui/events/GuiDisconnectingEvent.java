/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.events;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.gui.msg.addressing.GuiMessageSource;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.impl.EmptyMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class GuiDisconnectingEvent extends EmptyMessage<@NonNull GuiMessageSource> implements EventMessage<@NonNull GuiMessageSource> {

    /**
     * 
     */
    public GuiDisconnectingEvent () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public GuiDisconnectingEvent ( @NonNull GuiMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param messageSource
     */
    public GuiDisconnectingEvent ( @NonNull GuiMessageSource messageSource ) {
        super(messageSource);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Set<EventScope> getScopes () {
        Set<EventScope> scopes = new HashSet<>();
        scopes.add(new ServersEventScope());
        return scopes;
    }

}
