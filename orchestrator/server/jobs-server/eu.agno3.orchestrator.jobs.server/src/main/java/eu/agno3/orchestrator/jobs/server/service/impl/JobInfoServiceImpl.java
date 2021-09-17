/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.service.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobStatusInfo;
import eu.agno3.orchestrator.jobs.JobStatusInfoImpl;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.server.JobOutputBuffer;
import eu.agno3.orchestrator.jobs.server.JobOutputTracker;
import eu.agno3.orchestrator.jobs.server.JobProgressTracker;
import eu.agno3.orchestrator.jobs.service.JobInfoService;
import eu.agno3.orchestrator.jobs.service.JobInfoServiceDescriptor;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.orchestrator.jobs.targets.AnyServerTarget;
import eu.agno3.orchestrator.jobs.targets.ServerTarget;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    JobInfoService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.jobs.service.JobInfoService",
    targetNamespace = JobInfoServiceDescriptor.NAMESPACE,
    serviceName = "jobInfoService" )
@WebServiceAddress ( "/job/info" )
public class JobInfoServiceImpl implements JobInfoService {

    private static final String JOB_VIEW_ALL = "job:view:all"; //$NON-NLS-1$
    private static final String JOB_CANCEL_ALL = "job:cancel:all"; //$NON-NLS-1$
    private static final String JOB_LOG_ALL = "job:log:all"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(JobInfoServiceImpl.class);

    private JobCoordinator coordinator;

    private JobProgressTracker progressTracker;

    private JobOutputTracker outputTracker;

    private InstanceServerService instanceService;

    private ObjectAccessControl authz;


    @Reference
    protected synchronized void setJobCoordinator ( JobCoordinator coord ) {
        this.coordinator = coord;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator coord ) {
        if ( this.coordinator == coord ) {
            this.coordinator = null;
        }
    }


    @Reference
    protected synchronized void setJobProgressTracker ( JobProgressTracker tr ) {
        this.progressTracker = tr;
    }


    protected synchronized void unsetJobProgressTracker ( JobProgressTracker tr ) {
        if ( this.progressTracker == tr ) {
            this.progressTracker = null;
        }
    }


    @Reference
    protected synchronized void setJobOutputTracker ( JobOutputTracker ot ) {
        this.outputTracker = ot;
    }


    protected synchronized void unsetJobOutputTracker ( JobOutputTracker ot ) {
        if ( this.outputTracker == ot ) {
            this.outputTracker = null;
        }
    }


    @Reference
    protected synchronized void setInstanceService ( InstanceServerService iss ) {
        this.instanceService = iss;
    }


    protected synchronized void unsetInstanceService ( InstanceServerService iss ) {
        if ( this.instanceService == iss ) {
            this.instanceService = null;
        }
    }


    @Reference
    protected synchronized void setObjectAccessControl ( ObjectAccessControl oac ) {
        this.authz = oac;
    }


    protected synchronized void unsetObjectAccessControl ( ObjectAccessControl oac ) {
        if ( this.authz == oac ) {
            this.authz = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.service.JobInfoService#listJobs(int)
     */
    @Override
    @RequirePermissions ( "job:view:owned" )
    public List<JobStatusInfo> listJobs ( int max ) throws JobQueueException {
        Set<JobInfo> jobs = new HashSet<>();
        List<JobStatusInfo> res = new LinkedList<>();

        for ( JobGroup g : this.coordinator.getKnownGroups() ) {
            Collection<JobInfo> groupJobs = this.coordinator.getAllJobs(g);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found a %d jobs in group %s", groupJobs.size(), g.getClass().getName())); //$NON-NLS-1$
            }

            jobs.addAll(groupJobs);
        }

        List<JobInfo> sorted = new ArrayList<>(jobs);

        Collections.sort(sorted, new JobInfoDisplayComparator());
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found a total of %d jobs", jobs.size())); //$NON-NLS-1$
        }

        int cnt = 0;
        for ( JobInfo j : sorted ) {
            try {
                handleJob(j, res);
                cnt++;
                if ( max > 0 && cnt >= max ) {
                    break;
                }
            }
            catch ( JobUnknownException e ) {
                log.warn("Failed to get data for job " + j.getJobId(), e); //$NON-NLS-1$
            }
        }

        return res;
    }


    private void handleJob ( JobInfo j, List<JobStatusInfo> res ) throws JobUnknownException {
        StructuralObject obj = this.getObjectFromTarget(this.getJobTarget(j));

        if ( !hasJobPermission(JOB_VIEW_ALL, j, obj) ) {
            return;
        }

        JobStatusInfoImpl si = statusInfoFromJobInfo(j, obj);
        res.add(si);
    }


    private JobStatusInfoImpl statusInfoFromJobInfo ( JobInfo j, StructuralObject obj ) throws JobUnknownException {
        JobStatusInfoImpl si = JobStatusInfoImpl.fromJobInfo(j, this.coordinator.getJobData(j.getJobId()));

        if ( obj != null ) {
            si.setTargetDisplayName(obj.getDisplayName());
        }

        si.setProgress(this.progressTracker.getProgressInfo(j.getJobId()));
        return si;
    }


    /**
     * @param j
     * @return
     */
    private static boolean isJobOwner ( JobInfo j ) {

        Collection<UserPrincipal> ups = SecurityUtils.getSubject().getPrincipals().byType(UserPrincipal.class);

        if ( ups.size() != 1 ) {
            throw new UnauthorizedException("No or multiple user principals in subject"); //$NON-NLS-1$
        }

        UserPrincipal up = ups.iterator().next();
        return up.equals(j.getOwner());
    }


    /**
     * @param j
     * @return
     * @throws JobUnknownException
     */
    private JobTarget getJobTarget ( JobInfo j ) throws JobUnknownException {
        return this.coordinator.getJobData(j.getJobId()).getTarget();
    }


    /**
     * @param target
     * @return
     * @throws ModelObjectNotFoundException
     */
    private StructuralObject getObjectFromTarget ( JobTarget target ) {
        // TODO: this should be somewhere reusable
        if ( target instanceof AgentTarget ) {
            UUID agentId = ( (AgentTarget) target ).getAgentId();
            try {
                return this.instanceService.getInstanceForAgent(agentId);
            }
            catch (
                ModelObjectNotFoundException |
                ModelServiceException e ) {
                log.warn("Failed to lookup instance for agent id " + agentId, e); //$NON-NLS-1$
            }
        }
        else if ( target instanceof ServerTarget ) {
            // TODO:
            return null;
        }
        else if ( target instanceof AnyServerTarget ) {
            return null;
        }

        log.warn("Unknown job target " + target); //$NON-NLS-1$
        return null;
    }


    @Override
    @RequirePermissions ( "job:cancel:owned" )
    public void cancelJob ( UUID jobId ) {
        try {
            JobInfo jobInfo = this.coordinator.getJobInfo(jobId);
            StructuralObject obj = this.getObjectFromTarget(this.getJobTarget(jobInfo));
            checkJobPermission(JOB_CANCEL_ALL, jobInfo, obj);

            this.coordinator.cancelJob(jobId);
        }
        catch ( JobQueueException e ) {
            log.warn("Could not cancel job:", e); //$NON-NLS-1$
        }

    }


    /**
     * @param permission
     * @param jobInfo
     * @param obj
     */
    protected void checkJobPermission ( String permission, JobInfo jobInfo, StructuralObject obj ) {
        if ( this.authz.isDisabled() ) {
            return;
        }
        if ( !isJobOwner(jobInfo) ) {
            SecurityUtils.getSubject().checkPermission(permission);
            this.authz.checkAccess(obj, permission);
        }
    }


    protected boolean hasJobPermission ( String permission, JobInfo jobInfo, StructuralObject obj ) {
        if ( this.authz.isDisabled() ) {
            return true;
        }
        if ( !isJobOwner(jobInfo) ) {
            return SecurityUtils.getSubject().isPermitted(permission) && this.authz.hasAccess(obj, permission);
        }
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.service.JobInfoService#getJobInfo(java.util.UUID)
     */
    @Override
    @RequirePermissions ( "job:view:owned" )
    public JobStatusInfo getJobInfo ( UUID jobId ) throws JobUnknownException {
        try {
            if ( jobId == null ) {
                throw new JobUnknownException();
            }
            JobInfo jobInfo = this.coordinator.getJobInfo(jobId);
            StructuralObject obj = this.getObjectFromTarget(this.getJobTarget(jobInfo));
            checkJobPermission(JOB_VIEW_ALL, jobInfo, obj);
            return statusInfoFromJobInfo(jobInfo, obj);
        }
        catch ( JobUnknownException e ) {
            throw e;
        }
        catch ( JobQueueException e ) {
            log.warn("Unexpected error while fetching job info", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobUnknownException
     * 
     * @see eu.agno3.orchestrator.jobs.service.JobInfoService#getJobOutput(java.util.UUID, long)
     */
    @Override
    @RequirePermissions ( "job:log:owned" )
    public String getJobOutput ( UUID jobId, long off ) throws JobUnknownException {
        try {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Getting job %s output at %s", jobId, off)); //$NON-NLS-1$
            }
            JobInfo jobInfo = this.coordinator.getJobInfo(jobId);
            StructuralObject obj = this.getObjectFromTarget(this.getJobTarget(jobInfo));
            checkJobPermission(JOB_LOG_ALL, jobInfo, obj);
            JobOutputBuffer output = this.outputTracker.getOutput(jobId);
            if ( output == null ) {
                log.debug("No output found"); //$NON-NLS-1$
                return StringUtils.EMPTY;
            }
            return output.getCombinedOutput(off);
        }
        catch ( JobUnknownException e ) {
            throw e;
        }
        catch ( JobQueueException e ) {
            log.warn("Unexpected error while fetching job output", e); //$NON-NLS-1$
            return null;
        }
    }

}
