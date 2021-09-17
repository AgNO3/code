/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.acl;


import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.component.auth.AgentComponentPrincipal;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.msg.JobMessageTarget;
import eu.agno3.orchestrator.server.component.auth.ComponentSecurityContext;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccess;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVote;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter;


/**
 * Allow the job target destination to access it's job queue
 * 
 * @author mbechler
 * 
 */
@Component ( service = DestinationAccessVoter.class )
public class JobsDestinationAccessVoter implements DestinationAccessVoter {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter#getPriority()
     */
    @Override
    public int getPriority () {
        return 500;
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
    public DestinationAccessVote vote ( ConnectionContext connContext, SecurityContext ctx, ActiveMQDestination dest, DestinationAccess access ) {

        if ( !dest.isQueue() || !dest.getPhysicalName().startsWith("jobs/") ) { //$NON-NLS-1$
            // not a job queue, we don't care
            return DestinationAccessVote.NEUTRAL;
        }

        if ( ! ( ctx instanceof ComponentSecurityContext ) ) {
            return DestinationAccessVote.NEUTRAL;
        }

        if ( ! ( access == DestinationAccess.CREATE || access == DestinationAccess.CONSUME ) ) {
            // only care about creating and consuming
            return DestinationAccessVote.NEUTRAL;
        }

        return doVote((ComponentSecurityContext) ctx, dest);
    }


    private static DestinationAccessVote doVote ( ComponentSecurityContext ctx, ActiveMQDestination dest ) {

        String[] parts = StringUtils.split(dest.getPhysicalName(), '/');

        if ( parts == null || parts.length != 3 ) {
            // this is an unknown format, better disallow
            return DestinationAccessVote.NEGATIVE;
        }

        if ( ! ( ctx.getComponentPrincipal() instanceof AgentComponentPrincipal ) ) {
            return DestinationAccessVote.NEUTRAL;
        }
        JobTarget jobTarget = JobMessageTarget.targetFromString(parts[ 1 ]);
        JobTarget callerJobTarget = JobMessageTarget.fromMessageSource(ctx.getComponentPrincipal().getMessageSource());

        if ( jobTarget.equals(callerJobTarget) ) {
            return DestinationAccessVote.POSITIVE;
        }

        return DestinationAccessVote.NEUTRAL;
    }

}
