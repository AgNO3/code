/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import org.apache.activemq.RedeliveryPolicy;


/**
 * @author mbechler
 *
 */
public interface RedeliveryPolicyProvider {

    /**
     * @return the desired redelivery policy
     */
    RedeliveryPolicy getRedeliveryPolicy ();

}
