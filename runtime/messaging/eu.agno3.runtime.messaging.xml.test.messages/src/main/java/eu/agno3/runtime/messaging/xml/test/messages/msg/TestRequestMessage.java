/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.xml.test.messages.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.JMSQueueMessageTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
public class TestRequestMessage extends XmlMarshallableMessage<@NonNull MessageSource>
        implements RequestMessage<@NonNull MessageSource, TestResponseMessage, DefaultXmlErrorResponseMessage> {

    private ResponseStatus requestStatus;


    /**
     */
    public TestRequestMessage () {
        super();
    }


    /**
     * @param origin
     */
    public TestRequestMessage ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<TestResponseMessage> getResponseType () {
        return TestResponseMessage.class;
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
        return new JMSQueueMessageTarget("msgs/xml/test"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 10000;
    }


    /**
     * @return the requestStatus
     */
    public ResponseStatus getRequestStatus () {
        return this.requestStatus;
    }


    /**
     * @param requestStatus
     *            the requestStatus to set
     */
    public void setRequestStatus ( ResponseStatus requestStatus ) {
        this.requestStatus = requestStatus;
    }

}
