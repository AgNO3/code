/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.auth;


import java.security.Principal;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 *
 */
public interface ComponentPrincipal extends Principal {

    /**
     * 
     * @return the principal type
     */
    Class<? extends ComponentPrincipal> getType ();


    /**
     * 
     * @return the component id
     */
    @NonNull
    UUID getComponentId ();


    /**
     * 
     * @return the message source for this component
     */
    MessageSource getMessageSource ();
}
