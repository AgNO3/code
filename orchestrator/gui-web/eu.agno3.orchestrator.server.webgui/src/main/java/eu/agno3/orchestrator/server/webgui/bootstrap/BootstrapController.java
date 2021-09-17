/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.bootstrap;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;

import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.bootstrap.service.BootstrapService;
import eu.agno3.orchestrator.config.hostconfig.HostConfigurationMutable;
import eu.agno3.orchestrator.config.hostconfig.network.AddressConfigurationTypeV4;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfigurationMutable;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.jobs.JobDetailContextBean;
import eu.agno3.runtime.jsf.types.uri.URIUtil;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "bootstrapController" )
public class BootstrapController {

    private static final Logger log = Logger.getLogger(BootstrapController.class);

    @Inject
    private BootstrapHostConfigContextBean hcContext;

    @Inject
    private BootstrapOrchConfigContextBean orchContext;

    @Inject
    private BootstrapContextProvider bcp;

    @Inject
    private BootstrapExtraContext extraContext;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private JobDetailContextBean jobDetailContext;

    @Inject
    @Any
    private Instance<BootstrapControllerPlugin> plugins;

    private transient BootstrapControllerPlugin controllerPlugin;


    public boolean shouldShowNext () {
        return !"complete".equals(this.extraContext.getStep()); //$NON-NLS-1$
    }


    public String getNextTitle () {
        if ( this.extraContext.getStep().equals(getLastStep()) ) {
            return GuiMessages.get("installer.complete"); //$NON-NLS-1$
        }

        return GuiMessages.get("installer.next"); //$NON-NLS-1$
    }


    /**
     * @return the last step before the completion page
     */
    private String getLastStep () {
        BootstrapControllerPlugin plugin = getPlugin();
        if ( plugin != null ) {
            String last = plugin.getLastStep();
            if ( last != null ) {
                return last;
            }
        }
        return "hc_configureStorage"; //$NON-NLS-1$
    }


    public boolean shouldShowBack () {
        if ( "changeAdminPassword".equals(this.extraContext.getStep()) ) { //$NON-NLS-1$
            return false;
        }

        if ( "complete".equals(this.extraContext.getStep()) ) { //$NON-NLS-1$
            return this.extraContext.isFailed();
        }
        return true;
    }


    public String getPluginSource () {
        BootstrapControllerPlugin plugin = getPlugin();
        if ( plugin == null || plugin.getIncludeTemplate() == null ) {
            return "/tpl/bootstrap/standalone.xhtml"; //$NON-NLS-1$
        }
        return plugin.getIncludeTemplate();
    }


    public BootstrapControllerPlugin getPlugin () {
        if ( this.controllerPlugin != null ) {
            return this.controllerPlugin;
        }
        BootstrapContext context = this.bcp.getContext();
        if ( context == null || StringUtils.isEmpty(context.getType()) ) {
            return null;
        }

        Iterator<BootstrapControllerPlugin> iterator = this.plugins.iterator();
        while ( iterator.hasNext() ) {
            BootstrapControllerPlugin bp = iterator.next();
            if ( context.getType().equals(bp.getId()) ) {
                this.controllerPlugin = bp;
                return bp;
            }
        }
        return null;
    }


    public void checkContext ( ComponentSystemEvent ev ) {
        this.bcp.reset();
        if ( !this.extraContext.isCompleted() && this.bcp.getContext() == null ) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get("installer.not.available"), StringUtils.EMPTY)); //$NON-NLS-1$
        }
    }


    public boolean hasContext () {
        if ( this.extraContext.isCompleted() ) {
            return true;
        }
        return this.bcp.getContext() != null;
    }


    public String flowListener ( FlowEvent ev ) {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Wizard %s -> %s", ev.getOldStep(), ev.getNewStep())); //$NON-NLS-1$
        }

        RequestContext.getCurrentInstance().update("installWizardForm:wizardButtons"); //$NON-NLS-1$
        RequestContext.getCurrentInstance().execute("PF('block').hide();"); //$NON-NLS-1$
        try {

            if ( ev.getOldStep().equals("changeAdminPassword") && !this.extraContext.validateExtra() ) { //$NON-NLS-1$
                return ev.getOldStep();
            }

            if ( ev.getOldStep().startsWith("hc_") && !this.hcContext.validateConfiguration() ) { //$NON-NLS-1$
                return ev.getOldStep();
            }

            if ( ev.getOldStep().startsWith("orch_") && !this.orchContext.validateConfiguration() ) { //$NON-NLS-1$
                return ev.getOldStep();
            }

            if ( "complete".equals(ev.getNewStep()) ) { //$NON-NLS-1$
                if ( !completeBootstrap() ) {
                    return ev.getOldStep();
                }
            }

            this.extraContext.setStep(ev.getNewStep());
            return ev.getNewStep();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return ev.getOldStep();
        }
    }


    /**
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectValidationException
     * @throws ModelObjectConflictException
     * 
     */
    private boolean completeBootstrap () throws ModelObjectException, ModelServiceException, GuiWebServiceException {
        log.debug("Completing bootstrap process"); //$NON-NLS-1$

        this.extraContext.setFailed(false);

        if ( this.ssp.getService(BootstrapService.class).getBootstrapContext() == null ) {
            this.bcp.reset();
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("/"); //$NON-NLS-1$
            }
            catch ( IOException e ) {
                log.warn("Failed to redirect", e); //$NON-NLS-1$
            }
        }

        this.extraContext.setCompleted(true);
        try {

            BootstrapContext context = this.bcp.getContext();
            @Nullable
            HostConfigurationMutable hc = this.hcContext.getCurrent();
            @Nullable
            OrchestratorConfigurationMutable oc = this.orchContext.getCurrent();
            context.setHostConfig(hc);
            context.setOrchConfig(oc);
            context.setChangeAdminPassword(this.extraContext.getAdminPassword());

            determineGuiLocation(hc, oc);

            BootstrapControllerPlugin plugin = this.getPlugin();
            if ( plugin != null ) {
                plugin.contributeContext(context);
            }

            JobInfo job = this.ssp.getService(BootstrapService.class).completeBootstrap(context);

            this.jobDetailContext.setJobId(job.getJobId());
            this.extraContext.setCompleted(true);
            this.bcp.reset();
            return true;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            this.extraContext.setCompleted(false);
            return false;
        }
    }


    /**
     * @param hc
     * @param sc
     */
    private void determineGuiLocation ( @Nullable HostConfigurationMutable hc, @Nullable OrchestratorConfigurationMutable sc ) {
        try {
            URI currentBase = new URI(URIUtil.getCurrentBaseUriWithTrailingSlash());

            if ( hc != null && sc != null ) {
                Set<InterfaceEntry> intf = hc.getNetworkConfiguration().getInterfaceConfiguration().getInterfaces();
                if ( !intf.isEmpty() ) {
                    InterfaceEntry ie = intf.iterator().next();

                    if ( ie.getV4AddressConfigurationType() == AddressConfigurationTypeV4.STATIC && !ie.getStaticAddresses().isEmpty() ) {
                        String newHost = ie.getStaticAddresses().iterator().next().getAddress().toString();
                        currentBase = updateHost(currentBase, newHost);
                    }
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Updating gui uri to " + currentBase); //$NON-NLS-1$
            }
            this.extraContext.setGuiUri(currentBase.toASCIIString());
            RequestContext.getCurrentInstance().update("installWizardForm:customPageBlockMessage"); //$NON-NLS-1$
        }
        catch (
            URISyntaxException |
            IllegalArgumentException e ) {
            log.error("Failed to determine new GUI uri", e); //$NON-NLS-1$
        }
    }


    /**
     * @param currentBase
     * @param newHost
     * @return
     * @throws URISyntaxException
     */
    URI updateHost ( URI currentBase, String newHost ) throws URISyntaxException {
        return new URI(
            currentBase.getScheme(),
            currentBase.getUserInfo(),
            newHost,
            currentBase.getPort(),
            currentBase.getPath(),
            currentBase.getQuery(),
            currentBase.getFragment());
    }


    public void bootstrapFailed ( ActionEvent ev ) {
        this.extraContext.setFailed(true);
        this.extraContext.setCompleted(false);
        this.hcContext.reset();
        this.orchContext.reset();
    }


    /**
     * @param ev
     * 
     */
    public void resetContext ( ActionEvent ev ) {
        this.extraContext.setCompleted(true);
        log.debug("Resetting context"); //$NON-NLS-1$
        this.hcContext.reset();
        this.orchContext.reset();
        this.bcp.reset();
    }
}
