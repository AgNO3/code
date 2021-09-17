/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.acls.internal;


import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.SecurityContext;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.server.component.auth.AuthConstants;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccess;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVote;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    SystemDestinationAccessVoter.class, DestinationAccessVoter.class
}, immediate = true )
public class SystemDestinationAccessVoter implements DestinationAccessVoter {

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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter#vote(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.security.SecurityContext, org.apache.activemq.command.ActiveMQDestination,
     *      eu.agno3.runtime.messaging.broker.auth.DestinationAccess)
     */
    @Override
    public DestinationAccessVote vote ( ConnectionContext connContext, SecurityContext context, ActiveMQDestination dest, DestinationAccess access ) {

        if ( AuthConstants.SYSTEM_USER.equals(context.getUserName()) ) {
            return DestinationAccessVote.POSITIVE;
        }

        return DestinationAccessVote.NEUTRAL;
    }

}
