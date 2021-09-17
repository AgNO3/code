/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.resourcelibrary;


import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
public class ResourceLibraryListResponse extends XmlMarshallableMessage<@NonNull AgentMessageSource> implements
        ResponseMessage<@NonNull AgentMessageSource> {

    private List<ResourceLibraryEntry> entries;


    /**
     * 
     */
    public ResourceLibraryListResponse () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public ResourceLibraryListResponse ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public ResourceLibraryListResponse ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public ResourceLibraryListResponse ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.ResponseMessage#getStatus()
     */
    @Override
    public ResponseStatus getStatus () {
        return ResponseStatus.SUCCESS;
    }


    /**
     * @return the resource library entries
     */
    public List<ResourceLibraryEntry> getEntries () {
        return this.entries;
    }


    /**
     * @param entries
     *            the entries to set
     */
    public void setEntries ( List<ResourceLibraryEntry> entries ) {
        this.entries = entries;
    }

}
