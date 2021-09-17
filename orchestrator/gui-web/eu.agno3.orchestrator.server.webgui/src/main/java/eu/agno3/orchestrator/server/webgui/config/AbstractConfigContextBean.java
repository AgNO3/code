/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.myfaces.component.visit.FullVisitContext;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;
import eu.agno3.orchestrator.config.model.realm.context.ConfigurationEditContext;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.config.model.realm.service.InheritanceService;
import eu.agno3.orchestrator.config.model.realm.service.ValidationService;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.components.ViolationAddingVisitor;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.prefs.UserPreferencesBean;


/**
 * @author mbechler
 * @param <T>
 * @param <TMutable>
 * 
 */
public abstract class AbstractConfigContextBean <T extends ConfigurationObject, @Nullable TMutable extends T> implements ConfigContext<T, TMutable> {

    /**
     * 
     */
    private static final long serialVersionUID = 8944471313531874806L;

    private static final Logger log = Logger.getLogger(AbstractConfigContextBean.class);

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private ConfigLocalizationProvider configLocalizer;

    @Inject
    private ViolationMessageBuilder violationMessageBuilder;

    @Inject
    private TemplateCacheBean tplCache;

    @Inject
    private EffectiveConfigCacheBean configCache;

    @Inject
    private Conversation conversation;

    @Inject
    private UserPreferencesBean userPreferences;

    private Integer detailLevel;


    @PostConstruct
    protected void init () {
        this.initConversation();
    }


    public void initConversation () {
        log.debug("Called initConverstation"); //$NON-NLS-1$
        if ( !FacesContext.getCurrentInstance().isPostback() && this.conversation.isTransient() ) {
            this.conversation.begin();
            if ( log.isDebugEnabled() ) {
                log.debug("Started conversation " + this.conversation.getId()); //$NON-NLS-1$
            }
        }
    }


    public void endConversation () {
        log.debug("Called endConverstation"); //$NON-NLS-1$
        if ( !this.conversation.isTransient() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Ending conversation " + this.conversation.getId()); //$NON-NLS-1$
            }
            this.conversation.end();
        }
    }


    @Override
    public boolean isShowDefaultReset () {
        return true;
    }


    /**
     * @return the conversation
     */
    @Override
    public Conversation getConversation () {
        return this.conversation;
    }

    private ConfigurationEditContext<T, TMutable> context;
    private boolean contextLoaded = false;

    private ConfigUpdateInfo updateInfo = new ConfigUpdateInfo();

    private List<ViolationEntry> savedViolations = new ArrayList<>();

    private ConfigurationState overrideState;


    public void reset () {
        this.contextLoaded = false;
        this.context = null;
        this.updateInfo = new ConfigUpdateInfo();
        this.savedViolations.clear();
    }


    /**
     * @return the savedViolations
     */
    public List<ViolationEntry> getSavedViolations () {
        return Collections.unmodifiableList(this.savedViolations);
    }


    @Override
    public abstract boolean getAbstract ();


    @Override
    public abstract StructuralObject getAnchor () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    protected abstract void checkContext () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    @Override
    public abstract String getObjectTypeName () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    protected abstract ConfigurationEditContext<ConfigurationObject, ConfigurationObject> fetchContext ()
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    @Override
    public abstract String getInnerEditorDialogTemplate ();


    /**
     * @return the contextLoaded
     */
    public boolean isContextLoaded () {
        return this.contextLoaded;
    }


    /**
     * @return the ssp
     */
    protected ServerServiceProvider getSsp () {
        return this.ssp;
    }


    /**
     * 
     */
    public AbstractConfigContextBean () {
        super();
    }


    /**
     * @return the detailLevel
     */
    @Override
    public int getDetailLevel () {
        if ( this.detailLevel == null ) {
            return this.userPreferences.getDefaultDetailLevel();
        }
        return this.detailLevel;
    }


    /**
     * @param detailLevel
     *            the detailLevel to set
     */
    public void setDetailLevel ( int detailLevel ) {
        this.detailLevel = detailLevel;
    }


    /**
     * @return the context
     */
    @SuppressWarnings ( "unchecked" )
    public ConfigurationEditContext<T, TMutable> getContext () {

        if ( this.context == null && !this.isContextLoaded() ) {
            try {
                this.checkContext();

                log.debug("Loading edit context with CID " + this.conversation.getId()); //$NON-NLS-1$
                this.contextLoaded = true;
                this.context = (ConfigurationEditContext<T, TMutable>) this.fetchContext();
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                return null;
            }
        }

        return this.context;
    }


    /**
     * 
     * @return whether the configuration can be applied in current state
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public boolean getCanApply () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.getCurrent() == null || this.getAbstract() ) {
            return false;
        }

        return this.getState() == ConfigurationState.UPDATE_AVAILABLE || this.getState() == ConfigurationState.DEFAULTS_CHANGED;
    }


    /**
     * {@inheritDoc}
     * 
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getState()
     */
    @Override
    public synchronized ConfigurationState getState () {

        if ( this.overrideState != null ) {
            return this.overrideState;
        }

        ConfigurationEditContext<T, TMutable> ctx = this.getContext();
        if ( ctx != null ) {
            return ctx.getConfigurationState();
        }
        return ConfigurationState.UNKNOWN;
    }


    /**
     * @param overrideState
     *            the overrideState to set
     */
    protected void setOverrideState ( ConfigurationState overrideState ) {
        this.overrideState = overrideState;
    }


    public void refreshState () {

    }


    /**
     * @return the hostConfig
     */
    @Override
    @Valid
    public TMutable getCurrent () {
        ConfigurationEditContext<T, TMutable> ctx = this.getContext();
        if ( ctx != null ) {
            return ctx.getCurrent();
        }
        return null;
    }


    /**
     * @return the applied configuration defaults
     */
    @Override
    public @Nullable T getDefaults () {
        ConfigurationEditContext<T, @Nullable TMutable> ctx = this.getContext();
        if ( ctx != null ) {
            return ctx.getStructuralDefaults();
        }
        return null;
    }


    /**
     * @return the enforced host configuration
     */
    @Override
    public @Nullable T getEnforced () {
        ConfigurationEditContext<T, @Nullable TMutable> ctx = this.getContext();
        if ( ctx != null ) {
            return ctx.getEnforcedValues();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getInherited()
     */
    @Override
    public @Nullable T getInherited () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationEditContext<T, @Nullable TMutable> ctx = this.getContext();
        if ( ctx != null ) {
            return ctx.getInheritedValues();
        }
        return null;
    }


    @SuppressWarnings ( "unchecked" )
    @Override
    public synchronized boolean updateConfiguration () throws ModelObjectValidationException, ModelObjectNotFoundException, ModelServiceException,
            GuiWebServiceException, ModelObjectConflictException {

        ConfigurationEditContext<T, TMutable> ctx = this.getContext();
        if ( ctx != null ) {
            TMutable current = ctx.getCurrent();

            if ( current == null ) {
                return false;
            }

            log.debug("Config before update:"); //$NON-NLS-1$
            ConfigDumperBean.dumpConfig(log, Level.DEBUG, this.csp.getMarshallingService(), ctx.getCurrent());

            Validator validator = this.csp.getValidatorFactory().getValidator();
            Set<ConstraintViolation<T>> violations = validator.validate(current, this.getValidationGroups());

            if ( !violations.isEmpty() ) {
                throw new ModelObjectValidationException((Class<T>) current.getType(), current.getId(), violations);
            }

            internalDoUpdate();
            TMutable currentConfig = this.getContext().getCurrent();

            if ( currentConfig != null ) {
                TMutable inherited = this.ssp.getService(InheritanceService.class).getInherited(currentConfig, null);

                if ( inherited != null ) {
                    this.getContext().setInheritedValues(inherited);
                }
            }

            log.debug("Config after update:"); //$NON-NLS-1$
            ConfigDumperBean.dumpConfig(log, Level.DEBUG, this.csp.getMarshallingService(), this.getCurrent());
            return true;
        }
        return false;
    }


    protected abstract void internalDoUpdate () throws ModelServiceException, ModelObjectValidationException, ModelObjectNotFoundException,
            GuiWebServiceException, ModelObjectConflictException;


    /**
     * @return
     */
    protected abstract Class<?>[] getValidationGroups ();


    @Override
    public synchronized JobInfo applyConfiguration ( ConfigApplyContextBean applyCtx )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException, ModelObjectValidationException,
            ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {

        if ( this.getCurrent() != null && !this.getAbstract() ) {
            return internalApplyConfiguration(applyCtx);
        }

        return null;
    }


    public boolean validateConfiguration () throws ModelServiceException, GuiWebServiceException, ModelObjectException {

        TMutable config = this.getCurrent();
        StructuralObject anchor = this.getAnchor();

        if ( config != null && anchor != null ) {

            ValidationService service = this.ssp.getService(ValidationService.class);
            List<ViolationEntry> violations = service.validateObject(config, anchor);

            boolean containsError = false;

            if ( violations == null || violations.isEmpty() ) {
                log.debug("No violations found"); //$NON-NLS-1$
                return true;
            }

            this.savedViolations.clear();
            for ( ViolationEntry e : violations ) {
                if ( e.getLevel() == ViolationLevel.ERROR ) {
                    containsError = true;
                }
                else {
                    this.savedViolations.add(e);
                }
            }
            handleViolations(violations);

            if ( containsError ) {
                FacesContext.getCurrentInstance().validationFailed();
                FacesContext.getCurrentInstance().renderResponse();
            }
            else {
                return true;
            }

        }
        return false;
    }


    public void addSavedViolations () {
        log.debug("Adding saved violations to current view"); //$NON-NLS-1$
        for ( ViolationEntry e : this.savedViolations ) {
            FacesContext.getCurrentInstance().addMessage(null, this.violationMessageBuilder.makeMessage(StringUtils.EMPTY, e));
        }
        this.savedViolations.clear();
    }


    /**
     * @param violations
     * @param asMessages
     */
    private void handleViolations ( List<ViolationEntry> violations ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found %d violations", violations.size())); //$NON-NLS-1$

            for ( ViolationEntry e : violations ) {
                log.debug(e.getLevel() + ": " + e.getMessageTemplate()); //$NON-NLS-1$
                log.debug("Object path " + e.getPath()); //$NON-NLS-1$
            }
        }

        ViolationAddingVisitor violationAddingVisitor = new ViolationAddingVisitor(this.violationMessageBuilder, violations);
        FullVisitContext fullVisitContext = new FullVisitContext(FacesContext.getCurrentInstance());
        FacesContext.getCurrentInstance().getViewRoot().visitTree(fullVisitContext, violationAddingVisitor);
        violationAddingVisitor.addRemainingViolations(fullVisitContext);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getEmptyObject(java.lang.String)
     */
    @Override
    public ConfigurationObject getEmptyObject ( String objectType ) throws ModelServiceException, GuiWebServiceException {
        return this.getSsp().getService(ConfigurationService.class).getEmpty(objectType);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getObjectDefaults(java.lang.String,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public ConfigurationObject getObjectDefaults ( String objectType, ConfigurationObject object )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( object == null || object.getInherits() == null ) {
            return this.tplCache.getDefaultsForTypeAt(objectType, this.getAnchor(), this.getObjectTypeName());
        }

        if ( object.getInherits() instanceof ConfigurationObjectReference ) {
            return this.getTemplateEffective((ConfigurationObjectReference) object.getInherits(), this.getObjectTypeName());
        }
        return this.getTemplateEffective(new ConfigurationObjectReference(object.getInherits()), this.getObjectTypeName());
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getEligibleTemplatesForTypeName(java.lang.String)
     */

    @Override
    public List<ConfigurationObjectReference> getEligibleTemplatesForTypeName ( String objType )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.getEligibleTemplatesForType(objType, StringUtils.EMPTY);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getEligibleTemplates(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public List<ConfigurationObjectReference> getEligibleTemplates ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.getEligibleTemplates(obj, StringUtils.EMPTY);
    }


    private List<ConfigurationObjectReference> getEligibleTemplates ( ConfigurationObject obj, String filter )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( obj == null ) {
            return Collections.EMPTY_LIST;
        }
        List<ConfigurationObjectReference> tpls = this.getEligibleTemplatesForType(ConfigUtil.getObjectTypeName(obj), filter);
        if ( tpls != null ) {
            tpls = new ArrayList<>(tpls);
            tpls.remove(obj);
        }
        return tpls;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getEligibleTemplatesForType(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public List<ConfigurationObjectReference> getEligibleTemplatesForType ( String objType, String filter )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        StructuralObject anchor = this.getAnchor();
        if ( anchor == null ) {
            return Collections.EMPTY_LIST;
        }
        return this.tplCache.getTemplatesForType(anchor, objType, filter);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     *
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getApplicableTypes(java.lang.String)
     */
    @Override
    public Set<String> getApplicableTypes ( String objectType ) throws ModelServiceException, GuiWebServiceException {
        return this.tplCache.getApplicableTypesFor(objectType);
    }


    protected void clearTemplateCache () {
        this.tplCache.flush();
        this.configCache.flush();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#getTemplateEffective(eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference)
     */
    @Override
    public ConfigurationObject getTemplateEffective ( ConfigurationObjectReference ref )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return getTemplateEffective(ref, this.getObjectTypeName());
    }


    @Override
    public ConfigurationObject getTemplateEffective ( ConfigurationObjectReference tpl, String rootObjectType )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.configCache.getEffectiveConfig(tpl, rootObjectType);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.config.ConfigContext#fetch(eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference)
     */
    @Override
    public ConfigurationObject fetch ( ConfigurationObjectReference obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.ssp.getService(ConfigurationService.class).refresh(obj);
    }


    /**
     * @return
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * @throws AbstractModelException
     * 
     */
    protected abstract JobInfo internalApplyConfiguration ( ConfigApplyContextBean applyCtx )
            throws ModelObjectValidationException, ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException,
            ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;


    /**
     * 
     * @return the currently loaded global revision
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public Long getRevision () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        @Nullable
        TMutable current = this.getCurrent();
        if ( current == null ) {
            return null;
        }
        return current.getRevision();
    }


    /**
     * 
     * @return the currently loaded local version
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public Long getVersion () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        @Nullable
        TMutable current = this.getCurrent();
        if ( current == null ) {
            return null;
        }
        return current.getVersion();
    }


    /**
     * @return the configLocalizer
     */
    @Override
    public ConfigLocalizationProvider getConfigLocalizer () {
        return this.configLocalizer;
    }


    /**
     * @return the updateInfo
     */
    public ConfigUpdateInfo getUpdateInfo () {
        return this.updateInfo;
    }


    /**
     * @param updateInfo
     *            the updateInfo to set
     */
    public void setUpdateInfo ( ConfigUpdateInfo updateInfo ) {
        this.updateInfo = updateInfo;
    }
}