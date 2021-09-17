/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.listener;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.events.ServiceConfigAppliedEvent;
import eu.agno3.orchestrator.config.model.realm.server.service.ConfigApplyServerService;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 *
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.config.model.events.ServiceConfigAppliedEvent" )
public class ServiceConfigAppliedEventListener implements EventListener<ServiceConfigAppliedEvent> {

    private ConfigApplyServerService configApplyService;


    @Reference
    protected synchronized void setConfigApplyService ( ConfigApplyServerService ss ) {
        this.configApplyService = ss;
    }


    protected synchronized void unsetConfigApplyService ( ConfigApplyServerService ss ) {
        if ( this.configApplyService == ss ) {
            this.configApplyService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull ServiceConfigAppliedEvent event ) {
        this.configApplyService.handleConfigApplied(event);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<ServiceConfigAppliedEvent> getEventType () {
        return ServiceConfigAppliedEvent.class;
    }

}
