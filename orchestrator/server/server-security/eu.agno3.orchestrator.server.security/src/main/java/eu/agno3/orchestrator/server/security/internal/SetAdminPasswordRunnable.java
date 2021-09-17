/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.server.security.LocalUserServerService;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class SetAdminPasswordRunnable implements JobRunnable {

    private static final String LOCAL_REALM = "LOCAL"; //$NON-NLS-1$
    private static final String ADMIN = "admin"; //$NON-NLS-1$

    private SCryptResult passwordHash;
    private EntityTransactionService ets;
    private LocalUserServerService userService;


    /**
     * @param userService
     * @param ets
     * @param passwordHash
     */
    public SetAdminPasswordRunnable ( LocalUserServerService userService, EntityTransactionService ets, SCryptResult passwordHash ) {
        this.userService = userService;
        this.ets = ets;
        this.passwordHash = passwordHash;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
     */
    @Override
    public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {
        try ( EntityTransactionContext tx = this.ets.start() ) {
            UserPrincipal principal = new UserPrincipal(LOCAL_REALM, null, ADMIN);
            this.userService.changePassword(tx, principal, this.passwordHash);
            tx.commit();
        }
        return JobState.FINISHED;
    }

}
