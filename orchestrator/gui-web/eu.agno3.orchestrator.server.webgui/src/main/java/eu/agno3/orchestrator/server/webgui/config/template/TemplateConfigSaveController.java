/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.template;


import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.EditorUtil;
import eu.agno3.orchestrator.server.webgui.config.TemplateCacheBean;
import eu.agno3.orchestrator.server.webgui.exceptions.ModelExceptionHandler;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 * 
 */
@Named ( "templateConfigSaveController" )
@ApplicationScoped
public class TemplateConfigSaveController {

    private static final String CONFLICTING_EDIT = "Conflicting edit"; //$NON-NLS-1$
    private static final String SERVER_VALIDATION_FAILED = "Server validation failed"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(TemplateConfigSaveController.class);

    @Inject
    private TemplateConfigContextBean templateContext;

    @Inject
    private TemplateCacheBean templateCache;


    public String saveNewNoDialog () {
        ConfigurationObject saved = doSaveNoDialog();
        if ( saved != null ) {
            redirectToEditView(saved);
        }
        return null;
    }


    /**
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     * @throws ModelObjectException
     * @throws ModelObjectValidationException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectConflictException
     */
    private ConfigurationObject doSaveNoDialog () {
        ConfigurationObject res = null;
        try {
            if ( !this.templateContext.validateConfiguration() ) {
                return null;
            }

            if ( !this.templateContext.updateConfiguration() ) {
                return null;
            }

            res = this.templateContext.getCurrent();
            this.templateCache.flush();
            this.templateContext.reset();
            this.templateContext.refreshState();
            return res;
        }
        catch ( ModelObjectConflictException e ) {
            log.debug(CONFLICTING_EDIT, e);
            serverConflict(e);
        }
        catch ( ModelObjectValidationException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug(SERVER_VALIDATION_FAILED, e);
            }
            ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SSV_FAILED), e);
        }
        catch ( Exception e ) {
            handleSaveError(e);
        }
        return null;
    }


    public String saveNoDialog () {
        ConfigurationObject saved = doSaveNoDialog();
        if ( saved != null ) {
            EditorUtil.resetRootEditor();
            RequestContext.getCurrentInstance().addCallbackParam("saved", true); //$NON-NLS-1$
        }
        return null;
    }


    public String saveNoDialogAndReturn () {
        ConfigurationObject saved = doSaveNoDialog();
        if ( saved != null ) {
            return DialogContext.closeDialog(Boolean.TRUE);
        }
        return null;
    }


    public String saveNew () {
        try {
            if ( this.templateContext.updateConfiguration() ) {
                this.templateCache.flush();
                return DialogContext.closeDialog(Boolean.TRUE);
            }
        }
        catch ( ModelObjectValidationException e ) {
            log.debug(SERVER_VALIDATION_FAILED, e);
            serverValidationFailed(e);
        }
        catch ( ModelObjectConflictException e ) {
            log.debug(CONFLICTING_EDIT, e);
            serverConflict(e);
        }
        catch ( Exception e ) {
            return handleSaveError(e);
        }

        return null;
    }


    public String save () {
        try {
            if ( this.templateContext.updateConfiguration() ) {
                this.templateCache.flush();
                return DialogContext.closeDialog(Boolean.TRUE);
            }

        }
        catch ( ModelObjectValidationException e ) {
            log.debug(SERVER_VALIDATION_FAILED, e);
            serverValidationFailed(e);
        }
        catch ( ModelObjectConflictException e ) {
            log.debug(CONFLICTING_EDIT, e);
            serverConflict(e);
        }
        catch ( Exception e ) {
            return handleSaveError(e);
        }
        return null;

    }


    public boolean shouldOpenSave () {
        log.debug("shouldOpenSave called"); //$NON-NLS-1$

        try {
            return this.templateContext.validateConfiguration();
        }
        catch ( Exception e ) {
            log.warn("Validation failed", e); //$NON-NLS-1$
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


    public void savedReturnInDialog ( SelectEvent ev ) {
        if ( ev == null || ev.getObject() == null ) {
            EditorUtil.resetRootEditor();
            return;
        }
        this.templateContext.endConversation();
        FacesContext context = FacesContext.getCurrentInstance();
        NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
        navigationHandler.handleNavigation(context, null, DialogContext.closeDialog(null));
        RequestContext.getCurrentInstance().addCallbackParam("saved", true); //$NON-NLS-1$
    }


    public void savedReturn ( SelectEvent ev ) {

        @Nullable
        ConfigurationObject current = this.templateContext.getCurrent();

        if ( current == null || ev == null || ev.getObject() == null ) {
            EditorUtil.resetRootEditor();
            return;
        }
        log.debug("Found save dialog return"); //$NON-NLS-1$

        RequestContext.getCurrentInstance().addCallbackParam("saved", true); //$NON-NLS-1$

        redirectToEditView(current);

    }


    /**
     * @param saved
     * @param current
     */
    private void redirectToEditView ( ConfigurationObject saved ) {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            String redirectUrl = ec.encodeActionURL(String.format(
                "/config/template/edit.xhtml?anchor=%s&object=%s&cid=", //$NON-NLS-1$
                this.templateContext.getAnchor().getId(),
                saved.getId()));
            ServletContext ctx = (ServletContext) ec.getContext();
            this.templateContext.endConversation();
            try {
                ec.redirect(ctx.getContextPath() + redirectUrl);
            }
            catch ( IOException e ) {
                log.warn("Failed to redirect to clean view", e); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.warn("Failed to build redirect URL", e); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     */
    private static void serverConflict ( ModelObjectConflictException e ) {
        ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SAVE_CONFLICT), e);
    }


    private static void serverValidationFailed ( ModelObjectValidationException e ) {
        ModelExceptionHandler.handleException(GuiMessages.get(GuiMessages.CONFIG_SSV_FAILED), e);
    }


    public String cancelDialog () {
        return DialogContext.closeDialog(null);
    }
}
