/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.hostconfig.jobs.SetAdminPasswordJob;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.server.security.LocalUserServerService;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.security.SecurityManagementException;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    JobRunnableFactory.class
}, property = "jobType=eu.agno3.orchestrator.config.hostconfig.jobs.SetAdminPasswordJob" )
@JobType ( SetAdminPasswordJob.class )
public class SetAdminPasswordJobBuilder implements JobRunnableFactory<SetAdminPasswordJob> {

    private LocalUserServerService userService;
    private EntityTransactionService authEts;


    @Reference
    protected synchronized void setUserService ( LocalUserServerService init ) {
        this.userService = init;
    }


    protected synchronized void unsetUserService ( LocalUserServerService init ) {
        if ( this.userService == init ) {
            this.userService = null;
        }
    }


    @Reference ( target = "(persistenceUnit=auth)" )
    protected synchronized void bindEntityTransactionService ( EntityTransactionService ets ) {
        this.authEts = ets;
    }


    protected synchronized void unbindEntityTransactionService ( EntityTransactionService ets ) {
        if ( this.authEts == ets ) {
            this.authEts = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( SetAdminPasswordJob j ) throws JobRunnableException {
        SCryptResult passwordHash;
        try {
            String adminPassword = j.getAdminPassword();
            passwordHash = this.userService.generatePasswordHash(adminPassword, false);
        }
        catch ( SecurityManagementException e ) {
            throw new JobRunnableException("Failed to generate password hash", e); //$NON-NLS-1$
        }
        return new SetAdminPasswordRunnable(this.userService, this.authEts, passwordHash);
    }

}
