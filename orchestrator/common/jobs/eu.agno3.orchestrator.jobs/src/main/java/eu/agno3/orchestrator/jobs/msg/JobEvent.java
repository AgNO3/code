/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.msg;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class JobEvent <@NonNull T extends MessageSource> extends XmlMarshallableMessage<T> implements EventMessage<T> {

    private UUID jobId;


    /**
     * 
     */
    public JobEvent () {
        super();
    }


    /**
     * @param jobId
     * @param origin
     */
    public JobEvent ( UUID jobId, T origin ) {
        super(origin);
        this.jobId = jobId;
    }


    /**
     * @param jobId
     * @param origin
     * @param ttl
     */
    public JobEvent ( UUID jobId, T origin, int ttl ) {
        super(origin, ttl);
        this.jobId = jobId;
    }


    /**
     * @param jobId
     * @param origin
     * @param replyTo
     */
    public JobEvent ( UUID jobId, T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
        this.jobId = jobId;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Set<EventScope> getScopes () {
        Set<EventScope> scopes = new HashSet<>();
        scopes.add(new ServersEventScope());
        return scopes;
    }


    /**
     * @return the jobId
     */
    public UUID getJobId () {
        return this.jobId;
    }


    /**
     * @param jobId
     *            the jobId to set
     */
    public void setJobId ( UUID jobId ) {
        this.jobId = jobId;
    }

}