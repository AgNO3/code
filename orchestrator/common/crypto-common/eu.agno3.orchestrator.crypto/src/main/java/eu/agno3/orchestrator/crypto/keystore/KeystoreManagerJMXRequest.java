/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.crypto.keystore;


import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.jmsjmx.AbstractJMXRequest;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 *
 */
public class KeystoreManagerJMXRequest extends AbstractJMXRequest<@NonNull MessageSource, KeystoreManagerJMXErrorResponse> {

    /**
     * 
     */
    public KeystoreManagerJMXRequest () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public KeystoreManagerJMXRequest ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public KeystoreManagerJMXRequest ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public KeystoreManagerJMXRequest ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws MalformedObjectNameException
     *
     * @see eu.agno3.runtime.jmsjmx.AbstractJMXRequest#getObjectName()
     */
    @Override
    public ObjectName getObjectName () throws MalformedObjectNameException {
        return new ObjectName("eu.agno3.agent.crypto:type=KeystoreManagementBean"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<KeystoreManagerJMXErrorResponse> getErrorResponseType () {
        return KeystoreManagerJMXErrorResponse.class;
    }

}
