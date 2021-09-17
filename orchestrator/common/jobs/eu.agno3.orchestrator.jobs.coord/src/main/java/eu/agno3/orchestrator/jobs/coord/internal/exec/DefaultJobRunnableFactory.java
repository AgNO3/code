/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.exec;


import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.coord.JobRunnableFactoryInternal;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobResumptionHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.jobs.exec.ResumableJobRunnableFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = JobRunnableFactoryInternal.class )
@SuppressWarnings ( "rawtypes" )
public class DefaultJobRunnableFactory implements JobRunnableFactoryInternal {

    private static final Logger log = Logger.getLogger(DefaultJobRunnableFactory.class);

    /**
     * 
     */
    private static final String JOB_TYPE_PROPERTY = "jobType"; //$NON-NLS-1$

    private Map<Class<? extends Job>, JobRunnableFactory<Job>> jobBuilders = new WeakHashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindRunnableFactory ( JobRunnableFactory<Job> jrf, Map properties ) {
        String jobType = (String) properties.get(JOB_TYPE_PROPERTY);
        if ( StringUtils.isBlank(jobType) ) {
            return;
        }

        Class<? extends Job> jobClass = getJobClass(jrf);
        if ( jobClass == null ) {
            return;
        }

        this.jobBuilders.put(jobClass, jrf);
    }


    protected synchronized void unbindRunnableFactory ( JobRunnableFactory<Job> jrf, Map properties ) {
        Class<? extends Job> jobClass = getJobClass(jrf);
        if ( jobClass == null ) {
            return;
        }
        this.jobBuilders.remove(jobClass, jrf);
    }


    @Activate
    protected void activate ( ComponentContext ctx ) {}


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        this.jobBuilders.clear();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobRunnableFactoryInternal#getRunnableForJob(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobState)
     */
    @Override
    public <T extends Job> JobRunnable getRunnableForJob ( T j, JobState state ) throws JobRunnableException {
        return getRunnableForJob(j, state, null);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobRunnableFactoryInternal#hasRunnable(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public boolean hasRunnable ( Job j ) {
        return this.jobBuilders.get(j.getClass()) != null;
    }


    @Override
    public <T extends Job> JobRunnable getRunnableForJob ( T j, JobState s, JobResumptionHandler h ) throws JobRunnableException {

        @SuppressWarnings ( "unchecked" )
        JobRunnableFactory<T> f = (JobRunnableFactory<T>) this.jobBuilders.get(j.getClass());

        if ( f == null ) {
            log.warn("No runnable factory for job class " + j.getClass()); //$NON-NLS-1$
            return null;
        }

        if ( s == JobState.SUSPENDED ) {
            if ( h == null || ! ( f instanceof ResumableJobRunnableFactory ) ) {
                log.error("Tried to resume non-resumable job"); //$NON-NLS-1$
                return null;
            }
            try {
                return ( (ResumableJobRunnableFactory<T>) f ).getJobResumption(j, h);
            }
            catch ( Exception e ) {
                throw new JobRunnableException("Failed to create job resumption", e); //$NON-NLS-1$
            }
        }

        try {
            if ( f instanceof ResumableJobRunnableFactory ) {
                return ( (ResumableJobRunnableFactory<T>) f ).getRunnableForJob(j, h);
            }
            return f.getRunnableForJob(j);
        }
        catch ( Exception e ) {
            throw new JobRunnableException("Failed to create job runnable", e); //$NON-NLS-1$
        }
    }


    private static Class<? extends Job> getJobClass ( JobRunnableFactory<Job> f ) {
        JobType t = f.getClass().getAnnotation(JobType.class);
        if ( t == null ) {
            log.warn("No job type annotation on factory " + f.getClass().getName()); //$NON-NLS-1$
            return null;
        }

        Class<? extends Job> jobClass = t.value();

        if ( jobClass == null ) {
            log.warn("Failed to determine job class for factory " + f.getClass().getName()); //$NON-NLS-1$
            return null;
        }
        return jobClass;
    }

}
