/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.template;


import java.util.UUID;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.extensions.validator.beanval.annotation.BeanValidation;
import org.apache.myfaces.extensions.validator.beanval.annotation.ModelValidation;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.versioning.RevisionProvider;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigurationEditContext;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationContextService;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.config.model.validation.Abstract;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigApplyContextBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigUtil;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
@Named ( "templateConfigContext" )
@ConversationScoped
@BeanValidation ( useGroups = {
    Default.class
}, modelValidation = @ModelValidation ( isActive = true ) )
public class TemplateConfigContextBean extends AbstractConfigContextBean<ConfigurationObject, @Nullable ConfigurationObject> {

    private static final Logger log = Logger.getLogger(TemplateConfigContextBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 761952867847578157L;

    @Inject
    private StructureViewContextBean structureContext;

    private String objectType;

    private UUID editId;

    private StructuralObject cachedAnchor;

    private boolean isNew;


    /**
     * @return the objectType
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @Override
    public String getObjectTypeName () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.isContextLoaded() && this.getCurrent() != null ) {
            return ConfigUtil.getObjectTypeName(this.getCurrent());
        }
        return this.objectType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getInnerEditorDialogTemplate()
     */
    @Override
    public String getInnerEditorDialogTemplate () {
        return "/config/editInnerTemplate.dialog.xhtml"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getTestTemplate()
     */
    @Override
    public String getTestTemplate () {
        return "/config/testTemplate.xhtml"; //$NON-NLS-1$
    }


    /**
     * @param objectType
     *            the objectType to set
     */
    public void setObjectTypeName ( String objectType ) {
        this.objectType = objectType;
    }


    /**
     * @return the editId
     */
    public UUID getEditId () {
        return this.editId;
    }


    /**
     * @param editId
     *            the editId to set
     */
    public void setEditId ( UUID editId ) {
        this.editId = editId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getRevisionProvider()
     */
    @Override
    public RevisionProvider getRevisionProvider () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getAbstract()
     */
    @Override
    public boolean getAbstract () {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getAnchor()
     */
    @Override
    public StructuralObject getAnchor () {

        try {
            if ( this.editId != null ) {
                if ( this.cachedAnchor == null ) {
                    @Nullable
                    ConfigurationObject current = this.getCurrent();
                    if ( current != null ) {
                        this.cachedAnchor = this.getSsp().getService(ConfigurationService.class).getAnchor(current);
                    }
                }
                return this.cachedAnchor;
            }

            return this.structureContext.getSelectedObject();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#checkContext()
     */
    @Override
    protected void checkContext () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.editId != null ) {
            return;
        }

        if ( this.getAnchor() instanceof GroupStructuralObject || this.getAnchor() instanceof InstanceStructuralObject ) {
            return;
        }

        throw new ModelServiceException("Neither a configuration nor a template anchor is selected"); //$NON-NLS-1$
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
        @Nullable
        ConfigurationObject current = this.getCurrent();

        if ( current == null ) {
            return;
        }

        if ( ( this.isNew || !isInner() ) && StringUtils.isBlank(current.getName()) && StringUtils.isBlank(current.getDisplayName()) ) {
            throw new ModelServiceException(GuiMessages.get(GuiMessages.TEMPLATE_NAME_REQUIRED));
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Updating config " + current); //$NON-NLS-1$
            log.debug("Anchor is " + this.getAnchor()); //$NON-NLS-1$
        }
        ConfigurationObject updated = null;
        if ( !this.isNew ) {
            updated = this.getSsp().getService(ConfigurationService.class).update(current, this.getUpdateInfo());
        }
        else {
            updated = this.getSsp().getService(ConfigurationService.class)
                    .create(this.structureContext.getSelectedObject(), current, this.getUpdateInfo());
            this.isNew = false;
        }
        this.clearTemplateCache();
        this.getContext().setCurrent(updated);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#getValidationGroups()
     */
    @Override
    protected Class<?>[] getValidationGroups () {
        return new Class<?>[] {
            Abstract.class
        };
    }


    @Override
    protected JobInfo internalApplyConfiguration ( ConfigApplyContextBean applyCtx ) {
        return null;
    }


    public boolean isInner () {
        ConfigurationEditContext<ConfigurationObject, @Nullable ConfigurationObject> ctx = getContext();
        if ( ctx != null ) {
            return ctx.isInner();
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigContextBean#validateConfiguration()
     */
    @Override
    public boolean validateConfiguration () throws ModelServiceException, GuiWebServiceException, ModelObjectException {
        ConfigurationObject config = this.getCurrent();

        if ( ( this.isNew || !isInner() ) && config != null && StringUtils.isBlank(config.getName())
                && StringUtils.isBlank(config.getDisplayName()) ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get(GuiMessages.TEMPLATE_NAME_REQUIRED), StringUtils.EMPTY));
            return false;
        }

        return super.validateConfiguration();
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
        if ( this.editId != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Fetching configuration with id " + this.editId); //$NON-NLS-1$
            }
            return this.getSsp().getService(ConfigurationContextService.class).getForEditing(this.editId);
        }
        else if ( this.objectType != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Creating empty configuration for type " + this.objectType); //$NON-NLS-1$
            }
            this.isNew = true;
            return this.getSsp().getService(ConfigurationContextService.class)
                    .newForEditing(this.structureContext.getSelectedObject(), this.objectType);
        }
        throw new ModelServiceException("Neither a id nor a type is selected"); //$NON-NLS-1$
    }

}
