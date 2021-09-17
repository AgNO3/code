/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.auth;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 *
 */
public class ServerComponentPrincipal extends AbstractComponentPrincipal implements ComponentPrincipal {

    /**
     * 
     */
    public static final String SERVER_USER_PREFIX = "server-"; //$NON-NLS-1$


    /**
     * 
     * @param componentId
     */
    public ServerComponentPrincipal ( @NonNull UUID componentId ) {
        super(SERVER_USER_PREFIX, componentId);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.auth.ComponentPrincipal#getMessageSource()
     */
    @Override
    public MessageSource getMessageSource () {
        return new ServerMessageSource(this.getComponentId());
    }

}
