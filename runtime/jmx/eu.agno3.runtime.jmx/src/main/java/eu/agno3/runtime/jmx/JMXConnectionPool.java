/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2016 by mbechler
 */
package eu.agno3.runtime.jmx;


/**
 * @author mbechler
 *
 */
public interface JMXConnectionPool {

    /**
     * @return a pooled connection
     * @throws JMXException
     */
    JMXClient getConnection () throws JMXException;


    /**
     * Close the pool
     */
    void close ();
}
