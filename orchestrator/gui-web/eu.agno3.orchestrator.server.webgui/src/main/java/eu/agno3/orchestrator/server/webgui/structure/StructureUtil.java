/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure;


import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectState;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.ConfigServiceProvider;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Named ( "structureUtil" )
public class StructureUtil {

    private static final Logger log = Logger.getLogger(StructureUtil.class);

    /**
     * 
     */
    private static final String SERVICE_STATE_KEY_PREFIX = "service.state."; //$NON-NLS-1$
    /**
     * 
     */
    private static final String ROOT_LABEL = "Root"; //$NON-NLS-1$
    private static final String TYPE_PREFIX = "urn:agno3:1.0:"; //$NON-NLS-1$
    private static final String IMAGE_TYPE_PREFIX = "urn:agno3:images:1.0:"; //$NON-NLS-1$

    private static final String STRUCTURE_STATE_KEY_PREFIX = "structure.state."; //$NON-NLS-1$

    @Inject
    private StructureCacheBean structureCache;

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private ConfigServiceProvider confsp;

    @Inject
    private AgentStateTracker agentStateTracker;


    /**
     * 
     * @param obj
     * @return a navigation outcome redirecting to the overview page for the given object
     */
    public static String getOutcomeForObjectOverview ( StructuralObject obj ) {
        return getOutcomeForObjectOverview(obj, null);
    }


    public static String getOutcomeForObjectOverview ( StructuralObject obj, StructuralObject anchor ) {
        if ( obj == null ) {
            return "/?faces-redirect=true"; //$NON-NLS-1$
        }

        switch ( obj.getType() ) {
        case GROUP:
            return makeGroupOverviewUrl(obj, anchor);
        case INSTANCE:
            return makeInstanceOverviewUrl(obj, anchor);
        case SERVICE:
            return makeServiceOverviewUrl(obj, anchor);
        default:
            throw new UnsupportedOperationException();
        }
    }


    /**
     * 
     * @param obj
     * @return the display name for the structural object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public String getDisplayName ( StructuralObject obj ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        if ( obj == null ) {
            return null;
        }

        if ( obj.getDisplayName() != null ) {
            return getRegularDisplayName(obj);
        }

        if ( obj instanceof ServiceStructuralObject ) {
            InstanceStructuralObject i = (InstanceStructuralObject) this.structureCache.getParentFor(obj);
            return GuiMessages.format(GuiMessages.SERVICE_TITLE, getServiceDisplayName((ServiceStructuralObject) obj), this.getDisplayName(i));
        }

        return null;
    }


    /**
     * 
     * @param obj
     * @return the short object name for the structural object
     */
    public String getObjectShortName ( StructuralObject obj ) {
        if ( obj == null ) {
            return null;
        }

        if ( obj.getDisplayName() != null ) {
            return getRegularDisplayName(obj);
        }

        if ( obj instanceof ServiceStructuralObject ) {
            return getServiceDisplayName((ServiceStructuralObject) obj);
        }

        return null;
    }


    /**
     * 
     * @param obj
     * @return an icon to use for the given object
     */
    public String getObjectIcon ( StructuralObject obj ) {
        if ( obj instanceof GroupStructuralObject ) {
            return "ui-icon-folder-open"; //$NON-NLS-1$
        }
        else if ( obj instanceof InstanceStructuralObject ) {
            return "ui-icon-home"; //$NON-NLS-1$
        }
        else if ( obj instanceof ServiceStructuralObject ) {
            return "ui-icon-lightbulb"; //$NON-NLS-1$
        }
        return "ui-icon-blank"; //$NON-NLS-1$
    }


    private static String getRegularDisplayName ( StructuralObject obj ) {
        if ( obj instanceof GroupStructuralObject && ROOT_LABEL.equals(obj.getDisplayName()) ) {
            return GuiMessages.get(GuiMessages.STRUCTURE_ROOT_LABEL);
        }
        return obj.getDisplayName();
    }


    /**
     * @param obj
     * @return a descriptive display name
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public String getObjectDescriptiveName ( StructuralObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        String displayName = this.getDisplayName(obj);

        if ( obj instanceof GroupStructuralObject ) {
            return GuiMessages.format(GuiMessages.GROUP_DESCRIPTIVE, displayName);
        }
        else if ( obj instanceof InstanceStructuralObject ) {
            return GuiMessages.format(GuiMessages.INSTANCE_DESCRIPTIVE, displayName);
        }
        else if ( obj instanceof ServiceStructuralObject ) {
            return GuiMessages.format(GuiMessages.SERVICE_DESCRIPTIVE, displayName);
        }

        return displayName;
    }


    /**
     * 
     * @param obj
     * @return translated agent state
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public String getAgentStateForDisplay ( InstanceStructuralObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        if ( obj == null ) {
            return null;
        }

        if ( obj.getAgentId() == null ) {
            return GuiMessages.get(GuiMessages.AGENT_STATE_DETACHED);
        }

        return GuiMessages.get("ComponentState." + this.agentStateTracker.getAgentState(obj)); //$NON-NLS-1$
    }


    public String getAgentStateIcon ( InstanceStructuralObject obj ) {
        if ( obj == null ) {
            return null;
        }

        if ( obj.getAgentId() == null ) {
            return "ui-icon-gear"; //$NON-NLS-1$
        }

        switch ( this.agentStateTracker.getAgentState(obj) ) {
        case CONNECTED:
            return "ui-icon-check"; //$NON-NLS-1$
        case CONNECTING:
        case DISCONNECTED:
            return "ui-icon-arrowrefresh-1-e"; //$NON-NLS-1$
        case UNKNOWN:
        case FAILURE:
            return "ui-icon-alert"; //$NON-NLS-1$
        default:
            return "ui-icon-blank"; //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param obj
     * @return translated structural object overall state
     */
    public String getOverallDisplayState ( StructuralObject obj ) {
        if ( obj == null ) {
            return null;
        }
        StructuralObjectState state = obj.getOverallState();
        String key;
        if ( state == null ) {
            key = STRUCTURE_STATE_KEY_PREFIX + StructuralObjectState.UNKNOWN;
        }
        else {
            key = STRUCTURE_STATE_KEY_PREFIX + state.name();
        }

        return GuiMessages.get(key);
    }


    /**
     * 
     * @param obj
     * @return the display name for this service type
     */
    public String getServiceDisplayName ( ServiceStructuralObject obj ) {
        if ( obj == null ) {
            return null;
        }
        return getServiceTypeDisplayName(obj.getServiceType());
    }


    /**
     * 
     * @param obj
     * @return translated image type name
     */
    public String getInstanceImageTypeDisplayName ( InstanceStructuralObject obj ) {
        if ( obj == null ) {
            return null;
        }
        return getImageTypeDisplayName(obj.getImageType());
    }


    /**
     * @param imageTypeName
     * @return a user-friendly translated image type name
     */
    public String getImageTypeDisplayName ( String imageTypeName ) {
        ImageTypeDescriptor descriptor = null;
        try {
            descriptor = this.confsp.getImageTypeRegistry().getDescriptor(imageTypeName);
        }
        catch ( Exception e ) {
            log.warn("Failed to get image type descriptor", e); //$NON-NLS-1$
        }

        if ( descriptor == null ) {
            log.warn("No descriptor found for image type " + imageTypeName); //$NON-NLS-1$
            return imageTypeName;
        }

        if ( descriptor.getLocalizationBase() == null ) {
            log.warn("No localization base found in descriptor of imageTypeName type " + imageTypeName); //$NON-NLS-1$
            return imageTypeName;
        }

        String name = imageTypeName.substring(IMAGE_TYPE_PREFIX.length());
        ResourceBundle rb = this.csp.getLocalizationService()
                .getBundle(descriptor.getLocalizationBase(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
        return rb.getString("image." + name); //$NON-NLS-1$
    }


    /**
     * 
     * @param serviceTypeName
     * @return the display name for this service type
     */
    public String getServiceTypeDisplayName ( String serviceTypeName ) {
        ServiceTypeDescriptor<ConfigurationInstance, ConfigurationInstance> descriptor = null;
        try {
            descriptor = this.confsp.getServiceTypeRegistry().getDescriptor(serviceTypeName);
        }
        catch ( Exception e ) {
            log.warn("Failed to get service type descriptor", e); //$NON-NLS-1$
        }

        if ( descriptor == null ) {
            log.warn("No descriptor found for service type " + serviceTypeName); //$NON-NLS-1$
            return serviceTypeName;
        }

        if ( descriptor.getLocalizationBase() == null ) {
            log.warn("No localization base found in descriptor of service type " + serviceTypeName); //$NON-NLS-1$
            return serviceTypeName;
        }

        String name = serviceTypeName.substring(TYPE_PREFIX.length());
        ResourceBundle rb = this.csp.getLocalizationService()
                .getBundle(descriptor.getLocalizationBase(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
        return rb.getString("service." + name); //$NON-NLS-1$
    }


    public String iconForConfigState ( ConfigurationState state ) {
        if ( state == null ) {
            return "ui-icon-blank"; //$NON-NLS-1$
        }

        switch ( state ) {
        case APPLIED:
            return "ui-icon-check"; //$NON-NLS-1$
        case APPLYING:
        case DEFAULTS_CHANGED:
        case UPDATE_AVAILABLE:
            return "ui-icon-transferthick-e-w"; //$NON-NLS-1$

        case UNCONFIGURED:
            return "ui-icon-document"; //$NON-NLS-1$

        case FAILED:
        case UNKNOWN:
            return "ui-icon-notice"; //$NON-NLS-1$

        default:
            return "ui-icon-blank"; //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param state
     * @return translated service state
     */
    public String translateConfigState ( ConfigurationState state ) {
        String key;
        if ( state == null || state == ConfigurationState.UNKNOWN ) {
            key = SERVICE_STATE_KEY_PREFIX + ConfigurationState.UNCONFIGURED;
        }
        else {
            key = SERVICE_STATE_KEY_PREFIX + state.name();
        }

        return GuiMessages.get(key);
    }


    /**
     * 
     * @param state
     * @return translated service state
     */
    public String translateConfigStateDescription ( ConfigurationState state ) {
        String key;
        if ( state == null ) {
            key = SERVICE_STATE_KEY_PREFIX + ConfigurationState.UNCONFIGURED + ".description"; //$NON-NLS-1$
        }
        else {
            key = SERVICE_STATE_KEY_PREFIX + state.name() + ".description"; //$NON-NLS-1$
        }
        return GuiMessages.get(key);
    }


    /**
     * @param anchor
     * @return
     */
    private static String addAnchor ( StructuralObject anchor ) {
        if ( anchor == null ) {
            return StringUtils.EMPTY;
        }
        return "&anchor=" + anchor.getId(); //$NON-NLS-1$
    }


    private static String makeServiceOverviewUrl ( StructuralObject obj, StructuralObject anchor ) {
        return "/structure/service/index.xhtml?faces-redirect=true&service=" + obj.getId() + addAnchor(anchor); //$NON-NLS-1$
    }


    private static String makeInstanceOverviewUrl ( StructuralObject obj, StructuralObject anchor ) {
        return "/structure/instance/index.xhtml?faces-redirect=true&instance=" + obj.getId() + addAnchor(anchor); //$NON-NLS-1$
    }


    private static String makeGroupOverviewUrl ( StructuralObject obj, StructuralObject anchor ) {
        return "/structure/group/index.xhtml?faces-redirect=true&group=" + obj.getId() + addAnchor(anchor); //$NON-NLS-1$
    }
}
