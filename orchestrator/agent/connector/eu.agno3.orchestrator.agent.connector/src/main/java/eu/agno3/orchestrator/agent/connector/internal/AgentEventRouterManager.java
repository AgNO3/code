/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.server.connector.impl.AbstractComponentEventRouterManager;
import eu.agno3.runtime.messaging.routing.EventRouterManager;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventRouterManager.class, property = {
    Constants.SERVICE_RANKING + ":Integer=" + Integer.MAX_VALUE
} )
public class AgentEventRouterManager extends AbstractComponentEventRouterManager<@NonNull AgentConfig> {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractComponentEventRouterManager#setComponentConfig(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    @Override
    protected synchronized void setComponentConfig ( @NonNull AgentConfig c ) {
        super.setComponentConfig(c);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractComponentEventRouterManager#unsetComponentConfig(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    protected synchronized void unsetComponentConfig ( @NonNull AgentConfig c ) {
        super.unsetComponentConfig(c);
    }
}
