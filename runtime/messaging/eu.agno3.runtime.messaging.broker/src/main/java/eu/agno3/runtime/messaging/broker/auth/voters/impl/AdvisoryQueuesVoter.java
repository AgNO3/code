/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth.voters.impl;


import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.SecurityContext;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.broker.auth.DestinationAccess;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVote;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter;


/**
 * @author mbechler
 * 
 */
@Component ( service = DestinationAccessVoter.class )
public class AdvisoryQueuesVoter implements DestinationAccessVoter {

    private static final Logger log = Logger.getLogger(AdvisoryQueuesVoter.class);


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

        // TODO: find better way to check for advisory topic

        if ( dest.getPhysicalName().startsWith("ActiveMQ.Advisory.") ) { //$NON-NLS-1$
            if ( log.isTraceEnabled() ) {
                log.trace("Matched an advisory topic " + dest.getPhysicalName()); //$NON-NLS-1$
            }
            return DestinationAccessVote.POSITIVE;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Does not seem to be an advisory topic " + dest.getPhysicalName()); //$NON-NLS-1$
        }

        return DestinationAccessVote.NEUTRAL;
    }

}