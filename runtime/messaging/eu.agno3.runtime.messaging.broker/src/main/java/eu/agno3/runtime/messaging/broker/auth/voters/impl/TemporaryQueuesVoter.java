/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2014 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth.voters.impl;


import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTempDestination;
import org.apache.activemq.security.SecurityContext;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.broker.auth.DestinationAccess;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVote;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter;


/**
 * @author mbechler
 *
 */
@Component ( service = DestinationAccessVoter.class )
public class TemporaryQueuesVoter implements DestinationAccessVoter {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter#getPriority()
     */
    @Override
    public int getPriority () {
        return 1000;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter#vote(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.security.SecurityContext, org.apache.activemq.command.ActiveMQDestination,
     *      eu.agno3.runtime.messaging.broker.auth.DestinationAccess)
     */
    @Override
    public DestinationAccessVote vote ( ConnectionContext connContext, SecurityContext context, ActiveMQDestination dest, DestinationAccess access ) {

        if ( dest.isTemporary() ) {
            if ( ( (ActiveMQTempDestination) dest ).getConnectionId().equals(connContext.getConnectionId().getValue()) ) {
                return DestinationAccessVote.POSITIVE;
            }
            if ( access == DestinationAccess.CREATE ) {
                // allow creation, this is not necessarily the same connection
                return DestinationAccessVote.POSITIVE;
            }

            if ( access == DestinationAccess.PRODUCE ) {
                // any client may post responses to the temporary queue
                return DestinationAccessVote.POSITIVE;
            }

            return DestinationAccessVote.NEGATIVE;
        }

        return DestinationAccessVote.NEUTRAL;
    }
}
