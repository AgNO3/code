/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.update;


import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.jobs.JobDetailContextBean;
import eu.agno3.orchestrator.system.update.service.AgentUpdateService;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "updateController" )
public class UpdateController {

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private JobDetailContextBean jobContext;


    public String checkForUpdates ( String selectedStream ) {
        try {
            Set<String> extraStreams = selectedStream != null ? Collections.singleton(selectedStream) : Collections.EMPTY_SET;
            JobInfo checkForUpdates = this.ssp.getService(AgentUpdateService.class).checkForUpdates(extraStreams);
            this.jobContext.clear();
            if ( checkForUpdates != null ) {
                this.jobContext.setJobId(checkForUpdates.getJobId());
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }


    public JobInfo installUpdates ( InstanceStructuralObject inst, String stream, long sequence ) {

        return null;
    }

}
