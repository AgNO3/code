/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx;


import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.RequestMessage;


/**
 * @author mbechler
 * @param <T>
 * @param <TError>
 *
 */
public abstract class AbstractJMXRequest <@NonNull T extends MessageSource, TError extends JMXErrorResponse> extends AbstractJMXMessage<T>
        implements RequestMessage<T, JMXResponse, TError> {

    /**
     * invoke request
     */
    public static final int INVOKE = 0;

    /**
     * get attribute request
     */
    public static final int GET_ATTR = 1;

    /**
     * set attribute request
     */
    public static final int SET_ATTR = 2;

    private String[] signature;
    private String name;

    private int type;
    private Object[] params;
    private MessageTarget target;


    /**
     * 
     */
    public AbstractJMXRequest () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public AbstractJMXRequest ( @NonNull T origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public AbstractJMXRequest ( @NonNull T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public AbstractJMXRequest ( @NonNull T origin ) {
        super(origin);
    }


    /**
     * @param target
     *            the target to set
     */
    public void setTarget ( MessageTarget target ) {
        this.target = target;
    }


    /**
     * 
     * @return target object name
     * @throws MalformedObjectNameException
     */
    public abstract ObjectName getObjectName () throws MalformedObjectNameException;


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
     * @return the method to call
     */
    public String getName () {
        return this.name;
    }


    /**
     * @param operationName
     *            the operationName to set
     */
    public void setName ( String operationName ) {
        this.name = operationName;
    }


    /**
     * @return the type
     */
    public int getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( int type ) {
        this.type = type;
    }


    /**
     * @return the method signature
     */
    public String[] getSignature () {
        return this.signature;
    }


    /**
     * @param signature
     *            the signature to set
     */
    public void setSignature ( String[] signature ) {
        this.signature = signature;
    }


    /**
     * @return the params
     */
    public Object[] getParams () {
        return this.params;
    }


    /**
     * @param params
     *            the params to set
     */
    public void setParams ( Object[] params ) {
        this.params = params;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<JMXResponse> getResponseType () {
        return JMXResponse.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 15000;
    }


    /**
     * @param m
     * @throws JMSException
     */
    public void restoreExtraProperties ( BytesMessage m ) throws JMSException {
        // empty
    }


    /**
     * @param m
     * @throws JMSException
     */
    public void saveExtraProperties ( BytesMessage m ) throws JMSException {
        // empty
    }


    /**
     * @return a new cloned instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public AbstractJMXRequest<T, TError> createNew () throws InstantiationException, IllegalAccessException {
        AbstractJMXRequest<T, TError> cloned = this.getClass().newInstance();
        cloned.setTarget(this.getTarget());
        cloned.setTtl(this.getTTL());
        return cloned;
    }
}
