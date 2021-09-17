/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.crypto.keystore;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.jmsjmx.JMXErrorResponse;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 *
 */
public class KeystoreManagerJMXErrorResponse extends JMXErrorResponse {

    /**
     * 
     */
    public KeystoreManagerJMXErrorResponse () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public KeystoreManagerJMXErrorResponse ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public KeystoreManagerJMXErrorResponse ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public KeystoreManagerJMXErrorResponse ( @NonNull MessageSource origin ) {
        super(origin);
    }

}
