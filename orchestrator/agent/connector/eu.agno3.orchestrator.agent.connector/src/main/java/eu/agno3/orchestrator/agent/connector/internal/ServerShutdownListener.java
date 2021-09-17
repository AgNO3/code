/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 29, 2017 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.internal;


import javax.transaction.Status;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.server.component.msg.ServerShutdownEvent;
import eu.agno3.orchestrator.server.connector.ServerConnectorException;
import eu.agno3.runtime.messaging.listener.EventListener;
import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 *
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.server.component.msg.ServerShutdownEvent" )
public class ServerShutdownListener implements EventListener<ServerShutdownEvent> {

    private static final Logger log = Logger.getLogger(ServerShutdownListener.class);

    private AgentServerConnector connector;
    private TransactionService transactionService;


    @Reference
    protected synchronized void setConnector ( AgentServerConnector asc ) {
        this.connector = asc;
    }


    protected synchronized void unsetConnector ( AgentServerConnector asc ) {
        if ( this.connector == asc ) {
            this.connector = null;
        }
    }


    @Reference
    protected synchronized void setTransactionManager ( TransactionService ts ) {
        this.transactionService = ts;
    }


    protected synchronized void unsetTransactionManager ( TransactionService ts ) {
        if ( this.transactionService == ts ) {
            this.transactionService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull ServerShutdownEvent event ) {
        log.info("Received server shutdown event"); //$NON-NLS-1$
        try {
            try {
                // if we have an active transaction from reading the event, commit it here
                // we cannot commit it after disconnecting
                if ( this.transactionService.getTransactionManager().getStatus() == Status.STATUS_ACTIVE ) {
                    this.transactionService.getTransactionManager().commit();
                }
            }
            catch ( Exception e ) {
                log.warn("Failed to commit existing transaction", e); //$NON-NLS-1$
            }

            this.connector.serverShutdown();
        }
        catch ( ServerConnectorException e ) {
            log.warn("Failed to disconnect from server", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<ServerShutdownEvent> getEventType () {
        return ServerShutdownEvent.class;
    }

}
