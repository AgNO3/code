/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.server.internal;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.agent.update.server.UpdateServiceInternal;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.system.update.msg.SystemUpdatedEvent;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 *
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.system.update.msg.SystemUpdatedEvent" )
public class UpdatedEventListener implements EventListener<SystemUpdatedEvent> {

    private static final Logger log = Logger.getLogger(UpdatedEventListener.class);
    private UpdateServiceInternal updateService;
    private InstanceServerService instanceService;


    @Reference
    protected synchronized void setUpdateService ( UpdateServiceInternal usi ) {
        this.updateService = usi;
    }


    protected synchronized void unsetUpdateService ( UpdateServiceInternal usi ) {
        if ( this.updateService == usi ) {
            this.updateService = null;
        }
    }


    @Reference
    protected synchronized void setInstanceService ( InstanceServerService iss ) {
        this.instanceService = iss;
    }


    protected synchronized void unsetInstanceService ( InstanceServerService iss ) {
        if ( this.instanceService == iss ) {
            this.instanceService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull SystemUpdatedEvent event ) {
        @NonNull
        AgentMessageSource origin = event.getOrigin();
        try {
            @NonNull
            InstanceStructuralObject instance = this.instanceService.getInstanceForAgent(origin.getAgentId());
            this.updateService.updated(instance, event.getUpdatedStream(), event.getUpdatedSequence(), event.getRebootIndicated());
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException e ) {
            log.warn("Unknown agent " + origin.getAgentId(), e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<SystemUpdatedEvent> getEventType () {
        return SystemUpdatedEvent.class;
    }

}
