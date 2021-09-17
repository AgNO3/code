/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Conversation;

import org.eclipse.jdt.annotation.Nullable;

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
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;


/**
 * @author mbechler
 * @param <T>
 * @param <TMutable>
 * 
 */
public interface ConfigContext <T extends ConfigurationObject, TMutable extends T> extends Serializable {

    /**
     * @return a revision provider applicable for the current context
     */
    RevisionProvider getRevisionProvider ();


    /**
     * @return whether context object is an abstract (i.e. template for inheritance) or an instance (= leaf object)
     */
    boolean getAbstract ();


    /**
     * 
     * @return the configuration state
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    ConfigurationState getState () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @return the current configuration for the context object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    TMutable getCurrent () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @return the currently applied defaults for the context object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @Nullable
    T getDefaults () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @return the effective inherited values
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @Nullable
    T getInherited () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @return the currently effective enforcement values
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @Nullable
    T getEnforced () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * Triggers pushing an updated configuration in this context to the server
     * 
     * @return whether the update was successful
     * @throws ModelObjectValidationException
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectConflictException
     */
    boolean updateConfiguration () throws ModelObjectValidationException, ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException,
            ModelObjectConflictException;


    /**
     * @param applyCtx
     * @return whether config was applied
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * 
     */
    JobInfo applyConfiguration ( ConfigApplyContextBean applyCtx )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException, ModelObjectValidationException,
            ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;


    /**
     * @param inherits
     * @param rootType
     * @return the effective template values
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    ConfigurationObject getTemplateEffective ( ConfigurationObjectReference inherits, String rootType )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @param ref
     * @return the effective template values using the top level root type
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    ConfigurationObject getTemplateEffective ( ConfigurationObjectReference ref )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @param objType
     * @return references to the templates for the given type name at the contexts anchor
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    List<ConfigurationObjectReference> getEligibleTemplatesForTypeName ( String objType )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @param obj
     * @return references to the templates applicable for the given object
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    List<ConfigurationObjectReference> getEligibleTemplates ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @param objType
     * @param filter
     * @return references to the templates for the given type name at the contexts anchor matching the given filter
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    List<ConfigurationObjectReference> getEligibleTemplatesForType ( String objType, String filter )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @param objectType
     * @return an empty object of the given type
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     */
    ConfigurationObject getEmptyObject ( String objectType ) throws ModelServiceException, GuiWebServiceException;


    /**
     * @param objectType
     * @param object
     * @return the defaults/inherited values for the object
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    ConfigurationObject getObjectDefaults ( String objectType, ConfigurationObject object )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @param obj
     * @return the actual object for the reference
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    ConfigurationObject fetch ( ConfigurationObjectReference obj ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @return the config localizer
     */
    ConfigLocalizationProvider getConfigLocalizer ();


    /**
     * @param objectType
     * @return object types applicable for the given base type
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     */
    Set<String> getApplicableTypes ( String objectType ) throws ModelServiceException, GuiWebServiceException;


    /**
     * @return default inner editor template
     */
    String getInnerEditorDialogTemplate ();


    /**
     * @return template to use for test plugins
     */
    String getTestTemplate ();


    /**
     * @return the selected detail level
     */
    int getDetailLevel ();


    /**
     * @return the edited object type
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    String getObjectTypeName () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * @return the anchor where editing is performed
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    StructuralObject getAnchor () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * 
     * @return whether we should show controls that allow resetting to defaults
     */
    boolean isShowDefaultReset ();


    /**
     * @return the conversation used for editing
     */
    Conversation getConversation ();

}
