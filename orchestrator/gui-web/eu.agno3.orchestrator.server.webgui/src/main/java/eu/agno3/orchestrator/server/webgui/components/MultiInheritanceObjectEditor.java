/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.config.ConfigUtil;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 * 
 */
public class MultiInheritanceObjectEditor extends MultiObjectEditor {

    private static final Logger log = Logger.getLogger(MultiInheritanceObjectEditor.class);

    private static final Serializable CREATE_OBJECT_TYPE = "createObjectType"; //$NON-NLS-1$

    private List<String> cachedApplicableTypes;


    public String getCreateObjectType () {
        return (String) this.getStateHelper().get(CREATE_OBJECT_TYPE);
    }


    public void setCreateObjectType ( String objectType ) {
        this.getStateHelper().put(CREATE_OBJECT_TYPE, objectType);
    }


    public List<String> getApplicableObjectTypes () {
        if ( this.cachedApplicableTypes == null ) {
            try {
                this.cachedApplicableTypes = new ArrayList<>(this.internalGetContext().getApplicableTypes(this.getObjectType()));
                Collections.sort(this.cachedApplicableTypes);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
            }
        }
        return this.cachedApplicableTypes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.components.MultiObjectEditor#getDisplayTitleFor(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public String getDisplayTitleFor ( ConfigurationObject obj ) {
        return this.internalGetContext().getConfigLocalizer().getTypeName(ConfigUtil.getObjectTypeName(obj));
    }


    /**
     * 
     * @return the editor template to use
     */
    public String getInnerEditorTemplate () {
        ConfigContext<ConfigurationObject, ConfigurationObject> ctx = internalGetContext();

        String innerEditorTemplate = (String) this.getParameter("overrideInnerEditorTemplate"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(innerEditorTemplate) ) {
            return innerEditorTemplate;
        }
        return ctx.getInnerEditorDialogTemplate();
    }


    /**
     * 
     * @return the edited object's object type
     */
    @Override
    protected String internalGetObjectType () {
        String selectedObjectType = getSelectedObjectType();
        if ( selectedObjectType != null ) {
            return selectedObjectType;
        }
        return super.internalGetObjectType();
    }


    /**
     * @return the selected object type
     * 
     */
    public String getSelectedObjectType () {
        try {
            ConfigurationObject selectedObject = this.getSelectedObject();
            if ( selectedObject != null ) {
                return ConfigUtil.getObjectTypeName(selectedObject);
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public void returnFromEdit ( SelectEvent ev ) {
        boolean dirty = ev.getObject() instanceof Boolean && (boolean) ev.getObject();
        if ( log.isDebugEnabled() ) {
            log.debug("Have return from edit, dirty: " + dirty); //$NON-NLS-1$
        }

        RequestContext.getCurrentInstance().addCallbackParam("dirty", dirty); //$NON-NLS-1$
    }


    @Override
    public boolean resetComponent () {
        log.debug("Resetting component state"); //$NON-NLS-1$
        return super.resetComponent();
    }


    /**
     * 
     * @return the object path
     */
    public String getSelectedObjectPath () {
        return getAbsolutePath();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.components.MultiObjectEditor#addNew(javax.faces.event.ActionEvent)
     */
    @Override
    public void addNew ( ActionEvent ev ) throws ModelServiceException, GuiWebServiceException, ModelObjectNotFoundException {
        String objectType = getCreateObjectType();
        if ( StringUtils.isBlank(objectType) ) {
            return;
        }
        log.debug("Add new object " + objectType); //$NON-NLS-1$
        ConfigurationObject obj = this.internalGetContext().getEmptyObject(objectType);
        processUpdates(FacesContext.getCurrentInstance());
        List<ConfigurationObject> collection = this.getCurrent();
        collection.add(obj);
        this.setSelectedObjectInternal(obj);
        processUpdates(FacesContext.getCurrentInstance());
    }

}
