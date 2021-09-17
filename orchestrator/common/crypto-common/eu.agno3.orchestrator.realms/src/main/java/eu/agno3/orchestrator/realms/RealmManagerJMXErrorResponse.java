/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.realms;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.jmsjmx.JMXErrorResponse;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 *
 */
public class RealmManagerJMXErrorResponse extends JMXErrorResponse {

    /**
     * 
     */
    public RealmManagerJMXErrorResponse () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public RealmManagerJMXErrorResponse ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public RealmManagerJMXErrorResponse ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public RealmManagerJMXErrorResponse ( @NonNull MessageSource origin ) {
        super(origin);
    }

}
