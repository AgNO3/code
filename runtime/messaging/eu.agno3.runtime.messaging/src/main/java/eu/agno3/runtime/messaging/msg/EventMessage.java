/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.msg;


import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public interface EventMessage <@NonNull T extends MessageSource> extends Message<T> {

    /**
     * The scopes to which this message should be delivered
     * 
     * @return the message scope
     */
    Collection<EventScope> getScopes ();

}
