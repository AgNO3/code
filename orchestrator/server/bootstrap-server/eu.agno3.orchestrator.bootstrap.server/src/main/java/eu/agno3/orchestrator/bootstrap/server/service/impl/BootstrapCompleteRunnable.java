/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.server.service.impl;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.bootstrap.server.service.BootstrapServerService;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;


/**
 * @author mbechler
 *
 */
public class BootstrapCompleteRunnable implements JobRunnable {

    private BootstrapContext context;
    private BootstrapServerService bootstrapService;


    /**
     * @param bootstrapService
     * @param context
     */
    public BootstrapCompleteRunnable ( BootstrapServerService bootstrapService, BootstrapContext context ) {
        this.bootstrapService = bootstrapService;
        this.context = context;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
     */
    @Override
    public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {
        outHandler.logLineInfo("Completing bootstrap process"); //$NON-NLS-1$
        this.bootstrapService.afterApplyConfig(this.context);
        return JobState.FINISHED;
    }

}
