/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.events;


import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
@Component ( service = EventMessage.class )
public class ServiceConfigFailedEvent extends XmlMarshallableMessage<@NonNull MessageSource> implements EventMessage<@NonNull MessageSource> {

    private ServiceStructuralObject service;
    private StructuralObject anchor;
    private long revision;


    /**
     * 
     */
    public ServiceConfigFailedEvent () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public ServiceConfigFailedEvent ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public ServiceConfigFailedEvent ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public ServiceConfigFailedEvent ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * @return the service
     */
    public ServiceStructuralObject getService () {
        return this.service;
    }


    /**
     * @param service
     *            the service to set
     */
    public void setService ( ServiceStructuralObject service ) {
        this.service = service;
    }


    /**
     * @return the anchor
     */
    public StructuralObject getAnchor () {
        return this.anchor;
    }


    /**
     * @param anchor
     *            the anchor to set
     */
    public void setAnchor ( StructuralObject anchor ) {
        this.anchor = anchor;
    }


    /**
     * @return the revision
     */
    public long getRevision () {
        return this.revision;
    }


    /**
     * @param revision
     *            the revision to set
     */
    public void setRevision ( long revision ) {
        this.revision = revision;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Collection<EventScope> getScopes () {
        return Arrays.asList((EventScope) new ServersEventScope());
    }

}
