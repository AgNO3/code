/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.SecurityContext;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.component.auth.AgentComponentPrincipal;
import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.config.AgentConfigRequest;
import eu.agno3.orchestrator.agent.server.AgentConfigurationProvider;
import eu.agno3.orchestrator.server.component.ComponentConfigurationException;
import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;
import eu.agno3.orchestrator.server.component.auth.ComponentSecurityContext;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccess;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVote;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter;


/**
 * @author mbechler
 * 
 */
@Component ( service = DestinationAccessVoter.class )
public class AgentEventDestinationAccessVoter implements DestinationAccessVoter {

    private static final Logger log = Logger.getLogger(AgentEventDestinationAccessVoter.class);

    private AgentConfigurationProvider agentConfigProvider;


    /**
     * 
     */
    public AgentEventDestinationAccessVoter () {}

    private static Set<String> ALLOW_WRITE_QUEUES = new HashSet<>(Arrays.asList("eu.agno3.orchestrator.config.model.msg.ConfigTestResultUpdateRequest" //$NON-NLS-1$
    ));


    @Reference
    protected synchronized void setAgentConfigProvider ( AgentConfigurationProvider provider ) {
        this.agentConfigProvider = provider;
    }


    protected synchronized void unsetAgentConfigProvider ( AgentConfigurationProvider provider ) {
        if ( this.agentConfigProvider == provider ) {
            this.agentConfigProvider = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.broker.auth.DestinationAccessVoter#getPriority()
     */
    @Override
    public int getPriority () {
        return 0;
    }


    @Override
    public DestinationAccessVote vote ( ConnectionContext connContext, SecurityContext context, ActiveMQDestination dest, DestinationAccess access ) {

        if ( ! ( context instanceof ComponentSecurityContext ) ) {
            return DestinationAccessVote.NEUTRAL;
        }

        ComponentSecurityContext secContext = (ComponentSecurityContext) context;
        ComponentPrincipal princ = secContext.getComponentPrincipal();

        if ( ! ( princ instanceof AgentComponentPrincipal ) ) {
            return DestinationAccessVote.NEUTRAL;
        }

        return doVoteInternal(dest, access, princ);
    }


    /**
     * @param dest
     * @param access
     * @param princ
     * @return
     */
    private DestinationAccessVote doVoteInternal ( ActiveMQDestination dest, DestinationAccess access, ComponentPrincipal princ ) {
        try {
            if ( isAgentsSystemDestination(dest) && ( access == DestinationAccess.PRODUCE || access == DestinationAccess.CREATE ) ) {
                return DestinationAccessVote.POSITIVE;
            }

            if ( ( access == DestinationAccess.PRODUCE || access == DestinationAccess.CREATE ) && dest.isQueue()
                    && ALLOW_WRITE_QUEUES.contains(dest.getPhysicalName()) ) {
                return DestinationAccessVote.POSITIVE;
            }

            return voteAgentSpecific(dest, princ.getComponentId());

        }
        catch ( Exception e ) {
            log.warn("Failed to check agent destination access:", e); //$NON-NLS-1$
        }

        return DestinationAccessVote.NEGATIVE;
    }


    /**
     * @param dest
     * @param agentId
     * @return
     * @throws ComponentConfigurationException
     */
    private DestinationAccessVote voteAgentSpecific ( ActiveMQDestination dest, @NonNull UUID agentId ) throws ComponentConfigurationException {
        AgentConfig agentConfig = this.agentConfigProvider.getConfiguration(agentId);

        if ( dest.isQueue() && shouldAllowQueueAccess(dest, agentConfig) ) {
            return DestinationAccessVote.POSITIVE;
        }

        if ( isAgentEventDestination(dest, agentConfig) ) {
            return DestinationAccessVote.POSITIVE;
        }

        return DestinationAccessVote.NEUTRAL;
    }


    private static boolean isAgentEventDestination ( ActiveMQDestination dest, AgentConfig agentConfig ) {
        return isAgentEventOutQueue(dest, agentConfig) || isAgentEventTopic(dest, agentConfig);
    }


    private static boolean shouldAllowQueueAccess ( ActiveMQDestination dest, AgentConfig agentConfig ) {
        return isAgentConfigRequestQueue(dest) || isAgentRequestQueue(dest, agentConfig);
    }


    private static boolean isAgentConfigRequestQueue ( ActiveMQDestination dest ) {
        return dest.getPhysicalName().equals(AgentConfigRequest.class.getName());
    }


    private static boolean isAgentRequestQueue ( ActiveMQDestination dest, AgentConfig agentConfig ) {
        return dest.getPhysicalName().startsWith(agentConfig.getRequestQueuePrefix());
    }


    /**
     * @param dest
     * @return
     */
    private static boolean isAgentsSystemDestination ( ActiveMQDestination dest ) {
        return ( dest.isTopic() && "system-agents".equals(dest.getPhysicalName()) ) //$NON-NLS-1$
                || ( dest.isQueue() && "agents-ping".equals(dest.getPhysicalName()) ); //$NON-NLS-1$
    }


    /**
     * @param dest
     * @param agentConfig
     * @return
     */
    private static boolean isAgentEventTopic ( ActiveMQDestination dest, AgentConfig agentConfig ) {
        return dest.isTopic() && dest.getPhysicalName().equals(agentConfig.getEventTopic());
    }


    /**
     * @param dest
     * @param agentConfig
     * @return
     */
    private static boolean isAgentEventOutQueue ( ActiveMQDestination dest, AgentConfig agentConfig ) {
        return dest.isQueue() && dest.getPhysicalName().equals(agentConfig.getEventOutQueue());
    }
}
