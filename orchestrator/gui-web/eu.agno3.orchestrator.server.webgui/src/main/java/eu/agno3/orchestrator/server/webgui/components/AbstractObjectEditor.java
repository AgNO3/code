/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.runtime.jsf.components.ResetComponentsVisitCallback;
import eu.agno3.runtime.jsf.components.ResettableComponent;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractObjectEditor <T> extends UINamingContainer implements ResettableComponent {

    private static final Logger log = Logger.getLogger(AbstractObjectEditor.class);

    private static final String CONTEXT = "context"; //$NON-NLS-1$
    private static final String PATH = "path"; //$NON-NLS-1$
    private static final String OBJECT_TYPE = "objectType"; //$NON-NLS-1$
    private static final String HIDE_HEADER = "hideHeader"; //$NON-NLS-1$
    private static final String INHERITS_CHANGED = "inheritsChanged"; //$NON-NLS-1$
    private static final String INHERITS_MODIFIED = "inheritsModified"; //$NON-NLS-1$
    private static final String READ_ONLY = "readOnly"; //$NON-NLS-1$

    private static final String HIDDEN_CHILDREN = "hiddenChildren"; //$NON-NLS-1$
    private static final String HIDDEN_CHILDREN_CACHED_LEVEL = "hiddenChildrenForDetailLevel"; //$NON-NLS-1$

    private boolean resolvedParentEditor;
    private transient AbstractObjectEditor<?> cachedParentEditor;
    private transient ConfigContext<ConfigurationObject, ConfigurationObject> cachedContext;

    private transient Boolean cachedHaveVisibleChildren;


    public boolean hideHeader () {
        if ( Boolean.TRUE.equals(this.getAttributes().get(HIDE_HEADER)) ) {
            return true;
        }

        AbstractObjectEditor<?> parentEditor = getParentEditor();

        if ( parentEditor != null ) {
            return parentEditor.hideHeader();
        }

        return false;
    }


    public boolean isSimplified () {
        AbstractObjectEditor<?> parentEditor = getParentEditor();
        if ( parentEditor != null ) {
            return parentEditor.isSimplified();
        }
        return false;
    }


    /**
     * @return whether the child editors should be read only
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public boolean childrenAreReadOnly () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return (boolean) this.getAttributes().get(READ_ONLY);
    }


    public boolean shouldShowResetToDefaults () {
        return internalGetContext().isShowDefaultReset();
    }


    @SuppressWarnings ( "unchecked" )
    protected ConfigContext<ConfigurationObject, ConfigurationObject> internalGetContext () {
        if ( this.cachedContext == null ) {
            AbstractObjectEditor<?> parent = this.getParentEditor();
            if ( parent != null ) {
                this.cachedContext = parent.internalGetContext();
            }
            else {
                Object ctxObj = this.getAttributes().get(CONTEXT);
                if ( ! ( ctxObj instanceof ConfigContext<?, ?> ) ) {
                    throw new FacesException(String.format("Illegal context type %s at %s", ctxObj, this.getClientId())); //$NON-NLS-1$
                }
                this.cachedContext = (ConfigContext<ConfigurationObject, ConfigurationObject>) ctxObj;
            }
        }
        return this.cachedContext;
    }


    public Object getParameter ( String param ) {

        for ( UIComponent comp : this.getChildren() ) {
            if ( ! ( comp instanceof UIParameter ) ) {
                continue;
            }

            UIParameter paramComp = (UIParameter) comp;
            if ( !param.equals(paramComp.getName()) ) {
                continue;
            }

            Object value = paramComp.getValue();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found %s: %s", param, value)); //$NON-NLS-1$
            }
            return value;
        }

        AbstractObjectEditor<?> parentEditor = this.getParentEditor();
        if ( parentEditor != null ) {
            return parentEditor.getParameter(param);
        }
        return null;

    }


    @Override
    public boolean resetComponent () {
        this.resetChildren();
        return false;
    }


    protected void resetChildren () {
        log.debug("Resetting children"); //$NON-NLS-1$
        this.visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), new ResetComponentsVisitCallback(this));
    }


    /**
     * 
     * @return the title for the object editor (the object type)
     */
    public String getDisplayTitle () {
        String overrideTitle = (String) this.getAttributes().get("title"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(overrideTitle) ) {
            return overrideTitle;
        }
        return this.internalGetContext().getConfigLocalizer().getTypeName(this.internalGetObjectType());
    }


    /**
     * 
     * @return this editors relative path
     */
    public String internalGetPath () {
        return (String) this.getAttributes().get(PATH);
    }


    /**
     * 
     * @return the edited object's object type
     */
    protected String internalGetObjectType () {
        return (String) this.getAttributes().get(OBJECT_TYPE);
    }


    /**
     * @return whether the inherited object has been switched (references another object)
     */
    public boolean isInheritsChanged () {
        Boolean inheritsChanged = (Boolean) this.getStateHelper().get(INHERITS_CHANGED);
        if ( inheritsChanged == null ) {
            return false;
        }
        return inheritsChanged;
    }


    /**
     * @param inheritsChanged
     *            the inheritsChanged to set
     */
    public void setInheritsChanged ( boolean inheritsChanged ) {
        this.getStateHelper().put(INHERITS_CHANGED, inheritsChanged);
    }


    /**
     * 
     * @return whether the inherited object has been modified ( the object not the reference )
     */
    public boolean isInheritsModified () {
        Boolean inheritsModified = (Boolean) this.getStateHelper().get(INHERITS_MODIFIED);
        if ( inheritsModified == null ) {
            return false;
        }
        return inheritsModified;
    }


    /**
     * 
     * @param inheritsModified
     */
    public void setInheritsModified ( boolean inheritsModified ) {
        this.getStateHelper().put(INHERITS_MODIFIED, inheritsModified);
    }


    /**
     * 
     * @return the outer editor, or null if none exists
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

        return null;

    }


    /**
     * 
     * @param path
     * @return the child editor for the specified relative path
     */
    public AbstractObjectEditor<?> findChildByPath ( String path ) {
        AbstractObjectEditor<?> cur = this;
        String fragment = null;
        int sep;
        int pos = 0;

        if ( ( sep = path.indexOf('/', pos) ) >= 0 ) {
            fragment = path.substring(pos, sep);

            if ( fragment.startsWith("obj:") || //$NON-NLS-1$
                    fragment.startsWith("col:") ) { //$NON-NLS-1$
                throw new FacesException("Cannot handle path fragment " + fragment); //$NON-NLS-1$
            }

            cur = cur.findChildByPathFragment(fragment);

            if ( cur == null ) {
                throw new FacesException("Cannot resolve intermediate config path " + path); //$NON-NLS-1$
            }

            pos = sep + 1;
        }

        if ( fragment == null ) {
            return cur.findChildByPathFragment(path);
        }

        return cur.findChildByPathFragment(path.substring(pos));
    }


    protected AbstractObjectEditor<?> findChildByPathFragment ( String fragment ) {
        return this.findChildByPathFragmentInternal(this, fragment);
    }


    /**
     * @param children
     * @param fragment
     * @return
     */
    private AbstractObjectEditor<?> findChildByPathFragmentInternal ( UIComponent comp, String fragment ) {
        for ( UIComponent c : comp.getFacets().values() ) {
            AbstractObjectEditor<?> found = searchInComponent(fragment, c);
            if ( found != null ) {
                return found;
            }
        }
        for ( UIComponent c : comp.getChildren() ) {
            AbstractObjectEditor<?> found = searchInComponent(fragment, c);
            if ( found != null ) {
                return found;
            }
        }
        return null;
    }


    private AbstractObjectEditor<?> searchInComponent ( String fragment, UIComponent c ) {
        if ( c instanceof AbstractObjectEditor<?> ) {
            AbstractObjectEditor<?> editor = (AbstractObjectEditor<?>) c;
            if ( fragment.equals(editor.internalGetPath()) ) {
                return editor;
            }
        }
        else {
            AbstractObjectEditor<?> found = findChildByPathFragmentInternal(c, fragment);
            if ( found != null ) {
                return found;
            }
        }
        return null;
    }


    /**
     * 
     * @return the current local values
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public abstract T getCurrent () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * 
     * @return the applied enforced values
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public abstract T getEnforced () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * 
     * @return the applied default values
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public abstract T getDefaults () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * 
     * @return a read only proxy for the effective values
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public abstract T getEffective () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    /**
     * 
     * @return the local outer wrapper
     */
    public OuterWrapper<T> getOuterWrapper () {
        OuterWrapper<?> outer = null;
        AbstractObjectEditor<?> parent = this.getParentEditor();

        if ( parent != null ) {
            outer = parent.getOuterWrapper();
        }

        return new OuterWrapper<>(this, outer, internalGetObjectType());
    }


    protected static boolean objectsAreEqual ( ConfigurationObject a, ConfigurationObject b ) {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format(
                "Comparing %s with %s", //$NON-NLS-1$
                a == null ? "null" : a, //$NON-NLS-1$
                b == null ? "null" : b)); //$NON-NLS-1$
        }

        if ( a == null && b == null ) {
            return true;
        }
        else if ( a == null ) {
            return false;
        }
        else {

            return a.equals(b);
        }
    }


    public abstract String getAbsolutePath ();


    /**
     * @param objectEditor
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public void inheritanceChanged ( AbstractObjectEditor<?> objectEditor )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {}


    public boolean getHasHiddenChildren () {
        int level = this.internalGetContext().getDetailLevel();
        Integer cachedLevel = (Integer) this.getStateHelper().eval(HIDDEN_CHILDREN_CACHED_LEVEL);
        if ( cachedLevel != null && cachedLevel == level ) {
            return (boolean) this.getStateHelper().get(HIDDEN_CHILDREN);
        }
        boolean hasHidden = internalHasHiddenChildren();
        this.getStateHelper().put(HIDDEN_CHILDREN, hasHidden);
        this.getStateHelper().put(HIDDEN_CHILDREN_CACHED_LEVEL, level);
        return hasHidden;
    }


    protected boolean internalHasHiddenChildren () {
        final boolean[] res = new boolean[1];
        this.visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), new VisitCallback() {

            @Override
            public VisitResult visit ( VisitContext ctx, UIComponent child ) {

                if ( child == AbstractObjectEditor.this ) {
                    return VisitResult.ACCEPT;
                }

                if ( child instanceof HtmlOutputText ) {
                    return VisitResult.REJECT;
                }

                if ( child instanceof AbstractObjectEditor ) {
                    return VisitResult.REJECT;
                }
                else if ( child instanceof ObjectFieldEditor ) {
                    if ( ( (ObjectFieldEditor) child ).isHidden() ) {
                        res[ 0 ] = true;
                        return VisitResult.COMPLETE;
                    }
                    return VisitResult.REJECT;
                }

                return VisitResult.ACCEPT;
            }
        });

        return res[ 0 ];
    }


    protected boolean hasVisibleChildren () {

        if ( this.cachedHaveVisibleChildren != null ) {
            return this.cachedHaveVisibleChildren;
        }

        final boolean[] res = new boolean[1];

        this.visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), new VisitCallback() {

            @Override
            public VisitResult visit ( VisitContext ctx, UIComponent child ) {

                if ( child == AbstractObjectEditor.this ) {
                    return VisitResult.ACCEPT;
                }

                if ( child instanceof HtmlOutputText || !child.isRendered() ) {
                    return VisitResult.REJECT;
                }

                if ( child instanceof ObjectEditor ) {
                    if ( ! ( (ObjectEditor) child ).isHidden() ) {
                        res[ 0 ] = true;
                        return VisitResult.COMPLETE;
                    }
                    // children are handled by the component itself
                    return VisitResult.REJECT;
                }
                else if ( child instanceof ObjectFieldEditor ) {
                    if ( ! ( (ObjectFieldEditor) child ).isHidden() ) {
                        res[ 0 ] = true;
                        return VisitResult.COMPLETE;
                    }
                    return VisitResult.REJECT;
                }

                return VisitResult.ACCEPT;
            }
        });
        this.cachedHaveVisibleChildren = res[ 0 ];
        return this.cachedHaveVisibleChildren;
    }

}