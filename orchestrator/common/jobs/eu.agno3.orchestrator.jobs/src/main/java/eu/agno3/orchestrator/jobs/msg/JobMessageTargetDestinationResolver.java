/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.msg;


import java.util.HashSet;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.routing.MessageDestinationResolver;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    MessageDestinationResolver.class
}, property = "targetClass=eu.agno3.orchestrator.jobs.msg.JobMessageTarget" )
public class JobMessageTargetDestinationResolver implements MessageDestinationResolver {

    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.routing.MessageDestinationResolver#createDestination(javax.jms.Session,
     *      eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public Destination createDestination ( Session s,
            RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> msg )
                    throws MessagingException {

        JobMessageTarget t = (JobMessageTarget) msg.getTarget();
        try {
            return destinationForJobTargets(s, msg.getClass(), t.getJobTarget());
        }
        catch ( JMSException e ) {
            throw new MessagingException("Failed to create target queue:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param s
     * @param msgType
     * @param jts
     * @return the destination for the target
     * @throws JMSException
     */
    public static Destination destinationForJobTargets ( Session s, Class<?> msgType, JobTarget... jts ) throws JMSException {
        Set<String> queues = new HashSet<>();

        for ( JobTarget jt : jts ) {
            queues.add(makeTargetQueueName(msgType, jt));
        }

        return s.createQueue(StringUtils.join(queues, ','));
    }


    /**
     * @param msgType
     * @param jts
     * @return destination id
     */
    public static String destinationIdForJobTargets ( Class<?> msgType, JobTarget... jts ) {
        Set<String> queues = new HashSet<>();

        for ( JobTarget jt : jts ) {
            queues.add(makeTargetQueueName(msgType, jt));
        }

        return "queues://" + StringUtils.join(queues, ','); //$NON-NLS-1$
    }


    private static String makeTargetQueueName ( Class<?> msgType, JobTarget jt ) {
        return String.format("jobs/%s/%s", jt, msgType.getName()); //$NON-NLS-1$
    }

}
