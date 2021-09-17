/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.state;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.state.LocalJobStateListener;


/**
 * @author mbechler
 * 
 */
public class JobStateTrackerImpl extends AbstractJobStateTracker implements JobStateTracker {

    Map<Job, JobInfoImpl> jobStates = new ConcurrentHashMap<>();
    private Map<UUID, Job> jobIndex = new ConcurrentHashMap<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.AbstractJobStateTracker#bindListener(eu.agno3.orchestrator.jobs.state.LocalJobStateListener)
     */
    @Override
    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindListener ( LocalJobStateListener listener ) {
        super.bindListener(listener);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.AbstractJobStateTracker#unbindListener(eu.agno3.orchestrator.jobs.state.LocalJobStateListener)
     */
    @Override
    protected synchronized void unbindListener ( LocalJobStateListener listener ) {
        super.unbindListener(listener);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.AbstractJobStateTracker#getJobStateOrNull(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    protected JobInfoImpl getJobStateOrNull ( Job j ) {
        return this.jobStates.get(j);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getLoadableJobs()
     */
    @Override
    public Collection<Job> getLoadableJobs () {
        return Collections.EMPTY_LIST;
    }


    /**
     * 
     * @param jobId
     * @return the job data
     */
    @Override
    public Job getJobData ( UUID jobId ) {
        return this.jobIndex.get(jobId);
    }


    /**
     * @param j
     * @param s
     * @return
     */
    @Override
    protected JobInfoImpl addNewJob ( Job j, JobState s ) {
        JobInfoImpl info = makeNewJobInfo(j.getJobId(), j.getClass().getName(), j.getOwner(), s);
        this.jobIndex.put(j.getJobId(), j);
        trackJobState(j, info);
        return info;
    }


    /**
     * @param job
     * @param newInfo
     */
    @Override
    protected void trackJobState ( Job job, JobInfoImpl newInfo ) {
        this.jobStates.put(job, newInfo);
    }


    /**
     * @return
     */
    @Override
    protected Set<Job> listJobs () {
        return this.jobStates.keySet();
    }


    /**
     * @param cleared
     * @param e
     */
    @Override
    protected boolean removeJob ( Job j ) {
        this.jobIndex.remove(j.getJobId());
        return this.jobStates.remove(j) != null;
    }

}
