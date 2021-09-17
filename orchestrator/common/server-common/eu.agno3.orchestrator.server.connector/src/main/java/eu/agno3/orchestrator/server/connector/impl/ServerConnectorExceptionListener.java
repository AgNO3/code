/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import java.io.IOException;
import java.net.ConnectException;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.apache.activemq.ClientInternalExceptionListener;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class ServerConnectorExceptionListener implements ExceptionListener, ClientInternalExceptionListener {

    private static final Logger log = Logger.getLogger(ServerConnectorExceptionListener.class);


    /**
     * {@inheritDoc}
     *
     * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
     */
    @Override
    public void onException ( JMSException e ) {

        if ( e != null && e.getCause() instanceof IOException && e.getCause().getCause() instanceof SecurityException ) {
            return;
        }

        if ( e != null && e.getCause() instanceof ConnectException ) {
            return;
        }

        log.warn("JMS exception", e); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.ClientInternalExceptionListener#onException(java.lang.Throwable)
     */
    @Override
    public void onException ( Throwable e ) {
        log.warn("Client exception", e); //$NON-NLS-1$
    }

}
