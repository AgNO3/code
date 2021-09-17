/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.msg;


import javax.jms.DeliveryMode;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;
import eu.agno3.runtime.messaging.addressing.DefaultMessageTarget;
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
public class ConfigTestResultUpdateRequest extends XmlMarshallableMessage<@NonNull MessageSource>
        implements RequestMessage<@NonNull MessageSource, ConfigTestResultUpdateResponse, DefaultXmlErrorResponseMessage> {

    private ConfigTestResultImpl result;
    private long sequence;


    /**
     * 
     */
    public ConfigTestResultUpdateRequest () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public ConfigTestResultUpdateRequest ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public ConfigTestResultUpdateRequest ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public ConfigTestResultUpdateRequest ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<ConfigTestResultUpdateResponse> getResponseType () {
        return ConfigTestResultUpdateResponse.class;
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return new DefaultMessageTarget();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.msg.AbstractAgentRequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 1000;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.impl.AbstractMessage#getDeliveryMode()
     */
    @Override
    public int getDeliveryMode () {
        return DeliveryMode.NON_PERSISTENT;
    }


    /**
     * @return the result
     */
    public ConfigTestResultImpl getResult () {
        return this.result;
    }


    /**
     * @param result
     *            the result to set
     */
    public void setResult ( ConfigTestResultImpl result ) {
        this.result = result;
    }


    /**
     * @return the sequence
     */
    public long getSequence () {
        return this.sequence;
    }


    /**
     * @param sequence
     *            the sequence to set
     */
    public void setSequence ( long sequence ) {
        this.sequence = sequence;
    }
}
