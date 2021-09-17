/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
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
 * 
 */
public class ObjectEditor extends AbstractObjectEditor<ConfigurationObject> {

    /**
     * 
     */
    private static final String UNKNOWN_OBJECT_EDITOR = "Unknown object editor "; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(ObjectEditor.class);
    private static final String ORIGINAL_INHERITS = "originalInherits"; //$NON-NLS-1$

    private transient Map<UUID, ConfigurationObject> cachedEffectiveInherits = new ConcurrentHashMap<>();
    private transient ConfigurationObject cachedInherits;
    private transient Boolean cachedHaveLocalInherits;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        this.getStateHelper().remove(ORIGINAL_INHERITS);
        return super.resetComponent();
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
        int minLevel = (int) this.getAttributes().get("minLevel"); //$NON-NLS-1$
        boolean hidden = minLevel > this.internalGetContext().getDetailLevel() && !this.hasVisibleChildren();
        if ( hidden ) {
            log.debug("is hidden " + this.internalGetPath()); //$NON-NLS-1$
        }
        else {
            log.debug("not hidden " + this.internalGetPath()); //$NON-NLS-1$
        }
        return hidden;
    }


    /**
     * @return the originalInherits
     */
    public ConfigurationObject getOriginalInherits () {
        return (ConfigurationObject) this.getStateHelper().get(ORIGINAL_INHERITS);
    }


    /**
     * 
     * @param obj
     */
    public void setOriginalInherits ( ConfigurationObject obj ) {
        this.getStateHelper().put(ORIGINAL_INHERITS, obj);
    }


    /**
     * 
     * @return the display name for the defaults entry (i.e. when no template is selected)
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public String getDefaultsDisplayName () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject structureDefaults = this.getStructuralDefaults();

        if ( structureDefaults != null && !ConfigUtil.isAnonymous(structureDefaults) ) {
            return GuiMessages.format(
                GuiMessages.OBJEDIT_INHERIT_DEFAULT_DETAIL,
                this.internalGetContext().getConfigLocalizer().getTypeName(ConfigUtil.getObjectTypeName(structureDefaults)));
        }

        return GuiMessages.get(GuiMessages.OBJEDIT_INHERIT_DEFAULT);
    }


    /**
     * 
     * @return the local configuration object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public ConfigurationObject getCurrent () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject res = null;
        AbstractObjectEditor<?> parent = this.getParentEditor();
        String mypath = this.internalGetPath();
        if ( parent != null ) {
            if ( parent instanceof ObjectEditor ) {
                res = resolve( ( (AbstractObjectEditor<ConfigurationObject>) parent ).getCurrent(), mypath);
            }
            else if ( parent instanceof MultiObjectEditor ) {
                res = resolve( ( (MultiObjectEditor) parent ).getSelectedObject(), mypath);
            }
            else {
                throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR + parent);
            }
            if ( res == null && log.isDebugEnabled() ) {
                log.debug("Result is null at " + mypath); //$NON-NLS-1$
            }

            return res;
        }

        ConfigurationObject toplevel = this.internalGetContext().getCurrent();
        res = resolve(toplevel, mypath);

        if ( res == null && log.isDebugEnabled() ) {
            log.debug("Result is null at " + mypath); //$NON-NLS-1$
        }

        return res;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private ConfigurationObject getParentInherited () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        AbstractObjectEditor<?> parent = this.getParentEditor();
        if ( parent != null ) {
            if ( parent instanceof ObjectEditor ) {
                return resolve( ( (ObjectEditor) parent ).getInherits(), this.internalGetPath());
            }
            else if ( parent instanceof MultiObjectEditor ) {
                return resolve( ( (MultiObjectEditor) parent ).getSelectedObjectInherited(), this.internalGetPath());
            }
            else {
                throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR + parent);
            }
        }

        return this.getStructuralDefaults();
    }


    /**
     * 
     * @return the inherited object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ConfigurationObject getInherits () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return getLocalInherits();
    }


    public void resetLocalInherits ( SelectEvent ev ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        resetLocalInherits();
    }


    public void resetLocalInherits () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject inherited = this.getInherits();
        if ( inherited != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Inherited values modified " + inherited); //$NON-NLS-1$
            }
            this.setInheritsModified(true);
            this.cachedEffectiveInherits.remove(inherited.getId());
        }
    }


    public boolean hasLocalInherits () {
        if ( this.cachedHaveLocalInherits != null ) {
            return this.cachedHaveLocalInherits;
        }

        try {
            this.cachedHaveLocalInherits = this.getLocalInherits() != null;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            this.cachedHaveLocalInherits = false;
        }
        return this.cachedHaveLocalInherits;
    }


    /**
     * 
     * @return the locally inherited object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ConfigurationObject getLocalInherits () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject current = this.getCurrent();
        if ( current != null && current.getInherits() != null ) {
            // use the locally specified inheritance
            if ( log.isTraceEnabled() ) {
                log.trace("Using locally set inheritance from " + current); //$NON-NLS-1$
                log.trace("Inherits is " + current.getInherits()); //$NON-NLS-1$
            }
            return current.getInherits();
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
    public void setLocalInherits ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        this.cachedHaveLocalInherits = null;
        trackInheritanceChanges(obj);

        ConfigurationObject oldInherits = null;
        ConfigurationObject current = this.getCurrent();
        if ( current != null ) {
            oldInherits = current.getInherits();
        }
        else {
            log.debug("Current is unset"); //$NON-NLS-1$
            return;
        }
        if ( objectsAreEqual(oldInherits, obj) ) {
            log.debug("Inherits is equal"); //$NON-NLS-1$
            return;
        }

        ConfigurationObject fetched = obj;

        if ( obj instanceof ConfigurationObjectReference ) {
            fetched = this.internalGetContext().fetch((ConfigurationObjectReference) obj);
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Setting inherits to %s from %s", obj, oldInherits)); //$NON-NLS-1$
        }

        ( (ConfigurationObjectMutable) current ).setInherits(fetched);

        this.notifyChildren(this, oldInherits, fetched);
    }


    protected void notifyChildren ( UIComponent comp, ConfigurationObject oldInherits, ConfigurationObject newInherits )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        java.util.Iterator<UIComponent> children = comp.getFacetsAndChildren();

        while ( children != null && children.hasNext() ) {
            UIComponent child = children.next();

            if ( child instanceof AbstractObjectEditor ) {
                ( (AbstractObjectEditor<?>) child ).inheritanceChanged(this);
                continue;
            }
            else if ( child instanceof ObjectFieldEditor ) {
                ( (ObjectFieldEditor) child ).inheritanceChanged(this);
            }

            notifyChildren(child, oldInherits, newInherits);
        }
    }


    /**
     * Called by parents if their inherited object changed
     * 
     * @param changedParent
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @Override
    public void inheritanceChanged ( AbstractObjectEditor<?> changedParent )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Got inheritance changed event from " + changedParent.getClientId()); //$NON-NLS-1$
        }

        this.cachedHaveLocalInherits = null;
        this.setLocalInherits(this.getParentInherited());
    }


    private void trackInheritanceChanges ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        // TODO: this does not notice when the object is saved, this could be optimized
        if ( !this.isInheritsChanged() ) {
            this.setOriginalInherits(this.getLocalInherits());
        }

        if ( !this.isInheritsChanged() && !objectsAreEqual(this.getOriginalInherits(), obj) ) {
            this.setInheritsChanged(true);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Inherits modified, original %s now %s", this.getOriginalInherits(), obj)); //$NON-NLS-1$
            }
        }
        else if ( this.isInheritsChanged() && objectsAreEqual(this.getOriginalInherits(), obj) ) {
            this.setInheritsChanged(false);
            log.trace("Inherits returned to default"); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @return the templates to list for selection
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public List<ConfigurationObjectReference> getEligibleTemplates ()
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject obj = this.getCurrent();
        List<ConfigurationObjectReference> eligibleTemplates = this.internalGetContext().getEligibleTemplates(obj);
        if ( obj != null ) {
            ConfigurationObject inh = obj.getInherits();
            if ( inh != null ) {
                if ( inh instanceof ConfigurationObjectReference ) {
                    eligibleTemplates.add((ConfigurationObjectReference) inh);
                }
                else {
                    eligibleTemplates.add(new ConfigurationObjectReference(inh));
                }
            }
        }
        return eligibleTemplates;
    }


    /**
     * @param current
     * @param internalGetPath
     * @return
     */
    private static ConfigurationObject resolve ( ConfigurationObject current, String path ) {
        if ( current == null ) {
            return null;
        }

        if ( path == null || path.isEmpty() ) {
            return current;
        }

        String[] components = StringUtils.split(path, '/');

        ConfigurationObject cur = current;
        if ( log.isTraceEnabled() ) {
            log.trace("Resolving path " + path); //$NON-NLS-1$
        }

        for ( int i = 0; i < components.length; i++ ) {
            String component = components[ i ];

            if ( component.startsWith("obj:") ) { //$NON-NLS-1$
                String objId = component.substring(4);
                if ( cur.getId() == null || objId.equals(cur.getId().toString()) ) {
                    continue;
                }
                throw new FacesException(String.format("Invalid object id, have %s expected %s", cur.getId(), objId)); //$NON-NLS-1$
            }

            String peek = ( i + 1 < components.length ) ? components[ i + 1 ] : null;
            if ( peek != null && peek.startsWith("col:") ) { //$NON-NLS-1$
                String objId = peek.substring(4);
                log.trace("Next element is a collection"); //$NON-NLS-1$
                @SuppressWarnings ( "unchecked" )
                Collection<ConfigurationObject> col = (Collection<ConfigurationObject>) FacesContext.getCurrentInstance().getELContext()
                        .getELResolver().getValue(FacesContext.getCurrentInstance().getELContext(), cur, component);

                if ( col == null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Failed to resolve %s, collection is null: %s", objId, col)); //$NON-NLS-1$
                    }
                    return null;
                }

                ConfigurationObject found = null;
                for ( ConfigurationObject obj : col ) {
                    if ( objId.equals(obj.getId().toString()) ) {
                        found = obj;
                    }
                }

                if ( found == null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Failed to resolve %s, item not found in collection %s", objId, col)); //$NON-NLS-1$
                    }
                    return null;
                }

                cur = found;
                i++;
            }
            else {
                cur = (ConfigurationObject) FacesContext.getCurrentInstance().getELContext().getELResolver()
                        .getValue(FacesContext.getCurrentInstance().getELContext(), cur, component);

                if ( cur == null ) {
                    return cur;
                }
            }
        }

        return cur;
    }


    /**
     * 
     * @return the values to use if not locally overridden (inherited or structural default)
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @Override
    public ConfigurationObject getDefaults () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( !this.isInheritsChanged() && !this.isInheritsModified() ) {
            log.trace("Returning unchanged defaults"); //$NON-NLS-1$
            return getUnchangedDefaults();
        }

        log.trace("Returning modified defaults"); //$NON-NLS-1$
        return getModifiedDefaults();
    }


    private ConfigurationObject getModifiedDefaults () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        // inheritance either specified locally, or per default via outer object
        if ( this.getInherits() != null ) {
            // if inheritance is locally specified, it takes precedence
            log.trace("Inheriting locally"); //$NON-NLS-1$
            return getEffectiveInherits(this.getInherits());
        }

        // the structural defaults apply
        log.trace("Inheriting structural defaults"); //$NON-NLS-1$
        return getStructuralDefaults();
    }


    @SuppressWarnings ( "unchecked" )
    private ConfigurationObject getUnchangedDefaults () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        // the inheritance has not changed .. we can use the parent provided effective inherited values
        AbstractObjectEditor<?> parent = this.getParentEditor();

        // if the parent's inherits was modified it's effective values no longer reflect the actual object state
        if ( parent != null && !parent.isInheritsModified() ) {
            log.trace("Resolving defaults from parent"); //$NON-NLS-1$
            if ( parent instanceof ObjectEditor ) {
                return resolve( ( (AbstractObjectEditor<ConfigurationObject>) parent ).getDefaults(), this.internalGetPath());
            }
            else if ( parent instanceof MultiObjectEditor ) {
                return resolve( ( (MultiObjectEditor) parent ).getSelectedObjectDefaults(), this.internalGetPath());
            }
            else {
                throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR + parent);
            }
        }

        log.trace("Using server provided inherited values as defaults"); //$NON-NLS-1$
        return this.getInherited();
    }


    /**
     * 
     * @return the inherited values for the local object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ConfigurationObject getInherited () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        AbstractObjectEditor<?> parent = this.getParentEditor();
        String path = this.internalGetPath();
        if ( parent != null ) {
            if ( parent instanceof ObjectEditor ) {
                return resolve( ( (ObjectEditor) parent ).getInherited(), path);
            }
            else if ( parent instanceof MultiObjectEditor ) {
                return resolve( ( (MultiObjectEditor) parent ).getSelectedObjectInherited(), path);
            }
            else {
                throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR + parent);
            }
        }

        ConfigurationObject inherited = this.internalGetContext().getInherited();

        if ( inherited != null ) {
            ConfigurationObject resolved = resolve(inherited, path);

            if ( resolved != null ) {
                return resolved;
            }
        }

        return this.getStructuralDefaults();
    }


    /**
     * 
     * @return the structural defaults for the local object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    private ConfigurationObject getStructuralDefaults () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        AbstractObjectEditor<?> parent = this.getParentEditor();
        if ( parent != null ) {
            if ( parent instanceof ObjectEditor ) {
                return resolve( ( (ObjectEditor) parent ).getStructuralDefaults(), this.internalGetPath());
            }
            else if ( parent instanceof MultiObjectEditor ) {
                return resolve( ( (MultiObjectEditor) parent ).getSelectedObjectDefaults(), this.internalGetPath());
            }
            else {
                throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR + parent);
            }
        }

        ConfigurationObject def = resolve(this.internalGetContext().getDefaults(), this.internalGetPath());
        if ( def != null ) {
            return def;
        }

        if ( this.cachedInherits == null ) {
            this.cachedInherits = this.internalGetContext().getObjectDefaults(internalGetObjectType(), getCurrent());
        }

        return this.cachedInherits;
    }


    private ConfigurationObject getEffectiveInherits ( ConfigurationObject obj )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        UUID inheritsId = obj.getId();
        ConfigurationObject effectiveInherits = this.cachedEffectiveInherits.get(inheritsId);

        if ( effectiveInherits == null ) {
            ConfigurationObjectReference ref;
            if ( obj instanceof ConfigurationObjectReference ) {
                ref = (ConfigurationObjectReference) obj;
            }
            else {
                ref = new ConfigurationObjectReference(obj);
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Loading effective inherits " + obj); //$NON-NLS-1$
            }
            effectiveInherits = this.internalGetContext().getTemplateEffective(ref);
            this.cachedEffectiveInherits.put(inheritsId, effectiveInherits);
        }

        return effectiveInherits;

    }


    /**
     * 
     * @return the enforced values for the local object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public ConfigurationObject getEnforced () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        AbstractObjectEditor<?> parent = this.getParentEditor();

        if ( parent != null ) {
            if ( parent instanceof ObjectEditor ) {
                return resolve( ( (AbstractObjectEditor<ConfigurationObject>) parent ).getEnforced(), this.internalGetPath());
            }
            else if ( parent instanceof MultiObjectEditor ) {
                return resolve( ( (MultiObjectEditor) parent ).getSelectedObjectEnforced(), this.internalGetPath());
            }
            else {
                throw new IllegalArgumentException(UNKNOWN_OBJECT_EDITOR + parent);
            }
        }

        return resolve(this.internalGetContext().getEnforced(), this.internalGetPath());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor#getEffective()
     */
    @Override
    public ConfigurationObject getEffective () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject local = this.getCurrent();
        return (ConfigurationObject) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {
            local.getType()
        }, new ObjectEditorInheritanceProxy(this));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        AbstractObjectEditor<?> parentEditor = getParentEditor();
        if ( parentEditor == null ) {
            ConfigurationObject current;
            ConfigurationObject toplevel;
            try {
                current = getCurrent();
                toplevel = internalGetContext().getCurrent();
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                return null;
            }
            if ( current == null || current.getId() == null ) {
                return null;
            }

            String myPath = this.internalGetPath();
            if ( toplevel != null ) {
                UUID id = toplevel.getId();
                if ( id != null ) {
                    return "obj:" + id.toString() + '/' + myPath; //$NON-NLS-1$
                }
                return '/' + myPath;
            }

            return "obj:" + current.getId().toString(); //$NON-NLS-1$
        }
        String absolutePath = parentEditor.getAbsolutePath();
        if ( absolutePath == null ) {
            return this.internalGetPath();
        }
        return absolutePath + "/" + this.internalGetPath(); //$NON-NLS-1$
    }

}
