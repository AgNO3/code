/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.jmx;


import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 *
 */
public class FileshareUserJMXRequest extends BaseFileshareJMXRequest {

    /**
     * 
     */
    public FileshareUserJMXRequest () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public FileshareUserJMXRequest ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public FileshareUserJMXRequest ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public FileshareUserJMXRequest ( @NonNull MessageSource origin ) {
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
        return new ObjectName("eu.agno3.fileshare:type=UserService"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<FileshareJMXErrorResponse> getErrorResponseType () {
        return FileshareJMXErrorResponse.class;
    }
}
