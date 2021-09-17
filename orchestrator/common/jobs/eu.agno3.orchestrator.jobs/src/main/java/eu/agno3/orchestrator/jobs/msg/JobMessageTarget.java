/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.msg;


import java.util.UUID;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.orchestrator.jobs.targets.AnyServerTarget;
import eu.agno3.orchestrator.jobs.targets.ServerTarget;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;


/**
 * @author mbechler
 * 
 */
public class JobMessageTarget implements MessageTarget {

    /**
     * 
     */
    private static final String UNKNOWN_MESSAGE_SOURCE = "Unknown message source "; //$NON-NLS-1$
    /**
     * 
     */
    private static final String AGENT_TARGET_PREFIX = "agent:"; //$NON-NLS-1$
    private static final String SERVER_TARGET_PREFIX = "server:"; //$NON-NLS-1$

    private final JobTarget jobTarget;


    /**
     * @param jobTarget
     */
    public JobMessageTarget ( JobTarget jobTarget ) {
        this.jobTarget = jobTarget;
    }


    /**
     * @return the jobTarget
     */
    public JobTarget getJobTarget () {
        return this.jobTarget;
    }


    /**
     * @param ms
     * @return a matching job message target
     */
    public static final JobTarget fromMessageSource ( MessageSource ms ) {

        if ( ms instanceof ServerMessageSource ) {
            return new ServerTarget( ( (ServerMessageSource) ms ).getServerId());
        }
        else if ( ms instanceof AgentMessageSource ) {
            return new AgentTarget( ( (AgentMessageSource) ms ).getAgentId());
        }

        throw new IllegalArgumentException(UNKNOWN_MESSAGE_SOURCE + ms);
    }


    /**
     * @param target
     * @return a job target for the string definition
     */
    public static JobTarget targetFromString ( String target ) {

        if ( target.startsWith(AGENT_TARGET_PREFIX) ) {
            return makeAgentTarget(target);
        }
        else if ( target.startsWith(SERVER_TARGET_PREFIX) ) {
            return makeServerTarget(target);
        }

        throw new IllegalArgumentException(UNKNOWN_MESSAGE_SOURCE + target);
    }


    /**
     * @param target
     * @return
     */
    private static JobTarget makeServerTarget ( String target ) {
        UUID fromString = UUID.fromString(target.substring(SERVER_TARGET_PREFIX.length()));

        if ( fromString == null ) {
            throw new IllegalArgumentException();
        }

        return new ServerTarget(fromString);
    }


    /**
     * @param target
     * @return
     */
    private static JobTarget makeAgentTarget ( String target ) {
        UUID fromString = UUID.fromString(target.substring(AGENT_TARGET_PREFIX.length()));
        if ( fromString == null ) {
            throw new IllegalArgumentException();
        }
        return new AgentTarget(fromString);
    }


    /**
     * @param ms
     * @return a list of job targets this instance may listen to
     */
    public static final JobTarget[] listeningTargets ( MessageSource ms ) {
        if ( ms instanceof ServerMessageSource ) {
            return new JobTarget[] {
                fromMessageSource(ms), new AnyServerTarget()
            };
        }
        else if ( ms instanceof AgentMessageSource ) {
            return new JobTarget[] {
                fromMessageSource(ms)
            };
        }

        throw new IllegalArgumentException(UNKNOWN_MESSAGE_SOURCE + ms);
    }

}
