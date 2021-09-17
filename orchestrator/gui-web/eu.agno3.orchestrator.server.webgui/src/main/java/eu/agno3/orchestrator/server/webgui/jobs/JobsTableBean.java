/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.jobs;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.service.JobInfoService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.runtime.jsf.windowscope.WindowScoped;


/**
 * @author mbechler
 * 
 */
@Named ( "jobsTable" )
@WindowScoped
public class JobsTableBean implements Serializable {

    private static final Logger log = Logger.getLogger(JobsTableBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = -8837420128496845751L;

    @Inject
    private ServerServiceProvider ssp;

    private transient JobInfoDataModel model;

    private Set<JobInfo> selected = Collections.EMPTY_SET;

    private int max = 8;


    /**
     * @return the model
     * @throws GuiWebServiceException
     */
    public JobInfoDataModel getModel () throws GuiWebServiceException {
        if ( this.model == null ) {
            this.model = new JobInfoDataModel(this.ssp.getService(JobInfoService.class), this.max);
        }
        return this.model;
    }


    public Object refresh () throws GuiWebServiceException {
        log.debug("Complete reload of jobs"); //$NON-NLS-1$
        this.model = new JobInfoDataModel(this.ssp.getService(JobInfoService.class), this.max);
        return null;
    }


    public void refresh ( ActionEvent ev ) throws GuiWebServiceException {
        this.refresh();
    }


    /**
     * @return the selected
     */
    public JobInfo[] getSelected () {
        return this.selected.toArray(new JobInfo[] {});
    }


    /**
     * @param selected
     *            the selected to set
     */
    public void setSelected ( JobInfo[] selected ) {
        if ( selected != null ) {
            this.selected = new HashSet<>(Arrays.asList(selected));
        }
        else {
            this.selected = Collections.EMPTY_SET;
        }
    }
}
