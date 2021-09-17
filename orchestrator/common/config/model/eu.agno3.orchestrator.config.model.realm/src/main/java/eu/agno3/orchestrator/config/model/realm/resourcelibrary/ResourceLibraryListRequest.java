/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.resourcelibrary;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
public class ResourceLibraryListRequest extends XmlMarshallableMessage<@NonNull ServerMessageSource> implements
        RequestMessage<@NonNull ServerMessageSource, ResourceLibraryListResponse, DefaultXmlErrorResponseMessage> {

    private MessageTarget target;
    private ServiceStructuralObject serviceTarget;
    private String libraryType;
    private String hint;


    /**
     * 
     */
    public ResourceLibraryListRequest () {
        super();
    }


    /**
     * @param target
     * @param origin
     * @param ttl
     */
    public ResourceLibraryListRequest ( @NonNull AgentMessageTarget target, @NonNull ServerMessageSource origin, int ttl ) {
        super(origin, ttl);
        this.target = target;
    }


    /**
     * @param target
     * @param origin
     * @param replyTo
     */
    public ResourceLibraryListRequest ( @NonNull AgentMessageTarget target, @NonNull ServerMessageSource origin,
            Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
        this.target = target;
    }


    /**
     * @param target
     * @param origin
     */
    public ResourceLibraryListRequest ( @NonNull AgentMessageTarget target, @NonNull ServerMessageSource origin ) {
        super(origin);
        this.target = target;
    }


    /**
     * @return the serviceTarget
     */
    public ServiceStructuralObject getServiceTarget () {
        return this.serviceTarget;
    }


    /**
     * @param serviceTarget
     *            the serviceTarget to set
     */
    public void setServiceTarget ( ServiceStructuralObject serviceTarget ) {
        this.serviceTarget = serviceTarget;
    }


    /**
     * @return the libraryType
     */
    public String getLibraryType () {
        return this.libraryType;
    }


    /**
     * @param libraryType
     *            the libraryType to set
     */
    public void setLibraryType ( String libraryType ) {
        this.libraryType = libraryType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<ResourceLibraryListResponse> getResponseType () {
        return ResourceLibraryListResponse.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<DefaultXmlErrorResponseMessage> getErrorResponseType () {
        return DefaultXmlErrorResponseMessage.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return this.target;
    }


    /**
     * @return the hint
     */
    public String getHint () {
        return this.hint;
    }


    /**
     * @param hint
     *            the hint to set
     */
    public void setHint ( String hint ) {
        this.hint = hint;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 10000;
    }

}
