/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.auth.internal;


import org.apache.activemq.broker.Broker;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.addressing.MessageSourceRegistry;
import eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin;
import eu.agno3.runtime.messaging.broker.auth.BrokerAuthenticationPlugin;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    PrioritizedBrokerPlugin.class, BrokerAuthenticationPlugin.class
} )
public class AuthenticationPluginImpl implements BrokerAuthenticationPlugin {

    private MessageSourceRegistry msgSourceRegistry;
    private ClientCertificateHandlerImpl certficateHandler;


    @Reference
    protected synchronized void setMsgSourceRegistry ( MessageSourceRegistry msr ) {
        this.msgSourceRegistry = msr;
    }


    protected synchronized void unsetMsgSourceRegistry ( MessageSourceRegistry msr ) {
        if ( this.msgSourceRegistry == msr ) {
            this.msgSourceRegistry = null;
        }
    }


    @Reference
    protected synchronized void setCertificateHandler ( ClientCertificateHandlerImpl ch ) {
        this.certficateHandler = ch;
    }


    protected synchronized void unsetCertificateHandler ( ClientCertificateHandlerImpl ch ) {
        if ( this.certficateHandler == ch ) {
            this.certficateHandler = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin#getPriority()
     */
    @Override
    public int getPriority () {
        return Integer.MAX_VALUE;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.broker.BrokerPlugin#installPlugin(org.apache.activemq.broker.Broker)
     */
    @Override
    public Broker installPlugin ( Broker next ) throws Exception {
        return new AuthenticationBroker(new MessageSourceBrokerFilter(next, this.msgSourceRegistry), this.certficateHandler);
    }

}
