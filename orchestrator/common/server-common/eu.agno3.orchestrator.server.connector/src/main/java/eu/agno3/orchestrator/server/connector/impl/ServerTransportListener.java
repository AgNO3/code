/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;

import org.apache.activemq.transport.TransportListener;
import org.eclipse.jdt.annotation.NonNull;


class ServerTransportListener implements TransportListener {

    private AbstractServerConnector<@NonNull ?, ?> connector;


    /**
     * @param connector
     */
    public ServerTransportListener ( AbstractServerConnector<@NonNull ?, ?> connector ) {
        this.connector = connector;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.transport.TransportListener#onCommand(java.lang.Object)
     */
    @Override
    public void onCommand ( Object obj ) {
        // nothing to do
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.transport.TransportListener#onException(java.io.IOException)
     */
    @Override
    public void onException ( IOException ex ) {

        if ( ex instanceof ConnectException ) {
            AbstractServerConnector.getLog().debug("Connection failed", ex); //$NON-NLS-1$
            this.connector.onConnectionError();
            return;
        }
        else if ( ex instanceof EOFException || ( ex != null && ex.getCause() instanceof ConnectException ) ) {
            AbstractServerConnector.getLog().warn("Server connection has been closed"); //$NON-NLS-1$
            this.connector.onConnectionError();
            return;
        }
        else if ( ex != null && ex.getCause() instanceof SecurityException ) {
            AbstractServerConnector.getLog().debug("Server has rejected connection", ex); //$NON-NLS-1$
            this.connector.onConnectionError();
            return;
        }

        AbstractServerConnector.getLog().error("Error in server connection:", ex); //$NON-NLS-1$
        this.connector.onConnectionError();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.transport.TransportListener#transportInterupted()
     */
    @Override
    public void transportInterupted () {
        AbstractServerConnector.getLog().warn("Server connection has been interrupted"); //$NON-NLS-1$
        this.connector.interrupted();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.transport.TransportListener#transportResumed()
     */
    @Override
    public void transportResumed () {
        AbstractServerConnector.getLog().debug("Server connection has been restored"); //$NON-NLS-1$
    }

}