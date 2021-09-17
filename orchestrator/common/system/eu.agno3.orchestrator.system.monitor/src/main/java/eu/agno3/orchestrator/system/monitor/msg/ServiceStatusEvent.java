/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2015 by mbechler
 */
package eu.agno3.orchestrator.system.monitor.msg;


import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;
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
public class ServiceStatusEvent extends XmlMarshallableMessage<@NonNull AgentMessageSource> implements EventMessage<@NonNull AgentMessageSource> {

    private UUID serviceId;
    private ServiceRuntimeStatus oldStatus;
    private ServiceRuntimeStatus newStatus;


    /**
     * 
     */
    public ServiceStatusEvent () {
        super();
    }


    /**
     * 
     * @param origin
     * @param ttl
     */
    public ServiceStatusEvent ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * 
     * @param origin
     * @param replyTo
     */
    public ServiceStatusEvent ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * 
     * @param origin
     */
    public ServiceStatusEvent ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * @return the serviceId
     */
    public UUID getServiceId () {
        return this.serviceId;
    }


    /**
     * @param serviceId
     *            the serviceId to set
     */
    public void setServiceId ( UUID serviceId ) {
        this.serviceId = serviceId;
    }


    /**
     * @return the oldStatus
     */
    public ServiceRuntimeStatus getOldStatus () {
        return this.oldStatus;
    }


    /**
     * @param oldStatus
     *            the oldStatus to set
     */
    public void setOldStatus ( ServiceRuntimeStatus oldStatus ) {
        this.oldStatus = oldStatus;
    }


    /**
     * @return the newStatus
     */
    public ServiceRuntimeStatus getNewStatus () {
        return this.newStatus;
    }


    /**
     * @param newStatus
     *            the newStatus to set
     */
    public void setNewStatus ( ServiceRuntimeStatus newStatus ) {
        this.newStatus = newStatus;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Collection<EventScope> getScopes () {
        return Arrays.asList(new ServersEventScope());
    }

}
