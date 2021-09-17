/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.jobs;


import java.util.EnumSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobStatusInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 * 
 */
@Named ( "jobDisplay" )
@ApplicationScoped
public class JobDisplayBean {

    private static final Set<JobState> SHOW_PROGRESS_STATES = EnumSet
            .of(JobState.RUNNABLE, JobState.RUNNING, JobState.UNKNOWN, JobState.FAILED, JobState.FINISHED);


    public String getStateLabel ( JobStatusInfo i ) {
        if ( i == null ) {
            return null;
        }
        return i.getState().name();
    }


    public String getTarget ( JobStatusInfo i ) {
        if ( i == null ) {
            return null;
        }
        return i.getTargetDisplayName();
    }


    public String getType ( JobStatusInfo j ) {
        if ( j == null ) {
            return null;
        }
        String type = j.getJobType();

        if ( type.indexOf('.') >= 0 ) {
            return type.substring(type.lastIndexOf('.') + 1);
        }

        return type;
    }


    public boolean isFinished ( JobStatusInfo j ) {
        if ( j == null ) {
            return true;
        }

        return EnumSet.of(JobState.CANCELLED, JobState.FAILED, JobState.FINISHED).contains(j.getState());
    }


    public String getDisplayOwner ( JobStatusInfo j ) {
        UserPrincipal p = j.getOwner();

        if ( p == null ) {
            return null;
        }

        return String.format("%s@%s", j.getOwner().getUserName(), j.getOwner().getRealmName()); //$NON-NLS-1$
    }


    public boolean isShowProgress ( JobStatusInfo info ) {
        return SHOW_PROGRESS_STATES.contains(info.getState());
    }
}
