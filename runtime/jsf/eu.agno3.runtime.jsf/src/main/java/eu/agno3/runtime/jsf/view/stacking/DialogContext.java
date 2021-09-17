/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 * 
 */
public final class DialogContext {

    /**
     * 
     */
    private static final String DIALOG_CLOSING = "dialog.closing"; //$NON-NLS-1$
    private static final String DIALOG_CLOSED = "dialog.closed"; //$NON-NLS-1$

    private static final String RESTORED_VIEW_KEY_REQUEST_ATTR = "org.apache.myfaces.application.viewstate.ServerSideStateCacheImpl.RESTORED_VIEW_KEY"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DialogContext.class);


    /**
     * 
     */
    private DialogContext () {}


    /**
     * @param data
     * @return outcome
     */
    public static String closeDialog ( Serializable data ) {
        FacesContext context = FacesContext.getCurrentInstance();

        String returnTo = getCurrentReturnTo(context);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("closeDialog %s: %s", returnTo, data)); //$NON-NLS-1$
        }

        if ( returnTo == null ) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, BaseMessages.get("dialog.noReturnPointer"), StringUtils.EMPTY)); //$NON-NLS-1$
            return null;
        }

        if ( Objects.equals(context.getViewRoot().getViewMap().get(DIALOG_CLOSING), true) ) {
            log.warn("Dialog already closing"); //$NON-NLS-1$
            return null;
        }

        UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
        ViewStack.getViewStack(context.getExternalContext()).pushReturnValue(
            returnTo,
            new ReturnEntry(data, viewRoot.getViewId(), (Serializable) context.getAttributes().get(RESTORED_VIEW_KEY_REQUEST_ATTR)));

        // mark that this dialog has been closed, outstanding ajax requests may somehow still be executed
        context.getViewRoot().getViewMap().put(DIALOG_CLOSING, true); // $NON-NLS-1$
        return DialogConstants.DIALOG_CLOSE_OUTCOME;
    }


    /**
     * 
     * @return the dialog stack
     */
    public static List<StackEntry> getCurrentStack () {
        if ( !isInDialog() ) {
            log.debug("Dialog stack is empty"); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }

        ViewStack stack = ViewStack.getViewStack(FacesContext.getCurrentInstance().getExternalContext());
        StackEntry s = stack.getEntry(getCurrentParent());
        List<StackEntry> res = new ArrayList<>();

        if ( s.getParentId() == null ) {
            return Arrays.asList(s);
        }

        while ( true ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Adding entry %s with id %s", s.getLabel(), s.getId())); //$NON-NLS-1$
            }
            res.add(s);
            if ( s.getParentId() == null ) {
                break;
            }
            s = stack.getEntry(s.getParentId());
        }

        Collections.reverse(res);
        return res;
    }


    /**
     * @return whether a dialog is currently active
     */
    public static boolean isInDialog () {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return getCurrentReturnTo(ctx) != null && !Objects.equals(ctx.getViewRoot().getViewMap().get(DIALOG_CLOSED), true);
    }


    /**
     * @param context
     * @return the current return to pointer
     */
    static String getCurrentReturnTo ( FacesContext context ) {
        String returnTo = (String) context.getAttributes().get(DialogConstants.RETURN_TO_ATTR);

        if ( returnTo == null ) {
            returnTo = context.getExternalContext().getRequestParameterMap().get(DialogConstants.RETURN_TO_ATTR);
        }
        return returnTo;
    }


    /**
     * @return the current parent pointer
     */
    public static String getCurrentParent () {
        return getCurrentReturnTo(FacesContext.getCurrentInstance());
    }


    /**
     * @return whether the currently open dialog is closable
     */
    public static boolean isCurrentClosable () {
        if ( !isInDialog() ) {
            return false;
        }

        ViewStack stack = ViewStack.getViewStack(FacesContext.getCurrentInstance().getExternalContext());
        String parent = getCurrentParent();
        try {
            StackEntry s = stack.getEntry(parent);
            if ( s == null ) {
                return false;
            }

            return s.isClosable();
        }
        catch ( ViewStackException e ) {
            log.debug("Failed to get parent entry " + parent, e); //$NON-NLS-1$
            return false;
        }
    }

}
