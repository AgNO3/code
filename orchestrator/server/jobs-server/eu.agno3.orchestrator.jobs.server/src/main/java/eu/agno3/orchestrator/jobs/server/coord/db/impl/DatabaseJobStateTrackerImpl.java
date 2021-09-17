/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.db.impl;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.JobProgressInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.coord.JobStateMachine;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.coord.db.PersistentJobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobIllegalStateException;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.msg.JobProgressEvent;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.orchestrator.jobs.server.JobProgressTracker;
import eu.agno3.orchestrator.jobs.state.JobStateListener;
import eu.agno3.orchestrator.jobs.state.LocalJobStateListener;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.transaction.TransactionService;
import eu.agno3.runtime.xml.XMLParserConfigurationException;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 *
 */
@Component ( service = DatabaseJobStateTrackerImpl.class )
public class DatabaseJobStateTrackerImpl implements JobStateTracker, JobProgressTracker {

    /**
     * 
     */
    private static final String JOB_GROUP = "jobGroup"; //$NON-NLS-1$
    private static final String STATE = "state"; //$NON-NLS-1$
    private static final String QUEUED_TIME = "queuedTime"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DatabaseJobStateTrackerImpl.class);

    private static final int JOB_CACHE_SIZE = 25;

    private static final long REMOVE_UNRESTORABLE_TIME = 60000;

    private JobStateMachine jobSM = new JobStateMachine();
    private JobStateMachine noEventJobSM = new JobStateMachine();

    private EntityManagerFactory jobEmf;
    private XmlMarshallingService xmlMarshaller;
    private XmlParserFactory xmlParserFactory;

    private Map<UUID, SoftReference<Job>> jobCache = new LRUMap<>(JOB_CACHE_SIZE);
    private Map<UUID, PersistentJobInfo> jobInfoCache = new LRUMap<>(JOB_CACHE_SIZE);

    private TransactionService transactionService;


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindListener ( LocalJobStateListener listener ) {
        this.jobSM.addListener(listener);
    }


    protected synchronized void unbindListener ( LocalJobStateListener listener ) {
        this.jobSM.removeListener(listener);
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
    protected synchronized void setXmlMarshallingService ( XmlMarshallingService xms ) {
        this.xmlMarshaller = xms;
    }


    protected synchronized void unsetXmlMarshallingService ( XmlMarshallingService xms ) {
        if ( this.xmlMarshaller == xms ) {
            this.xmlMarshaller = null;
        }
    }


    @Reference
    protected synchronized void setXmlParserFactory ( XmlParserFactory xpf ) {
        this.xmlParserFactory = xpf;
    }


    protected synchronized void unsetXmlParserFactory ( XmlParserFactory xpf ) {
        if ( this.xmlParserFactory == xpf ) {
            this.xmlParserFactory = null;
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


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getJobState(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobInfo getJobState ( Job j ) throws JobUnknownException {
        return this.getJobState(j.getJobId());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getJobState(java.util.UUID)
     */
    @Override
    public JobInfo getJobState ( UUID jobId ) throws JobUnknownException {
        return getJobState(jobId, getEM());
    }


    /**
     * @param jobId
     * @param em
     * @return
     * @throws JobUnknownException
     */
    protected PersistentJobInfo getJobState ( UUID jobId, EntityManager em ) throws JobUnknownException {

        PersistentJobInfo cached = this.jobInfoCache.get(jobId);
        if ( cached != null ) {
            return cached;
        }

        PersistentJobInfo jobInfo = em.find(PersistentJobInfo.class, jobId);
        if ( jobInfo == null ) {
            throw new JobUnknownException("Job not found in database " + jobId); //$NON-NLS-1$
        }

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Got job state for %s: %s", jobId, jobInfo.getState())); //$NON-NLS-1$
        }
        this.jobInfoCache.put(jobId, jobInfo);
        return jobInfo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getJobStates(java.util.Collection)
     */
    @Override
    public Collection<JobInfo> getJobStates ( Collection<Job> jobs ) throws JobUnknownException {
        EntityManager em = getEM();
        List<UUID> jobIds = new LinkedList<>();
        for ( Job j : jobs ) {
            jobIds.add(j.getJobId());
        }

        List<PersistentJobInfo> jobStates = getJobStates(em, jobIds);
        return mapPersistentJobInfo(jobStates);
    }


    /**
     * @param jobStates
     * @return
     */
    private static Collection<JobInfo> mapPersistentJobInfo ( Collection<PersistentJobInfo> jobStates ) {
        List<JobInfo> res = new ArrayList<>();
        for ( PersistentJobInfo j : jobStates ) {
            res.add(j);
        }
        return res;
    }


    /**
     * @param jobIds
     * @return
     */
    protected List<PersistentJobInfo> getJobStates ( EntityManager em, List<UUID> jobIds ) {
        EntityType<PersistentJobInfo> entity = em.getMetamodel().entity(PersistentJobInfo.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PersistentJobInfo> cq = cb.createQuery(PersistentJobInfo.class);
        Root<PersistentJobInfo> from = cq.from(PersistentJobInfo.class);
        cq.where(from.get(entity.getSingularAttribute("jobId", UUID.class)).in(jobIds)); //$NON-NLS-1$
        List<PersistentJobInfo> resultList = em.createQuery(cq).getResultList();
        for ( PersistentJobInfo pji : resultList ) {
            this.jobInfoCache.put(pji.getJobId(), pji);
        }
        return resultList;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getAllJobInfo(eu.agno3.orchestrator.jobs.JobGroup)
     */
    @Override
    public Collection<JobInfo> getAllJobInfo ( JobGroup g ) {
        return mapPersistentJobInfo(getAllJobInfo(getEM(), g));
    }


    /**
     * @param createEntityManager
     * @param g
     * @return
     */
    protected Collection<PersistentJobInfo> getAllJobInfo ( EntityManager em, JobGroup g ) {
        EntityType<PersistentJobInfo> entity = em.getMetamodel().entity(PersistentJobInfo.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PersistentJobInfo> cq = cb.createQuery(PersistentJobInfo.class);
        Root<PersistentJobInfo> from = cq.from(PersistentJobInfo.class);
        cq.where(cb.equal(from.get(entity.getSingularAttribute(JOB_GROUP, String.class)), g.getId()));
        cq.orderBy(Arrays.asList(cb.desc(from.get(QUEUED_TIME)), cb.asc(from.get("progress")))); //$NON-NLS-1$
        TypedQuery<PersistentJobInfo> query = em.createQuery(cq);
        return query.getResultList();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#updateJobState(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobState)
     */
    @Override
    public JobInfo updateJobState ( Job j, JobState s ) throws JobQueueException {
        return updateJobState(j, s, false);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#updateJobState(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobState)
     */
    @Override
    public JobInfo updateJobStateExternal ( Job j, JobState s ) throws JobQueueException {
        return updateJobState(j, s, true);
    }


    /**
     * @param j
     * @param s
     * @param suppressEvent
     * @return
     * @throws JobQueueException
     * @throws JobUnknownException
     * @throws JobIllegalStateException
     */
    private JobInfo updateJobState ( Job j, JobState s, boolean suppressEvent )
            throws JobQueueException, JobUnknownException, JobIllegalStateException {
        try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {
            EntityManager em = getEM();

            if ( log.isDebugEnabled() ) {
                log.debug("Updating to job state " + s); //$NON-NLS-1$
            }
            PersistentJobInfo info;
            if ( s == JobState.NEW ) {
                info = makeNewJobInfo(j, s);
                synchronized ( this.jobCache ) {
                    this.jobCache.put(j.getJobId(), new SoftReference<>(j));
                }
            }
            else {
                info = getJobState(j.getJobId(), em);
                if ( info.getState() == s ) {
                    if ( log.isTraceEnabled() ) {
                        log.trace("State unchanged " + s); //$NON-NLS-1$
                    }
                    tx.commit();
                    return info;
                }
            }

            if ( info == null ) {
                throw new JobQueueException("Failed to restore job"); //$NON-NLS-1$
            }

            JobInfo ji = this.updateStateInternal(em, s, !suppressEvent ? this.jobSM : this.noEventJobSM, info);
            tx.commit();
            return ji;
        }
    }


    /**
     * @param em
     * @param s
     * @param jobSM2
     * @param j
     * @param info
     * @return
     * @throws JobIllegalStateException
     */
    protected JobInfo updateStateInternal ( EntityManager em, JobState s, JobStateMachine sm, PersistentJobInfo info )
            throws JobIllegalStateException {
        PersistentJobInfo pinfo = em.find(PersistentJobInfo.class, info.getJobId());
        if ( pinfo == null ) {
            pinfo = info;
        }

        JobInfo newInfo = this.noEventJobSM.applyState(info, s);

        pinfo.setType(newInfo.getType());
        pinfo.setState(newInfo.getState());
        pinfo.setLastKeepAliveTime(DateTime.now());
        pinfo.setQueuedTime(newInfo.getQueuedTime());
        pinfo.setStartedTime(newInfo.getStartedTime());
        pinfo.setFinishedTime(newInfo.getFinishedTime());

        if ( s == JobState.FINISHED || s == JobState.FAILED ) {
            log.debug("Setting progress to 100%"); //$NON-NLS-1$
            pinfo.setProgress(100);
        }
        em.persist(pinfo);
        this.jobInfoCache.put(info.getJobId(), pinfo);
        log.debug("Flush"); //$NON-NLS-1$
        em.flush();
        sm.applyState(info, s);
        return info;
    }


    /**
     * @param userPrincipal
     * @param j
     * @param s
     * @return
     * @throws JobQueueException
     */
    private PersistentJobInfo makeNewJobInfo ( Job j, JobState s ) throws JobQueueException {
        if ( log.isTraceEnabled() ) {
            log.trace("Creating new job info for " + j.getJobId()); //$NON-NLS-1$
        }
        PersistentJobInfo info = new PersistentJobInfo();
        info.setJobId(j.getJobId());
        info.setType(j.getClass().getName());
        info.setJobGroup(j.getJobGroup().getId());
        info.setOwner(j.getOwner());
        info.setQueuedTime(DateTime.now());
        info.setState(s);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(bos);
            this.xmlMarshaller.marshall(j, writer);
            info.setSerializedJob(bos.toByteArray());
        }
        catch (
            XMLStreamException |
            FactoryConfigurationError |
            XMLBindingException e ) {
            throw new JobQueueException("Failed to save job", e); //$NON-NLS-1$
        }
        this.jobInfoCache.put(info.getJobId(), info);
        return info;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#handleEvent(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public void handleEvent ( JobStateUpdatedEvent ev ) {

        try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {

            EntityManager em = getEM();
            PersistentJobInfo info;
            try {
                info = this.getJobState(ev.getJobId(), em);
            }
            catch ( JobUnknownException e ) {
                log.warn("Job event for unknown job " + ev.getJobId(), e); //$NON-NLS-1$
                tx.commit();
                return;
            }

            if ( info == ev.getJobInfo() || info.getState().equals(ev.getJobInfo().getState()) ) {
                // this already is the persistent version (or already current), some outer call is updating
                // and doing again so again would cause a deadlock
                return;
            }

            try {
                updateStateInternal(em, ev.getJobInfo().getState(), this.noEventJobSM, info);
                tx.commit();
            }
            catch ( JobIllegalStateException e ) {
                log.debug("Failed to update job state from event:", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.server.JobProgressTracker#handleEvent(eu.agno3.orchestrator.jobs.msg.JobProgressEvent)
     */
    @Override
    public void handleEvent ( JobProgressEvent ev ) {
        try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {
            EntityManager em = getEM();
            PersistentJobInfo info;
            try {
                info = this.getJobState(ev.getJobId(), em);
            }
            catch ( JobUnknownException e ) {
                log.warn("Job progress event for unknown job " + ev.getJobId(), e); //$NON-NLS-1$
                tx.commit();
                return;
            }

            PersistentJobInfo pinfo = em.find(PersistentJobInfo.class, info.getJobId());
            if ( pinfo == null ) {
                pinfo = info;
            }

            if ( (int) ev.getProgressInfo().getProgress() > info.getProgress() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Setting progress to " + ev.getProgressInfo().getProgress()); //$NON-NLS-1$
                }
                info.setProgress((int) ev.getProgressInfo().getProgress());
                pinfo.setProgress(info.getProgress());
            }
            DateTime now = DateTime.now();
            info.setLastKeepAliveTime(now);
            pinfo.setLastKeepAliveTime(now);
            em.persist(pinfo);
            tx.commit();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.server.JobProgressTracker#getProgressInfo(java.util.UUID)
     */
    @Override
    public JobProgressInfo getProgressInfo ( UUID jobId ) {

        JobProgressInfoImpl progressInfo = new JobProgressInfoImpl();
        try {
            PersistentJobInfo info = this.getJobState(jobId, this.getEM());
            progressInfo.setLastUpdate(info.getLastKeepAliveTime());
            progressInfo.setProgress(info.getProgress());
            progressInfo.setState(info.getState());
            return progressInfo;
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to get job progess for " + jobId, e); //$NON-NLS-1$
            return progressInfo;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#doKeepAlive(java.util.UUID)
     */
    @Override
    public void doKeepAlive ( UUID jobId ) {
        try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {
            EntityManager em = getEM();
            PersistentJobInfo info = this.getJobState(jobId, em);
            if ( info != null ) {
                PersistentJobInfo pinfo = em.find(PersistentJobInfo.class, info.getJobId());
                if ( pinfo == null ) {
                    pinfo = info;
                }

                DateTime now = DateTime.now();
                pinfo.setLastKeepAliveTime(now);
                info.setLastKeepAliveTime(now);
                em.persist(pinfo);
            }
            tx.commit();
        }
        catch ( JobUnknownException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Tried to keep alive non-existant job " + jobId, e); //$NON-NLS-1$
            }
        }

    }


    private EntityManager getEM () {
        return this.jobEmf.createEntityManager();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#clearFinishedJobs()
     */
    @Override
    public int clearFinishedJobs () {
        try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {
            EntityManager em = getEM();
            EntityType<PersistentJobInfo> entity = em.getMetamodel().entity(PersistentJobInfo.class);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaDelete<PersistentJobInfo> cq = cb.createCriteriaDelete(PersistentJobInfo.class);
            Root<PersistentJobInfo> from = cq.from(PersistentJobInfo.class);
            cq.where(
                from.get(entity.getSingularAttribute(STATE, JobState.class)).in(EnumSet.of(JobState.CANCELLED, JobState.FINISHED, JobState.FAILED)));
            int numRows = em.createQuery(cq).executeUpdate();
            tx.commit();
            return numRows;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getJobData(java.util.UUID)
     */
    @Override
    public Job getJobData ( UUID jobId ) {

        synchronized ( this.jobCache ) {
            SoftReference<Job> j = this.jobCache.get(jobId);
            if ( j != null ) {
                Job job = j.get();
                if ( job != null ) {
                    return job;
                }

                this.jobCache.remove(jobId);
            }
        }

        try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {
            tx.commit();
            EntityManager em = getEM();
            PersistentJobInfo jobState = this.getJobState(jobId, em);
            Job job;
            try {
                job = unserializeJob(jobState);
            }
            catch ( Exception e ) {
                removeUnrestorable(em, jobState, e);
                return null;
            }
            return job;
        }
        catch ( JobUnknownException e ) {
            log.warn("Failed to get job", e); //$NON-NLS-1$
            return null;
        }

    }


    /**
     * @param jobState
     * @return
     * @throws XMLParserConfigurationException
     * @throws XMLBindingException
     */
    private Job unserializeJob ( PersistentJobInfo jobState ) throws XMLParserConfigurationException, XMLBindingException {
        Job j;
        XMLStreamReader reader = this.xmlParserFactory.createStreamReader(new ByteArrayInputStream(jobState.getSerializedJob()));
        j = this.xmlMarshaller.unmarshall(Job.class, reader);
        synchronized ( this.jobCache ) {
            this.jobCache.put(j.getJobId(), new SoftReference<>(j));
        }
        return j;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getLoadableJobs()
     */
    @Override
    public Collection<Job> getLoadableJobs () {
        try ( TransactionContext tx = this.transactionService.ensureTransacted() ) {
            tx.commit();
            EntityManager em = getEM();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<PersistentJobInfo> cq = cb.createQuery(PersistentJobInfo.class);
            Root<PersistentJobInfo> from = cq.from(PersistentJobInfo.class);
            EntityType<PersistentJobInfo> entity = em.getMetamodel().entity(PersistentJobInfo.class);

            cq.where(
                from.get(entity.getSingularAttribute(STATE, JobState.class))
                        .in(EnumSet.of(JobState.QUEUED, JobState.RUNNABLE, JobState.STALLED, JobState.RUNNING, JobState.SUSPENDED)));

            cq.orderBy(cb.asc(from.get(entity.getSingularAttribute(QUEUED_TIME, DateTime.class))));

            List<Job> jobs = new LinkedList<>();

            for ( PersistentJobInfo info : em.createQuery(cq).getResultList() ) {
                try {
                    jobs.add(unserializeJob(info));
                }
                catch ( Exception e ) {
                    if ( info.getLastKeepAliveTime().plus(REMOVE_UNRESTORABLE_TIME).isBeforeNow() ) {
                        removeUnrestorable(em, info, e);
                    }
                }
            }

            return jobs;
        }

    }


    private static void removeUnrestorable ( EntityManager em, PersistentJobInfo info, Exception ex ) {
        log.warn(String.format("Failed to restore job %s in group %s, removing:", info.getJobId(), info.getJobGroup()), ex); //$NON-NLS-1$
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Serialized data: %s", //$NON-NLS-1$
                new String(info.getSerializedJob(), Charset.forName("UTF-8")))); //$NON-NLS-1$
        }
        try {
            em.remove(info);
            em.flush();
        }
        catch ( PersistenceException e ) {
            log.warn("Failed to remove unrestorable job", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#addListener(eu.agno3.orchestrator.jobs.state.JobStateListener)
     */
    @Override
    public void addListener ( JobStateListener l ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Adding listener " + l.getClass().getName()); //$NON-NLS-1$
        }
        this.jobSM.addListener(l);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#removeListener(eu.agno3.orchestrator.jobs.state.JobStateListener)
     */
    @Override
    public void removeListener ( JobStateListener l ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Removing listener " + l.getClass().getName()); //$NON-NLS-1$
        }
        this.jobSM.removeListener(l);
    }

}
