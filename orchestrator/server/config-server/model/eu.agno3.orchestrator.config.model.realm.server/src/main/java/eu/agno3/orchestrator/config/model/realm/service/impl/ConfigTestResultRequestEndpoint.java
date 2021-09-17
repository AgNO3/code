/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.msg.ConfigTestResultUpdateRequest;
import eu.agno3.orchestrator.config.model.msg.ConfigTestResultUpdateResponse;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.config.model.msg.ConfigTestResultUpdateRequest" )
public class ConfigTestResultRequestEndpoint
        implements RequestEndpoint<ConfigTestResultUpdateRequest, ConfigTestResultUpdateResponse, DefaultXmlErrorResponseMessage> {

    private static final Logger log = Logger.getLogger(ConfigTestResultRequestEndpoint.class);

    private Optional<@NonNull ServerMessageSource> msgSource = Optional.empty();

    private ConfigTestResultCache resultCache;


    @Reference
    protected synchronized void setResultCache ( ConfigTestResultCache ctrc ) {
        this.resultCache = ctrc;
    }


    protected synchronized void unsetResultCache ( ConfigTestResultCache ctrc ) {
        if ( this.resultCache == ctrc ) {
            this.resultCache = null;
        }
    }


    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource ms ) {
        this.msgSource = Optional.of((ServerMessageSource) ms);
    }


    protected synchronized void unsetMessageSource ( MessageSource ms ) {
        if ( this.msgSource.equals(ms) ) {
            this.msgSource = Optional.empty();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public ConfigTestResultUpdateResponse onReceive ( @NonNull ConfigTestResultUpdateRequest msg )
            throws MessageProcessingException, MessagingException {
        log.debug("Received synchronous config test result"); //$NON-NLS-1$
        this.resultCache.update(msg.getSequence(), msg.getResult());
        return new ConfigTestResultUpdateResponse(this.msgSource.get(), msg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<ConfigTestResultUpdateRequest> getMessageType () {
        return ConfigTestResultUpdateRequest.class;
    }

}
