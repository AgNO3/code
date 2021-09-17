/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlForm;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.EmptyCheckableObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.runtime.jsf.components.simplefield.SimpleField;


/**
 * @author mbechler
 * 
 */
public class ObjectFieldEditor extends SimpleField {

    private static final Logger log = Logger.getLogger(ObjectFieldEditor.class);

    private static final String PATH = "path"; //$NON-NLS-1$

    private boolean resolvedParentEditor;
    private AbstractObjectEditor<?> cachedParentEditor;

    private String cachedDescriptionValue;

    private String cachedLabelValue;

    private transient Boolean cachedHaveEnforcedValue;
    private transient Boolean cachedHaveInheritedDefaultValue;
    private transient Boolean cachedIsInheritedDefaultValue;
    private transient Boolean cachedIsInEditMode;
    private transient Boolean cachedHaveLocalValue;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#showEditField()
     */
    @Override
    public boolean showEditField () {
        return !this.isHidden() && super.showEditField();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#showReadOnly()
     */
    @Override
    public boolean showReadOnly () {
        return !this.isHidden() && ( super.showReadOnly() || isPersistentReadOnly() );
    }


    /**
     * @return
     */
    private boolean isPersistentReadOnly () {
        boolean persistentReadOnly = (boolean) this.getAttributes().get("readOnlyIfPersistent"); //$NON-NLS-1$
        if ( !persistentReadOnly ) {
            return false;
        }

        try {
            AbstractObjectEditor<?> po = getParentEditor();
            Object obj = null;
            if ( po instanceof MultiObjectEditor ) {
                obj = ( (MultiObjectEditor) po ).getSelectedObject();
            }
            else if ( po instanceof ObjectEditor ) {
                obj = po.getCurrent();
            }

            if ( ! ( obj instanceof AbstractConfigurationObject<?> ) ) {
                return false;
            }

            AbstractConfigurationObject<?> co = (AbstractConfigurationObject<?>) obj;
            return co.getVersion() != null;
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException |
            GuiWebServiceException e ) {
            ExceptionHandler.handle(e);
            return false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#shouldShow()
     */
    @Override
    public boolean shouldShow () {
        return !this.isHidden() && super.shouldShow();
    }


    /**
     * 
     * @return whether to show reset to default button
     */
    public boolean shouldShowResetToDefaults () {
        return this.getParentEditor().internalGetContext().isShowDefaultReset();
    }


    /**
     * @return whether the component is hidden based on the detail level
     */
    public boolean isHidden () {
        Boolean hideIfSimplified = (Boolean) this.getAttributes().get("hideIfSimplified"); //$NON-NLS-1$
        if ( hideIfSimplified != null && hideIfSimplified && getParentEditor().isSimplified() ) {
            return true;
        }
        int minLevel = (int) this.getAttributes().get("minLevel"); //$NON-NLS-1$
        return ( this.internalIsReadOnly() || !this.haveLocalValue() ) && minLevel > this.getParentEditor().internalGetContext().getDetailLevel(); // $NON-NLS-1$
    }


    /**
     * 
     * @return this editors relative path
     */
    public String internalGetPath () {
        return (String) this.getAttributes().get(PATH);
    }


    /**
     * @return whether an enforced value exists
     */
    @Override
    public boolean hasEnforcedValue () {
        if ( this.cachedHaveEnforcedValue != null ) {
            return this.cachedHaveEnforcedValue;
        }
        Object enforced = getEnforcedValue();
        this.cachedHaveEnforcedValue = enforced != null && !isEmptyCollection(enforced) && !isEmptyMap(enforced) && !isEmptyValue(enforced);
        return this.cachedHaveEnforcedValue;
    }


    /**
     * @return whether a inherited default value exists
     */
    @Override
    public boolean hasInheritedDefaultValue () {
        if ( this.cachedHaveInheritedDefaultValue != null ) {
            return this.cachedHaveInheritedDefaultValue;
        }
        Object inherited = getInheritedValue();
        this.cachedHaveInheritedDefaultValue = inherited != null && !isEmptyCollection(inherited) && !isEmptyMap(inherited)
                && !isEmptyValue(inherited);
        return this.cachedHaveInheritedDefaultValue;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#isInheritedDefaultValue()
     */
    @Override
    public boolean isInheritedDefaultValue () {
        if ( this.cachedIsInheritedDefaultValue != null ) {
            return this.cachedIsInheritedDefaultValue;
        }
        this.cachedIsInheritedDefaultValue = !haveLocalValue() && this.hasInheritedDefaultValue();
        return this.cachedIsInheritedDefaultValue;
    }


    /**
     * @return
     */
    private boolean haveLocalValue () {
        if ( this.cachedHaveLocalValue != null ) {
            return this.cachedHaveLocalValue;
        }
        Object obj = getLocalValue();
        this.cachedHaveLocalValue = obj != null && !isEmptyValue(obj) && !isEmptyCollection(obj) && !isEmptyMap(obj);
        return this.cachedHaveLocalValue;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#isInEditMode()
     */
    @Override
    public boolean isInEditMode () {
        if ( this.cachedIsInEditMode != null ) {
            return this.cachedIsInEditMode;
        }
        this.cachedIsInEditMode = super.isInEditMode();
        return this.cachedIsInEditMode;
    }


    /**
     * 
     * @return message to display as value source
     */
    public String getValueSourceMessage () {
        return GuiMessages.get("config.object.defaultValue"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#doEdit()
     */
    @Override
    public void doEdit () {
        this.cachedIsInEditMode = null;
        super.doEdit();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#resetToDefault()
     */
    @Override
    public void resetToDefault () {
        this.cachedIsInEditMode = null;
        this.cachedHaveLocalValue = null;
        super.resetToDefault();
    }


    /**
     * @param obj
     * @return
     */
    private static boolean isEmptyValue ( Object obj ) {
        if ( obj instanceof EmptyCheckableObject ) {
            return ( (EmptyCheckableObject) obj ).isEmpty();
        }
        return false;
    }


    /**
     * @param objectEditor
     */
    public void inheritanceChanged ( AbstractObjectEditor<ConfigurationObject> objectEditor ) {
        clearCache();
    }


    /**
     * 
     */
    private void clearCache () {
        this.cachedHaveEnforcedValue = null;
        this.cachedHaveEnforcedValue = null;
        this.cachedHaveInheritedDefaultValue = null;
        this.cachedIsInheritedDefaultValue = null;
        this.cachedIsInEditMode = null;
        this.cachedHaveLocalValue = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        clearCache();
        return super.resetComponent();
    }


    /**
     * 
     * @return the parent editor, or null if none exists
     */
    public synchronized AbstractObjectEditor<?> getParentEditor () {

        if ( this.resolvedParentEditor ) {
            return this.cachedParentEditor;
        }

        this.resolvedParentEditor = true;

        UIComponent parent = this.getParent();

        while ( parent != null && ! ( parent instanceof HtmlForm ) ) {

            if ( parent instanceof AbstractObjectEditor<?> ) {
                this.cachedParentEditor = (AbstractObjectEditor<?>) parent;
                log.trace("Found parent"); //$NON-NLS-1$
                return this.cachedParentEditor;
            }

            parent = parent.getParent();
        }

        throw new FacesException("Failed to find parent editor"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#internalIsReadOnly()
     */
    @Override
    public boolean internalIsReadOnly () {
        try {
            return this.getParentEditor().childrenAreReadOnly();
        }
        catch ( Exception e ) {
            throw new FacesException("Failed to determine whether the field is read only", e); //$NON-NLS-1$
        }
    }


    protected String getObjectType () {
        return this.getParentEditor().getOuterWrapper().getType();
    }


    /**
     * 
     * @return the translated label
     */
    @Override
    public String getLabelValue () {
        if ( this.cachedLabelValue == null ) {
            this.cachedLabelValue = this.getParentEditor().internalGetContext().getConfigLocalizer()
                    .getFieldName(this.getObjectType(), this.internalGetPath());
        }
        return this.cachedLabelValue;
    }


    @Override
    public String getDescriptionValue () {
        if ( this.cachedDescriptionValue == null ) {
            this.cachedDescriptionValue = this.getParentEditor().internalGetContext().getConfigLocalizer()
                    .getFieldDescription(this.getObjectType(), this.internalGetPath());
        }
        return this.cachedDescriptionValue;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#cloneValue(java.lang.Object)
     */
    @Override
    protected Object cloneValue ( Object obj ) {
        MethodExpression cloneMethod = getCloneMethod();
        if ( cloneMethod == null && obj instanceof ConfigurationObject ) {
            throw new FacesException("Clone method required but not set on component"); //$NON-NLS-1$
        }

        return super.cloneValue(obj);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#getInheritedValue()
     */
    @Override
    public Object getInheritedValue () {
        try {
            AbstractObjectEditor<?> parentEditor = this.getParentEditor();
            if ( parentEditor instanceof MultiObjectEditor ) {
                return resolve( ( (MultiObjectEditor) parentEditor ).getSelectedObjectDefaults(), internalGetPath());
            }
            Object o = resolve(parentEditor.getDefaults(), internalGetPath());
            if ( log.isTraceEnabled() ) {
                log.trace(this.internalGetPath() + ": inherited value is " + o); //$NON-NLS-1$
            }
            return o;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            throw new FacesException("Failed to fetch inherited value", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#getEnforcedValue()
     */
    @Override
    public Object getEnforcedValue () {
        try {
            AbstractObjectEditor<?> parentEditor = this.getParentEditor();
            if ( parentEditor instanceof MultiObjectEditor ) {
                return resolve( ( (MultiObjectEditor) parentEditor ).getSelectedObjectEnforced(), internalGetPath());
            }
            Object o = resolve(parentEditor.getEnforced(), internalGetPath());
            if ( log.isTraceEnabled() ) {
                log.trace(this.internalGetPath() + ": enforced value is " + o); //$NON-NLS-1$
            }
            return o;
        }
        catch ( Exception e ) {
            throw new FacesException("Failed to fetch enforced value", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#getLocalValue()
     */
    @Override
    public Object getLocalValue () {
        try {
            AbstractObjectEditor<?> parentEditor = this.getParentEditor();
            if ( parentEditor instanceof MultiObjectEditor ) {
                return resolve( ( (MultiObjectEditor) parentEditor ).getSelectedObject(), internalGetPath());
            }
            Object o = resolve(parentEditor.getCurrent(), internalGetPath());
            if ( log.isTraceEnabled() ) {
                log.trace(this.internalGetPath() + ": local value is " + o); //$NON-NLS-1$
            }
            return o;
        }
        catch ( Exception e ) {
            throw new FacesException("Failed to fetch local value", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.components.simplefield.SimpleField#setLocalValue(java.lang.Object)
     */
    @Override
    protected void setLocalValue ( Object val ) {
        try {
            this.cachedIsInheritedDefaultValue = null;
            this.cachedHaveLocalValue = null;
            Object obj;
            AbstractObjectEditor<?> parentEditor = this.getParentEditor();
            if ( parentEditor instanceof MultiObjectEditor ) {
                obj = ( (MultiObjectEditor) parentEditor ).getSelectedObject();
            }
            else {
                obj = parentEditor.getCurrent();
            }
            FacesContext.getCurrentInstance().getELContext().getELResolver()
                    .setValue(FacesContext.getCurrentInstance().getELContext(), obj, internalGetPath(), val);
        }
        catch ( Exception e ) {
            throw new FacesException("Failed to set local value", e); //$NON-NLS-1$
        }
    }


    /**
     * @param current
     * @param internalGetPath
     * @return
     */
    private static Object resolve ( Object current, String path ) {
        if ( current == null ) {
            return null;
        }

        if ( path == null || path.isEmpty() ) {
            return current;
        }

        Object resolved = FacesContext.getCurrentInstance().getELContext().getELResolver()
                .getValue(FacesContext.getCurrentInstance().getELContext(), current, path);

        return resolved;
    }

}
