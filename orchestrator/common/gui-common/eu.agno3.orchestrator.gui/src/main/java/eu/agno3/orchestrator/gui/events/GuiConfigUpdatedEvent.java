/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.events;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.orchestrator.gui.msg.addressing.GuiEventScope;
import eu.agno3.orchestrator.server.component.msg.ComponentConfigUpdatedEvent;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class GuiConfigUpdatedEvent extends XmlMarshallableMessage<@NonNull ServerMessageSource> implements ComponentConfigUpdatedEvent<GuiConfig> {

    private GuiConfig config;


    /**
     * 
     */
    public GuiConfigUpdatedEvent () {
        super();
    }


    /**
     * @param config
     * @param origin
     */
    public GuiConfigUpdatedEvent ( GuiConfig config, @NonNull ServerMessageSource origin ) {
        super(origin);
        this.config = config;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Set<EventScope> getScopes () {
        Set<EventScope> scopes = new HashSet<>();
        scopes.add(new GuiEventScope(this.config.getId()));
        scopes.add(new ServersEventScope());
        return scopes;
    }


    /**
     * @return the config
     */
    @Override
    public GuiConfig getConfig () {
        return this.config;
    }


    /**
     * @param config
     *            the config to set
     */
    public void setConfig ( GuiConfig config ) {
        this.config = config;
    }
}
