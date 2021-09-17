/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.client;


import javax.jms.ConnectionFactory;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.routing.EventRouterManager;


/**
 * @author mbechler
 * 
 */
public interface MessagingClientFactory {

    /**
     * @param source
     * @param cf
     * @return a new client instance
     */
    <T extends MessageSource> MessagingClient<T> createClient ( @NonNull T source, @NonNull ConnectionFactory cf );


    /**
     * 
     * @param source
     * @param cf
     * @param erm
     * @return a new client instance using a non default event router manager
     */
    <T extends MessageSource> MessagingClient<T> createClient ( @NonNull T source, @NonNull ConnectionFactory cf, @NonNull EventRouterManager erm );


    /**
     * @param source
     * @param cf
     * @return a new client instance supporting transactions
     */
    <T extends MessageSource> MessagingClient<T> createTransactedClient ( @NonNull T source, @NonNull ConnectionFactory cf );
}
