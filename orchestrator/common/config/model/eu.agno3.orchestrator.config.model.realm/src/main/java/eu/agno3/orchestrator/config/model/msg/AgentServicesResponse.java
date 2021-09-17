/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.10.2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.msg;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
public class AgentServicesResponse extends XmlMarshallableMessage<@NonNull AgentMessageSource>
        implements ResponseMessage<@NonNull AgentMessageSource> {

    private Set<AgentServiceEntry> services = new HashSet<>();


    /**
     * 
     */
    public AgentServicesResponse () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public AgentServicesResponse ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public AgentServicesResponse ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public AgentServicesResponse ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.ResponseMessage#getStatus()
     */
    @Override
    public ResponseStatus getStatus () {
        return ResponseStatus.SUCCESS;
    }


    /**
     * @return the services
     */
    public Set<AgentServiceEntry> getServices () {
        return this.services;
    }


    /**
     * @param services
     *            the services to set
     */
    public void setServices ( Set<AgentServiceEntry> services ) {
        this.services = services;
    }

}
