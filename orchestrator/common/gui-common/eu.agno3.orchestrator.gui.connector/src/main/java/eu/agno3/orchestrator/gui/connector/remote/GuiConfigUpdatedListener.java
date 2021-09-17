/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.remote;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.orchestrator.gui.events.GuiConfigUpdatedEvent;
import eu.agno3.orchestrator.server.connector.impl.AbstractConfigUpdatedListener;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.gui.events.GuiConfigUpdatedEvent" )
public class GuiConfigUpdatedListener extends AbstractConfigUpdatedListener<GuiConfigUpdatedEvent, GuiConfig> {

    @Reference
    protected synchronized void setServerConnector ( RemoteGuiConnector c ) {
        // dependency only
    }


    protected synchronized void unsetServerConnector ( RemoteGuiConnector c ) {
        // dependency only
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<GuiConfigUpdatedEvent> getEventType () {
        return GuiConfigUpdatedEvent.class;
    }

}
