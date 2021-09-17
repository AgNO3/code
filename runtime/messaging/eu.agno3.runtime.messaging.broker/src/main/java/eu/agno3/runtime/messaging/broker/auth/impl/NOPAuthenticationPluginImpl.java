/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth.impl;


import org.apache.activemq.broker.Broker;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin;
import eu.agno3.runtime.messaging.broker.auth.BrokerAuthenticationPlugin;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    PrioritizedBrokerPlugin.class, BrokerAuthenticationPlugin.class
}, configurationPid = "messaging.broker.auth.nop", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class NOPAuthenticationPluginImpl implements PrioritizedBrokerPlugin, BrokerAuthenticationPlugin {

    private static final Logger log = Logger.getLogger(NOPAuthenticationPluginImpl.class);


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.broker.BrokerPlugin#installPlugin(org.apache.activemq.broker.Broker)
     */
    @Override
    public Broker installPlugin ( Broker next ) {
        log.debug("Installing NOP authentication plugin"); //$NON-NLS-1$
        log.error("NOP Authentication plugin active, never ever use in production"); //$NON-NLS-1$
        return new NOPAuthenticationBroker(next);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin#getPriority()
     */
    @Override
    public int getPriority () {
        return Integer.MIN_VALUE;
    }
}
