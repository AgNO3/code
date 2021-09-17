/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;


/**
 * @author mbechler
 * @param <TSource>
 * @param <TConfig>
 * @param <TConfigResponse>
 * @param <TError>
 * 
 */
public interface ComponentConfigRequest <@NonNull TSource extends MessageSource, TConfig, TConfigResponse extends ComponentConfigResponse<TConfig>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>>
        extends RequestMessage<TSource, TConfigResponse, TError> {

}
