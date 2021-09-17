/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.ConfigUtil;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 * 
 */
public class MultiObjectEditor extends AbstractObjectEditor<List<ConfigurationObject>> {

    private static final Logger log = Logger.getLogger(MultiObjectEditor.class);

    private static final String WAS_VISIBLE = "was-visible"; //$NON-NLS-1$
    private static final String SIMPLIFIED = "simplified"; //$NON-NLS-1$
    private static final String SIMPLIFIED_CHECK_LEVEL = "simplified-check-level"; //$NON-NLS-1$
    private static final String UNKNOWN_OBJECT_EDITOR_TYPE = "Unknown object editor type "; //$NON-NLS-1$
    private static final String NO_PATH_IS_SET = "No path is set"; //$NON-NLS-1$
    private static final String NOT_A_LIST_OR_SET = "Not a list or set"; //$NON-NLS-1$

    private static final String SELECTED_OBJECT = "selectedObject"; //$NON-NLS-1$
    private static final String TYPE = "objectType"; //$NON-NLS-1$
    private static final String LIST_LABEL = "listLabel"; //$NON-NLS-1$
    private static final String LIST_LABEL_EX = "listLabelEx"; //$NON-NLS-1$
    private static final String LIST_EDIT_MODE = "listEditMode"; //$NON-NLS-1$
    private static final String WRAPPED_SET = "wrappedSet"; //$NON-NLS-1$
    private static final String SELECTED_OBJECT_ID = "selectedId"; //$NON-NLS-1$
    private static final String COMPARATOR = "setComparator"; //$NON-NLS-1$
    private static final String CLONE = "clone"; //$NON-NLS-1$
    private static final String COLLECTION_TYPE = "collectionType"; //$NON-NLS-1$
    private static final String COLLECTION_TYPE_LIST = "list"; //$NON-NLS-1$
    private static final String COLLECTION_TYPE_SET = "set"; //$NON-NLS-1$

    private transient UUID defaultsCachedFor = null;
    private transient ConfigurationObject defaultsCached = null;
    private transient ConfigurationObject delaySelectObject;
    private transient ConfigurationObject cachedEnforcement;
    private transient Map<String, Integer> hiddenElementsCache;


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     * @see eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor#childrenAreReadOnly()
     */
    @Override
    public boolean childrenAreReadOnly () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return super.childrenAreReadOnly() || this.isReadOnlyValueSource();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UINamingContainer#isRendered()
     */
    @Override
    public boolean isRendered () {
        return super.isRendered() && !isHidden();
    }


    /**
     * @return whether the component is hidden based on the detail level
     */
    public boolean isHidden () {
        Boolean wasvisible = (Boolean) getStateHelper().eval(WAS_VISIBLE);
        if ( wasvisible != null && wasvisible ) {
            return false;
        }

        int minLevel = (int) this.getAttributes().get("minLevel"); //$NON-NLS-1$
        boolean hidden = false;
        try {
            hidden = minLevel > this.internalGetContext().getDetailLevel() && !this.hasLocalValues();
            if ( !hidden ) {
                getStateHelper().put(WAS_VISIBLE, true);
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return hidden;
    }


    @Override
    public boolean isSimplified () {
        Integer threshLevel = (Integer) getAttributes().get("simplifyLevel"); //$NON-NLS-1$
        if ( threshLevel == null || threshLevel < 0 ) {
            return false;
        }

        try {
            int detailLevel = this.internalGetContext().getDetailLevel();
            Boolean simplified = (Boolean) getStateHelper().eval(SIMPLIFIED);
            Integer simplifiedChecked = (Integer) getStateHelper().eval(SIMPLIFIED_CHECK_LEVEL);
            if ( simplified != null && simplifiedChecked == detailLevel ) {
                return simplified;
            }

            String expectName = (String) this.getAttributes().get("simplifyExpectName"); //$NON-NLS-1$
            getStateHelper().put(SIMPLIFIED_CHECK_LEVEL, detailLevel);
            if ( threshLevel <= detailLevel ) {
                getStateHelper().put(SIMPLIFIED, false);
                return false;
            }

            List<ConfigurationObject> current = getSelectOptions();

            int nvisible = 0;
            for ( ConfigurationObject co : current ) {
                String objectName = fetchObjectName(co);
                if ( isElementHidden(objectName, detailLevel) ) {
                    continue;
                }
                nvisible++;
                if ( StringUtils.isBlank(objectName) || ( expectName != null && !expectName.equals(objectName) ) ) {
                    getStateHelper().put(SIMPLIFIED, false);
                    return false;
                }
            }

            if ( nvisible != 1 ) {
                log.debug("Elements are visible " + nvisible); //$NON-NLS-1$
                getStateHelper().put(SIMPLIFIED, false);
                return false;
            }

            getStateHelper().put(SIMPLIFIED, true);
            return true;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return false;
        }
    }


    private boolean isElementHidden ( ConfigurationObject co, int detailLevel ) {
        return isElementHidden(fetchObjectName(co), detailLevel);
    }


    private boolean isElementHidden ( String name, int detailLevel ) {
        if ( StringUtils.isBlank(name) ) {
            return false;
        }
        Integer maxLevel = getHiddenElements().get(name);
        if ( maxLevel == null || ( maxLevel > 0 && detailLevel >= maxLevel ) ) {
            return false;
        }
        return true;
    }


    private Map<String, Integer> getHiddenElements () {
        if ( this.hiddenElementsCache == null ) {
            String elemSpec = (String) this.getAttributes().get("hiddenElements"); //$NON-NLS-1$
            if ( StringUtils.isBlank(elemSpec) ) {
                this.hiddenElementsCache = Collections.EMPTY_MAP;
                return this.hiddenElementsCache;
            }

            StringTokenizer tok = new StringTokenizer(elemSpec, ":"); //$NON-NLS-1$
            Map<String, Integer> hidden = new HashMap<>();
            while ( tok.hasMoreTokens() ) {
                String elem = tok.nextToken();
                int sep = elem.indexOf('<');
                if ( sep < 0 ) {
                    hidden.put(elem, -1);
                }
                else {
                    hidden.put(elem.substring(0, sep), Integer.parseInt(elem.substring(sep + 1)));
                }
            }
            this.hiddenElementsCache = hidden;

        }
        return this.hiddenElementsCache;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor#getDisplayTitle()
     */
    @Override
    public String getDisplayTitle () {
        if ( isSimplified() ) {
            String simplifiedTitle = (String) getAttributes().get("simplifiedTitle"); //$NON-NLS-1$
            if ( !StringUtils.isBlank(simplifiedTitle) ) {
                return simplifiedTitle;
            }
        }
        return super.getDisplayTitle();
    }


    /**
     * @param selectObject
     * @return title for the given object
     */
    public String getDisplayTitleFor ( ConfigurationObject selectObject ) {
        return getDisplayTitle();
    }


    /**
     * 
     * @return wether this editor has enforced values
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public boolean hasEnforcedValues () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        Collection<ConfigurationObject> enforced = this.getEnforced();
        return enforced != null && !enforced.isEmpty();
    }


    /**
     * 
     * @return whether this editor has inherited values
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public boolean hasInheritedValues () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        Collection<ConfigurationObject> inherited = this.getDefaults();
        return inherited != null && !inherited.isEmpty();
    }


    /**
     * 
     * @return whether this editor has local values
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public boolean hasLocalValues () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        Collection<ConfigurationObject> current = this.getCurrent();
        return current != null && !current.isEmpty();
    }


    /**
     * 
     * @return available objects
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public List<ConfigurationObject> getSelectOptions () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        List<ConfigurationObject> opts;
        if ( this.hasEnforcedValues() ) {
            opts = this.getEnforced();
        }
        else if ( this.isInEditMode() ) {
            opts = this.getCurrent();
        }
        else {
            opts = this.getDefaults();
        }
        return opts;
    }


    /**
     * 
     * @param obj
     * @return a proper copy of a potentially reference object from the list of selection items
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ConfigurationObject getFromSelectOptions ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        List<ConfigurationObject> options = getSelectOptions();

        int idx = options.indexOf(obj);

        if ( idx < 0 ) {
            log.warn("Option not found in select options " + obj); //$NON-NLS-1$
            return obj;
        }

        int otherIdx = options.lastIndexOf(obj);

        if ( otherIdx != idx ) {
            return obj;
        }

        return options.get(idx);
    }


    /**
     * @param enforced
     * @return
     */
    private List<ConfigurationObject> wrapCollectionReadOnly ( Collection<ConfigurationObject> values ) {

        if ( values == null ) {
            return Collections.EMPTY_LIST;
        }
        if ( values instanceof List ) {
            return (List<ConfigurationObject>) values;
        }
        else if ( values instanceof Set ) {
            List<ConfigurationObject> res = new ArrayList<>(values);
            Collections.sort(res, this.getSetComparator());
            return res;
        }

        throw new IllegalArgumentException("Unhandled collection type " + values); //$NON-NLS-1$
    }


    /**
     * 
     * @param obj
     * @return the label value for the given object
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public String proxyLabel ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( obj == null ) {
            return null;
        }

        MethodExpression labelMethod = (MethodExpression) this.getAttributes().get(LIST_LABEL);
        MethodExpression labelExMethod = (MethodExpression) this.getAttributes().get(LIST_LABEL_EX);
        if ( labelMethod == null ) {
            String objectName = fetchObjectName(obj);
            if ( objectName == null ) {
                objectName = GuiMessages.get(GuiMessages.UNNAMED_CONFIG_OBJECT);
            }
            if ( labelExMethod != null ) {
                labelExMethod.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] {
                    internalGetContext(), obj, objectName
                });
            }
            return objectName;
        }
        return (String) labelMethod.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] {
            this.getFromSelectOptions(obj)
        });
    }


    /**
     * @param obj
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    private String fetchObjectName ( ConfigurationObject obj ) {
        try {
            String objectName = ConfigUtil.getObjectName(obj);
            if ( objectName == null ) {
                ConfigurationObject defaults = internalGetContext().getObjectDefaults(ConfigUtil.getObjectTypeName(obj), obj);
                objectName = ConfigUtil.getObjectName(defaults);
            }
            return objectName;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    /**
     * 
     * @return this editor's effective values should be read only
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public boolean isReadOnlyValueSource () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.hasEnforcedValues() ) {
            return true;
        }

        if ( this.isInEditMode() ) {
            return false;
        }

        return true;
    }


    /**
     * Adds a new object
     * 
     * @param ev
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     * @throws ModelObjectNotFoundException
     */
    public void addNew ( ActionEvent ev ) throws ModelServiceException, GuiWebServiceException, ModelObjectNotFoundException {
        add(this.internalGetContext().getEmptyObject(getObjectType()));
    }


    /**
     * @param obj
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    private void add ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Add new object " + obj); //$NON-NLS-1$
        }
        processUpdates(FacesContext.getCurrentInstance());
        this.getCurrent().add(obj);
        this.setSelectedObjectInternal(obj);
    }


    public String addNewCustom ( Object o ) throws ModelServiceException, GuiWebServiceException, ModelObjectNotFoundException {
        if ( ! ( o instanceof ConfigurationObject ) ) {
            add(this.internalGetContext().getEmptyObject(getObjectType()));
            return null;
        }
        add((ConfigurationObject) o);
        return null;
    }


    protected String getObjectType () {
        return (String) this.getAttributes().get(TYPE);
    }


    /**
     * Removed the currently selected object
     * 
     * @param ev
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public void removeSelected ( ActionEvent ev ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        log.debug("Remove currently selected"); //$NON-NLS-1$
        this.getCurrent().remove(this.getSelectedObject());
        this.setSelectedObjectInternal(null);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        if ( log.isDebugEnabled() ) {
            log.debug("Resetting component state for " + internalGetPath()); //$NON-NLS-1$
        }
        try {
            this.setSelectedObjectInternal(null);
            this.getStateHelper().remove(WRAPPED_SET);
            getStateHelper().remove(SIMPLIFIED);
            getStateHelper().remove(SIMPLIFIED_CHECK_LEVEL);
        }
        catch ( Exception e ) {
            log.warn("Failed to reset selected object"); //$NON-NLS-1$
        }
        return super.resetComponent();
    }


    /**
     * Reset the editor to it's default values
     * 
     * @param ev
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public void resetToDefault ( ActionEvent ev ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        log.debug("Reset to default"); //$NON-NLS-1$
        Collection<?> c = resolveTargetCollection();
        if ( c != null ) {
            c.clear();
        }
        getStateHelper().remove(WRAPPED_SET);
        getStateHelper().remove(SELECTED_OBJECT);
        getStateHelper().remove(SIMPLIFIED);
        getStateHelper().remove(SIMPLIFIED_CHECK_LEVEL);
        getStateHelper().put(LIST_EDIT_MODE, false);
        setSelectedObjectInternal(null);
    }


    /**
     * Enter edit mode
     * 
     * @param ev
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public void doEdit ( ActionEvent ev ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        this.getStateHelper().put(LIST_EDIT_MODE, true);
        this.setSelectedObjectInternal(null);

        List<ConfigurationObject> inherited = this.getDefaults();
        for ( ConfigurationObject obj : inherited ) {
            this.getCurrent().add(cloneValue(obj));
        }
    }


    /**
     * @param effective
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     */
    private ConfigurationObject cloneValue ( ConfigurationObject effective ) throws ModelServiceException, GuiWebServiceException {
        MethodExpression cloneMethod = getCloneMethod();

        if ( cloneMethod == null ) {
            return cloneWithInherits(effective);
        }

        return (ConfigurationObject) cloneMethod.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] {
            internalGetContext(), effective
        });
    }


    private MethodExpression getCloneMethod () {
        return (MethodExpression) getAttributes().get(CLONE);
    }


    /**
     * @param obj
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     */
    private ConfigurationObject cloneWithInherits ( ConfigurationObject obj ) throws ModelServiceException, GuiWebServiceException {
        ConfigurationObjectMutable cloned = (ConfigurationObjectMutable) this.internalGetContext().getEmptyObject(ConfigUtil.getObjectTypeName(obj));
        cloned.setInherits(obj);
        return cloned;
    }


    /**
     * 
     * @return whether this field is currently in edit-mode
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public boolean isInEditMode () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        Object res = this.getStateHelper().get(LIST_EDIT_MODE);
        return valueNeedsEdit(res) || ( res != null && (boolean) res );
    }


    private boolean valueNeedsEdit ( Object res ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return res == null && ( this.hasLocalValues() || !this.hasInheritedValues() );
    }


    /**
     * 
     * @return the currently selected object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ConfigurationObject getSelectedObject () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        int detailLevel = this.internalGetContext().getDetailLevel();
        if ( isInEditMode() ) {
            UUID selectedId = (UUID) this.getStateHelper().get(SELECTED_OBJECT_ID);
            List<ConfigurationObject> current = this.getCurrent();

            if ( selectedId == null ) {
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("No object selected in %s", this.internalGetPath())); //$NON-NLS-1$
                }

                if ( current.isEmpty() ) {
                    return null;
                }

                for ( ConfigurationObject co : current ) {
                    if ( !isElementHidden(co, detailLevel) ) {
                        selectedId = co.getId();
                        break;
                    }
                }

                if ( selectedId == null ) {
                    // only have hidden elements
                    return null;
                }
            }

            for ( ConfigurationObject obj : current ) {
                if ( selectedId.equals(obj.getId()) ) {
                    return obj;
                }
            }

            return null;
        }

        ConfigurationObject obj = (ConfigurationObject) this.getStateHelper().get(SELECTED_OBJECT);

        if ( obj != null ) {
            return this.getFromSelectOptions(obj);
        }
        List<ConfigurationObject> opts = getSelectOptions();

        for ( ConfigurationObject co : opts ) {
            if ( !isElementHidden(co, detailLevel) ) {
                return co;
            }
        }
        return null;
    }


    /**
     * Listener called when the selected value is changed on client
     * 
     * @param ev
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public void objectChangedListener ( AjaxBehaviorEvent ev ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( log.isTraceEnabled() ) {
            log.trace("Object changed event " + ev); //$NON-NLS-1$
        }
        this.setSelectedObjectInternal(this.delaySelectObject);
        this.delaySelectObject = null;
    }


    public boolean hasDelaySelectedObject () {
        return this.delaySelectObject != null;
    }


    /**
     * 
     * @param obj
     */
    public void setSelectedObject ( ConfigurationObject obj ) {
        log.trace("Queuing selected value " + obj); //$NON-NLS-1$
        this.delaySelectObject = obj;
    }


    /**
     * 
     * @param obj
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public void setSelectedObjectInternal ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        if ( log.isTraceEnabled() ) {
            log.trace("Setting selected value to " + obj); //$NON-NLS-1$
        }

        UUID oldid = (UUID) this.getStateHelper().get(SELECTED_OBJECT_ID);

        if ( obj == null ) {
            this.getStateHelper().remove(SELECTED_OBJECT_ID);
            this.getStateHelper().remove(SELECTED_OBJECT);
        }
        else if ( isInEditMode() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Selected from local values " + obj); //$NON-NLS-1$
            }

            this.getStateHelper().put(SELECTED_OBJECT_ID, obj.getId());
            this.getStateHelper().remove(SELECTED_OBJECT);
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug("Selected inherited value " + obj); //$NON-NLS-1$
            }

            if ( !obj.equals(this.getStateHelper().get(SELECTED_OBJECT)) ) {
                this.getStateHelper().put(SELECTED_OBJECT, this.getFromSelectOptions(obj));
            }
            this.getStateHelper().remove(SELECTED_OBJECT_ID);
        }

        this.getStateHelper().remove(SIMPLIFIED);
        this.getStateHelper().remove(SIMPLIFIED_CHECK_LEVEL);

        if ( oldid == null || !oldid.equals(this.getStateHelper().get(SELECTED_OBJECT_ID)) ) {
            this.resetChildren();
        }
    }


    /**
     * Listener called when an outer inherits relationship has been changed
     * 
     * @param ev
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public void changedInherits ( AjaxBehaviorEvent ev ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        inheritanceChanged(this);
    }


    @Override
    public void inheritanceChanged ( AbstractObjectEditor<?> objectEditor )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        log.debug("Inheritance changed"); //$NON-NLS-1$
        this.defaultsCached = null;
        this.defaultsCachedFor = null;
        this.getStateHelper().remove(SELECTED_OBJECT_ID);
        this.getStateHelper().remove(SELECTED_OBJECT);
        this.getStateHelper().remove(SIMPLIFIED);
        this.getStateHelper().remove(SIMPLIFIED_CHECK_LEVEL);
    }


    /**
     * 
     * @return the applied defaults for the selected object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ConfigurationObject getSelectedObjectDefaults () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        ConfigurationObject selectedObject = this.getSelectedObject();

        if ( selectedObject == null ) {
            return null;
        }

        if ( haveCachedDefaults(selectedObject) ) {
            return this.defaultsCached;
        }

        log.debug("Loading defaults"); //$NON-NLS-1$
        ConfigurationObject defaults = internalGetContext().getObjectDefaults(getObjectType(), selectedObject);
        this.defaultsCachedFor = selectedObject.getId();
        this.defaultsCached = defaults;
        return defaults;
    }


    private boolean haveCachedDefaults ( ConfigurationObject selectedObject ) {
        if ( this.defaultsCachedFor == null && selectedObject.getId() == null && this.defaultsCached != null ) {
            return true;
        }

        return this.defaultsCachedFor != null && this.defaultsCachedFor.equals(selectedObject.getId());
    }


    /**
     * 
     * @return the inherited values for the currently selected object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     * @todo Unimplemented, do when needed
     */
    public ConfigurationObject getSelectedObjectInherited () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.getSelectedLocalInherits();
    }


    /**
     * 
     * @return the enforced values for the currently selected object
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ConfigurationObject getSelectedObjectEnforced () throws ModelServiceException, GuiWebServiceException {
        // TODO: implement when really needed
        if ( this.cachedEnforcement == null ) {
            this.cachedEnforcement = internalGetContext().getEmptyObject(getObjectType());
        }
        return this.cachedEnforcement;
    }


    /**
     * 
     * @return the directly inherited object of the selected object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ConfigurationObject getSelectedLocalInherits () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject selectedObject = this.getSelectedObject();
        if ( selectedObject != null ) {
            return selectedObject.getInherits();
        }
        return null;
    }


    /**
     * 
     * @param obj
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public void setSelectedLocalInherits ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Setting selected object inherits" + obj); //$NON-NLS-1$
        }
        AbstractConfigurationObject<?> selectedObject = (AbstractConfigurationObject<?>) this.getSelectedObject();
        if ( selectedObject != null ) {
            selectedObject.setInherits(obj);
        }
    }


    public void resetLocalInherits ( SelectEvent ev ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        resetLocalInherits();
    }


    public void resetLocalInherits () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject inherited = this.getSelectedLocalInherits();
        if ( inherited != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Inherited values modified " + inherited); //$NON-NLS-1$
            }
            setInheritsModified(true);
        }
    }


    /**
     * 
     * @return the label for the default inheritance option
     */
    public String getDefaultsDisplayName () {
        return GuiMessages.get(GuiMessages.OBJEDIT_INHERIT_DEFAULT);
    }


    /**
     * 
     * @return templates that may be used for inheritance in the selected object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public List<ConfigurationObjectReference> getEligibleTemplates ()
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject obj = this.getSelectedObject();
        List<ConfigurationObjectReference> tpls = this.internalGetContext().getEligibleTemplates(obj);
        if ( obj != null ) {
            ConfigurationObject inh = obj.getInherits();
            if ( inh != null ) {
                if ( inh instanceof ConfigurationObjectReference ) {
                    tpls.add((ConfigurationObjectReference) inh);
                }
                else {
                    tpls.add(new ConfigurationObjectReference(inh));
                }
            }
        }
        return tpls;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor#getCurrent()
     */
    @Override
    public List<ConfigurationObject> getCurrent () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        AbstractObjectEditor<?> parentEditor = getParentEditor();
        if ( Set.class.isAssignableFrom(getCollectionTypeClass()) ) {
            return wrapSet(parentEditor);
        }

        return wrapList(parentEditor);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        ConfigurationObject selectedObject;
        try {
            selectedObject = this.getSelectedObject();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }

        AbstractObjectEditor<?> parentEditor = getParentEditor();

        if ( parentEditor == null ) {
            if ( selectedObject == null ) {
                return this.internalGetPath();
            }
            return "obj:" + selectedObject.getId().toString(); //$NON-NLS-1$
        }
        else if ( selectedObject == null ) {
            return parentEditor.getAbsolutePath() + "/" + this.internalGetPath(); //$NON-NLS-1$
        }
        return parentEditor.getAbsolutePath() + "/" + this.internalGetPath() + "/col:" + selectedObject.getId(); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * @param parentEditor
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private List<ConfigurationObject> wrapList ( AbstractObjectEditor<?> parentEditor )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        log.trace("Value is a list"); //$NON-NLS-1$
        if ( parentEditor instanceof ObjectEditor ) {
            return wrapCollectionReadOnly(
                resolveReadWriteList( ( (AbstractObjectEditor<ConfigurationObject>) parentEditor ).getCurrent(), internalGetPath()));
        }
        else if ( parentEditor instanceof MultiObjectEditor ) {
            return wrapCollectionReadOnly(resolveReadWriteList( ( (MultiObjectEditor) parentEditor ).getSelectedObject(), internalGetPath()));
        }

        throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR_TYPE + parentEditor);
    }


    /**
     * @param parentEditor
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    private List<ConfigurationObject> wrapSet ( AbstractObjectEditor<?> parentEditor )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        @SuppressWarnings ( "unchecked" )
        List<ConfigurationObject> wrapped = (List<ConfigurationObject>) this.getStateHelper().get(WRAPPED_SET);

        if ( wrapped != null ) {
            return wrapped;
        }

        wrapped = getWrappedSet(parentEditor);
        Collections.sort(wrapped, this.getSetComparator());
        this.getStateHelper().put(WRAPPED_SET, wrapped);
        return wrapped;
    }


    /**
     * @param parentEditor
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private List<ConfigurationObject> getWrappedSet ( AbstractObjectEditor<?> parentEditor )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        List<ConfigurationObject> wrapped;
        Set<ConfigurationObject> resolveSet;
        ConfigurationObject current;
        if ( parentEditor instanceof ObjectEditor ) {
            log.trace("outer editor is a objecteditor"); //$NON-NLS-1$
            current = ( (AbstractObjectEditor<ConfigurationObject>) parentEditor ).getCurrent();
        }
        else if ( parentEditor instanceof MultiObjectEditor ) {
            log.trace("outer editor is a multiobjecteditor"); //$NON-NLS-1$
            current = ( (MultiObjectEditor) parentEditor ).getSelectedObject();
        }
        else {
            throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR_TYPE + parentEditor);
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Outer object is " + current); //$NON-NLS-1$
        }

        resolveSet = resolveSet(current, internalGetPath());

        if ( log.isTraceEnabled() ) {
            log.trace("Wrapping set " + resolveSet); //$NON-NLS-1$
        }

        if ( resolveSet != null ) {
            wrapped = new ArrayList<>(resolveSet);
        }
        else {
            wrapped = new ArrayList<>();
        }
        return wrapped;
    }


    private Comparator<ConfigurationObject> getSetComparator () {
        @SuppressWarnings ( "unchecked" )
        Comparator<ConfigurationObject> comp = (Comparator<ConfigurationObject>) this.getAttributes().get(COMPARATOR);
        if ( comp == null ) {
            throw new FacesException("Value is set-valued but no setComparator is specified"); //$NON-NLS-1$
        }
        return comp;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIComponentBase#processDecodes(javax.faces.context.FacesContext)
     */
    @Override
    public void processDecodes ( FacesContext ctx ) {
        super.processDecodes(ctx);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.component.UIComponentBase#processUpdates(javax.faces.context.FacesContext)
     */
    @Override
    public void processUpdates ( FacesContext ctx ) {
        try {
            log.trace("Processing updates"); //$NON-NLS-1$
            super.processUpdates(ctx);

            if ( this.delaySelectObject != null ) {
                log.trace("Setting delayed selected object after processing updates"); //$NON-NLS-1$
                this.setSelectedObjectInternal(this.delaySelectObject);
                super.processUpdates(ctx);
                this.delaySelectObject = null;
            }

            processUpdatesInternal();
        }
        catch ( Exception e ) {
            throw new FacesException("Failed to update wrapped set", e); //$NON-NLS-1$
        }

    }


    /**
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    private void processUpdatesInternal () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        // convert wrapped set back
        if ( Set.class.isAssignableFrom(getCollectionTypeClass()) ) {
            @SuppressWarnings ( "unchecked" )
            List<ConfigurationObject> wrapped = (List<ConfigurationObject>) this.getStateHelper().get(WRAPPED_SET);
            Set<ConfigurationObject> newSet = new HashSet<>();

            if ( wrapped != null ) {
                newSet.addAll(wrapped);
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Updating set value to " + newSet); //$NON-NLS-1$
            }

            AbstractObjectEditor<?> parentEditor = getParentEditor();
            Object base;
            if ( parentEditor instanceof ObjectEditor ) {
                base = parentEditor.getCurrent();
            }
            else if ( parentEditor instanceof MultiObjectEditor ) {
                base = ( (MultiObjectEditor) parentEditor ).getSelectedObject();
            }
            else {
                throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR_TYPE + parentEditor);
            }

            FacesContext.getCurrentInstance().getELContext().getELResolver()
                    .setValue(FacesContext.getCurrentInstance().getELContext(), base, internalGetPath(), newSet);
        }
    }


    /**
     * 
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    protected Collection<?> resolveTargetCollection () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        AbstractObjectEditor<?> parentEditor = getParentEditor();
        Object base;
        if ( parentEditor instanceof ObjectEditor ) {
            base = parentEditor.getCurrent();
        }
        else if ( parentEditor instanceof MultiObjectEditor ) {
            base = ( (MultiObjectEditor) parentEditor ).getSelectedObject();
        }
        else {
            throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR_TYPE + parentEditor);
        }

        return (Collection<?>) FacesContext.getCurrentInstance().getELContext().getELResolver()
                .getValue(FacesContext.getCurrentInstance().getELContext(), base, internalGetPath());
    }


    @SuppressWarnings ( "unchecked" )
    @Override
    public List<ConfigurationObject> getDefaults () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        AbstractObjectEditor<?> parentEditor = getParentEditor();

        if ( parentEditor instanceof ObjectEditor ) {
            return wrapCollectionReadOnly(
                resolveReadOnly( ( (AbstractObjectEditor<ConfigurationObject>) parentEditor ).getDefaults(), internalGetPath()));
        }
        else if ( parentEditor instanceof MultiObjectEditor ) {
            return wrapCollectionReadOnly(resolveReadOnly( ( (MultiObjectEditor) parentEditor ).getSelectedObjectDefaults(), internalGetPath()));
        }

        throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR_TYPE + parentEditor);
    }


    @SuppressWarnings ( "unchecked" )
    @Override
    public List<ConfigurationObject> getEnforced () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        AbstractObjectEditor<?> parentEditor = getParentEditor();

        if ( parentEditor instanceof ObjectEditor ) {
            return wrapCollectionReadOnly(
                resolveReadOnly( ( (AbstractObjectEditor<ConfigurationObject>) parentEditor ).getEnforced(), internalGetPath()));
        }
        else if ( parentEditor instanceof MultiObjectEditor ) {
            return wrapCollectionReadOnly(resolveReadOnly( ( (MultiObjectEditor) parentEditor ).getSelectedObjectEnforced(), internalGetPath()));
        }

        throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR_TYPE + parentEditor);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor#getEffective()
     */
    @Override
    public List<ConfigurationObject> getEffective () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return getSelectOptions();
    }


    /**
     * 
     * @return the collection type
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public Class<?> getCollectionTypeClass () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        String collectionTypeAttr = (String) this.getAttributes().get(COLLECTION_TYPE);

        if ( COLLECTION_TYPE_LIST.equals(collectionTypeAttr) ) {
            return List.class;
        }
        else if ( COLLECTION_TYPE_SET.equals(collectionTypeAttr) ) {
            return Set.class;
        }

        AbstractObjectEditor<?> parentEditor = getParentEditor();

        Object base;
        if ( parentEditor instanceof ObjectEditor ) {
            base = parentEditor.getCurrent();
        }
        else if ( parentEditor instanceof MultiObjectEditor ) {
            base = ( (MultiObjectEditor) parentEditor ).getSelectedObject();
        }
        else {
            throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR_TYPE + parentEditor);
        }

        Class<?> type = FacesContext.getCurrentInstance().getELContext().getELResolver()
                .getType(FacesContext.getCurrentInstance().getELContext(), base, internalGetPath());

        if ( !Collection.class.isAssignableFrom(type) ) {
            throw new IllegalArgumentException(String.format("'%s' is not a collection type: %s", internalGetPath(), type.getName())); //$NON-NLS-1$
        }

        if ( !List.class.isAssignableFrom(type) && !Set.class.isAssignableFrom(type) ) {
            throw new FacesException(NOT_A_LIST_OR_SET);
        }

        return type;
    }


    /**
     * @param current
     * @param internalGetPath
     * @return
     */
    private List<ConfigurationObject> resolveReadOnly ( ConfigurationObject current, String path ) {
        if ( current == null ) {
            return null;
        }

        if ( path == null || path.isEmpty() ) {
            throw new FacesException(NO_PATH_IS_SET);
        }

        Object obj = FacesContext.getCurrentInstance().getELContext().getELResolver()
                .getValue(FacesContext.getCurrentInstance().getELContext(), current, path);

        return mapCollection(obj);
    }


    /**
     * @param obj
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private List<ConfigurationObject> mapCollection ( Object obj ) {
        if ( obj == null ) {
            return new ArrayList<>();
        }
        else if ( obj instanceof List<?> ) {
            return (List<ConfigurationObject>) obj;
        }
        else if ( obj instanceof Set<?> ) {
            List<ConfigurationObject> res = new ArrayList<>((Set<ConfigurationObject>) obj);
            Collections.sort(res, this.getSetComparator());
            return res;
        }
        else {
            throw new FacesException(NOT_A_LIST_OR_SET);
        }
    }


    @SuppressWarnings ( "unchecked" )
    private static List<ConfigurationObject> resolveReadWriteList ( ConfigurationObject current, String path ) {
        if ( current == null ) {
            return null;
        }

        if ( path == null || path.isEmpty() ) {
            throw new FacesException(NO_PATH_IS_SET);
        }

        Object obj = FacesContext.getCurrentInstance().getELContext().getELResolver()
                .getValue(FacesContext.getCurrentInstance().getELContext(), current, path);

        if ( obj == null ) {
            List<ConfigurationObject> newObj = new ArrayList<>();
            FacesContext.getCurrentInstance().getELContext().getELResolver()
                    .setValue(FacesContext.getCurrentInstance().getELContext(), current, path, newObj);
            return newObj;
        }

        return (List<ConfigurationObject>) obj;
    }


    @SuppressWarnings ( "unchecked" )
    private static Set<ConfigurationObject> resolveSet ( ConfigurationObject current, String path ) {
        if ( current == null ) {
            return null;
        }

        if ( path == null || path.isEmpty() ) {
            throw new FacesException(NO_PATH_IS_SET);
        }

        Object obj = FacesContext.getCurrentInstance().getELContext().getELResolver()
                .getValue(FacesContext.getCurrentInstance().getELContext(), current, path);

        if ( obj == null ) {
            return new HashSet<>();
        }

        return (Set<ConfigurationObject>) obj;
    }

}
