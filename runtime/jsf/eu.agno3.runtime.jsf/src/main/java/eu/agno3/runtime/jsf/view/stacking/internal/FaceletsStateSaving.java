/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.agno3.runtime.jsf.view.stacking.internal;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;
import javax.faces.view.ViewMetadata;

import org.apache.log4j.Logger;
import org.apache.myfaces.context.RequestViewContext;
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.myfaces.view.facelets.AttachedFullStateWrapper;
import org.apache.myfaces.view.facelets.ComponentState;
import org.apache.myfaces.view.facelets.DefaultFaceletsStateManagementStrategy;
import org.apache.myfaces.view.facelets.ViewPoolProcessor;
import org.apache.myfaces.view.facelets.compiler.CheckDuplicateIdFaceletUtils;
import org.apache.myfaces.view.facelets.pool.ViewEntry;
import org.apache.myfaces.view.facelets.pool.ViewPool;
import org.apache.myfaces.view.facelets.pool.ViewStructureMetadata;
import org.apache.myfaces.view.facelets.tag.jsf.ComponentSupport;
import org.apache.myfaces.view.facelets.tag.jsf.FaceletState;


/**
 * Saves and restores facelet view state
 * 
 * Extracted from myfaces 2.2 DefaultFacleletsStateManagementStrategy
 * 
 * @author mbechler
 * 
 */
public final class FaceletsStateSaving extends DefaultFaceletsStateManagementStrategy {

    private static final String SKIP_ITERATION_HINT = "javax.faces.visit.SKIP_ITERATION"; //$NON-NLS-1$
    private static final String CHECK_ID_PRODUCTION_MODE_TRUE = "true"; //$NON-NLS-1$
    private static final String CHECK_ID_PRODUCTION_MODE_AUTO = "auto"; //$NON-NLS-1$

    private static final Object[] EMPTY_STATES = new Object[] {
        null, null
    };

    private static final Set<VisitHint> VISIT_HINTS = Collections.unmodifiableSet(EnumSet.of(VisitHint.SKIP_ITERATION));

    private static final String UNIQUE_ID_COUNTER_KEY = "oam.view.uniqueIdCounter"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(FaceletsStateSaving.class);
    private ViewDeclarationLanguageFactory _vdlFactory;
    private ViewPoolProcessor _viewPoolProcessor;


    /**
     * 
     * @param context
     */
    public FaceletsStateSaving ( FacesContext context ) {
        this._vdlFactory = (ViewDeclarationLanguageFactory) FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY);
        this._viewPoolProcessor = ViewPoolProcessor.getInstance(context);
    }


    /**
     * @param context
     * @param input
     * @return restored view state
     */
    @SuppressWarnings ( "unchecked" )
    public UIViewRoot restoreViewState ( FacesContext context, Object input ) {
        UIViewRoot view = null;
        Object[] s = (Object[]) input;

        if ( s == null ) {
            // No state could be restored, return null causing ViewExpiredException
            return null;
        }

        final boolean oldContextEventState = context.isProcessingEvents();

        String viewId = (String) s[ 0 ];
        Object[] state = (Object[]) s[ 2 ];

        if ( state == null ) {
            // No state could be restored, return null causing ViewExpiredException
            return null;
        }

        if ( state[ 1 ] instanceof Object[] ) {
            Object[] fullState = (Object[]) state[ 1 ];
            view = (UIViewRoot) internalRestoreTreeStructure((TreeStructComponent) fullState[ 0 ]);

            if ( view != null ) {
                context.setViewRoot(view);
                view.processRestoreState(context, fullState[ 1 ]);

                // If the view is restored fully, it is necessary to refresh RequestViewContext, otherwise at
                // each ajax request new components associated with @ResourceDependency annotation will be added
                // to the tree, making the state bigger without real need.
                RequestViewContext.getCurrentInstance(context).refreshRequestViewContext(context, view);

                if ( fullState.length == 3 && fullState[ 2 ] != null ) {
                    context.setResourceLibraryContracts((List<String>) UIComponentBase.restoreAttachedState(context, fullState[ 2 ]));
                }
            }
        }
        else {
            // Per the spec: build the view.
            ViewDeclarationLanguage vdl = this._vdlFactory.getViewDeclarationLanguage(viewId);
            Object faceletViewState = null;
            Map<String, Object> states;
            try {
                ViewMetadata metadata = vdl.getViewMetadata(context, viewId);

                if ( metadata != null ) {
                    view = metadata.createMetadataView(context);

                    // If no view and response complete there is no need to continue
                    if ( view == null && context.getResponseComplete() ) {
                        return null;
                    }
                }
                if ( view == null ) {
                    view = context.getApplication().getViewHandler().createView(context, viewId);
                }

                context.setViewRoot(view);
                boolean skipBuildView = false;
                if ( state[ 1 ] != null ) {
                    // Since JSF 2.2, UIViewRoot.restoreViewScopeState() must be called, but
                    // to get the state of the root, it is necessary to force calculate the
                    // id from this location. Remember in this point, PSS is enabled, so the
                    // code match with the assigment done in
                    // FaceletViewDeclarationLanguage.buildView()
                    states = (Map<String, Object>) state[ 1 ];
                    faceletViewState = UIComponentBase.restoreAttachedState(context, states.get(ComponentSupport.FACELET_STATE_INSTANCE));
                    if ( faceletViewState != null && this._viewPoolProcessor != null ) {
                        log.error("TODO"); //$NON-NLS-1$
                        ViewPool viewPool = this._viewPoolProcessor.getViewPool(context, view);
                        if ( viewPool != null ) {
                            ViewStructureMetadata viewMetadata = viewPool
                                    .retrieveDynamicViewStructureMetadata(context, view, (FaceletState) faceletViewState);
                            if ( viewMetadata != null ) {
                                ViewEntry entry = viewPool.popDynamicStructureView(context, view, (FaceletState) faceletViewState);
                                if ( entry != null ) {
                                    skipBuildView = true;
                                    this._viewPoolProcessor.cloneAndRestoreView(context, view, entry, viewMetadata);
                                }
                            }
                        }
                    }
                    if ( view.getId() == null ) {
                        view.setId(view.createUniqueId(context, null));
                    }
                    if ( faceletViewState != null ) {
                        // if (skipBuildView)
                        // {
                        FaceletState newFaceletState = (FaceletState) view.getAttributes().get(ComponentSupport.FACELET_STATE_INSTANCE);
                        if ( newFaceletState != null ) {
                            newFaceletState.restoreState(context, ( (FaceletState) faceletViewState ).saveState(context));
                            faceletViewState = newFaceletState;
                        }
                        else {
                            view.getAttributes().put(ComponentSupport.FACELET_STATE_INSTANCE, faceletViewState);
                        }
                        // }
                        // else
                        // {
                        // view.getAttributes().put(ComponentSupport.FACELET_STATE_INSTANCE, faceletViewState);
                        // }
                    }
                    if ( state.length == 3 ) {
                        // Jump to where the count is
                        view.getAttributes().put(UNIQUE_ID_COUNTER_KEY, state[ 2 ]);
                    }
                    Object viewRootState = states.get(view.getClientId(context));
                    if ( viewRootState != null ) {
                        try {
                            view.pushComponentToEL(context, view);
                            view.restoreViewScopeState(context, viewRootState);
                        }
                        finally {
                            view.popComponentFromEL(context);
                        }
                    }
                }
                // On RestoreViewExecutor, setProcessingEvents is called first to false
                // and then to true when postback. Since we need listeners registered to PostAddToViewEvent
                // event to be handled, we should enable it again. For partial state saving we need this listeners
                // be called from here and relocate components properly.
                if ( !skipBuildView ) {
                    try {
                        context.setProcessingEvents(true);
                        vdl.buildView(context, view);
                        // In the latest code related to PostAddToView, it is
                        // triggered no matter if it is applied on postback. It seems that MYFACES-2389,
                        // TRINIDAD-1670 and TRINIDAD-1671 are related.
                        suscribeListeners(view);
                    }
                    finally {
                        context.setProcessingEvents(oldContextEventState);
                    }
                }
            }
            catch ( Throwable e ) {
                throw new FacesException("unable to create view \"" + viewId + "\"", e); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if ( state[ 1 ] != null ) {
                states = (Map<String, Object>) state[ 1 ];
                // Save the last unique id counter key in UIViewRoot
                Integer lastUniqueIdCounter = (Integer) view.getAttributes().get(UNIQUE_ID_COUNTER_KEY);
                // Retrieve the facelet state before restore anything. The reason is
                // it could be necessary to restore the bindings map from here.
                FaceletState oldFaceletState = (FaceletState) view.getAttributes().get(ComponentSupport.FACELET_STATE_INSTANCE);

                // Visit the children and restore their state.
                boolean emptyState = false;
                boolean containsFaceletState = states.containsKey(ComponentSupport.FACELET_STATE_INSTANCE);
                if ( states.isEmpty() ) {
                    emptyState = true;
                }
                else if ( states.size() == 1 && containsFaceletState ) {
                    emptyState = true;
                }
                // Restore state of current components
                if ( !emptyState ) {
                    // Check if there is only one component state
                    // and that state is UIViewRoot instance (for example
                    // when using ViewScope)
                    if ( ( states.size() == 1 && !containsFaceletState ) || ( states.size() == 2 && containsFaceletState ) ) {
                        Object viewState = states.get(view.getClientId(context));
                        if ( viewState != null ) {
                            restoreViewRootOnlyFromMap(context, viewState, view);
                        }
                        else {
                            // The component is not viewRoot, restore as usual.
                            restoreStateFromMap(context, states, view);
                        }
                    }
                    else {
                        restoreStateFromMap(context, states, view);
                    }
                }
                if ( faceletViewState != null ) {
                    // Make sure binding map
                    if ( oldFaceletState != null && oldFaceletState.getBindings() != null && !oldFaceletState.getBindings().isEmpty() ) {
                        // Be sure the new facelet state has the binding map filled from the old one.
                        // When vdl.buildView() is called by restoreView, FaceletState.bindings map is filled, but
                        // when view pool is enabled, vdl.buildView() could restore the view, but create an alternate
                        // FaceletState instance, different from the one restored. In this case, the restored instance
                        // has precedence, but we need to fill bindings map using the entries from the instance that
                        // comes from the view pool.
                        FaceletState newFaceletState = (FaceletState) faceletViewState;
                        for ( Map.Entry<String, Map<String, ValueExpression>> entry : oldFaceletState.getBindings().entrySet() ) {
                            for ( Map.Entry<String, ValueExpression> entry2 : entry.getValue().entrySet() ) {
                                ValueExpression expr = newFaceletState.getBinding(entry.getKey(), entry2.getKey());
                                if ( expr == null ) {
                                    newFaceletState.putBinding(entry.getKey(), entry2.getKey(), entry2.getValue());
                                }
                            }
                        }
                        view.getAttributes().put(ComponentSupport.FACELET_STATE_INSTANCE, newFaceletState);
                    }
                    else {
                        // restore bindings
                        view.getAttributes().put(ComponentSupport.FACELET_STATE_INSTANCE, faceletViewState);
                    }
                }
                if ( lastUniqueIdCounter != null ) {
                    Integer newUniqueIdCounter = (Integer) view.getAttributes().get(UNIQUE_ID_COUNTER_KEY);
                    if ( newUniqueIdCounter != null && lastUniqueIdCounter.intValue() > newUniqueIdCounter.intValue() ) {
                        // The unique counter was restored by a side effect of
                        // restoreState() over UIViewRoot with a lower count,
                        // to avoid a component duplicate id exception we need to fix the count.
                        view.getAttributes().put(UNIQUE_ID_COUNTER_KEY, lastUniqueIdCounter);
                    }
                }
                handleDynamicAddedRemovedComponents(context, view, states);
            }
        }
        return view;
    }


    /**
     * 
     * @param context
     * @return the current view state, serialized
     */
    @SuppressWarnings ( "unchecked" )
    public Object dumpViewState ( FacesContext context ) {
        UIViewRoot view = context.getViewRoot();
        Object states;

        if ( view == null ) {
            // Not much that can be done.

            return null;
        }

        Object serializedView;

        // Note on ajax case the method saveState could be called twice: once before start
        // document rendering and the other one when it is called StateManager.getViewState method.

        // Make sure the client IDs are unique per the spec.

        if ( context.isProjectStage(ProjectStage.Production) ) {
            if ( CHECK_ID_PRODUCTION_MODE_AUTO.equals(getCheckIdProductionMode(context)) ) {
                CheckDuplicateIdFaceletUtils.checkIdsStatefulComponents(context, view);
            }
            else if ( CHECK_ID_PRODUCTION_MODE_TRUE.equals(getCheckIdProductionMode(context)) ) {
                CheckDuplicateIdFaceletUtils.checkIds(context, view);
            }
        }
        else {
            CheckDuplicateIdFaceletUtils.checkIds(context, view);
        }

        // Create save state objects for every component.

        boolean viewResetable = false;
        int count = 0;
        Object faceletViewState = null;
        boolean saveViewFully = view.getAttributes().containsKey(COMPONENT_ADDED_AFTER_BUILD_VIEW);
        if ( saveViewFully ) {
            ensureClearInitialState(view);
            Object rlcStates = !context.getResourceLibraryContracts().isEmpty()
                    ? UIComponentBase.saveAttachedState(context, new ArrayList<>(context.getResourceLibraryContracts())) : null;
            states = new Object[] {
                internalBuildTreeStructureToSave(view), view.processSaveState(context), rlcStates
            };
        }
        else {
            states = new HashMap<String, Object>();

            faceletViewState = view.getAttributes().get(ComponentSupport.FACELET_STATE_INSTANCE);
            if ( faceletViewState != null ) {
                ( (Map<String, Object>) states )
                        .put(ComponentSupport.FACELET_STATE_INSTANCE, UIComponentBase.saveAttachedState(context, faceletViewState));
                // Do not save on UIViewRoot
                view.getAttributes().remove(ComponentSupport.FACELET_STATE_INSTANCE);
                view.getTransientStateHelper().putTransient(ComponentSupport.FACELET_STATE_INSTANCE, faceletViewState);
            }
            if ( this._viewPoolProcessor != null && this._viewPoolProcessor.isViewPoolEnabledForThisView(context, view) ) {
                SaveStateAndResetViewCallback cb = saveStateOnMapVisitTreeAndReset(
                    context,
                    (Map<String, Object>) states,
                    view,
                    Boolean.TRUE.equals(context.getAttributes().get(ViewPoolProcessor.FORCE_HARD_RESET)));
                viewResetable = cb.isViewResetable();
                count = cb.getCount();
            }
            else {
                saveStateOnMapVisitTree(context, (Map<String, Object>) states, view);
            }

            if ( ( (Map<String, Object>) states ).isEmpty() ) {
                states = null;
            }
        }

        Integer uniqueIdCount = (Integer) view.getAttributes().get(UNIQUE_ID_COUNTER_KEY);
        if ( uniqueIdCount != null && !uniqueIdCount.equals(1) ) {
            serializedView = new Object[] {
                null, states, uniqueIdCount
            };
        }
        else if ( states == null ) {
            serializedView = EMPTY_STATES;
        }
        else {
            serializedView = new Object[] {
                null, states
            };
        }

        // If view cache enabled store the view state into the pool
        if ( !saveViewFully && this._viewPoolProcessor != null ) {
            if ( viewResetable ) {
                this._viewPoolProcessor.pushResetableView(context, view, (FaceletState) faceletViewState);
            }
            else {
                this._viewPoolProcessor.pushPartialView(context, view, (FaceletState) faceletViewState, count);
            }
        }

        return new Object[] {
            view.getViewId(), view.getRenderKitId(), serializedView
        };
    }


    private static void restoreViewRootOnlyFromMap ( final FacesContext context, final Object viewState, final UIComponent view ) {
        // Only viewState found, process it but skip tree
        // traversal, saving some time.
        try {
            // Restore view
            view.pushComponentToEL(context, view);
            if ( viewState != null && ! ( viewState instanceof AttachedFullStateWrapper ) ) {
                try {
                    view.restoreState(context, viewState);
                }
                catch ( Exception e ) {
                    throw new IllegalStateException("Error restoring component: " + view.getClientId(context), e); //$NON-NLS-1$
                }
            }
        }
        finally {
            view.popComponentFromEL(context);
        }
    }


    private void restoreStateFromMap ( final FacesContext context, final Map<String, Object> states, final UIComponent component ) {
        if ( states == null ) {
            return;
        }

        try {
            // Restore view
            component.pushComponentToEL(context, component);
            Object state = states.get(component.getClientId(context));
            if ( state != null ) {
                if ( state instanceof AttachedFullStateWrapper ) {
                    // Don't restore this one! It will be restored when the algorithm remove and add it.
                    return;
                }
                try {
                    component.restoreState(context, state);
                }
                catch ( Exception e ) {
                    throw new IllegalStateException("Error restoring component: " + component.getClientId(context), e); //$NON-NLS-1$
                }
            }

            // Scan children
            if ( component.getChildCount() > 0 ) {
                // String currentClientId = component.getClientId();

                List<UIComponent> children = component.getChildren();
                for ( int i = 0; i < children.size(); i++ ) {
                    UIComponent child = children.get(i);
                    if ( child != null && !child.isTransient() ) {
                        restoreStateFromMap(context, states, child);
                    }
                }
            }

            // Scan facets
            if ( component.getFacetCount() > 0 ) {
                Map<String, UIComponent> facetMap = component.getFacets();

                for ( Map.Entry<String, UIComponent> entry : facetMap.entrySet() ) {
                    UIComponent child = entry.getValue();
                    if ( child != null && !child.isTransient() ) {
                        // String facetName = entry.getKey();
                        restoreStateFromMap(context, states, child);
                    }
                }
            }
        }
        finally {
            component.popComponentFromEL(context);
        }
    }


    @SuppressWarnings ( "unchecked" )
    static List<String> getClientIdsAdded ( UIViewRoot root ) {
        return (List<String>) root.getAttributes().get(CLIENTIDS_ADDED);
    }


    static void setClientsIdsAdded ( UIViewRoot root, List<String> clientIdsList ) {
        root.getAttributes().put(CLIENTIDS_ADDED, clientIdsList);
    }


    @SuppressWarnings ( "unchecked" )
    static List<String> getClientIdsRemoved ( UIViewRoot root ) {
        return (List<String>) root.getAttributes().get(CLIENTIDS_REMOVED);
    }


    static void setClientsIdsRemoved ( UIViewRoot root, List<String> clientIdsList ) {
        root.getAttributes().put(CLIENTIDS_REMOVED, clientIdsList);
    }


    static void registerOnAddRemoveList ( FacesContext facesContext, String clientId ) {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();

        List<String> clientIdsAdded = getClientIdsAdded(uiViewRoot);
        if ( clientIdsAdded == null ) {
            // Create a set that preserve insertion order
            clientIdsAdded = new ArrayList<>();
        }
        clientIdsAdded.add(clientId);

        setClientsIdsAdded(uiViewRoot, clientIdsAdded);

        List<String> clientIdsRemoved = getClientIdsRemoved(uiViewRoot);
        if ( clientIdsRemoved == null ) {
            // Create a set that preserve insertion order
            clientIdsRemoved = new ArrayList<>();
        }

        clientIdsRemoved.add(clientId);

        setClientsIdsRemoved(uiViewRoot, clientIdsRemoved);
    }


    static void registerOnAddList ( FacesContext facesContext, String clientId ) {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();

        List<String> clientIdsAdded = getClientIdsAdded(uiViewRoot);
        if ( clientIdsAdded == null ) {
            // Create a set that preserve insertion order
            clientIdsAdded = new ArrayList<>();
        }
        clientIdsAdded.add(clientId);

        setClientsIdsAdded(uiViewRoot, clientIdsAdded);
    }


    @Override
    protected void ensureClearInitialState ( UIComponent c ) {
        super.ensureClearInitialState(c);
    }


    private void saveStateOnMapVisitTree ( final FacesContext fc, final Map<String, Object> states, final UIViewRoot uiViewRoot ) {
        fc.getAttributes().put(SKIP_ITERATION_HINT, Boolean.TRUE);
        try {
            uiViewRoot.visitTree(getVisitContextFactory().getVisitContext(fc, null, VISIT_HINTS), new VisitCallback() {

                @Override
                public VisitResult visit ( VisitContext context, UIComponent target ) {
                    FacesContext facesContext = context.getFacesContext();
                    Object state;

                    if ( ( target == null ) || target.isTransient() ) {
                        // No need to bother with these components or their children.

                        return VisitResult.REJECT;
                    }

                    ComponentState componentAddedAfterBuildView = (ComponentState) target.getAttributes().get(COMPONENT_ADDED_AFTER_BUILD_VIEW);

                    // Note if UIViewRoot has this marker, JSF 1.2 like state saving is used.
                    if ( componentAddedAfterBuildView != null && ( target.getParent() != null ) ) {
                        if ( ComponentState.REMOVE_ADD.equals(componentAddedAfterBuildView) ) {
                            registerOnAddRemoveList(facesContext, target.getClientId(facesContext));
                            target.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                        }
                        else if ( ComponentState.ADD.equals(componentAddedAfterBuildView) ) {
                            registerOnAddList(facesContext, target.getClientId(facesContext));
                            target.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                        }
                        else if ( ComponentState.ADDED.equals(componentAddedAfterBuildView) ) {
                            registerOnAddList(facesContext, target.getClientId(facesContext));
                        }
                        ensureClearInitialState(target);
                        // Save all required info to restore the subtree.
                        // This includes position, structure and state of subtree

                        int childIndex = target.getParent().getChildren().indexOf(target);
                        if ( childIndex >= 0 ) {
                            states.put(target.getClientId(facesContext), new AttachedFullStateWrapper(new Object[] {
                                target.getParent().getClientId(facesContext), null, childIndex, internalBuildTreeStructureToSave(target),
                                target.processSaveState(facesContext)
                            }));
                        }
                        else {
                            String facetName = null;
                            if ( target.getParent().getFacetCount() > 0 ) {
                                for ( Map.Entry<String, UIComponent> entry : target.getParent().getFacets().entrySet() ) {
                                    if ( target.equals(entry.getValue()) ) {
                                        facetName = entry.getKey();
                                        break;
                                    }
                                }
                            }
                            states.put(target.getClientId(facesContext), new AttachedFullStateWrapper(new Object[] {
                                target.getParent().getClientId(facesContext), facetName, null, internalBuildTreeStructureToSave(target),
                                target.processSaveState(facesContext)
                            }));
                        }
                        return VisitResult.REJECT;
                    }
                    else if ( target.getParent() != null ) {
                        state = target.saveState(facesContext);

                        if ( state != null ) {
                            // Save by client ID into our map.

                            states.put(target.getClientId(facesContext), state);
                        }

                        return VisitResult.ACCEPT;
                    }
                    else {
                        // Only UIViewRoot has no parent in a component tree.
                        return VisitResult.ACCEPT;
                    }
                }
            });
        }
        finally {
            fc.getAttributes().remove(SKIP_ITERATION_HINT);
        }
        if ( !uiViewRoot.isTransient() ) {
            Object state = uiViewRoot.saveState(fc);
            if ( state != null ) {
                // Save by client ID into our map.
                states.put(uiViewRoot.getClientId(fc), state);
            }
        }
    }


    private SaveStateAndResetViewCallback saveStateOnMapVisitTreeAndReset ( final FacesContext facesContext, final Map<String, Object> states,
            final UIViewRoot uiViewRoot, boolean forceHardReset ) {
        facesContext.getAttributes().put(SKIP_ITERATION_HINT, Boolean.TRUE);
        SaveStateAndResetViewCallback callback = new SaveStateAndResetViewCallback(facesContext.getViewRoot(), states, forceHardReset);
        if ( forceHardReset ) {
            uiViewRoot.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, ViewPoolProcessor.RESET_MODE_HARD);
        }
        else {
            uiViewRoot.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, ViewPoolProcessor.RESET_MODE_SOFT);
        }
        try {
            if ( this._viewPoolProcessor != null && !this._viewPoolProcessor.isViewPoolEnabledForThisView(facesContext, uiViewRoot) ) {
                callback.setViewResetable(false);
            }

            // Check if the view has removed components. If that so, it
            // means there is some manipulation over the component tree that
            // can be rollback, so it is ok to set the view as resetable.
            if ( callback.isViewResetable() ) {
                List<String> removedIds = getClientIdsRemoved(uiViewRoot);
                if ( removedIds != null && !removedIds.isEmpty() ) {
                    callback.setViewResetable(false);
                }
            }

            try {
                uiViewRoot.visitTree(getVisitContextFactory().getVisitContext(facesContext, null, VISIT_HINTS), callback);
            }
            finally {
                facesContext.getAttributes().remove(SKIP_ITERATION_HINT);
            }

            if ( callback.isViewResetable() && callback.isRemoveAddedComponents() ) {
                List<String> clientIdsToRemove = getClientIdsAdded(uiViewRoot);

                if ( clientIdsToRemove != null ) {
                    // perf: clientIds are ArrayList: see method registerOnAddRemoveList(String)
                    for ( int i = 0, size = clientIdsToRemove.size(); i < size; i++ ) {
                        String clientId = clientIdsToRemove.get(i);
                        uiViewRoot.invokeOnComponent(facesContext, clientId, new RemoveComponentCallback());
                    }
                }
            }

            Object state = uiViewRoot.saveState(facesContext);
            if ( state != null ) {
                // Save by client ID into our map.
                states.put(uiViewRoot.getClientId(facesContext), state);

                // Hard reset (or reset and check state again)
                Integer oldResetMode = (Integer) uiViewRoot.getAttributes()
                        .put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, ViewPoolProcessor.RESET_MODE_HARD);
                state = uiViewRoot.saveState(facesContext);
                uiViewRoot.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, oldResetMode);
                if ( state != null ) {
                    callback.setViewResetable(false);
                }
            }
        }
        finally {
            uiViewRoot.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, ViewPoolProcessor.RESET_MODE_OFF);
        }
        return callback;
    }

    private class SaveStateAndResetViewCallback implements VisitCallback {

        private final Map<String, Object> states;

        private final UIViewRoot view;

        private boolean viewResetable;

        private boolean skipRoot;

        private int count;

        private boolean forceHardReset;

        private boolean removeAddedComponents;


        public SaveStateAndResetViewCallback ( UIViewRoot view, Map<String, Object> states, boolean forceHardReset ) {
            this.states = states;
            this.view = view;
            this.viewResetable = true;
            this.skipRoot = true;
            this.count = 0;
            this.forceHardReset = forceHardReset;
            this.removeAddedComponents = false;
        }


        @Override
        public VisitResult visit ( VisitContext context, UIComponent target ) {
            FacesContext facesContext = context.getFacesContext();
            Object state;
            this.count++;

            if ( ( target == null ) || target.isTransient() ) {
                // No need to bother with these components or their children.

                return VisitResult.REJECT;
            }

            if ( this.skipRoot && target instanceof UIViewRoot ) {
                // UIViewRoot should be scanned at last.
                this.skipRoot = false;
                return VisitResult.ACCEPT;
            }

            ComponentState componentAddedAfterBuildView = (ComponentState) target.getAttributes().get(COMPONENT_ADDED_AFTER_BUILD_VIEW);

            // Note if UIViewRoot has this marker, JSF 1.2 like state saving is used.
            if ( componentAddedAfterBuildView != null && ( target.getParent() != null ) ) {
                // Set this view as not resetable.
                // setViewResetable(false);
                // Enable flag to remove added components later
                setRemoveAddedComponents(true);
                if ( this.forceHardReset ) {
                    // The ideal is remove the added component here but visitTree does not support that
                    // kind of tree manipulation.
                    if ( isViewResetable() && ComponentState.REMOVE_ADD.equals(componentAddedAfterBuildView) ) {
                        setViewResetable(false);
                    }
                    // it is not important to save anything, skip
                    return VisitResult.REJECT;
                }
                if ( ComponentState.REMOVE_ADD.equals(componentAddedAfterBuildView) ) {
                    // If the view has removed components, set the view as non resetable
                    setViewResetable(false);
                    registerOnAddRemoveList(facesContext, target.getClientId(facesContext));
                    target.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                }
                else if ( ComponentState.ADD.equals(componentAddedAfterBuildView) ) {
                    registerOnAddList(facesContext, target.getClientId(facesContext));
                    target.getAttributes().put(COMPONENT_ADDED_AFTER_BUILD_VIEW, ComponentState.ADDED);
                }
                else if ( ComponentState.ADDED.equals(componentAddedAfterBuildView) ) {
                    // Later on the check of removed components we'll see if the view
                    // is resetable or not.
                    registerOnAddList(facesContext, target.getClientId(facesContext));
                }
                ensureClearInitialState(target);
                // Save all required info to restore the subtree.
                // This includes position, structure and state of subtree

                int childIndex = target.getParent().getChildren().indexOf(target);
                if ( childIndex >= 0 ) {
                    this.states.put(target.getClientId(facesContext), new AttachedFullStateWrapper(new Object[] {
                        target.getParent().getClientId(facesContext), null, childIndex, internalBuildTreeStructureToSave(target),
                        target.processSaveState(facesContext)
                    }));
                }
                else {
                    String facetName = null;
                    if ( target.getParent().getFacetCount() > 0 ) {
                        for ( Map.Entry<String, UIComponent> entry : target.getParent().getFacets().entrySet() ) {
                            if ( target.equals(entry.getValue()) ) {
                                facetName = entry.getKey();
                                break;
                            }
                        }
                    }
                    this.states.put(target.getClientId(facesContext), new AttachedFullStateWrapper(new Object[] {
                        target.getParent().getClientId(facesContext), facetName, null, internalBuildTreeStructureToSave(target),
                        target.processSaveState(facesContext)
                    }));
                }
                return VisitResult.REJECT;
            }
            else if ( target.getParent() != null ) {
                if ( this.forceHardReset ) {
                    // force hard reset set reset move on top
                    state = target.saveState(facesContext);
                    if ( state != null ) {
                        setViewResetable(false);
                        return VisitResult.REJECT;
                    }
                }
                else {
                    state = target.saveState(facesContext);

                    if ( state != null ) {
                        // Save by client ID into our map.
                        this.states.put(target.getClientId(facesContext), state);

                        if ( isViewResetable() ) {
                            // Hard reset (or reset and check state again)
                            Integer oldResetMode = (Integer) this.view.getAttributes()
                                    .put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, ViewPoolProcessor.RESET_MODE_HARD);
                            state = target.saveState(facesContext);
                            this.view.getAttributes().put(ViewPoolProcessor.RESET_SAVE_STATE_MODE_KEY, oldResetMode);
                            if ( state != null ) {
                                setViewResetable(false);
                            }
                        }
                    }
                }

                return VisitResult.ACCEPT;
            }
            else {
                // Only UIViewRoot has no parent in a component tree.
                return VisitResult.ACCEPT;
            }
        }


        /**
         * @return the viewResetable
         */
        public boolean isViewResetable () {
            return this.viewResetable;
        }


        /**
         * @param viewResetable
         *            the viewResetable to set
         */
        public void setViewResetable ( boolean viewResetable ) {
            this.viewResetable = viewResetable;
        }


        public int getCount () {
            return this.count;
        }


        /**
         * @return the removeAddedComponents
         */
        public boolean isRemoveAddedComponents () {
            return this.removeAddedComponents;
        }


        /**
         * @param removeAddedComponents
         *            the removeAddedComponents to set
         */
        public void setRemoveAddedComponents ( boolean removeAddedComponents ) {
            this.removeAddedComponents = removeAddedComponents;
        }
    }


    static TreeStructComponent internalBuildTreeStructureToSave ( UIComponent component ) {
        TreeStructComponent structComp = new TreeStructComponent(component.getClass().getName(), component.getId());

        // children
        if ( component.getChildCount() > 0 ) {
            List<TreeStructComponent> structChildList = new ArrayList<>();
            for ( int i = 0, childCount = component.getChildCount(); i < childCount; i++ ) {
                UIComponent child = component.getChildren().get(i);
                if ( !child.isTransient() ) {
                    TreeStructComponent structChild = internalBuildTreeStructureToSave(child);
                    structChildList.add(structChild);
                }
            }

            TreeStructComponent[] childArray = structChildList.toArray(new TreeStructComponent[structChildList.size()]);
            structComp.setChildren(childArray);
        }

        // facets

        if ( component.getFacetCount() > 0 ) {
            Map<String, UIComponent> facetMap = component.getFacets();
            List<Object[]> structFacetList = new ArrayList<>();
            for ( Map.Entry<String, UIComponent> entry : facetMap.entrySet() ) {
                UIComponent child = entry.getValue();
                if ( !child.isTransient() ) {
                    String facetName = entry.getKey();
                    TreeStructComponent structChild = internalBuildTreeStructureToSave(child);
                    structFacetList.add(new Object[] {
                        facetName, structChild
                    });
                }
            }

            Object[] facetArray = structFacetList.toArray(new Object[structFacetList.size()]);
            structComp.setFacets(facetArray);
        }

        return structComp;
    }


    private static UIComponent internalRestoreTreeStructure ( TreeStructComponent treeStructComp ) {
        String compClass = treeStructComp.getComponentClass();
        String compId = treeStructComp.getComponentId();
        UIComponent component = (UIComponent) ClassUtils.newInstance(compClass);
        component.setId(compId);

        // children
        TreeStructComponent[] childArray = treeStructComp.getChildren();
        if ( childArray != null ) {
            List<UIComponent> childList = component.getChildren();
            for ( int i = 0, len = childArray.length; i < len; i++ ) {
                UIComponent child = internalRestoreTreeStructure(childArray[ i ]);
                childList.add(child);
            }
        }

        // facets
        Object[] facetArray = treeStructComp.getFacets();
        if ( facetArray != null ) {
            Map<String, UIComponent> facetMap = component.getFacets();
            for ( int i = 0, len = facetArray.length; i < len; i++ ) {
                Object[] tuple = (Object[]) facetArray[ i ];
                String facetName = (String) tuple[ 0 ];
                TreeStructComponent structChild = (TreeStructComponent) tuple[ 1 ];
                UIComponent child = internalRestoreTreeStructure(structChild);
                facetMap.put(facetName, child);
            }
        }

        return component;
    }

    static class TreeStructComponent implements Serializable {

        private static final long serialVersionUID = 5069109074684737231L;
        private String _componentClass;
        private String _componentId;
        private TreeStructComponent[] _children = null; // Array of children
        private Object[] _facets = null; // Array of Array-tuples with Facetname and TreeStructComponent


        TreeStructComponent ( String componentClass, String componentId ) {
            this._componentClass = componentClass;
            this._componentId = componentId;
        }


        public String getComponentClass () {
            return this._componentClass;
        }


        public String getComponentId () {
            return this._componentId;
        }


        void setChildren ( TreeStructComponent[] children ) {
            this._children = children;
        }


        TreeStructComponent[] getChildren () {
            return this._children;
        }


        Object[] getFacets () {
            return this._facets;
        }


        void setFacets ( Object[] facets ) {
            this._facets = facets;
        }
    }

}
