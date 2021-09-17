/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2016 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import javax.jms.Connection;
import javax.jms.JMSException;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.SharedConnectionFactory;


/**
 * @author mbechler
 *
 */
public class SystemConnectionFactory implements SharedConnectionFactory {

    private AbstractServerConnector<@NonNull ?, ?> sc;


    /**
     * @param sc
     */
    public SystemConnectionFactory ( AbstractServerConnector<@NonNull ?, ?> sc ) {
        this.sc = sc;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.jms.ConnectionFactory#createConnection()
     */
    @Override
    public Connection createConnection () throws JMSException {
        return this.sc.getSystemConnection();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.jms.ConnectionFactory#createConnection(java.lang.String, java.lang.String)
     */
    @Override
    public Connection createConnection ( String arg0, String arg1 ) throws JMSException {
        throw new UnsupportedOperationException();
    }

}
