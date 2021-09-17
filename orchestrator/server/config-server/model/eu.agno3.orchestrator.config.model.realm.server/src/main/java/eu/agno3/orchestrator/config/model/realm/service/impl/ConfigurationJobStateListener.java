/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 18, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.events.ServiceConfigFailedEvent;
import eu.agno3.orchestrator.config.model.jobs.ConfigApplyJob;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.server.service.ConfigApplyServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.orchestrator.jobs.state.LocalJobStateListener;
import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 *
 */
@Component ( service = LocalJobStateListener.class )
public class ConfigurationJobStateListener implements LocalJobStateListener {

    private static final Logger log = Logger.getLogger(ConfigurationJobStateListener.class);
    private static final String CONFIG_JOB_TYPE = ConfigApplyJob.class.getName();

    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private JobCoordinator jobCoord;
    private Optional<@NonNull MessageSource> messageSource = Optional.empty();
    private ConfigApplyServerService configApplyService;


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
    protected synchronized void setPersistenceUtil ( PersistenceUtil pu ) {
        this.persistenceUtil = pu;
    }


    protected synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil == pu ) {
            this.persistenceUtil = null;
        }
    }


    @Reference
    protected synchronized void setJobCoordinator ( JobCoordinator jc ) {
        this.jobCoord = jc;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator jc ) {
        if ( this.jobCoord == jc ) {
            this.jobCoord = null;
        }
    }


    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource mss ) {
        this.messageSource = Optional.of(mss);
    }


    protected synchronized void unsetMessageSource ( @NonNull MessageSource mss ) {
        this.messageSource = Optional.empty();
    }


    @Reference
    protected synchronized void setConfigApplyService ( ConfigApplyServerService ss ) {
        this.configApplyService = ss;
    }


    protected synchronized void unsetConfigApplyService ( ConfigApplyServerService ss ) {
        if ( this.configApplyService == ss ) {
            this.configApplyService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobUpdated(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public void jobUpdated ( JobStateUpdatedEvent ev ) {
        if ( ev == null || ev.getJobInfo() == null || !CONFIG_JOB_TYPE.equals(ev.getJobInfo().getType())
                || ev.getJobInfo().getState() != JobState.FAILED ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format(
                    "Ignoring job event %s state %s", //$NON-NLS-1$
                    ( ev != null && ev.getJobInfo() != null ? ev.getJobInfo().getType() : null ),
                    ( ev != null && ev.getJobInfo() != null ? ev.getJobInfo().getState() : null )));
            }
            return;
        }

        try {
            Job jobData = this.jobCoord.getJobData(ev.getJobId());
            if ( ! ( jobData instanceof ConfigApplyJob ) ) {
                log.debug("Invalid job type"); //$NON-NLS-1$
                return;
            }

            @NonNull
            EntityManager em = this.sctx.createConfigEM();
            ConfigApplyJob caj = (ConfigApplyJob) jobData;
            List<Job> subjobs = caj.getJobs();
            if ( subjobs == null ) {
                return;
            }
            for ( Job subjob : subjobs ) {
                if ( ! ( subjob instanceof ConfigurationJob ) ) {
                    continue;
                }

                ConfigurationJob cj = (ConfigurationJob) subjob;

                ServiceStructuralObject service = cj.getService();

                @NonNull
                ServiceStructuralObjectImpl pservice = this.persistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service);
                if ( pservice.getState() != ConfigurationState.APPLYING ) {
                    continue;
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Found service in applying state " + pservice); //$NON-NLS-1$
                }

                try {
                    JobInfo ji = this.jobCoord.getJobInfo(subjob.getJobId());

                    if ( log.isDebugEnabled() ) {
                        log.debug("Job state is " + ji.getState()); //$NON-NLS-1$
                    }

                    if ( ji.getState() != JobState.FAILED ) {
                        continue;
                    }
                }
                catch ( JobUnknownException e ) {
                    log.debug("Failed to locate job state", e); //$NON-NLS-1$
                }

                ServiceConfigFailedEvent failev = new ServiceConfigFailedEvent(this.messageSource.get());
                failev.setService(service);
                failev.setAnchor(cj.getAnchor());

                this.configApplyService.handleConfigFailed(failev);
            }
        }
        catch ( Exception e ) {
            log.warn("Failed to check service config state", e); //$NON-NLS-1$
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
        // ignore
    }

}
