/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * @param <TConfig>
 * 
 */
public interface ComponentConfigUpdatedEvent <TConfig extends ComponentConfig> extends EventMessage<@NonNull ServerMessageSource> {

    /**
     * @return the updated configuration
     */
    TConfig getConfig ();
}
