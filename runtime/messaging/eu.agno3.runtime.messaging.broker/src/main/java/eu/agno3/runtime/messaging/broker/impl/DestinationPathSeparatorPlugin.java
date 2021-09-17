/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.impl;


import org.apache.activemq.broker.util.DestinationPathSeparatorBroker;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    PrioritizedBrokerPlugin.class
}, enabled = false )
public class DestinationPathSeparatorPlugin extends DestinationPathSeparatorBroker implements PrioritizedBrokerPlugin {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.broker.PrioritizedBrokerPlugin#getPriority()
     */
    @Override
    public int getPriority () {
        return Integer.MAX_VALUE;
    }

}
