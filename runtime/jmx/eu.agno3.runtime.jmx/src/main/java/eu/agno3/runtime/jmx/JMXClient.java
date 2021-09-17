/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.jmx;


import java.io.IOException;

import javax.management.MBeanServerConnection;


/**
 * @author mbechler
 *
 */
public interface JMXClient extends MBeanServerConnection, AutoCloseable {

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws IOException;


    /**
     * @return whether the connection is valid
     */
    boolean isValid ();
}
