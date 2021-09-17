/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth;


import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.SecurityContext;


/**
 * @author mbechler
 * 
 */
public interface DestinationAccessVoter {

    /**
     * The higher the earlier this voter will be asked, potentially allowing early short-circuiting for this decision
     * 
     * @return the voter's priority, higher is aked earlier
     */
    int getPriority ();


    /**
     * 
     * @param connContext
     * @param context
     * @param dest
     * @param access
     * @return this voter's opinion on whether access should be allowed.
     */
    DestinationAccessVote vote ( ConnectionContext connContext, SecurityContext context, ActiveMQDestination dest, DestinationAccess access );

}
