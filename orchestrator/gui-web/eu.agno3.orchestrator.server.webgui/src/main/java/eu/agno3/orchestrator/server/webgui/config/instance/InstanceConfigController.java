/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.instance;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigApplyContextBean;
import eu.agno3.orchestrator.server.webgui.config.EditorUtil;
import eu.agno3.orchestrator.server.webgui.exceptions.ModelExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.jsf.view.stacking.DialogConstants;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 * 
 */
@Named ( "instanceConfigController" )
@ApplicationScoped
public class InstanceConfigController {

    /**
     * 
     */
    private static final String SERVER_VALIDATION_FAILED = "Server validation failed"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(InstanceConfigController.class);

    @Inject
    private AbstractConfigContextBean<ConfigurationObject, @Nullable ConfigurationObjectMutable> instanceContext;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private AgentStateTracker agentStateTracker;


    public String saveNoDialog () {
        try {
            if ( !this.instanceContext.validateConfiguration() ) {
                return null;
            }

            this.instanceContext.updateConfiguration();

            this.instanceContext.reset();
            this.instanceContext.refreshState();
            EditorUtil.resetRootEditor();
            RequestContext.getCurrentInstance().addCallbackParam("saved", true); //$NON-NLS-1$
            return null;
        }
        catch (
            ModelObjectValidationException |
            ModelObjectConflictException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug(SERVER_VALIDATION_FAILED, e);
            }
            ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SSV_FAILED), e);
            return null;
        }
        catch ( Exception e ) {
            return handleSaveError(e);
        }
    }


    public String cancelNoDialog () {
        this.instanceContext.reset();
        this.instanceContext.refreshState();
        EditorUtil.resetRootEditor();
        RequestContext.getCurrentInstance().addCallbackParam("saved", true); //$NON-NLS-1$
        return null;
    }


    public String save () {
        try {
            this.instanceContext.updateConfiguration();
            return DialogContext.closeDialog(Boolean.TRUE);
        }
        catch (
            ModelObjectValidationException |
            ModelObjectConflictException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug(SERVER_VALIDATION_FAILED, e);
            }
            ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SSV_FAILED), e);
        }
        catch ( Exception e ) {
            return handleSaveError(e);
        }
        return null;
    }


    public boolean shouldOpenSave () {
        log.debug("shouldOpenSave called"); //$NON-NLS-1$

        try {
            return this.instanceContext.validateConfiguration();
        }
        catch ( Exception e ) {
            log.debug("Config validation failed", e); //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get(GuiMessages.CONFIG_VALIDATE_ERROR), e.getMessage()));
            return false;
        }

    }


    /**
     * @param e
     * @return
     */
    private static String handleSaveError ( Exception e ) {
        log.warn("Failure while saving config", e); //$NON-NLS-1$
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get(GuiMessages.CONFIG_SAVE_ERROR), e.getMessage()));

        return null;
    }


    public boolean getCanApply () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.instanceContext.getCanApply() && this.getAgentOnline();
    }


    /**
     * @return whether the agent is online
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public boolean getAgentOnline () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        StructuralObject parent = this.structureContext.getParentForSelection();

        if ( ! ( parent instanceof InstanceStructuralObject ) ) {
            return false;
        }

        InstanceStructuralObject instance = (InstanceStructuralObject) parent;

        return this.agentStateTracker.isAgentOnline(instance);
    }


    public String saveAndApply ( ConfigApplyContextBean applyCtx ) {
        try {
            this.instanceContext.updateConfiguration();
        }
        catch (
            ModelObjectValidationException |
            ModelObjectConflictException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug(SERVER_VALIDATION_FAILED, e);
            }
            ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SSV_FAILED), e);
            return null;
        }
        catch ( Exception e ) {
            return handleSaveError(e);
        }

        return apply(applyCtx);
    }


    public String apply ( ConfigApplyContextBean applyCtx ) {
        try {
            if ( !applyCtx.handleChallenges() ) {
                return null;
            }
            JobInfo applyJob = this.instanceContext.applyConfiguration(applyCtx);
            if ( applyJob != null ) {
                return makeApplyJobDialogOutcome(applyJob);
            }
        }
        catch (
            ModelObjectValidationException |
            ModelObjectReferentialIntegrityException |
            ModelObjectNotFoundException |
            AgentDetachedException e ) {
            ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SSV_FAILED), e);
        }
        catch ( Exception e ) {
            return handleApplyError(e);
        }
        return null;
    }


    private static String makeApplyJobDialogOutcome ( JobInfo applyJob ) {
        return String.format(
            "/jobs/showJobDetailDialog.xhtml?faces-redirect=true&cid=&jobId=%s&%s=%s", //$NON-NLS-1$
            applyJob.getJobId(),
            DialogConstants.RETURN_TO_ATTR,
            DialogContext.getCurrentParent());
    }


    /**
     * @param e
     * @return
     */
    private static String handleApplyError ( Exception e ) {
        log.warn("Failure while applying config", e); //$NON-NLS-1$
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get(GuiMessages.CONFIG_APPLY_ERROR), e.getMessage()));

        return null;
    }


    public void applyReturn ( SelectEvent ev ) {
        log.debug("Found apply dialog return"); //$NON-NLS-1$

        try {
            this.structureContext.refreshSelected();
        }
        catch ( Exception e ) {
            log.warn("Failed to refresh structural object", e); //$NON-NLS-1$
        }

        this.instanceContext.reset();
        this.instanceContext.refreshState();
        EditorUtil.resetRootEditor();
    }


    public void savedReturn ( SelectEvent ev ) {
        if ( ev == null || ev.getObject() == null ) {
            EditorUtil.resetRootEditor();
            return;
        }
        log.debug("Found save dialog return"); //$NON-NLS-1$
        this.instanceContext.reset();
        this.instanceContext.refreshState();
        EditorUtil.resetRootEditor();

        RequestContext.getCurrentInstance().addCallbackParam("saved", true); //$NON-NLS-1$
    }

}
