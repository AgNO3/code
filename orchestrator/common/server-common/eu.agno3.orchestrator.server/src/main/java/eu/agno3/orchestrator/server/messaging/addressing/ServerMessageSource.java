/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.messaging.addressing;


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
@Component ( service = MessageSourceRegistration.class, property = "type=server" )
public class ServerMessageSource implements MessageSource, MessageSourceRegistration {

    /**
     * 
     */
    private static final long serialVersionUID = -733302275246890431L;

    private static final String SERVER_PREFIX = "server:"; //$NON-NLS-1$

    private Optional<@NonNull UUID> serverId = Optional.empty();


    /**
     * 
     */
    public ServerMessageSource () {

    }


    /**
     * @param serverId
     */
    public ServerMessageSource ( @NonNull UUID serverId ) {
        this.serverId = Optional.of(serverId);
    }


    /**
     * @return the serverId
     */
    @NonNull
    public UUID getServerId () {
        return this.serverId.get();
    }


    /**
     * @param serverId
     *            the serverId to set
     */
    public void setServerId ( @NonNull UUID serverId ) {
        this.serverId = Optional.of(serverId);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSource#encode()
     */
    @Override
    public String encode () {
        return String.format("%s%s", SERVER_PREFIX, this.getServerId()); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSource#parse(java.lang.String)
     */
    @Override
    public void parse ( String encoded ) {
        if ( !encoded.startsWith(SERVER_PREFIX) ) {
            throw new IllegalArgumentException("Invalid agent message source"); //$NON-NLS-1$
        }

        UUID fromString = UUID.fromString(encoded.substring(SERVER_PREFIX.length()));

        if ( fromString == null ) {
            throw new IllegalArgumentException();
        }

        this.serverId = Optional.of(fromString);
    }
}
