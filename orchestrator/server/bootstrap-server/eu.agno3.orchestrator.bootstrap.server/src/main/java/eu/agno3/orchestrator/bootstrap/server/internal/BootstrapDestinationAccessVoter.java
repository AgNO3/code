/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.server.internal;


import java.net.URI;
import java.net.URISyntaxException;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.SecurityContext;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.component.auth.AgentComponentPrincipal;
import eu.agno3.orchestrator.bootstrap.msg.BootstrapRequestMessage;
import eu.agno3.orchestrator.server.component.auth.ComponentSecurityContext;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccess;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVote;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter;


/**
 * @author mbechler
 *
 */
@Component ( service = DestinationAccessVoter.class )
public class BootstrapDestinationAccessVoter implements DestinationAccessVoter {

    /**
     * 
     */
    private static final String LOCAL_IP = "127.0.0.1"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(BootstrapDestinationAccessVoter.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter#getPriority()
     */
    @Override
    public int getPriority () {
        return 0;
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
        if ( isBootstrapRequest(dest) ) {
            return voteBootstrapRequest(connContext, context);
        }

        return DestinationAccessVote.NEUTRAL;
    }


    /**
     * @param connContext
     * @param context
     * @return
     */
    private static DestinationAccessVote voteBootstrapRequest ( ConnectionContext connContext, SecurityContext context ) {
        if ( !connContext.isNetworkConnection() ) {
            return DestinationAccessVote.POSITIVE;
        }

        if ( ! ( context instanceof ComponentSecurityContext ) ) {
            log.error("Security incident: Unauthenticated bootstrap request"); //$NON-NLS-1$
            return DestinationAccessVote.NEGATIVE;
        }

        ComponentSecurityContext secContext = (ComponentSecurityContext) context;

        if ( ! ( secContext instanceof AgentComponentPrincipal ) ) {
            log.error("Security incident: Bootstrap not coming from agent"); //$NON-NLS-1$
            return DestinationAccessVote.NEGATIVE;
        }

        return checkLocalHost(connContext);
    }


    /**
     * @param connContext
     * @return
     */
    private static DestinationAccessVote checkLocalHost ( ConnectionContext connContext ) {
        String remoteAddr = connContext.getConnection().getRemoteAddress();

        try {
            URI remote = new URI(remoteAddr);
            if ( LOCAL_IP.equals(remote.getHost()) ) {
                return DestinationAccessVote.POSITIVE;
            }

            log.error("Security incident: Bootstrap request from remote host " + remote.getHost()); //$NON-NLS-1$
            return DestinationAccessVote.NEGATIVE;
        }
        catch ( URISyntaxException e ) {
            log.warn("Failed to parse remote address URI", e); //$NON-NLS-1$
            return DestinationAccessVote.NEUTRAL;
        }
    }


    private static boolean isBootstrapRequest ( ActiveMQDestination dest ) {
        return dest.getPhysicalName().equals(BootstrapRequestMessage.class.getName());
    }

}
