/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.jobs;


import java.io.Serializable;
import java.util.UUID;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.jobs.JobStatusInfo;
import eu.agno3.orchestrator.jobs.service.JobInfoService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "jobDetailContextBean" )
@ViewScoped
public class JobDetailContextBean implements Serializable {

    private static final Logger log = Logger.getLogger(JobDetailContextBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 8136346425130736894L;

    @Inject
    private ServerServiceProvider ssp;

    private UUID jobId;
    private int currentOffset;
    private DateTime lastUpdate;
    private StringBuilder buffer = new StringBuilder();
    private JobStatusInfo initialJobInfo;

    private boolean failed = false;


    /**
     * 
     */
    public JobDetailContextBean () {}


    /**
     * @param jobId
     *            the jobId to set
     */
    public void setJobId ( UUID jobId ) {
        this.jobId = jobId;
    }


    /**
     * @return the jobId
     */
    public UUID getJobId () {
        return this.jobId;
    }


    /**
     * @return the currentOffset
     */
    public int getCurrentOffset () {
        return this.currentOffset;
    }


    /**
     * @return the lastUpdate
     */
    public DateTime getLastUpdate () {
        return this.lastUpdate;
    }


    public synchronized void append ( String text ) {

        if ( text == null ) {
            this.lastUpdate = new DateTime();
            return;
        }

        this.buffer.append(text);
        this.currentOffset += text.length();
        this.lastUpdate = new DateTime();
    }


    /**
     * @return the buffer
     */
    public String getBuffer () {
        return this.buffer.toString();
    }


    /**
     * @param text
     */
    public void setBuffer ( String text ) {
        this.buffer = new StringBuilder(text);
        this.currentOffset = text.length();
        this.lastUpdate = new DateTime();
    }


    /**
     * @param initialJobInfo
     *            the initialJobInfo to set
     */
    public void setInitialJobInfo ( JobStatusInfo initialJobInfo ) {
        this.initialJobInfo = initialJobInfo;
    }


    public void clear () {
        this.lastUpdate = new DateTime();
        this.jobId = null;
        this.initialJobInfo = null;
        this.buffer = new StringBuilder();
        this.currentOffset = 0;
    }


    public JobStatusInfo getInitialJobInfo () {
        if ( this.initialJobInfo == null ) {
            loadInitialJobInfo();
        }
        return this.initialJobInfo;
    }


    private void loadInitialJobInfo () {
        try {
            if ( this.failed ) {
                return;
            }
            this.initialJobInfo = this.ssp.getService(JobInfoService.class).getJobInfo(this.getJobId());
            if ( log.isDebugEnabled() ) {
                log.debug("Initial job state " + ( this.initialJobInfo != null ? this.initialJobInfo.getState() : null )); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            this.failed = true;
            ExceptionHandler.handle(e);
        }
    }


    public void updateInitial ( ComponentSystemEvent ev ) {
        log.debug("Initial"); //$NON-NLS-1$
        forceUpdate();
    }


    public void update ( ActionEvent ev ) {
        update();
    }


    public void forceUpdate ( ActionEvent ev ) {
        forceUpdate();
    }


    /**
     * 
     */
    public void forceUpdate () {
        if ( this.failed ) {
            return;
        }
        loadInitialJobInfo();
        if ( log.isDebugEnabled() ) {
            log.debug("Reloading job output, state is " + this.getInitialJobInfo().getState()); //$NON-NLS-1$
        }

        try {
            String jobOutput = this.ssp.getService(JobInfoService.class).getJobOutput(this.getJobId(), 0);
            if ( log.isDebugEnabled() ) {
                log.debug("Full output " + jobOutput); //$NON-NLS-1$
            }

            this.setBuffer(jobOutput);
        }
        catch ( Exception e ) {
            this.failed = true;
            ExceptionHandler.handle(e);
        }

    }


    public void update () {
        if ( log.isDebugEnabled() ) {
            log.debug("Updating job output from offset " + this.getCurrentOffset()); //$NON-NLS-1$
        }

        try {
            if ( this.failed ) {
                return;
            }
            String jobOutput = this.ssp.getService(JobInfoService.class).getJobOutput(this.getJobId(), this.getCurrentOffset());

            if ( log.isDebugEnabled() ) {
                log.debug("Appending " + jobOutput); //$NON-NLS-1$
            }

            this.append(jobOutput);
        }
        catch ( Exception e ) {
            this.failed = true;
            ExceptionHandler.handle(e);
        }

    }
}
