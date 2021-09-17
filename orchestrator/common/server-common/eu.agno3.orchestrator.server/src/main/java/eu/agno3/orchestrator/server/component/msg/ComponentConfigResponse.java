/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.msg.ResponseMessage;


/**
 * @author mbechler
 * @param <TConfig>
 * 
 */
public interface ComponentConfigResponse <TConfig> extends ResponseMessage<@NonNull ServerMessageSource> {

    /**
     * @return the component configuration
     */
    TConfig getConfiguration ();

}
