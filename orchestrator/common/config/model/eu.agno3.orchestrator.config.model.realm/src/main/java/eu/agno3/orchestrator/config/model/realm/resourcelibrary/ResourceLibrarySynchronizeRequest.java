/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.resourcelibrary;


import java.util.Set;

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
public class ResourceLibrarySynchronizeRequest extends XmlMarshallableMessage<@NonNull ServerMessageSource> implements
        RequestMessage<@NonNull ServerMessageSource, ResourceLibraryListResponse, DefaultXmlErrorResponseMessage> {

    private MessageTarget target;
    private ServiceStructuralObject serviceTarget;
    private String libraryType;

    private Set<ResourceLibraryEntry> add;
    private Set<ResourceLibraryEntry> update;
    private Set<ResourceLibraryEntry> delete;
    private String hint;


    /**
     * 
     */
    public ResourceLibrarySynchronizeRequest () {
        super();
    }


    /**
     * @param target
     * @param origin
     * @param ttl
     */
    public ResourceLibrarySynchronizeRequest ( @NonNull AgentMessageTarget target, @NonNull ServerMessageSource origin, int ttl ) {
        super(origin, ttl);
        this.target = target;
    }


    /**
     * @param target
     * @param origin
     * @param replyTo
     */
    public ResourceLibrarySynchronizeRequest ( @NonNull AgentMessageTarget target, @NonNull ServerMessageSource origin,
            Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
        this.target = target;
    }


    /**
     * @param target
     * @param origin
     */
    public ResourceLibrarySynchronizeRequest ( @NonNull AgentMessageTarget target, @NonNull ServerMessageSource origin ) {
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 10000;
    }


    /**
     * @return the add
     */
    public Set<ResourceLibraryEntry> getAdd () {
        return this.add;
    }


    /**
     * @param add
     *            the add to set
     */
    public void setAdd ( Set<ResourceLibraryEntry> add ) {
        this.add = add;
    }


    /**
     * @return the update
     */
    public Set<ResourceLibraryEntry> getUpdate () {
        return this.update;
    }


    /**
     * @param update
     *            the update to set
     */
    public void setUpdate ( Set<ResourceLibraryEntry> update ) {
        this.update = update;
    }


    /**
     * @return the delete
     */
    public Set<ResourceLibraryEntry> getDelete () {
        return this.delete;
    }


    /**
     * @param delete
     *            the delete to set
     */
    public void setDelete ( Set<ResourceLibraryEntry> delete ) {
        this.delete = delete;
    }


    /**
     * @return the hint
     */
    public String getHint () {
        return this.hint;
    }


    /**
     * @param hint
     */
    public void setHint ( String hint ) {
        this.hint = hint;
    }

}
