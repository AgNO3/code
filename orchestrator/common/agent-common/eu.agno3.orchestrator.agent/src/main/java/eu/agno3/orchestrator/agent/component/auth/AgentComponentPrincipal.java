/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.component.auth;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.server.component.auth.AbstractComponentPrincipal;
import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;
import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 *
 */
public class AgentComponentPrincipal extends AbstractComponentPrincipal implements ComponentPrincipal {

    /**
     * 
     */
    public static final String AGENT_USER_PREFIX = "agent-"; //$NON-NLS-1$


    /**
     * 
     * @param componentId
     */
    public AgentComponentPrincipal ( @NonNull UUID componentId ) {
        super(AGENT_USER_PREFIX, componentId);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.auth.ComponentPrincipal#getMessageSource()
     */
    @Override
    public MessageSource getMessageSource () {
        return new AgentMessageSource(this.getComponentId());
    }

}
