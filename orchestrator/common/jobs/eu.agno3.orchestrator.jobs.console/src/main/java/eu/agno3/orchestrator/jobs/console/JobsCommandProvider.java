/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.orchestrator.jobs.console;


import java.util.Collection;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class JobsCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(JobsCommandProvider.class);
    private ComponentContext componentContext;

    private JobCoordinator coordinator;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.componentContext = null;
    }


    @Reference
    protected synchronized void setCoordinator ( JobCoordinator jc ) {
        this.coordinator = jc;
    }


    protected synchronized void unsetCoordinator ( JobCoordinator jc ) {
        if ( this.coordinator == jc ) {
            this.coordinator = null;
        }
    }


    /**
     * @return the componentContext
     */
    ComponentContext getComponentContext () {
        return this.componentContext;
    }


    /**
     * @return the coordinator
     */
    JobCoordinator getCoordinator () {
        return this.coordinator;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }

    /**
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "jobs", name = "list", description = "List known jobs" )
    public class RunCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;

        @Option ( aliases = {
            "-a"
        }, name = "active", description = "show only active jobs" )
        private boolean activeOnly;

        @Option ( aliases = {
            "-v"
        }, name = "verbose", description = "verbose" )
        private boolean verbose;


        @Override
        public Object execute () throws JobQueueException {

            for ( JobGroup jobGroup : getCoordinator().getKnownGroups() ) {

                Collection<JobInfo> jobs = this.activeOnly ? getCoordinator().getActiveJobs(jobGroup) : getCoordinator().getAllJobs(jobGroup);

                if ( jobs.isEmpty() ) {
                    continue;
                }

                Ansi ansi = Ansi.ansi();
                ansi.a("In group ").bold().a(jobGroup.getClass().getSimpleName()).boldOff().a(':').newline(); //$NON-NLS-1$
                for ( JobInfo ji : jobs ) {
                    Job jobData = getCoordinator().getJobData(ji.getJobId());
                    ansi.bold().a(ji.getState()).a('\t').a(ji.getJobId()).a('\t').a(jobData.getClass().getSimpleName());
                    if ( this.verbose ) {
                        ansi.a('\t').a(jobData).newline();
                    }
                    ansi.newline();
                }
                ansi.newline();
                this.session.getConsole().print(ansi.toString());
            }

            return null;
        }
    }

    /**
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "jobs", name = "cancel", description = "Cancel job" )
    public class CancelCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;

        @Argument ( index = 0, required = true, name = "jobId" )
        private String jobId;


        @Override
        public Object execute () throws JobQueueException {
            getCoordinator().cancelJob(UUID.fromString(this.jobId));
            return null;
        }
    }

    /**
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "jobs", name = "cleanup", description = "Cleanup finished jobs" )
    public class CleanupCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws JobQueueException {
            int removed = getCoordinator().clearFinishedJobs();
            this.session.getConsole().println(String.format("Removed %d finished jobs", removed)); //$NON-NLS-1$
            return null;
        }
    }

}
