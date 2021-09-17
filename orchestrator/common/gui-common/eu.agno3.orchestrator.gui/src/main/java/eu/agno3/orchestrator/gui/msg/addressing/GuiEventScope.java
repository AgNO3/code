/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.msg.addressing;


import java.util.Objects;
import java.util.UUID;

import eu.agno3.runtime.messaging.addressing.EventScope;


/**
 * @author mbechler
 * 
 */
public class GuiEventScope extends GuisEventScope {

    private UUID guiId;


    /**
     * @param guiId
     */
    public GuiEventScope ( UUID guiId ) {
        this.guiId = guiId;
    }


    /**
     * @return the agentId
     */
    public UUID getGuiId () {
        return this.guiId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.EventScope#getParent()
     */
    @Override
    public EventScope getParent () {
        return new GuisEventScope();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.gui.msg.addressing.GuisEventScope#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return "events-gui-" + this.guiId; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope#hashCode()
     */
    @Override
    public int hashCode () {
        return super.hashCode() + ( this.guiId != null ? 3 * this.guiId.hashCode() : 0 );
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        return super.equals(obj) && Objects.equals(this.guiId, ( (GuiEventScope) obj ).guiId);
    }

}
