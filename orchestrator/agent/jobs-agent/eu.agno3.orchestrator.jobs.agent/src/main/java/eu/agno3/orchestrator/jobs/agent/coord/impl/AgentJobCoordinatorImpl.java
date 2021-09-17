/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.coord.impl;


import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.coord.ExecutorFactory;
import eu.agno3.orchestrator.jobs.coord.PersistentJobStateTracker;
import eu.agno3.orchestrator.jobs.coord.QueueFactory;
import eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 * 
 */
@Component ( service = JobCoordinator.class )
public class AgentJobCoordinatorImpl extends JobCoordinatorImpl {

    private static final Logger log = Logger.getLogger(AgentJobCoordinatorImpl.class);

    private static final int MIN_KEEPALIVE_INTERVAL = 20;

    private Object msgClientLock = new Object();
    private MessagingClient<AgentMessageSource> msgClient;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected void setMessageClient ( MessagingClient<AgentMessageSource> mc ) {
        synchronized ( this.msgClientLock ) {
            this.msgClient = mc;
        }
    }


    protected void unsetMessageClient ( MessagingClient<AgentMessageSource> mc ) {
        synchronized ( this.msgClientLock ) {
            if ( this.msgClient == mc ) {
                this.msgClient = null;
            }
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        super.shutdown();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#setExecutorFactory(eu.agno3.orchestrator.jobs.coord.ExecutorFactory)
     */
    @Override
    @Reference
    protected synchronized void setExecutorFactory ( ExecutorFactory ef ) {
        super.setExecutorFactory(ef);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#unsetExecutorFactory(eu.agno3.orchestrator.jobs.coord.ExecutorFactory)
     */
    @Override
    protected synchronized void unsetExecutorFactory ( ExecutorFactory ef ) {
        super.unsetExecutorFactory(ef);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#setQueueFactory(eu.agno3.orchestrator.jobs.coord.QueueFactory)
     */
    @Override
    @Reference
    protected synchronized void setQueueFactory ( QueueFactory queueFactory ) {
        super.setQueueFactory(queueFactory);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#unsetQueueFactory(eu.agno3.orchestrator.jobs.coord.QueueFactory)
     */
    @Override
    protected synchronized void unsetQueueFactory ( QueueFactory qf ) {
        super.unsetQueueFactory(qf);
    }


    @Reference
    protected synchronized void setJobStateTracker ( PersistentJobStateTracker pjst ) {
        super.setJobStateTracker(pjst);
    }


    protected synchronized void unsetJobStateTracker ( PersistentJobStateTracker pjst ) {
        super.unsetJobStateTracker(pjst);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#run()
     */
    @Override
    public void run () throws JobQueueException {
        super.run();

        for ( JobGroup g : this.getKnownGroups() ) {
            this.doKeepAlive(this.getActiveJobs(g));
            this.doKeepAlive(this.getQueuedJobs(g));
        }
    }


    /**
     * @param jobs
     */
    private void doKeepAlive ( Collection<JobInfo> jobs ) {
        DateTime minimumKeepAlive = DateTime.now().minusSeconds(MIN_KEEPALIVE_INTERVAL);

        for ( JobInfo info : jobs ) {
            if ( info.getLastKeepAliveTime() == null && info.getLastKeepAliveTime().isBefore(minimumKeepAlive) ) {
                this.doKeepAlive(info.getJobId());
            }

            this.getJobStateTracker().doKeepAlive(info.getJobId());
        }
    }


    /**
     * @param job
     */
    private void doKeepAlive ( UUID jobId ) {
        synchronized ( this.msgClientLock ) {
            if ( this.msgClient != null ) {
                JobKeepAliveEvent ev = new JobKeepAliveEvent(jobId, this.msgClient.getMessageSource());
                try {
                    this.msgClient.publishEvent(ev);
                }
                catch (
                    MessagingException |
                    InterruptedException e ) {
                    log.warn("Failed to publish keep alive event:", e); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.JobCoordinatorImpl#jobUpdated(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public void jobUpdated ( JobStateUpdatedEvent ev ) {
        publishUpdateEvent(ev);
        super.jobUpdated(ev);
    }


    private void publishUpdateEvent ( JobStateUpdatedEvent ev ) {
        synchronized ( this.msgClientLock ) {
            try {
                if ( this.msgClient != null ) {
                    JobStateUpdatedEvent nev = new JobStateUpdatedEvent(ev.getJobId(), this.msgClient.getMessageSource());
                    nev.setJobInfo(ev.getJobInfo());
                    this.msgClient.publishEvent(nev);
                }
                else {
                    log.debug("Failed to publish job state update event as no connection to server is available"); //$NON-NLS-1$
                }
            }
            catch (
                MessagingException |
                InterruptedException e ) {
                log.warn("Failed to publish job state event:", e); //$NON-NLS-1$
            }
        }
    }

}
