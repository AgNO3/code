/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth.impl;


import org.apache.activemq.broker.Broker;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin;
import eu.agno3.runtime.messaging.broker.auth.BrokerAuthorizationPlugin;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    PrioritizedBrokerPlugin.class, BrokerAuthorizationPlugin.class
} )
public class DynamicAuthorizationPluginImpl implements PrioritizedBrokerPlugin, BrokerAuthorizationPlugin {

    private static final Logger log = Logger.getLogger(DynamicAuthorizationPluginImpl.class);

    private DynamicAccessDecisionManager accessManager;


    @Reference
    protected synchronized void setAccessManager ( DynamicAccessDecisionManager am ) {
        this.accessManager = am;
    }


    protected synchronized void unsetAccessManager ( DynamicAccessDecisionManager am ) {
        if ( this.accessManager == am ) {
            this.accessManager = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.broker.BrokerPlugin#installPlugin(org.apache.activemq.broker.Broker)
     */
    @Override
    public Broker installPlugin ( Broker next ) {
        log.debug("Installing dynamic authorization plugin"); //$NON-NLS-1$
        return new DynamicAuthorizationBroker(next, this.accessManager);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin#getPriority()
     */
    @Override
    public int getPriority () {
        return 0;
    }

}
