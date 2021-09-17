/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.instance;


import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.extensions.validator.beanval.annotation.BeanValidation;
import org.apache.myfaces.extensions.validator.beanval.annotation.ModelValidation;
import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.context.RequestContext;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.versioning.RevisionProvider;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigurationEditContext;
import eu.agno3.orchestrator.config.model.realm.service.ConfigApplyService;
import eu.agno3.orchestrator.config.model.realm.service.ServiceService;
import eu.agno3.orchestrator.config.model.validation.Abstract;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigApplyContextBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigUtil;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.InstanceStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
@Named ( "instanceConfigContext" )
@ConversationScoped
@BeanValidation ( useGroups = {
    Default.class
}, modelValidation = @ModelValidation ( isActive = true ) )
public class InstanceConfigContextBean extends AbstractConfigContextBean<ConfigurationObject, @Nullable ConfigurationObjectMutable> {

    private static final Logger log = Logger.getLogger(InstanceConfigContextBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 4338591484487573077L;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private InstanceStateTracker instanceState;

    @Inject
    private ServerServiceProvider ssp;


    @Override
    protected void checkContext () throws ModelServiceException {
        if ( !this.structureContext.isServiceSelected() ) {
            throw new ModelServiceException("No service is selected"); //$NON-NLS-1$
        }
    }


    public String getServiceTypeName () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( !this.structureContext.isServiceSelected() ) {
            return null;
        }
        return this.structureContext.getSelectedService().getServiceType();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getInnerEditorDialogTemplate()
     */
    @Override
    public String getInnerEditorDialogTemplate () {
        return "/config/editInnerInstance.dialog.xhtml"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getTestTemplate()
     */
    @Override
    public String getTestTemplate () {
        return "/config/testInstance.xhtml"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getObjectTypeName()
     */
    @Override
    public String getObjectTypeName () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.getCurrent() != null ) {
            return ConfigUtil.getObjectTypeName(this.getCurrent());
        }
        return null;
    }


    @Override
    public ServiceStructuralObject getAnchor () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.structureContext.getSelectedService();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getAbstract()
     */
    @Override
    public boolean getAbstract () {
        return false;
    }


    public String getStyleClass () {
        ConfigurationEditContext<ConfigurationObject, @Nullable ConfigurationObjectMutable> ctx = getContext();
        if ( ctx == null ) {
            return StringUtils.EMPTY;
        }

        if ( ctx.getConfigurationState() == ConfigurationState.UNCONFIGURED ) {
            return "dirty-initial"; //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#fetchContext()
     */
    @Override
    protected ConfigurationEditContext<ConfigurationObject, ConfigurationObject> fetchContext ()
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationEditContext<ConfigurationObject, ConfigurationObject> got = this.ssp.getService(ServiceService.class)
                .getEditContext(this.structureContext.getSelectedService());

        return got;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelObjectConflictException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#internalDoUpdate()
     */
    @Override
    protected void internalDoUpdate () throws ModelServiceException, ModelObjectValidationException, ModelObjectNotFoundException,
            GuiWebServiceException, ModelObjectConflictException {
        ConfigurationObjectMutable updatedConfig = this.ssp.getService(ServiceService.class)
                .updateServiceConfiguration(this.structureContext.getSelectedService(), this.getCurrent(), this.getUpdateInfo());
        this.getContext().setCurrent(updatedConfig);
        this.clearTemplateCache();

        StructuralObject anchor = this.structureContext.getSelectedAnchor();
        if ( anchor instanceof InstanceStructuralObject ) {
            this.instanceState.forceRefresh((InstanceStructuralObject) anchor);
            RequestContext.getCurrentInstance().update("menuForm"); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#refreshState()
     */
    @Override
    public void refreshState () {
        try {
            log.debug("Refreshing config state"); //$NON-NLS-1$
            this.setOverrideState(this.ssp.getService(ServiceService.class).getConfigState(getAnchor()));
            if ( log.isDebugEnabled() ) {
                log.debug("New state is " + this.getState()); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
    }


    @Override
    public RevisionProvider getRevisionProvider () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * 
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#internalApplyConfiguration()
     */
    @Override
    protected JobInfo internalApplyConfiguration ( ConfigApplyContextBean applyCtx )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException, ModelObjectReferentialIntegrityException,
            AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {
        @Nullable
        ConfigurationObjectMutable current = this.getCurrent();

        if ( current == null ) {
            throw new ModelServiceException("No config available"); //$NON-NLS-1$
        }

        if ( log.isInfoEnabled() ) {
            log.info("Applying configuration " + current); //$NON-NLS-1$
        }

        try {
            JobInfo ji = this.ssp.getService(ConfigApplyService.class).applyServiceConfiguration(
                this.structureContext.getSelectedService(),
                applyCtx.getRevision() != null ? applyCtx.getRevision() : getRevision(),
                applyCtx.getApplyInfo());
            this.setOverrideState(ConfigurationState.APPLYING);
            return ji;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            setOverrideState(ConfigurationState.FAILED);
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getValidationGroups()
     */
    @Override
    protected Class<?>[] getValidationGroups () {
        if ( this.getAbstract() ) {
            return new Class<?>[] {
                Abstract.class
            };
        }
        return new Class<?>[] {
            Instance.class
        };
    }

}
