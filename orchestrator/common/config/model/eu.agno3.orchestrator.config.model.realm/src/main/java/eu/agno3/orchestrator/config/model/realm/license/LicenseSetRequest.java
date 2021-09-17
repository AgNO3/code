/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.license;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
public class LicenseSetRequest extends XmlMarshallableMessage<@NonNull ServerMessageSource>
        implements RequestMessage<@NonNull ServerMessageSource, LicenseInfoResponse, DefaultXmlErrorResponseMessage> {

    private MessageTarget target;
    private LicenseInfo license;


    /**
     * 
     */
    public LicenseSetRequest () {
        super();
    }


    /**
     * @param target
     * @param origin
     * @param ttl
     */
    public LicenseSetRequest ( @NonNull AgentMessageTarget target, @NonNull ServerMessageSource origin, int ttl ) {
        super(origin, ttl);
        this.target = target;
    }


    /**
     * @param target
     * @param origin
     * @param replyTo
     */
    public LicenseSetRequest ( @NonNull AgentMessageTarget target, @NonNull ServerMessageSource origin,
            Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
        this.target = target;
    }


    /**
     * @param target
     * @param origin
     */
    public LicenseSetRequest ( @NonNull AgentMessageTarget target, @NonNull ServerMessageSource origin ) {
        super(origin);
        this.target = target;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<LicenseInfoResponse> getResponseType () {
        return LicenseInfoResponse.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<DefaultXmlErrorResponseMessage> getErrorResponseType () {
        return DefaultXmlErrorResponseMessage.class;
    }


    /**
     * @return the license
     */
    public LicenseInfo getLicense () {
        return this.license;
    }


    /**
     * @param license
     *            the license to set
     */
    public void setLicense ( LicenseInfo license ) {
        this.license = license;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return this.target;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 5000;
    }

}
