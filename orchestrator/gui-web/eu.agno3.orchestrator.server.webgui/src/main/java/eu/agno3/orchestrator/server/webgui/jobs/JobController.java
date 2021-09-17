/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.jobs;


import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.service.JobInfoService;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
@Named ( "jobController" )
@ApplicationScoped
public class JobController {

    private static final Logger log = Logger.getLogger(JobController.class);

    @Inject
    private ServerServiceProvider ssp;


    public String cancel ( UUID jobId ) throws GuiWebServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Cancel job " + jobId); //$NON-NLS-1$
        }

        try {
            this.ssp.getService(JobInfoService.class).cancelJob(jobId);
        }
        catch ( JobUnknownException e ) {
            log.warn("Failed to cancel job", e); //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, GuiMessages.get(GuiMessages.JOBS_DOESNOTEXIST), e.getMessage()));
        }
        return null;
    }
}
