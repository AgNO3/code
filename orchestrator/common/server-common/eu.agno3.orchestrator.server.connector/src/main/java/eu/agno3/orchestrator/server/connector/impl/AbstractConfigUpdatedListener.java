/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.orchestrator.server.component.msg.ComponentConfigUpdatedEvent;
import eu.agno3.orchestrator.server.connector.ServerConnector;
import eu.agno3.orchestrator.server.connector.ServerConnectorException;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 * 
 * @param <TEvent>
 * @param <TConfig>
 */
public abstract class AbstractConfigUpdatedListener <TEvent extends ComponentConfigUpdatedEvent<TConfig>, TConfig extends ComponentConfig> implements
        EventListener<TEvent> {

    private static final Logger log = Logger.getLogger(AbstractConfigUpdatedListener.class);
    private ServerConnector<TConfig> serverConnector;


    /**
     * 
     */
    public AbstractConfigUpdatedListener () {
        super();
    }


    @Reference
    protected synchronized void setServerConnector ( ServerConnector<TConfig> connector ) {
        this.serverConnector = connector;
    }


    protected synchronized void unsetServerConnector ( ServerConnector<TConfig> connector ) {
        if ( this.serverConnector == connector ) {
            this.serverConnector = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull TEvent event ) {
        log.info("Recieved AgentConfigUpdatedEvent"); //$NON-NLS-1$
        try {
            this.serverConnector.updateConfig(event.getConfig());
        }
        catch ( ServerConnectorException e ) {
            log.error("Failed to update connector configuration:", e); //$NON-NLS-1$
        }
    }

}