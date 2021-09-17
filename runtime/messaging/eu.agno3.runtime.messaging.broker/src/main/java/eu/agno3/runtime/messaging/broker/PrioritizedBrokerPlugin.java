/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker;


import org.apache.activemq.broker.BrokerPlugin;


/**
 * @author mbechler
 * 
 */
public interface PrioritizedBrokerPlugin extends BrokerPlugin {

    /**
     * 
     * @return priority of this plugin, higher comes first
     */
    int getPriority ();
}
