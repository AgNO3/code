/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles.internal;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryEntry;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryException;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryListResponse;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 * @param <T>
 *
 */
public abstract class AbstractResourceLibraryRequestEndpoint <T extends RequestMessage<@NonNull ServerMessageSource, ResourceLibraryListResponse, DefaultXmlErrorResponseMessage>>
        implements RequestEndpoint<T, ResourceLibraryListResponse, DefaultXmlErrorResponseMessage> {

    static final Logger log = Logger.getLogger(ResourceLibraryListRequestEndpoint.class);
    private Optional<@NonNull AgentMessageSource> messageSource = Optional.empty();
    private Map<String, ResourceLibrarySynchronizationHandler> handlers = new HashMap<>();


    /**
     * 
     */
    public AbstractResourceLibraryRequestEndpoint () {
        super();
    }


    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource source ) {
        this.messageSource = Optional.of((AgentMessageSource) source);
    }


    protected synchronized void unsetMessageSource ( MessageSource source ) {
        if ( this.messageSource.equals(source) ) {
            this.messageSource = Optional.empty();
        }
    }


    /**
     * @return the messageSource
     */
    public @NonNull AgentMessageSource getMessageSource () {
        return this.messageSource.get();
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindSynchronizationHandler ( ResourceLibrarySynchronizationHandler rls ) {
        if ( this.handlers.put(rls.getType(), rls) != null ) {
            log.warn("Multiple handlers for type " + rls.getType()); //$NON-NLS-1$
        }
    }


    protected synchronized void unbindSynchronizationHandler ( ResourceLibrarySynchronizationHandler rls ) {
        this.handlers.remove(rls.getType());
    }


    /**
     * @param type
     * @param msg
     * @return
     * @throws ResourceLibraryException
     */
    protected ResourceLibrarySynchronizationHandler getSynchronizationHandler ( String type ) throws ResourceLibraryException {
        ResourceLibrarySynchronizationHandler handler = this.handlers.get(type);
        if ( handler == null ) {
            throw new ResourceLibraryException("No handler found for " + type); //$NON-NLS-1$
        }
        return handler;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public ResourceLibraryListResponse onReceive ( @NonNull T msg ) throws MessageProcessingException, MessagingException {
        @NonNull
        AgentMessageSource ms = this.getMessageSource();
        try {
            ResourceLibraryListResponse resp = new ResourceLibraryListResponse(ms, msg);
            resp.setEntries(getResults(msg));
            return resp;
        }
        catch ( ResourceLibraryException e ) {
            log.warn("Failed to handle request", e); //$NON-NLS-1$
            throw new MessageProcessingException(new DefaultXmlErrorResponseMessage(e, ms, msg));
        }
        catch ( Exception e ) {
            log.warn("Runtime exception", e); //$NON-NLS-1$
            throw e;
        }

    }


    /**
     * @param msg
     * @param type
     * @return
     */
    protected abstract List<ResourceLibraryEntry> getResults ( @NonNull T msg ) throws ResourceLibraryException;

}