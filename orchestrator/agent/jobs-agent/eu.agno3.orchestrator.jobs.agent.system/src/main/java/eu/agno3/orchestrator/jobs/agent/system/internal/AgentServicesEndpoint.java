/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.10.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system.internal;


import java.util.Collection;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.config.model.msg.AgentServiceEntry;
import eu.agno3.orchestrator.config.model.msg.AgentServicesRequest;
import eu.agno3.orchestrator.config.model.msg.AgentServicesResponse;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.config.model.msg.AgentServicesRequest" )
public class AgentServicesEndpoint implements RequestEndpoint<AgentServicesRequest, AgentServicesResponse, DefaultXmlErrorResponseMessage> {

    private static final Logger log = Logger.getLogger(AgentServicesEndpoint.class);

    private Optional<@NonNull AgentMessageSource> messageSource = Optional.empty();
    private ConfigRepository configRepository;


    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource ms ) {
        this.messageSource = Optional.of((AgentMessageSource) ms);
    }


    protected synchronized void unsetMessageSource ( MessageSource ms ) {
        if ( this.messageSource.equals(ms) ) {
            this.messageSource = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository cr ) {
        this.configRepository = cr;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository cr ) {
        if ( this.configRepository == cr ) {
            this.configRepository = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public AgentServicesResponse onReceive ( @NonNull AgentServicesRequest msg ) throws MessageProcessingException, MessagingException {
        log.debug("Received services request"); //$NON-NLS-1$
        @NonNull
        AgentMessageSource ms = this.messageSource.get();

        AgentServicesResponse response = new AgentServicesResponse(ms, msg);

        try {
            Collection<ServiceStructuralObject> services = this.configRepository.getServices();
            for ( ServiceStructuralObject service : services ) {
                AgentServiceEntry e = new AgentServiceEntry();
                e.setService(service);
                Optional<ConfigurationInstance> activecfg = Optional.empty();
                try {
                    activecfg = this.configRepository.getActiveConfiguration(service);
                    if ( activecfg.isPresent() ) {
                        e.setAppliedRevision(activecfg.get().getRevision());
                    }
                }
                catch ( ConfigRepositoryException ex ) {
                    log.error("Failed to read stored active configuration", ex); //$NON-NLS-1$
                }

                Optional<ConfigurationInstance> lastcfg = Optional.empty();
                try {
                    lastcfg = this.configRepository.getFailsafeConfiguration(service);

                    if ( lastcfg.isPresent() ) {
                        e.setFailsafeRevision(lastcfg.get().getRevision());
                    }
                }
                catch ( ConfigRepositoryException ex ) {
                    log.error("Failed to read stored failsafe configuration", ex); //$NON-NLS-1$
                }

                response.getServices().add(e);
            }
        }
        catch ( ConfigRepositoryException e ) {
            throw new MessageProcessingException(new DefaultXmlErrorResponseMessage(e, ms, msg));
        }

        return response;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<AgentServicesRequest> getMessageType () {
        return AgentServicesRequest.class;
    }

}
