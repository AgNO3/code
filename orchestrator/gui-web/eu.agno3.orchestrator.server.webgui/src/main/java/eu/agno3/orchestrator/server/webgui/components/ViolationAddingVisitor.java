/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.orchestrator.server.webgui.config.ViolationMessageBuilder;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
public class ViolationAddingVisitor implements VisitCallback {

    /**
     * 
     */
    private static final String INPUT_ID = "inputId"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ViolationAddingVisitor.class);

    private Set<ViolationEntry> violations;

    private boolean firstViolation = true;

    private ViolationMessageBuilder vmb;


    /**
     * @param vmb
     * @param violations
     */
    public ViolationAddingVisitor ( ViolationMessageBuilder vmb, List<ViolationEntry> violations ) {
        this.vmb = vmb;
        this.violations = new HashSet<>(violations);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.visit.VisitCallback#visit(javax.faces.component.visit.VisitContext,
     *      javax.faces.component.UIComponent)
     */
    @Override
    public VisitResult visit ( VisitContext ctx, UIComponent comp ) {

        if ( log.isTraceEnabled() ) {
            log.trace("Found component " + comp); //$NON-NLS-1$
        }

        if ( comp instanceof ObjectEditor ) {
            ObjectEditor comp2 = (ObjectEditor) comp;
            return visitRoot(ctx, comp2, StringUtils.isBlank(comp2.internalGetPath()));
        }

        if ( this.violations.isEmpty() ) {
            return VisitResult.COMPLETE;
        }

        return VisitResult.ACCEPT;
    }


    /**
     * @param ctx
     * @param comp
     * @return
     */
    private VisitResult visitRoot ( VisitContext ctx, AbstractObjectEditor<ConfigurationObject> comp, boolean reportUnmatched ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found root editor %s with path %s", comp, comp.getAbsolutePath())); //$NON-NLS-1$
        }

        Stack<String> pathStack = new Stack<>();

        try {
            if ( !StringUtils.isBlank(comp.getAbsolutePath()) && comp.getCurrent() != null ) {
                pushParents(comp.internalGetContext().getCurrent(), StringUtils.split(comp.getAbsolutePath(), '/'), pathStack);
            }
        }
        catch ( Exception e ) {
            log.warn("Failed to get current value", e); //$NON-NLS-1$
        }

        this.visitChildren(ctx.getFacesContext(), comp, pathStack);

        if ( reportUnmatched ) {
            addRemainingViolations(ctx);
            return VisitResult.COMPLETE;
        }

        return VisitResult.REJECT;
    }


    /**
     * @param current
     * @param objectStack
     * @param pathStack
     * @param split
     */
    private static void pushParents ( ConfigurationObject current, String[] segments, Stack<String> pathStack ) {
        ConfigurationObject cur = current;
        for ( int i = 0; i < segments.length; i++ ) {
            String segment = segments[ i ];
            String idStr = cur.getId().toString();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Pushing parent for %s", segment)); //$NON-NLS-1$
            }

            if ( segment.startsWith("obj:") ) { //$NON-NLS-1$
                String id = segment.substring(4);
                if ( id.equals(idStr) ) {
                    pathStack.push(segment);
                    continue;
                }
                throw new FacesException(String.format("Unexpected object %s (expected: %s)", idStr, id)); //$NON-NLS-1$
            }

            pathStack.push(segment);
            cur = (ConfigurationObject) FacesContext.getCurrentInstance().getELContext().getELResolver()
                    .getValue(FacesContext.getCurrentInstance().getELContext(), cur, segment);
        }
    }


    /**
     * @param ctx
     */
    public void addRemainingViolations ( VisitContext ctx ) {
        for ( ViolationEntry violation : this.violations ) {
            log.warn("Unhandled violation entry " + violation); //$NON-NLS-1$
            ctx.getFacesContext().addMessage(null, this.vmb.makeMessage("UNKNOWN", violation)); //$NON-NLS-1$
        }
        this.violations.clear();
    }


    private void visitChildren ( FacesContext ctx, UIComponent comp, Stack<String> pathStack ) {
        if ( visitObject(ctx, comp, pathStack) ) {
            doVisitChildren(ctx, comp, pathStack);
        }

    }


    /**
     * @param ctx
     * @param comp
     * @param pathStack
     * @param objectStack
     */
    private void doVisitChildren ( FacesContext ctx, UIComponent comp, Stack<String> pathStack ) {
        Iterator<UIComponent> iter = comp.getFacetsAndChildren();
        while ( iter.hasNext() ) {
            UIComponent next = iter.next();
            visitChildren(ctx, next, pathStack);
        }
    }


    /**
     * @param ctx
     * @param comp
     * @param pathStack
     * @param objectStack
     */
    private boolean visitObject ( FacesContext ctx, UIComponent comp, Stack<String> pathStack ) {
        if ( comp instanceof ObjectFieldEditor ) {
            return handleFieldEditor(ctx, comp, pathStack);
        }
        else if ( comp instanceof ObjectEditor ) {
            return handleEditor(ctx, comp, pathStack);

        }
        else if ( comp instanceof MultiObjectEditor ) {
            return handleMultiObjectEditor(ctx, comp, pathStack);
        }

        return true;
    }


    /**
     * @param comp
     * @return
     */
    private boolean handleMultiObjectEditor ( FacesContext ctx, UIComponent comp, Stack<String> pathStack ) {
        MultiObjectEditor multiObjectEditor = (MultiObjectEditor) comp;
        String path = multiObjectEditor.internalGetPath();

        if ( !isViolationPath(pathStack, path) ) {
            return false;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found multi-object editor at %s.%s", StringUtils.join(pathStack, '.'), path)); //$NON-NLS-1$
        }

        List<String> nextIds = getNextIds(pathStack);

        if ( nextIds == null ) {
            addAllMatchesToComponent(ctx, comp, pathStack, comp.getClientId(ctx), multiObjectEditor.getDisplayTitle());
            return false;
        }

        ConfigurationObject selectObj = null;
        if ( log.isDebugEnabled() ) {
            log.debug("Next ids are " + nextIds); //$NON-NLS-1$
        }

        if ( nextIds.size() > 2 ) {
            String nextId = nextIds.get(1);

            if ( !nextId.startsWith("col:") ) { //$NON-NLS-1$
                log.warn("Invalid collection path " + nextIds); //$NON-NLS-1$
            }
            else {
                selectObj = getSelectObject(multiObjectEditor, nextId.substring(4));
            }
        }

        if ( selectObj != null && ! ( multiObjectEditor instanceof MultiInheritanceObjectEditor ) ) {
            try {
                if ( selectObj.equals(multiObjectEditor.getSelectedObject()) ) {
                    pathStack.push(nextIds.get(0));
                    pathStack.push(nextIds.get(1));
                    return true;
                }
            }
            catch ( Exception e ) {
                log.warn("Failed to get selected object", e); //$NON-NLS-1$
            }
        }

        try {
            pathStack.push(nextIds.get(0));
            for ( ConfigurationObject obj : multiObjectEditor.getSelectOptions() ) {
                try {
                    pathStack.push("col:" + obj.getId()); //$NON-NLS-1$
                    String labelValue = buildItemLabel(multiObjectEditor, obj);
                    addAllMatchesToComponent(ctx, comp, pathStack, comp.getClientId(ctx), labelValue);
                }
                finally {
                    pathStack.pop();

                }
            }
            return false;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        finally {
            pathStack.pop();
        }
        return false;
    }


    /**
     * @param multiObjectEditor
     * @param nextId
     * @return
     */
    private static String buildItemLabel ( MultiObjectEditor multiObjectEditor, ConfigurationObject selectObject ) {
        String labelValue = multiObjectEditor.getDisplayTitleFor(selectObject);
        String itemLabel = null;
        if ( selectObject != null ) {
            try {
                itemLabel = multiObjectEditor.proxyLabel(selectObject);
            }
            catch ( Exception e1 ) {
                log.warn("Failed to get item label", e1); //$NON-NLS-1$
            }
        }
        else {
            log.debug("Failed to locate object for " + labelValue); //$NON-NLS-1$
        }

        if ( itemLabel != null ) {
            labelValue = String.format("%s - '%s'", labelValue, itemLabel); //$NON-NLS-1$
        }
        return labelValue;
    }


    /**
     * @param objectStack
     * @return
     */
    private List<String> getNextIds ( Stack<String> objectStack ) {
        // check
        for ( ViolationEntry e : this.violations ) {
            int remainingDepth = e.getPath().size() - objectStack.size();
            if ( remainingDepth <= 0 ) {
                continue;
            }
            List<String> toMatch = e.getPath().subList(0, objectStack.size());
            if ( toMatch.equals(objectStack) ) {
                return e.getPath().subList(objectStack.size(), e.getPath().size());
            }
        }
        return Collections.EMPTY_LIST;
    }


    /**
     * @param multiObjectEditor
     * @param nextId
     * @return
     */
    private static ConfigurationObject getSelectObject ( MultiObjectEditor multiObjectEditor, String nextId ) {
        if ( nextId == null ) {
            return null;
        }

        try {
            for ( ConfigurationObject obj : multiObjectEditor.getSelectOptions() ) {
                if ( objectMatches(nextId, obj) ) {
                    return obj;
                }
            }
        }
        catch ( Exception e1 ) {
            log.warn("Failed to get selection options", e1); //$NON-NLS-1$
        }

        return null;
    }


    private static boolean objectMatches ( String nextId, ConfigurationObject obj ) {
        return obj.getId() != null && nextId.equals(obj.getId().toString());
    }


    /**
     * @param ctx
     * @param comp
     * @param pathStack
     * @return
     */
    private boolean handleFieldEditor ( FacesContext ctx, UIComponent comp, Stack<String> pathStack ) {
        ObjectFieldEditor editor = (ObjectFieldEditor) comp;
        String clientId = comp.getClientId(ctx);
        String path = editor.internalGetPath();

        if ( !isViolationPath(pathStack, path) ) {
            return false;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found editor at %s/%s", StringUtils.join(pathStack, '/'), path)); //$NON-NLS-1$
        }

        String inputId = (String) comp.getAttributes().get(INPUT_ID);
        if ( inputId != null ) {
            UIComponent inputComp = comp.findComponent(inputId);
            if ( inputComp != null ) {
                clientId = inputComp.getClientId(ctx);
            }
        }

        Stack<String> localPath = new Stack<>();
        localPath.addAll(pathStack);
        localPath.push(path);

        String labelValue = editor.getLabelValue();
        addAllMatchesToComponent(ctx, comp, localPath, clientId, labelValue);
        return false;
    }


    /**
     * @param ctx
     * @param comp
     * @param idStack
     * @param clientId
     * @param localPath
     * @param labelValue
     */
    private void addAllMatchesToComponent ( FacesContext ctx, UIComponent comp, Stack<String> pathStack, String clientId, String labelValue ) {
        Set<ViolationEntry> toRemove = new HashSet<>();
        for ( ViolationEntry e : this.violations ) {
            if ( pathStack.size() > e.getPath().size() ) {
                continue;
            }

            List<String> toMatch = e.getPath().subList(0, pathStack.size());
            if ( toMatch.equals(pathStack) ) {

                if ( addMessageToComponent(ctx, comp, e, labelValue) ) {
                    toRemove.add(e);
                }
            }
            else if ( log.isDebugEnabled() ) {
                log.debug("toMatch " + toMatch); //$NON-NLS-1$
                log.debug("actual " + pathStack); //$NON-NLS-1$
            }
        }

        this.violations.removeAll(toRemove);
    }


    /**
     * @param ctx
     * @param comp
     * @param pathStack
     * @param objectStack
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private boolean handleEditor ( FacesContext ctx, UIComponent comp, Stack<String> pathStack ) {
        String localPath = ( (AbstractObjectEditor<ConfigurationObject>) comp ).internalGetPath();

        if ( !isViolationPath(pathStack, localPath) ) {
            return false;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found editor at %s/%s", StringUtils.join(pathStack, '/'), localPath)); //$NON-NLS-1$
        }

        if ( !StringUtils.isBlank(localPath) ) {
            pathStack.push(localPath);
        }

        try {
            doVisitChildren(ctx, comp, pathStack);
        }
        finally {
            if ( !StringUtils.isBlank(localPath) ) {
                pathStack.pop();
            }
        }

        return false;
    }


    /**
     * @param pathStack
     * @param localPath
     * @return
     */
    private boolean isViolationPath ( Stack<String> pathStack, String localPath ) {
        if ( pathStack.isEmpty() || StringUtils.isEmpty(localPath) ) {
            return true;
        }
        for ( ViolationEntry e : this.violations ) {

            if ( e == null || e.getPath() == null ) {
                log.warn("Path is null " + e); //$NON-NLS-1$
                continue;
            }

            log.trace(e.getPath());

            if ( e.getPath().size() <= pathStack.size() ) {
                continue;
            }

            String nextElem = e.getPath().get(pathStack.size());
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Checking localPath %s against %s", localPath, nextElem)); //$NON-NLS-1$
            }
            if ( localPath.equals(nextElem) ) {
                return true;
            }
        }

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Not a violation path %s @ %s", localPath, pathStack)); //$NON-NLS-1$
        }
        return false;
    }


    /**
     * @param ctx
     * @param comp
     * @param clientId
     * @param v
     * @param labelValue
     */
    private boolean addMessageToComponent ( FacesContext ctx, UIComponent comp, ViolationEntry v, String labelValue ) {
        if ( !comp.isRendered() ) {
            log.debug("Component is not rendered"); //$NON-NLS-1$
            return false;
        }
        UIComponent realInput = comp;
        UIComponent msgComp = comp.findComponent("msgs"); //$NON-NLS-1$
        if ( msgComp != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("adding message to component " + realInput.getClientId(ctx)); //$NON-NLS-1$
            }
            ctx.addMessage(realInput.getClientId(ctx), this.vmb.makeMessage(labelValue, v));
            String msgClientId = msgComp.getClientId(ctx);

            if ( log.isDebugEnabled() ) {
                log.debug("force rendering of  message component " + msgClientId); //$NON-NLS-1$
            }
            ctx.getPartialViewContext().getRenderIds().add(msgClientId);

            if ( this.firstViolation ) {
                selectTabs(ctx, comp);
                this.firstViolation = false;
            }
            return true;
        }

        log.debug("Could not find message component for " + comp.getClientId()); //$NON-NLS-1$
        return false;
    }


    /**
     * @param comp
     */
    private static void selectTabs ( FacesContext ctx, UIComponent comp ) {
        UIComponent parent = comp;
        while ( parent != null ) {
            if ( parent instanceof Tab && parent.getParent() instanceof TabView ) {
                log.debug("Found tab view in chain"); //$NON-NLS-1$

                Tab t = (Tab) parent;
                TabView tv = (TabView) parent.getParent();

                if ( !tv.isRepeating() ) {
                    selectTab(ctx, t, tv);
                }
            }
            parent = parent.getParent();
        }
    }


    /**
     * @param ctx
     * @param t
     * @param tv
     */
    private static void selectTab ( FacesContext ctx, Tab t, TabView tv ) {
        int idx = 0;
        for ( UIComponent tc : tv.getChildren() ) {
            if ( tc.equals(t) ) {
                tv.setActiveIndex(idx);
                ctx.getPartialViewContext().getRenderIds().add(tv.getClientId(ctx));
                break;
            }
            if ( tc.isRendered() ) {
                idx++;
            }
        }
    }

}
