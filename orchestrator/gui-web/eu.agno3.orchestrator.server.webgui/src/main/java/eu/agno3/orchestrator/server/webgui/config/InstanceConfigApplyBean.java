/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 16, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo;
import eu.agno3.orchestrator.config.model.realm.InstanceStatus;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigApplyContext;
import eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.exceptions.ModelExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.InstanceStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "instanceConfigApplyBean" )
public class InstanceConfigApplyBean {

    @Inject
    private InstanceStateTracker instanceState;

    @Inject
    private AgentStateTracker agentState;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;


    private InstanceStructuralObject getInstance () {
        try {
            InstanceStructuralObject instance = (InstanceStructuralObject) this.structureContext.getSelectedAnchor();
            if ( this.structureContext.getSelectedAnchor() instanceof InstanceStructuralObject ) {
                return instance;
            }
            return this.structureContext.getSelectedInstance();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public boolean getCanApply () {
        return this.agentState.isAgentOnline(getInstance());
    }


    public boolean getHaveChanges () {
        InstanceStatus st = this.instanceState.getInstanceState(getInstance());
        if ( st == null ) {
            return false;
        }
        return statusHasChanges(st.getCompositeConfigurationState());
    }


    private static boolean statusHasChanges ( ConfigurationState cst ) {
        return cst == ConfigurationState.DEFAULTS_CHANGED || cst == ConfigurationState.UPDATE_AVAILABLE || cst == ConfigurationState.FAILED
                || cst == ConfigurationState.APPLYING;
    }


    public boolean getUnconfigured () {
        InstanceStatus st = this.instanceState.getInstanceState(getInstance());
        if ( st == null ) {
            return false;
        }
        return st.getCompositeConfigurationState() == ConfigurationState.UNCONFIGURED;
    }


    public String getMessage () {
        InstanceStatus st = this.instanceState.getInstanceState(getInstance());
        if ( st == null ) {
            return StringUtils.EMPTY;
        }

        if ( st.getCompositeConfigurationState() == ConfigurationState.FAILED ) {
            return GuiMessages.get("structure.instance.applyConfig.failed.msg"); //$NON-NLS-1$
        }
        else if ( st.getCompositeConfigurationState() == ConfigurationState.APPLYING ) {
            return GuiMessages.get("structure.instance.applyConfig.applying.msg"); //$NON-NLS-1$
        }
        else if ( statusHasChanges(st.getCompositeConfigurationState()) ) {
            return GuiMessages.get("structure.instance.applyConfig.msg"); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    public String getStyleClass () {
        InstanceStatus st = this.instanceState.getInstanceState(getInstance());
        if ( st == null ) {
            return StringUtils.EMPTY;
        }

        if ( st.getCompositeConfigurationState() == ConfigurationState.FAILED ) {
            return "have-changes failed"; //$NON-NLS-1$
        }
        else if ( st.getCompositeConfigurationState() == ConfigurationState.APPLYING ) {
            return "have-changes applying"; //$NON-NLS-1$
        }
        else if ( statusHasChanges(st.getCompositeConfigurationState()) ) {
            return "have-changes"; //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    public String goApply () {
        InstanceStructuralObject instance = getInstance();
        if ( instance == null ) {
            return null;
        }

        try {
            ConfigApplyContext ctx = this.ssp.getService(ConfigApplyService.class).preApplyInstanceConfigurations(instance, null);

            if ( ctx.getChallenges().isEmpty() ) {
                JobInfo applyJob = this.ssp.getService(ConfigApplyService.class)
                        .applyInstanceConfigurations(instance, ctx.getRevision(), new ConfigApplyInfo());
                if ( applyJob != null ) {
                    return "/config/apply.xhtml?faces-redirect=true&cid=&instance=" + instance.getId() + "&jobId=" + applyJob.getJobId(); //$NON-NLS-1$//$NON-NLS-2$
                }
            }

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("applyContext", ctx); //$NON-NLS-1$
            return "/structure/instance/applyConfig.xhtml?faces-redirect=true&cid=&instance=" + instance.getId(); //$NON-NLS-1$
        }
        catch (
            ModelObjectReferentialIntegrityException |
            ModelObjectNotFoundException e ) {
            ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SSV_FAILED), e);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String apply ( ConfigApplyContextBean ctx ) {
        try {
            InstanceStructuralObject instance = getInstance();
            if ( instance == null ) {
                return null;
            }
            if ( !ctx.handleChallenges() ) {
                return null;
            }
            JobInfo applyJob = this.ssp.getService(ConfigApplyService.class)
                    .applyInstanceConfigurations(instance, ctx.getRevision(), ctx.getApplyInfo());
            if ( applyJob != null ) {
                return "/config/apply.xhtml?faces-redirect=true&cid=&instance=" + instance.getId() + "&jobId=" + applyJob.getJobId(); //$NON-NLS-1$//$NON-NLS-2$
            }
        }
        catch (
            ModelObjectReferentialIntegrityException |
            ModelObjectNotFoundException |
            AgentDetachedException e ) {
            ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SSV_FAILED), e);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }

}
