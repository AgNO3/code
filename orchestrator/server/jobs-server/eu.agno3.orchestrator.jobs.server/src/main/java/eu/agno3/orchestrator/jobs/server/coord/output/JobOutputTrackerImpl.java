/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.output;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.gui.connector.GuiNotificationEvent;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.coord.db.JobOutputSegment;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;
import eu.agno3.orchestrator.jobs.msg.JobOutputEvent;
import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.orchestrator.jobs.server.JobOutputBuffer;
import eu.agno3.orchestrator.jobs.server.JobOutputTracker;
import eu.agno3.orchestrator.jobs.state.JobStateListener;
import eu.agno3.orchestrator.jobs.state.JobStateObservable;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 *
 */
@Component ( service = JobOutputTracker.class )
public class JobOutputTrackerImpl implements JobOutputTracker, JobStateListener {

    /**
     * 
     */
    private static final String FAILED_TO_GET_JOB_INFO = "Failed to get job info"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(JobOutputTrackerImpl.class);

    private JobCoordinator coordinator;
    private MessagingClient<ServerMessageSource> msgClient;
    private EntityManagerFactory jobEmf;

    private Map<UUID, JobOutputBufferImpl> buffers = new HashMap<>();

    private TransactionService transactionService;


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {
            Set<UUID> active = new HashSet<>(this.buffers.keySet());
            for ( UUID jobId : active ) {
                evictJobOutput(jobId);
            }
            tx.commit();
        }
        catch ( Exception e ) {
            log.warn("Failed to flush job output", e); //$NON-NLS-1$
        }
    }


    @Reference ( service = EntityManagerFactory.class, target = "(persistenceUnit=jobs)" )
    protected synchronized void setEMF ( EntityManagerFactory emf ) {
        this.jobEmf = emf;
    }


    protected synchronized void unsetEMF ( EntityManagerFactory emf ) {
        if ( this.jobEmf == emf ) {
            this.jobEmf = null;
        }
    }


    @Reference
    protected synchronized void setTransactionService ( TransactionService ts ) {
        this.transactionService = ts;
    }


    protected synchronized void unsetTransactionService ( TransactionService ts ) {
        if ( this.transactionService == ts ) {
            this.transactionService = null;
        }
    }


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        this.msgClient = mc;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.msgClient == mc ) {
            this.msgClient = null;
        }
    }


    @Reference
    protected synchronized void setCoordinator ( JobCoordinator coord ) {
        this.coordinator = coord;

        if ( this.coordinator instanceof JobStateObservable ) {
            ( (JobStateObservable) this.coordinator ).registerStateListener(this);
        }
    }


    protected synchronized void unsetCoordinator ( JobCoordinator coord ) {
        if ( this.coordinator == coord ) {
            if ( this.coordinator instanceof JobStateObservable ) {
                ( (JobStateObservable) this.coordinator ).unregisterStateListener(this);
            }
            this.coordinator = null;
        }
    }


    protected JobOutputBufferImpl getBuffer ( UUID jobId ) {
        JobInfo jobInfo;
        try {
            jobInfo = this.coordinator.getJobInfo(jobId);
        }
        catch ( JobQueueException e ) {
            log.warn(FAILED_TO_GET_JOB_INFO, e);
            return new JobOutputBufferImpl(jobId);
        }

        if ( EnumSet.of(JobState.QUEUED, JobState.RUNNABLE, JobState.NEW).contains(jobInfo.getState()) ) {
            // cannot yet have produced output
            return new JobOutputBufferImpl(jobId);
        }
        if ( jobInfo.getState() == JobState.RUNNING ) {
            // cannot yet have been evicted
            return getOrCreateBuffer(jobId);
        }

        // could be evicted
        JobOutputBufferImpl buffer = this.buffers.get(jobId);

        if ( buffer != null ) {
            // not yet evicted
            if ( log.isDebugEnabled() ) {
                log.debug("Found non evicted buffer for " + jobId); //$NON-NLS-1$
            }
            return buffer;
        }

        EntityManager em = this.jobEmf.createEntityManager();
        return loadEvictedBuffer(em, jobId);
    }


    /**
     * @param jobId
     * @return
     */
    private static JobOutputBufferImpl loadEvictedBuffer ( EntityManager em, UUID jobId ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Loading evicted buffer for " + jobId); //$NON-NLS-1$
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<JobOutputSegment> cq = cb.createQuery(JobOutputSegment.class);
        EntityType<JobOutputSegment> entity = em.getMetamodel().entity(JobOutputSegment.class);
        Root<JobOutputSegment> from = cq.from(JobOutputSegment.class);

        cq.where(cb.equal(from.get(entity.getSingularAttribute("jobId", UUID.class)), jobId)); //$NON-NLS-1$
        cq.orderBy(
            cb.asc(from.get(entity.getSingularAttribute("combinedOffset"))), //$NON-NLS-1$
            cb.asc(from.get(entity.getSingularAttribute("offset")))); //$NON-NLS-1$
        List<JobOutputSegment> segments = em.createQuery(cq).getResultList();
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found %d segments for %s", segments.size(), jobId)); //$NON-NLS-1$
        }
        return new JobOutputBufferImpl(jobId, segments);
    }


    /**
     * @param jobId
     * @return
     */
    private JobOutputBufferImpl getOrCreateBuffer ( UUID jobId ) {
        synchronized ( this.buffers ) {
            JobOutputBufferImpl e = this.buffers.get(jobId);

            if ( e == null ) {
                e = new JobOutputBufferImpl(jobId);
                this.buffers.put(jobId, e);
            }

            return e;
        }
    }


    /**
     * @param jobId
     * @return the job output buffer
     */
    @Override
    public JobOutputBuffer getOutput ( UUID jobId ) {
        return this.getBuffer(jobId);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobUpdated(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public void jobUpdated ( JobStateUpdatedEvent ev ) {
        if ( EnumSet.of(JobState.FINISHED, JobState.FAILED, JobState.CANCELLED, JobState.TIMEOUT).contains(ev.getJobInfo().getState()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Job %s changed state to %s", ev.getJobId(), ev.getJobInfo().getState())); //$NON-NLS-1$
            }
            // evictJobOutput(ev.getJobId());
        }
    }


    /**
     * @param uuid
     */
    private synchronized void evictJobOutput ( UUID uuid ) {
        JobOutputBufferImpl buffer = this.buffers.get(uuid);

        if ( buffer != null ) {
            synchronized ( buffer ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Evicting output for job " + uuid); //$NON-NLS-1$
                }
                try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {
                    EntityManager em = this.jobEmf.createEntityManager();

                    JobOutputBufferImpl evictedSegments = loadEvictedBuffer(em, uuid);
                    Map<JobOutputLevel, Long> offsets = evictedSegments.getOffsets();

                    if ( log.isDebugEnabled() ) {
                        for ( Entry<JobOutputLevel, Long> e : offsets.entrySet() ) {
                            log.debug(String.format("Persistent offsets %s: %d", e.getKey(), e.getValue())); //$NON-NLS-1$
                        }
                    }

                    for ( JobOutputSegment seg : buffer.getSegments() ) {
                        JobOutputSegment persistSegment = new JobOutputSegment(
                            seg.getJobId(),
                            seg.getOffset() + offsets.get(seg.getLevel()),
                            seg.getCombinedOffset(),
                            seg.getLevel(),
                            seg.getContent());

                        persistSegment.setEof(buffer.isEof());

                        if ( log.isTraceEnabled() ) {
                            log.trace("Evicting buffer segment " + persistSegment); //$NON-NLS-1$
                        }

                        em.persist(persistSegment);
                    }
                    em.flush();
                    tx.commit();
                    this.buffers.remove(uuid);
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobKeepalive(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent)
     */
    @Override
    public void jobKeepalive ( Job job, JobKeepAliveEvent ev ) {
        // ignored
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.JobOutputTracker#handleOutputEvent(eu.agno3.orchestrator.jobs.msg.JobOutputEvent)
     */
    @Override
    public void handleOutputEvent ( JobOutputEvent ev ) {
        JobInfo jobInfo;
        try {
            jobInfo = this.coordinator.getJobInfo(ev.getJobId());
        }
        catch ( JobQueueException e ) {
            log.warn(FAILED_TO_GET_JOB_INFO, e);
            return;
        }

        JobOutputBufferImpl buffer = getOrCreateBuffer(ev.getJobId());
        synchronized ( buffer ) {
            if ( ev.isEof() ) {
                log.debug("Have  EOF event"); //$NON-NLS-1$
                // make sure there is one segment to safe the EOF state
                buffer.append(StringUtils.EMPTY, JobOutputLevel.INFO);
                buffer.markEof();
            }
            else {
                int length = ev.getText().length();
                long currentOffset = buffer.getLevelOffset(ev.getOutputLevel());
                long expectPosition = currentOffset + length;
                if ( expectPosition != ev.getOutputPosition() ) {
                    log.debug(String.format(
                        "Expect output position %d got %d (old %d, len %d, level %s), might be already evicted", //$NON-NLS-1$
                        expectPosition,
                        ev.getOutputPosition(),
                        currentOffset,
                        length,
                        ev.getOutputLevel()));
                }

                buffer.append(ev.getText(), ev.getOutputLevel());
            }

            try {
                log.trace("Notifying GUI of progress"); //$NON-NLS-1$
                notifyGuiOutput(ev);
            }
            catch (
                MessagingException |
                InterruptedException e ) {
                log.debug("Failed to notify GUIs of progress", e); //$NON-NLS-1$
            }

            if ( EnumSet.of(JobState.FINISHED, JobState.FAILED, JobState.CANCELLED, JobState.TIMEOUT).contains(jobInfo.getState()) && ev.isEof() ) {
                evictJobOutput(ev.getJobId());
            }
        }
    }


    /**
     * @param ev
     * @throws MessagingException
     * @throws InterruptedException
     */
    private void notifyGuiOutput ( JobOutputEvent ev ) throws MessagingException, InterruptedException {
        String path = "/jobs/output/" + ev.getJobId(); //$NON-NLS-1$
        this.msgClient.publishEvent(
            new GuiNotificationEvent(
                this.msgClient.getMessageSource(),
                path,
                ev.getOutputLevel() != null ? ev.getOutputLevel().name() : StringUtils.EMPTY));
    }

}
