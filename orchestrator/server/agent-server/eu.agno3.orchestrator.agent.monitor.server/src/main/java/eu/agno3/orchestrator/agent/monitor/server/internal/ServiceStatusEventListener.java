/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.monitor.server.internal;


import java.util.UUID;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.monitor.server.MonitoringServiceInternal;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService;
import eu.agno3.orchestrator.system.monitor.msg.ServiceStatusEvent;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 *
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.system.monitor.msg.ServiceStatusEvent" )
public class ServiceStatusEventListener implements EventListener<ServiceStatusEvent> {

    private static final Logger log = Logger.getLogger(ServiceStatusEventListener.class);

    private InstanceServerService instanceService;
    private ServiceServerService serviceService;
    private MonitoringServiceInternal monitoringService;


    @Reference
    protected synchronized void setInstanceService ( InstanceServerService iss ) {
        this.instanceService = iss;
    }


    protected synchronized void unsetInstanceService ( InstanceServerService iss ) {
        if ( this.instanceService == iss ) {
            this.instanceService = null;
        }
    }


    @Reference
    protected synchronized void setServiceService ( ServiceServerService sss ) {
        this.serviceService = sss;
    }


    protected synchronized void unsetServiceService ( ServiceServerService sss ) {
        if ( this.serviceService == sss ) {
            this.serviceService = null;
        }
    }


    @Reference
    protected synchronized void setMonitoringService ( MonitoringServiceInternal ms ) {
        this.monitoringService = ms;
    }


    protected synchronized void unsetMonitoringService ( MonitoringServiceInternal ms ) {
        if ( this.monitoringService == ms ) {
            this.monitoringService = ms;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull ServiceStatusEvent event ) {
        @NonNull
        AgentMessageSource origin = event.getOrigin();
        try {
            @NonNull
            InstanceStructuralObject instance = this.instanceService.getInstanceForAgent(origin.getAgentId());

            UUID serviceId = event.getServiceId();
            if ( serviceId == null ) {
                return;
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Got service status event for %s (%s): %s -> %s", //$NON-NLS-1$
                    instance.getDisplayName(),
                    serviceId,
                    event.getOldStatus(),
                    event.getNewStatus()));
            }

            @NonNull
            ServiceStructuralObject service = this.serviceService.getServiceById(instance, serviceId);

            this.monitoringService.haveServiceState(service, event.getNewStatus());
        }
        catch ( ModelObjectNotFoundException e ) {
            log.debug("Ignoring unknown instance", e); //$NON-NLS-1$
        }
        catch (
            ModelServiceException |
            ModelObjectReferentialIntegrityException e ) {
            log.warn("Failed to lookup service " + origin.getAgentId(), e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<ServiceStatusEvent> getEventType () {
        return ServiceStatusEvent.class;
    }

}
