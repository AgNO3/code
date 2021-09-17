/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 * 
 */
public class JobInfoRequestMessage extends JobCoordinatorRequestMessage<@NonNull MessageSource> {

    /**
     * 
     */
    public JobInfoRequestMessage () {
        super();
    }


    /**
     * @param target
     * @param origin
     * @param ttl
     */
    public JobInfoRequestMessage ( JobTarget target, @NonNull MessageSource origin, int ttl ) {
        super(target, origin, ttl);
    }


    /**
     * @param target
     * @param origin
     * @param replyTo
     */
    public JobInfoRequestMessage ( JobTarget target, @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(target, origin, replyTo);
    }


    /**
     * @param target
     * @param origin
     */
    public JobInfoRequestMessage ( JobTarget target, @NonNull MessageSource origin ) {
        super(target, origin);
    }

}
