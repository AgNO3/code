/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx;


import javax.management.MBeanServerConnection;


/**
 * @author mbechler
 *
 */
public interface JMSJMXClient extends MBeanServerConnection {

    /**
     * Create MXBean proxy
     * 
     * @param type
     * @return a invocation proxy
     */
    <T> T getProxy ( Class<T> type );


    /**
     * Create MBean proxy
     * 
     * @param type
     * @return a invocation proxy
     */
    <T> T getProxyMBean ( Class<T> type );

}