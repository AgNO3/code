/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.update;


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.server.webgui.util.i18n.I18NUtil;
import eu.agno3.orchestrator.system.update.InstanceUpdateStatus;
import eu.agno3.orchestrator.system.update.UpdateDescriptor;
import eu.agno3.orchestrator.system.update.UpdateState;
import eu.agno3.orchestrator.system.update.service.AgentUpdateService;


/**
 * @author mbechler
 *
 */
@Named ( "updateContextBean" )
@ViewScoped
public class UpdateContextBean implements Serializable {

    private static final Logger log = Logger.getLogger(UpdateContextBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 2034288183221300961L;

    private static final List<String> AVAILABLE_STREAMS = Arrays.asList(
        "RELEASE", //$NON-NLS-1$
        "TESTING", //$NON-NLS-1$
        "SNAPSHOT"); //$NON-NLS-1$

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private ServerServiceProvider ssp;

    private InstanceUpdateStatus status;

    private String overrideStream;

    private boolean statusLoaded;


    /**
     * @return the status
     */
    public synchronized InstanceUpdateStatus getStatus () {
        if ( !this.statusLoaded ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Loading from stream " + this.overrideStream); //$NON-NLS-1$
                }
                this.statusLoaded = true;
                this.status = this.ssp.getService(AgentUpdateService.class)
                        .getUpdateStatus(this.structureContext.getSelectedInstance(), this.overrideStream);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
            }
        }
        return this.status;
    }


    public List<String> getAvailableStreams () {
        return AVAILABLE_STREAMS;
    }


    /**
     * @return the overrideStream
     */
    public String getOverrideStream () {
        if ( this.overrideStream == null ) {
            InstanceUpdateStatus state = this.getStatus();
            if ( state == null ) {
                return null;
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Current stream is " + state.getCurrentStream()); //$NON-NLS-1$
            }
            return state.getCurrentStream();
        }
        return this.overrideStream;
    }


    /**
     * @param overrideStream
     *            the overrideStream to set
     */
    public void setOverrideStream ( String overrideStream ) {
        this.overrideStream = overrideStream;
        this.statusLoaded = false;
        this.status = null;
        getStatus();
    }


    public String getDescriptorStream () {
        InstanceUpdateStatus state = this.getStatus();
        if ( state != null ) {
            return state.getDescriptorStream();
        }
        return null;
    }


    public UpdateState getUpdateState () {
        InstanceUpdateStatus state = getStatus();
        if ( state != null && state.getCurrentSequence() != null ) {
            return state.getState();
        }
        return UpdateState.UNKNOWN;
    }


    public String getTranslatedUpdateState () {
        ResourceBundle rb = ResourceBundle.getBundle(
            GuiMessages.GUI_MESSAGES_BASE,
            FacesContext.getCurrentInstance().getViewRoot().getLocale(),
            Thread.currentThread().getContextClassLoader());
        return I18NUtil.translateEnumValue(rb, UpdateState.class, getUpdateState());
    }


    public boolean getRebootIndicated () {
        InstanceUpdateStatus state = getStatus();
        if ( state == null ) {
            return false;
        }
        return state.getRebootIndicated();
    }


    public String getUpdateStateStyle () {
        switch ( getUpdateState() ) {
        case NEEDSUPDATE:
            return "bg-color-warn"; //$NON-NLS-1$
        case UNKNOWN:
            return "bg-color-failure"; //$NON-NLS-1$
        case UPTODATE:
            return "bg-color-ok"; //$NON-NLS-1$
        default:
            return "bg-color-failure"; //$NON-NLS-1$
        }
    }


    public Long getRevertSequence () {
        InstanceUpdateStatus state = this.getStatus();
        if ( state != null ) {
            return state.getRevertSequence();
        }
        return null;
    }


    public String getRevertStream () {
        InstanceUpdateStatus state = this.getStatus();
        if ( state != null ) {
            return state.getRevertStream();
        }
        return null;
    }


    public DateTime getRevertTimestamp () {
        InstanceUpdateStatus state = this.getStatus();
        if ( state != null ) {
            return state.getRevertTimestamp();
        }
        return null;
    }


    public long getCurrentSequence () {
        Long currentSequence = getStatus().getCurrentSequence();
        if ( currentSequence == null ) {
            return 0;
        }
        return currentSequence;
    }


    public String getCurrentStream () {
        InstanceUpdateStatus state = this.getStatus();
        if ( state != null ) {
            return state.getCurrentStream();
        }
        return null;
    }


    public DateTime getCurrentInstallDate () {
        InstanceUpdateStatus state = this.getStatus();
        if ( state != null ) {
            return state.getCurrentInstallDate();
        }
        return null;
    }


    public UpdateDescriptor getLatestDescriptor () {
        InstanceUpdateStatus state = getStatus();
        if ( state == null ) {
            return null;
        }
        return state.getLatestDescriptor();
    }


    public synchronized String refresh () {
        this.statusLoaded = false;
        this.status = null;
        getStatus();
        return null;
    }


    public String install () {
        log.info("Install updates"); //$NON-NLS-1$
        UpdateDescriptor latestDescriptor = this.getLatestDescriptor();

        if ( latestDescriptor == null ) {
            log.warn("Descriptor is NULL"); //$NON-NLS-1$
            return null;
        }
        try {
            InstanceStructuralObject instance = this.structureContext.getSelectedInstance();
            if ( instance == null ) {
                return null;
            }
            JobInfo i = this.ssp.getService(AgentUpdateService.class).installUpdates(instance, getOverrideStream(), latestDescriptor.getSequence());
            return "/update/install.xhtml?faces-redirect=true&instance=" + instance.getId() + "&jobId=" + i.getJobId(); //$NON-NLS-1$//$NON-NLS-2$
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;

    }


    public String revert () {
        InstanceUpdateStatus state = this.status;

        if ( state == null ) {
            return null;
        }
        try {
            InstanceStructuralObject instance = this.structureContext.getSelectedInstance();
            if ( instance == null ) {
                return null;
            }
            JobInfo i = this.ssp.getService(AgentUpdateService.class).revert(instance, getRevertStream(), getRevertSequence());
            return "/update/install.xhtml?faces-redirect=true&instance=" + instance.getId() + "&jobId=" + i.getJobId(); //$NON-NLS-1$//$NON-NLS-2$
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }
}
