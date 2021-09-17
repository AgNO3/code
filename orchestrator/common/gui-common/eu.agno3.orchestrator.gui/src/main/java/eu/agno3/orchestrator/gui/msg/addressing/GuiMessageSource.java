/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.msg.addressing;


import java.util.Optional;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistration;


/**
 * @author mbechler
 * 
 */
@Component ( service = MessageSourceRegistration.class, property = "type=gui" )
public class GuiMessageSource implements MessageSource, MessageSourceRegistration {

    private static final String GUI_PREFIX = "gui:"; //$NON-NLS-1$
    /**
     * 
     */
    private static final long serialVersionUID = -2761325778982002679L;

    private Optional<@NonNull UUID> guiId = Optional.empty();


    /**
     * Internal
     * 
     */
    public GuiMessageSource () {

    }


    /**
     * 
     * @param guiId
     */
    public GuiMessageSource ( @NonNull UUID guiId ) {
        this.guiId = Optional.of(guiId);
    }


    /**
     * @return the agentId
     */
    @NonNull
    public UUID getGuiId () {
        return this.guiId.get();
    }


    /**
     * @param guiId
     *            the agentId to set
     */
    protected void setGuiId ( @NonNull UUID guiId ) {
        this.guiId = Optional.of(guiId);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSource#encode()
     */
    @Override
    public String encode () {
        return String.format("%s%s", GUI_PREFIX, this.getGuiId()); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSource#parse(java.lang.String)
     */
    @Override
    public void parse ( String encoded ) {
        if ( !encoded.startsWith(GUI_PREFIX) ) {
            throw new IllegalArgumentException("Invalid agent message source"); //$NON-NLS-1$
        }

        UUID fromString = UUID.fromString(encoded.substring(GUI_PREFIX.length()));

        if ( fromString == null ) {
            throw new IllegalArgumentException();
        }

        this.guiId = Optional.of(fromString);
    }
}
