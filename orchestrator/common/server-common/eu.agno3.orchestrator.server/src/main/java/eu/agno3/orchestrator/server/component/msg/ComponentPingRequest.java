/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.RequestMessage;


/**
 * @author mbechler
 * @param <TSource>
 * 
 */
public interface ComponentPingRequest <@NonNull TSource extends MessageSource>
        extends RequestMessage<TSource, ComponentPongMessage, ComponentConnStateFailureMessage> {

}
