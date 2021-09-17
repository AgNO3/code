/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 10, 2017 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import org.apache.log4j.Logger;

import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.server.connector.ServerConnectorException;
import eu.agno3.orchestrator.system.agent.AgentConnectionService;


/**
 * @author mbechler
 *
 */
public class AgentConnectionServiceImpl implements AgentConnectionService {

    private static final Logger log = Logger.getLogger(AgentConnectionServiceImpl.class);
    private final AgentServerConnector connector;


    /**
     * @param connector
     */
    public AgentConnectionServiceImpl ( AgentServerConnector connector ) {
        this.connector = connector;
    }


    @Override
    public boolean tryConnect () {
        return this.connector.tryConnect();
    }


    @Override
    public void disconnect () {
        try {
            this.connector.disconnect();
        }
        catch ( ServerConnectorException e ) {
            log.debug("Failed to disconnect", e); //$NON-NLS-1$
        }
    }
}
