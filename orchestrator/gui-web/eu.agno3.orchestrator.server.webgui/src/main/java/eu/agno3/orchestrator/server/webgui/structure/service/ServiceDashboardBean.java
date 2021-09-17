/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.03.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.service;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.ServiceStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;


/**
 * @author mbechler
 *
 */
@Named ( "serviceDashboardBean" )
@ViewScoped
public class ServiceDashboardBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7427939747217128082L;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private ServiceStateTracker serviceState;

    @Inject
    private AgentStateTracker agentState;

    @Inject
    private StructureCacheBean structureCache;


    public void refresh () {
        try {
            this.serviceState.forceRefresh(this.structureContext.getSelectedService());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
    }


    public ServiceRuntimeStatus getRuntimeState () {
        try {
            return this.serviceState.getServiceState(this.structureContext.getSelectedService());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return ServiceRuntimeStatus.UNKNOWN;
        }
    }


    public ConfigurationState getConfigState () {
        try {
            return this.structureContext.getSelectedService().getState();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return ConfigurationState.UNKNOWN;
        }
    }


    public boolean isAgentOnline () {
        try {
            StructuralObject parentFor = this.structureCache.getParentFor(this.structureContext.getSelectedService());
            if ( parentFor instanceof InstanceStructuralObject ) {
                return this.agentState.isAgentOnline((InstanceStructuralObject) parentFor);
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return false;
    }


    public String getTranslatedRuntimeState () {
        return GuiMessages.get("service.runtimeState." + this.getRuntimeState().name()); //$NON-NLS-1$
    }


    public String getRuntimeStateIcon () {
        switch ( getRuntimeState() ) {
        case ACTIVE:
            return "ui-icon-check"; //$NON-NLS-1$
        case DISABLED:
            return "ui-icon-cancel"; //$NON-NLS-1$
        case ERROR:
        case UNKNOWN:
            return "ui-icon-alert"; //$NON-NLS-1$
        case TRANSIENT:
            return "ui-icon-arrowrefresh-1-e"; //$NON-NLS-1$
        case WARNING:
            return "ui-icon-notice"; //$NON-NLS-1$
        default:
            return "ui-icon-blank"; //$NON-NLS-1$
        }
    }


    public boolean canActivate () {
        ConfigurationState configState = getConfigState();
        return configState != ConfigurationState.UNKNOWN && configState != ConfigurationState.UNCONFIGURED
                && getRuntimeState() == ServiceRuntimeStatus.DISABLED;
    }


    public boolean canDeactivate () {
        ConfigurationState configState = getConfigState();
        return configState != ConfigurationState.UNKNOWN && configState != ConfigurationState.UNCONFIGURED
                && getRuntimeState() != ServiceRuntimeStatus.DISABLED;
    }


    public boolean canDelete () {
        return getRuntimeState() == ServiceRuntimeStatus.DISABLED || getConfigState() == ConfigurationState.UNCONFIGURED
                || getConfigState() == ConfigurationState.UNKNOWN;
    }

}
