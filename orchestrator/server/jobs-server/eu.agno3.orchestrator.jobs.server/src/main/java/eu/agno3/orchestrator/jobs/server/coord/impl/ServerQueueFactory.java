/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.impl;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.coord.InternalQueue;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.coord.QueueFactory;
import eu.agno3.orchestrator.jobs.coord.internal.queue.QueueFactoryImpl;
import eu.agno3.orchestrator.jobs.targets.AnyServerTarget;
import eu.agno3.orchestrator.jobs.targets.ServerTarget;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    QueueFactory.class
} )
public class ServerQueueFactory extends QueueFactoryImpl implements QueueFactory {

    private static final Logger log = Logger.getLogger(ServerQueueFactory.class);

    private static final AnyServerTarget ANY_SERVER_TARGET = new AnyServerTarget();

    private JobTarget localTarget;

    private RemoteQueueClient remoteClient;

    private MessagingClient<ServerMessageSource> messagingClient;


    /**
     * 
     */
    public ServerQueueFactory () {}


    /**
     * @param local
     * @param remoteClient
     */
    public ServerQueueFactory ( JobTarget local, RemoteQueueClient remoteClient ) {
        this.localTarget = local;
        this.remoteClient = remoteClient;
    }


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        this.messagingClient = mc;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.messagingClient == mc ) {
            this.messagingClient = null;
        }
    }


    @Reference
    protected synchronized void setRemoteQueueClient ( RemoteQueueClient cl ) {
        this.remoteClient = cl;
    }


    protected synchronized void unsetRemoteQueueClient ( RemoteQueueClient cl ) {
        if ( this.remoteClient == cl ) {
            this.remoteClient = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.QueueFactoryImpl#createTargetQueue(eu.agno3.orchestrator.jobs.coord.InternalQueue,
     *      eu.agno3.orchestrator.jobs.JobTarget, eu.agno3.orchestrator.jobs.coord.JobStateTracker)
     */
    @Override
    public InternalQueue createTargetQueue ( InternalQueue groupQueue, JobTarget target, JobStateTracker jst ) {

        if ( isLocal(target) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Creating local queue for " + target); //$NON-NLS-1$
            }
            return super.createTargetQueue(groupQueue, target, jst);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Creating remote queue for " + target); //$NON-NLS-1$
        }

        return new RemoteTargetQueue(groupQueue, target, jst, this.remoteClient);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.QueueFactory#isLocal(eu.agno3.orchestrator.jobs.JobTarget)
     */
    @Override
    public boolean isLocal ( JobTarget target ) {
        return ANY_SERVER_TARGET.equals(target) || this.getLocalTarget().equals(target);
    }


    /**
     * @return
     */
    private JobTarget getLocalTarget () {
        if ( this.localTarget == null ) {
            this.localTarget = new ServerTarget(this.messagingClient.getMessageSource().getServerId());
        }
        return this.localTarget;
    }

}
