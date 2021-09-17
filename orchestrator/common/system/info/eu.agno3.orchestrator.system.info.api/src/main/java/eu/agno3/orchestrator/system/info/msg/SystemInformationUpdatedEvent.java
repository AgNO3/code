/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.msg;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class SystemInformationUpdatedEvent extends XmlMarshallableMessage<@NonNull AgentMessageSource>
        implements EventMessage<@NonNull AgentMessageSource> {

    private AgentSystemInformation systemInfo;


    /**
     * 
     */
    public SystemInformationUpdatedEvent () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public SystemInformationUpdatedEvent ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public SystemInformationUpdatedEvent ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public SystemInformationUpdatedEvent ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * 
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


    /**
     * @return the systemInfo
     */
    public AgentSystemInformation getSystemInfo () {
        return this.systemInfo;
    }


    /**
     * @param systemInfo
     *            the systemInfo to set
     */
    public void setSystemInfo ( AgentSystemInformation systemInfo ) {
        this.systemInfo = systemInfo;
    }
}
