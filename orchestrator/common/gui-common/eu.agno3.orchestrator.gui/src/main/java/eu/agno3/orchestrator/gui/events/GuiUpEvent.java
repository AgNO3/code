/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.events;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.gui.msg.addressing.GuiMessageSource;
import eu.agno3.orchestrator.gui.msg.addressing.GuisEventScope;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class GuiUpEvent extends XmlMarshallableMessage<@NonNull GuiMessageSource> implements EventMessage<@NonNull GuiMessageSource> {

    private UUID guiId;


    /**
     * 
     */
    public GuiUpEvent () {
        super();
    }


    /**
     * @param guiId
     * @param origin
     */
    public GuiUpEvent ( UUID guiId, @NonNull GuiMessageSource origin ) {
        super(origin);
        this.guiId = guiId;
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
        scopes.add(new GuisEventScope());
        scopes.add(new ServersEventScope());
        return scopes;
    }


    /**
     * @return the guiId
     */
    public UUID getGuiId () {
        return this.guiId;
    }


    /**
     * @param guiId
     *            the guiId to set
     */
    public void setGuiId ( UUID guiId ) {
        this.guiId = guiId;
    }

}
