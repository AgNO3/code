/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.server.internal;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.update.server.UpdateServiceInternal;
import eu.agno3.orchestrator.agent.update.server.data.DescriptorCache;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.coord.LoggingJobOutputHandler;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.update.UpdateDescriptor;
import eu.agno3.orchestrator.system.update.UpdateDescriptorParser;
import eu.agno3.orchestrator.system.update.UpdateException;
import eu.agno3.orchestrator.system.update.jobs.UpdateCheckJob;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.xml.XMLParserConfigurationException;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    UpdateCheckJobRunnableFactory.class, JobRunnableFactory.class
}, property = "jobType=eu.agno3.orchestrator.config.model.jobs.UpdateCheckJob" )
@JobType ( value = UpdateCheckJob.class )
public class UpdateCheckJobRunnableFactory implements JobRunnableFactory<UpdateCheckJob> {

    private static final Logger log = Logger.getLogger(UpdateJobRunnable.class);

    private static final Set<String> DEFAULT_RELEASE_STREAMS = new HashSet<>(Arrays.asList(
        "TESTING", //$NON-NLS-1$
        "RELEASE")); //$NON-NLS-1$

    DefaultServerServiceContext sctx;

    AgentServerService agentService;
    UpdateDescriptorParser updateParser;

    XmlMarshallingService xmlMarshaller;
    XmlParserFactory xmlParser;

    UpdateServiceInternal updateService;


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setUpdateParser ( UpdateDescriptorParser udp ) {
        this.updateParser = udp;
    }


    protected synchronized void unsetUpdateParser ( UpdateDescriptorParser udp ) {
        if ( this.updateParser == udp ) {
            this.updateParser = null;
        }
    }


    @Reference
    protected synchronized void setXmlMarshaller ( XmlMarshallingService xms ) {
        this.xmlMarshaller = xms;
    }


    protected synchronized void unsetXmlMarshaller ( XmlMarshallingService xms ) {
        if ( this.xmlMarshaller == xms ) {
            this.xmlMarshaller = null;
        }
    }


    @Reference
    protected synchronized void setXmlParser ( XmlParserFactory xpf ) {
        this.xmlParser = xpf;
    }


    protected synchronized void unsetXmlParser ( XmlParserFactory xpf ) {
        if ( this.xmlParser == xpf ) {
            this.xmlParser = null;
        }
    }


    @Reference
    protected synchronized void setAgentService ( AgentServerService as ) {
        this.agentService = as;
    }


    protected synchronized void unsetAgentService ( AgentServerService as ) {
        if ( this.agentService == as ) {
            this.agentService = null;
        }
    }


    @Reference
    protected synchronized void setUpdateService ( UpdateServiceInternal us ) {
        this.updateService = us;
    }


    protected synchronized void unsetUpdateService ( UpdateServiceInternal us ) {
        if ( this.updateService == us ) {
            this.updateService = null;
        }
    }


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( UpdateCheckJob j ) throws JobRunnableException {
        return new UpdateJobRunnable(j);
    }


    /**
     * 
     * @return runnable checking for updates
     */
    public UpdateJobRunnable updateAll () {
        return new UpdateJobRunnable(this.updateService.createUpdateJob(DEFAULT_RELEASE_STREAMS, null));

    }

    /**
     * @author mbechler
     *
     */
    public class UpdateJobRunnable implements JobRunnable {

        private final Set<String> updateStreams;
        private final Set<String> updateImageTypes;
        private final DateTime retainAfter;
        private final DateTime updateBefore;

        private final Map<String, Set<String>> streamsWithUpdate = new HashMap<>();


        /**
         * @param j
         * 
         */
        public UpdateJobRunnable ( UpdateCheckJob j ) {
            this.updateStreams = j.getUpdateStreams();
            this.updateImageTypes = j.getUpdateImageTypes();
            this.retainAfter = j.getRetainAfterTime();
            this.updateBefore = j.getUpdateBeforeTime();
        }


        /**
         * @return the streamsWithUpdate
         */
        public Map<String, Set<String>> getStreamsWithUpdate () {
            return this.streamsWithUpdate;
        }


        /**
         * @throws Exception
         */
        public void run () throws Exception {
            JobState st = run(new LoggingJobOutputHandler(getLog()));
            if ( st != JobState.FINISHED ) {
                throw new JobRunnableException("Job did not complete: " + st); //$NON-NLS-1$
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
         */
        @Override
        public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {

            for ( String stream : this.updateStreams ) {
                for ( String imageType : this.updateImageTypes ) {
                    try ( TransactionContext tx = UpdateCheckJobRunnableFactory.this.sctx.getTransactionService().ensureTransacted() ) {
                        EntityManager orchem = UpdateCheckJobRunnableFactory.this.sctx.createOrchEM();
                        EntityManager em = UpdateCheckJobRunnableFactory.this.sctx.getConfigEMF().createEntityManager();

                        outHandler.logLineInfo(String.format("Checking for updates of %s (%s)", imageType, stream)); //$NON-NLS-1$

                        if ( !updateDescriptors(orchem, stream, imageType, this.retainAfter, this.updateBefore, outHandler) ) {
                            tx.commit();
                            continue;
                        }

                        Set<String> streamUpdates = this.streamsWithUpdate.get(imageType);
                        if ( streamUpdates == null ) {
                            streamUpdates = new HashSet<>();
                            this.streamsWithUpdate.put(imageType, streamUpdates);
                        }
                        streamUpdates.add(stream);

                        TypedQuery<InstanceStructuralObjectImpl> instanceQuery = em.createQuery(
                            "SELECT inst FROM InstanceStructuralObjectImpl inst WHERE inst.imageType = :imageType", //$NON-NLS-1$
                            InstanceStructuralObjectImpl.class);
                        instanceQuery.setParameter("imageType", imageType); //$NON-NLS-1$

                        for ( InstanceStructuralObject inst : instanceQuery.getResultList() ) {
                            outHandler.logLineInfo(String.format("Found updates for %s", inst)); //$NON-NLS-1$
                            UpdateCheckJobRunnableFactory.this.updateService.foundUpdates(inst, stream);
                        }

                        tx.commit();
                    }
                    catch ( PersistenceException e ) {
                        getLog().error("Failed to update descriptor cache", e); //$NON-NLS-1$
                    }
                }
            }

            try ( TransactionContext tx = UpdateCheckJobRunnableFactory.this.sctx.getTransactionService().ensureTransacted() ) {
                EntityManager orchem = UpdateCheckJobRunnableFactory.this.sctx.createOrchEM();
                Query removalQuery = createDescriptorRemovalQuery(orchem);
                int removed = removalQuery.executeUpdate();
                if ( removed > 0 && getLog().isDebugEnabled() ) {
                    getLog().debug(String.format("Removed %d superfluous cached descriptors", removed)); //$NON-NLS-1$
                }

            }
            catch ( PersistenceException e ) {
                getLog().error("Failed to remove superfluous caches", e); //$NON-NLS-1$
            }

            return JobState.FINISHED;
        }


        /**
         * @param orchem
         * @return
         */
        private Query createDescriptorRemovalQuery ( EntityManager orchem ) {
            Query removalQuery;
            if ( !this.updateImageTypes.isEmpty() && !this.updateStreams.isEmpty() ) {
                removalQuery = orchem
                        .createQuery("DELETE FROM DescriptorCache d WHERE d.imageType NOT IN :imageTypes OR d.stream NOT IN :streams"); //$NON-NLS-1$
                removalQuery.setParameter("imageTypes", this.updateImageTypes); //$NON-NLS-1$
                removalQuery.setParameter("streams", this.updateStreams); //$NON-NLS-1$
            }
            else if ( !this.updateImageTypes.isEmpty() ) {
                removalQuery = orchem.createQuery("DELETE FROM DescriptorCache d WHERE d.imageType NOT IN :imageTypes"); //$NON-NLS-1$
                removalQuery.setParameter("imageTypes", this.updateImageTypes); //$NON-NLS-1$
            }
            else if ( !this.updateStreams.isEmpty() ) {
                removalQuery = orchem.createQuery("DELETE FROM DescriptorCache d WHERE d.stream NOT IN :streams"); //$NON-NLS-1$
                removalQuery.setParameter("streams", this.updateStreams); //$NON-NLS-1$
            }
            else {
                removalQuery = orchem.createQuery("DELETE FROM DescriptorCache d"); //$NON-NLS-1$
            }
            return removalQuery;
        }


        /**
         * @param orchem
         * @param stream
         * @param imageType
         * @param retainTime
         * @param minUpdate
         * @return whether an updated descriptor has been found
         */
        private boolean updateDescriptors ( EntityManager orchem, String stream, String imageType, DateTime retainTime, DateTime minUpdate,
                @NonNull JobOutputHandler outHandler ) {

            if ( getLog().isDebugEnabled() ) {
                getLog().debug(String.format("Updating descriptors for %s/%s", imageType, stream)); //$NON-NLS-1$
            }

            TypedQuery<DescriptorCache> cacheQuery = orchem.createQuery(
                "SELECT d FROM DescriptorCache d WHERE " //$NON-NLS-1$
                        + "d.stream = :stream AND d.imageType = :imageType " + //$NON-NLS-1$
                        "ORDER BY d.sequence DESC", //$NON-NLS-1$
                DescriptorCache.class);

            cacheQuery.setParameter("stream", stream); //$NON-NLS-1$
            cacheQuery.setParameter("imageType", imageType); //$NON-NLS-1$

            List<DescriptorCache> resultList = cacheQuery.getResultList();
            DescriptorCache latestCache = null;
            if ( resultList.size() > 0 ) {
                latestCache = resultList.get(0);
                if ( minUpdate != null && latestCache.getLastUpdated().isAfter(minUpdate) ) {
                    getLog().debug("Not updating, minimum wait period not over"); //$NON-NLS-1$
                    return false;
                }
            }

            UpdateDescriptor latest = decodeCached(latestCache);
            UpdateDescriptor fetched = null;
            try {
                fetched = UpdateCheckJobRunnableFactory.this.updateParser.getLatestEffective(stream, imageType, latest);
            }
            catch ( UpdateException e ) {
                String msg = String.format("Failed to get update descriptor for %s/%s", imageType, stream); //$NON-NLS-1$
                outHandler.logLineError(msg);
                outHandler.logLineError(e.getMessage());
                if ( e.getCause() != null ) {
                    outHandler.logLineError(e.getCause().getMessage());
                }
                getLog().warn(msg, e);
                return false;
            }

            boolean res = false;
            if ( fetched == null ) {
                // result is cached
                getLog().debug("Descriptor is cached"); //$NON-NLS-1$
                if ( latestCache != null ) {
                    latestCache.setLastUpdated(DateTime.now());
                    orchem.persist(latestCache);
                    res = false;
                }
            }
            else {
                if ( latestCache != null && latestCache.getSequence() == fetched.getSequence() ) {
                    res = onCached(orchem, latestCache, fetched);
                }
                else {
                    try {
                        if ( getLog().isDebugEnabled() ) {
                            getLog().debug("New descriptor with sequence " + fetched.getSequence()); //$NON-NLS-1$
                        }
                        updateCached(orchem, stream, imageType, fetched);
                        res = true;
                    }
                    catch (
                        XMLBindingException |
                        XMLStreamException |
                        FactoryConfigurationError e ) {
                        getLog().error("Failed to create descriptor data", e); //$NON-NLS-1$
                        return false;
                    }
                }
            }

            removeStale(orchem, retainTime, resultList);
            orchem.flush();
            return res;
        }


        /**
         * @param orchem
         * @param latestCache
         * @param fetched
         * @return
         */
        private boolean onCached ( EntityManager orchem, DescriptorCache latestCache, UpdateDescriptor fetched ) {
            boolean res;
            if ( latestCache.getData() != null ) {
                getLog().debug("Descriptor is cached (but was returned by the server)"); //$NON-NLS-1$
                res = false;
            }
            else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    UpdateCheckJobRunnableFactory.this.xmlMarshaller.marshall(fetched, XMLOutputFactory.newInstance().createXMLStreamWriter(bos));
                    latestCache.setData(bos.toByteArray());
                    orchem.persist(latestCache);
                    res = true;
                }
                catch (
                    XMLBindingException |
                    XMLStreamException |
                    FactoryConfigurationError e ) {
                    getLog().warn("Failed to write descriptor", e); //$NON-NLS-1$
                    res = false;
                }
            }
            return res;
        }


        /**
         * @param orchem
         * @param stream
         * @param imageType
         * @param fetched
         * @throws XMLBindingException
         * @throws XMLStreamException
         * @throws FactoryConfigurationError
         */
        private void updateCached ( EntityManager orchem, String stream, String imageType, UpdateDescriptor fetched )
                throws XMLBindingException, XMLStreamException, FactoryConfigurationError {
            DescriptorCache cache = new DescriptorCache();
            cache.setImageType(imageType);
            cache.setStream(stream);
            cache.setSequence(fetched.getSequence());
            cache.setLastUpdated(DateTime.now());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            UpdateCheckJobRunnableFactory.this.xmlMarshaller.marshall(fetched, XMLOutputFactory.newInstance().createXMLStreamWriter(bos));

            cache.setData(bos.toByteArray());
            orchem.persist(cache);
        }


        /**
         * @param orchem
         * @param retainTime
         * @param resultList
         */
        private void removeStale ( EntityManager orchem, DateTime retainTime, List<DescriptorCache> resultList ) {
            for ( int i = 1; i < resultList.size(); i++ ) {
                DescriptorCache old = resultList.get(i);
                if ( retainTime != null && old.getLastUpdated().isBefore(retainTime) ) {
                    orchem.remove(old);
                }
            }
        }


        /**
         * @param latestCache
         * @return
         */
        private UpdateDescriptor decodeCached ( DescriptorCache latestCache ) {
            UpdateDescriptor latest = null;
            if ( latestCache != null ) {
                try {
                    if ( latestCache.getData() != null ) {
                        latest = UpdateCheckJobRunnableFactory.this.xmlMarshaller.unmarshall(
                            UpdateDescriptor.class,
                            UpdateCheckJobRunnableFactory.this.xmlParser.createStreamReader(new ByteArrayInputStream(latestCache.getData())));
                    }
                }
                catch (
                    XMLBindingException |
                    XMLParserConfigurationException e ) {
                    getLog().warn("Failed to parse cached descriptor", e); //$NON-NLS-1$
                }
            }
            return latest;
        }
    }

}
