/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx.internal;


import javax.management.MalformedObjectNameException;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.jmsjmx.AbstractJMXRequest;
import eu.agno3.runtime.jmsjmx.JMSJMXClient;
import eu.agno3.runtime.jmsjmx.JMSJMXClientFactory;
import eu.agno3.runtime.jmsjmx.JMXErrorResponse;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 *
 */

@Component ( service = JMSJMXClientFactory.class )
public class JMSJMXClientFactoryImpl implements JMSJMXClientFactory {

    private MessagingClient<MessageSource> msgClient;


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<MessageSource> cl ) {
        this.msgClient = cl;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<MessageSource> cl ) {
        if ( this.msgClient == cl ) {
            this.msgClient = null;
        }
    }


    @Override
    public JMSJMXClient getClient ( AbstractJMXRequest<@NonNull MessageSource, ? extends JMXErrorResponse> type )
            throws MalformedObjectNameException {
        return new JMSJMXClientImpl(this.msgClient, type);
    }
}
