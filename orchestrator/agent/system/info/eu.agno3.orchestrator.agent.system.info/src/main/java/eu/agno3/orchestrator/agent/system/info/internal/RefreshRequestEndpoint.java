/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.03.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.system.info.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.connector.AbstractAgentRequestEndpoint;
import eu.agno3.orchestrator.agent.system.info.SystemInformationRefresher;
import eu.agno3.orchestrator.system.info.msg.RefreshRequest;
import eu.agno3.orchestrator.system.info.msg.RefreshResponse;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = RequestEndpoint.class, property = {
    "msgType=eu.agno3.orchestrator.system.info.msg.RefreshRequest"
} )
public class RefreshRequestEndpoint extends AbstractAgentRequestEndpoint<RefreshRequest, RefreshResponse, DefaultXmlErrorResponseMessage> {

    private SystemInformationRefresher refresher;


    @Reference
    protected synchronized void setSystemInformationRefresher ( SystemInformationRefresher sir ) {
        this.refresher = sir;
    }


    protected synchronized void unsetSystemInformationRefresher ( SystemInformationRefresher sir ) {
        if ( this.refresher == sir ) {
            this.refresher = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.connector.AbstractAgentRequestEndpoint#setAgentConfig(eu.agno3.orchestrator.agent.config.AgentConfig)
     */
    @Override
    @Reference
    protected synchronized void setAgentConfig ( AgentConfig config ) {
        super.setAgentConfig(config);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.connector.AbstractAgentRequestEndpoint#unsetAgentConfig(eu.agno3.orchestrator.agent.config.AgentConfig)
     */
    @Override
    protected synchronized void unsetAgentConfig ( AgentConfig config ) {
        super.unsetAgentConfig(config);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public synchronized RefreshResponse onReceive ( @NonNull RefreshRequest msg ) throws MessageProcessingException, MessagingException {
        this.refresher.triggerRefresh(false);
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<RefreshRequest> getMessageType () {
        return RefreshRequest.class;
    }

}
