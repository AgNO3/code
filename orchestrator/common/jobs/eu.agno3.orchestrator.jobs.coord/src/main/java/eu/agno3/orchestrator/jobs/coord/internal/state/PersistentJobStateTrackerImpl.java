/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.state;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.coord.PersistentJobStateTracker;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exec.JobResumptionHandler;
import eu.agno3.orchestrator.jobs.state.LocalJobStateListener;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.serialization.FilteredObjectInputStream;
import eu.agno3.runtime.xml.XMLParserConfigurationException;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 *
 */
@Component ( service = PersistentJobStateTracker.class, configurationPid = "jobstate.file", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class PersistentJobStateTrackerImpl extends AbstractJobStateTracker implements PersistentJobStateTracker, JobResumptionHandler {

    /**
     * 
     */
    private static final FileAttribute<Set<PosixFilePermission>> DIR_PERMS = PosixFilePermissions
            .asFileAttribute(PosixFilePermissions.fromString("rwx------")); //$NON-NLS-1$

    private static final int CACHE_SIZE = 10;

    private Map<Job, JobInfoImpl> jobStates = new ConcurrentHashMap<>(new LRUMap<>(CACHE_SIZE));
    private Map<UUID, Job> jobIndex = new ConcurrentHashMap<>(new LRUMap<>(CACHE_SIZE));

    private static final String JOB_XML = "job.xml"; //$NON-NLS-1$
    private static final String STATE = "state"; //$NON-NLS-1$
    private static final String SUSPEND = "suspend"; //$NON-NLS-1$

    private XmlMarshallingService xmlMarshaller;
    private XmlParserFactory xmlParserFactory;

    private Path basePath;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        String basePathString = ConfigUtil.parseString(ctx.getProperties(), "basePath", null); //$NON-NLS-1$
        if ( StringUtils.isBlank(basePathString) ) {
            log.error("No base path configued"); //$NON-NLS-1$
            return;
        }

        this.basePath = Paths.get(basePathString);
        if ( !Files.exists(this.basePath, LinkOption.NOFOLLOW_LINKS) ) {
            try {
                this.basePath = Files.createDirectories(this.basePath, DIR_PERMS);
            }
            catch ( IOException e ) {
                log.error("Failed to create jobs directory", e); //$NON-NLS-1$
                this.basePath = null;
                return;
            }
        }
    }


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


    protected Path getJobPath ( UUID jobId ) throws IOException {
        return getJobDir(jobId, true).resolve(JOB_XML);
    }


    protected Path getSuspendPath ( UUID jobId ) throws IOException {
        return getJobDir(jobId, true).resolve(SUSPEND);
    }


    protected Path getJobInfoPath ( UUID jobId ) throws IOException {
        return getJobDir(jobId, true).resolve(STATE);
    }


    /**
     * @param jobId
     * @return
     * @throws IOException
     */
    private Path getJobDir ( UUID jobId, boolean create ) throws IOException {
        if ( this.basePath == null ) {
            throw new IOException("No storage path configured"); //$NON-NLS-1$
        }
        Path jobDir = this.basePath.resolve(jobId.toString());
        if ( create && !Files.exists(jobDir, LinkOption.NOFOLLOW_LINKS) ) {
            return Files.createDirectory(jobDir, DIR_PERMS);
        }
        return jobDir;

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.JobStateTrackerImpl#getJobData(java.util.UUID)
     */
    @Override
    public Job getJobData ( UUID jobId ) {
        Job job = this.jobIndex.get(jobId);
        if ( job != null ) {
            return job;
        }

        try {
            if ( !Files.exists(getJobDir(jobId, false)) ) {
                return null;
            }
            return loadJobById(jobId);
        }
        catch ( IOException e ) {
            log.warn("Failed to get job data", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param jobId
     * @return
     * @throws IOException
     */
    private synchronized Job loadJobById ( UUID jobId ) throws IOException {
        Job j = null;
        Path jobPath = getJobPath(jobId);
        if ( !Files.exists(jobPath) ) {
            return null;
        }
        try ( FileChannel ch = FileChannel.open(jobPath, StandardOpenOption.READ);
              InputStream is = Channels.newInputStream(ch) ) {
            XMLStreamReader sr = this.xmlParserFactory.createStreamReader(is);
            j = this.xmlMarshaller.unmarshall(Job.class, sr);
        }
        catch (
            XMLParserConfigurationException |
            XMLBindingException e ) {
            throw new IOException("Failed to read job data", e); //$NON-NLS-1$
        }

        this.jobIndex.put(jobId, j);
        return j;
    }


    private synchronized void writeJob ( Job j ) throws IOException {

        Path jobPath = getJobPath(j.getJobId());
        if ( !Files.exists(jobPath) ) {
            try ( FileChannel ch = FileChannel.open(jobPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
                  OutputStream os = Channels.newOutputStream(ch) ) {
                this.xmlMarshaller.marshall(j, XMLOutputFactory.newInstance().createXMLStreamWriter(os));
            }
            catch (
                XMLBindingException |
                XMLStreamException |
                FactoryConfigurationError e ) {
                Files.deleteIfExists(jobPath);
                throw new IOException("Failed to write job data", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.AbstractJobStateTracker#getJobStateOrNull(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    protected JobInfoImpl getJobStateOrNull ( Job j ) {
        JobInfoImpl cached = this.jobStates.get(j);

        if ( cached != null ) {
            return cached;
        }

        try {
            return loadJobState(j);
        }
        catch ( IOException e ) {
            log.warn("Failed to restore job state", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param j
     * @return
     * @throws IOException
     */
    private synchronized JobInfoImpl loadJobState ( Job j ) throws IOException {
        JobInfoImpl ji = null;
        Path jobInfoPath = getJobInfoPath(j.getJobId());
        if ( !Files.exists(jobInfoPath) ) {
            return null;
        }
        try ( FileChannel ch = FileChannel.open(jobInfoPath, StandardOpenOption.READ);
              InputStream is = Channels.newInputStream(ch);
              FilteredObjectInputStream ois = new FilteredObjectInputStream(is, this.getClass().getClassLoader()) ) {
            ji = (JobInfoImpl) ois.readObject();
        }
        catch ( ClassNotFoundException e ) {
            throw new IOException("Failed to read job info", e); //$NON-NLS-1$
        }

        this.jobStates.put(j, ji);
        return ji;
    }


    private synchronized void writeJobState ( JobInfo ji ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Writing job state " + ji); //$NON-NLS-1$
        }

        // prevent being interrupted
        try {
            doWrite(ji);
        }
        catch ( ClosedByInterruptException e ) {
            Thread.interrupted();
            doWrite(ji);
            Thread.currentThread().interrupt();
        }
    }


    /**
     * @param ji
     * @throws IOException
     */
    private void doWrite ( JobInfo ji ) throws IOException {
        try ( FileChannel ch = FileChannel
                .open(getJobInfoPath(ji.getJobId()), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
              OutputStream os = Channels.newOutputStream(ch);
              ObjectOutputStream oos = new ObjectOutputStream(os) ) {
            oos.writeObject(ji);
            ch.force(true);
        }
        catch ( IOException e ) {
            log.warn("Failed to write job state", e); //$NON-NLS-1$
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.JobStateTrackerImpl#trackJobState(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobInfoImpl)
     */
    @Override
    protected void trackJobState ( Job job, JobInfoImpl newInfo ) {
        try {
            writeJobState(newInfo);
            this.jobStates.put(job, newInfo);
        }
        catch ( IOException e ) {
            log.warn("Failed to write job state", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.JobStateTrackerImpl#listJobs()
     */
    @Override
    protected Set<Job> listJobs () {
        Set<Job> jobs = new HashSet<>();

        if ( this.basePath == null ) {
            log.error("No base path configured"); //$NON-NLS-1$
            return jobs;
        }

        try {
            Iterator<Path> iterator = Files.list(this.basePath).iterator();
            while ( iterator.hasNext() ) {
                Path jobPath = iterator.next();

                if ( !Files.isDirectory(jobPath, LinkOption.NOFOLLOW_LINKS) ) {
                    continue;
                }

                if ( !Files.exists(jobPath.resolve(JOB_XML)) || !Files.exists(jobPath.resolve(STATE)) ) {
                    removeJobPath(jobPath);
                    continue;
                }

                try {
                    UUID jobId = UUID.fromString(jobPath.getFileName().toString());
                    Job j = loadJobById(jobId);
                    if ( j != null ) {
                        jobs.add(j);
                    }
                }
                catch (
                    IOException |
                    IllegalArgumentException e ) {
                    log.warn("Failed to restore job", e); //$NON-NLS-1$
                    continue;
                }
            }

            return jobs;
        }
        catch ( IOException e ) {
            log.warn("Failed to enumerate jobs", e); //$NON-NLS-1$
            return Collections.EMPTY_SET;
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.JobStateTrackerImpl#getLoadableJobs()
     */
    @Override
    public Collection<Job> getLoadableJobs () {
        Set<Job> jobs = new HashSet<>();

        for ( Job j : listJobs() ) {
            try {
                JobInfoImpl jobState = getJobState(j);
                if ( log.isDebugEnabled() ) {
                    log.debug("Current state is " + jobState.getState()); //$NON-NLS-1$
                }

                if ( EnumSet.of(JobState.FINISHED, JobState.FAILED, JobState.CANCELLED, JobState.TIMEOUT).contains(jobState.getState()) ) {
                    continue;
                }

                if ( EnumSet.of(JobState.QUEUED, JobState.RUNNABLE).contains(jobState.getState()) ) {
                    updateJobState(j, JobState.NEW);
                }
                else if ( EnumSet.of(JobState.RUNNING, JobState.RESUMED).contains(jobState.getState()) ) {
                    // this should be failed, but there is some trouble writing the state reliably
                    log.warn(String.format("Setting job in state %s to suspended %s", jobState.getState(), j)); //$NON-NLS-1$
                    updateJobState(j, JobState.SUSPENDED);
                }

                jobs.add(j);
            }
            catch ( JobQueueException e ) {
                log.warn("Failed to restore job", e); //$NON-NLS-1$
            }
        }

        return jobs;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @return
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.JobStateTrackerImpl#removeJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    protected boolean removeJob ( Job j ) {
        try {
            log.info("Cleaning up job " + j.getJobId()); //$NON-NLS-1$

            Path jobPath = getJobDir(j.getJobId(), false);
            if ( !Files.exists(jobPath, LinkOption.NOFOLLOW_LINKS) ) {
                return false;
            }
            removeJobPath(jobPath);
            return true;

        }
        catch ( IOException e ) {
            log.warn("Failed to remove job", e); //$NON-NLS-1$
            return false;
        }

    }


    /**
     * @param jobPath
     * @throws IOException
     */
    private static void removeJobPath ( Path jobPath ) throws IOException {
        Files.deleteIfExists(jobPath.resolve(JOB_XML));
        Files.deleteIfExists(jobPath.resolve(STATE));
        Files.deleteIfExists(jobPath.resolve(SUSPEND));
        Files.delete(jobPath);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.state.AbstractJobStateTracker#addNewJob(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobState)
     */
    @Override
    protected JobInfoImpl addNewJob ( Job j, JobState s ) throws JobQueueException {
        JobInfoImpl ji = makeNewJobInfo(j.getJobId(), j.getClass().getName(), j.getOwner(), s);
        try {
            writeJob(j);
            writeJobState(ji);
        }
        catch ( IOException e ) {
            throw new JobQueueException("Failed to queue job", e); //$NON-NLS-1$
        }

        this.jobIndex.put(j.getJobId(), j);
        this.jobStates.put(j, ji);
        return ji;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobResumptionHandler#getSuspendData(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public <T extends Job> InputStream getSuspendData ( T job ) throws JobQueueException {
        try {
            Path suspendPath = getSuspendPath(job.getJobId());
            if ( !Files.exists(suspendPath, LinkOption.NOFOLLOW_LINKS) ) {
                return null;
            }

            return Channels.newInputStream(FileChannel.open(suspendPath, LinkOption.NOFOLLOW_LINKS));
        }
        catch ( IOException e ) {
            throw new JobQueueException("Failed to restore suspended job", e); //$NON-NLS-1$
        }
    }


    @Override
    public <T extends Job> void resumed ( T job ) throws JobQueueException {
        this.updateJobState(job, JobState.RESUMED);
        try {
            Path suspendPath = getSuspendPath(job.getJobId());
            if ( !Files.exists(suspendPath, LinkOption.NOFOLLOW_LINKS) ) {
                return;
            }
            Files.deleteIfExists(suspendPath);
        }
        catch ( IOException e ) {
            throw new JobQueueException("Failed to remove suspended job", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobResumptionHandler#writeSuspendData(eu.agno3.orchestrator.jobs.Job,
     *      java.io.Serializable)
     */
    @Override
    public <T extends Job> void writeSuspendData ( T job, Serializable s ) throws JobQueueException {
        try {
            Path suspendPath = getSuspendPath(job.getJobId());

            try ( FileChannel ch = FileChannel
                    .open(suspendPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                  OutputStream os = Channels.newOutputStream(ch);
                  ObjectOutputStream oos = new ObjectOutputStream(os) ) {

                oos.writeObject(s);
            }

            this.updateJobState(job, JobState.SUSPENDED);
        }
        catch ( IOException e ) {
            throw new JobQueueException("Failed to write suspended job", e); //$NON-NLS-1$
        }
    }
}
