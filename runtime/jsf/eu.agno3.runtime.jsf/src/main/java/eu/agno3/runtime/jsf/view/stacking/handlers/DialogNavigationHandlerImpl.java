/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.handlers;


import java.util.Objects;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationHandler;
import javax.faces.application.NavigationHandlerWrapper;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;
import eu.agno3.runtime.jsf.view.stacking.StackEntry;
import eu.agno3.runtime.jsf.view.stacking.ViewStack;
import eu.agno3.runtime.jsf.view.stacking.ViewStackException;


/**
 * @author mbechler
 * 
 */
public class DialogNavigationHandlerImpl extends NavigationHandlerWrapper {

    private static final Logger log = Logger.getLogger(DialogNavigationHandlerImpl.class);
    private static final String DIALOG_CLOSED = "dialog.closed"; //$NON-NLS-1$
    private ConfigurableNavigationHandler wrapped;


    /**
     * @param base
     */
    public DialogNavigationHandlerImpl ( ConfigurableNavigationHandler base ) {
        this.wrapped = base;
    }


    @Override
    public void handleNavigation ( FacesContext context, String fromAction, String outcome ) {
        if ( outcome != null && outcome.startsWith(DialogConstants.DIALOG_CLOSE_OUTCOME) ) {

            String returnToId = context.getExternalContext().getRequestParameterMap().get(DialogConstants.RETURN_TO_ATTR);
            if ( returnToId == null ) {
                log.warn("Could not find return ID"); //$NON-NLS-1$
                return;
            }

            if ( Objects.equals(context.getViewRoot().getViewMap().get(DIALOG_CLOSED), true) ) {
                log.warn("Already closed dialog"); //$NON-NLS-1$
                return;
            }
            context.getViewRoot().getViewMap().put(DIALOG_CLOSED, true);

            StackEntry e;
            try {
                e = getReturnEntry(outcome, returnToId);
            }
            catch ( ViewStackException ex ) {
                log.warn("Failed to locate view stack entry", ex); //$NON-NLS-1$
                // unconditionally close all dialog windows
                RequestContext.getCurrentInstance().execute("parent.AgNO3DialogOverlay.closeDialog()"); //$NON-NLS-1$
                return;
            }

            if ( StringUtils.isBlank(e.getReturnComponentId()) ) {
                log.debug("Not having a component to return to, removing from stack"); //$NON-NLS-1$
                getViewStack().popEntry(e.getId());
            }

            if ( e.getParentId() != null ) {
                log.debug("Return from nested dialog"); //$NON-NLS-1$
                super.handleNavigation(context, fromAction, makeReturnOutcome(e));
                return;
            }

            log.debug("Return from outermost dialog"); //$NON-NLS-1$
            RequestContext.getCurrentInstance().execute(makeOverlayDialogClose(e.getId()));
            return;
        }

        super.handleNavigation(context, fromAction, outcome);
    }


    /**
     * @param outcome
     * @param returnToId
     * @return
     */
    private static StackEntry getReturnEntry ( String outcome, String returnToId ) {
        StackEntry e = getViewStack().getEntry(returnToId);
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Return to %s component %s", e.getReturnUrl(), e.getReturnComponentId())); //$NON-NLS-1$
        }

        if ( DialogConstants.DIALOG_CLOSE_OUTCOME.equals(outcome) ) {
            log.debug("Single level return"); //$NON-NLS-1$
        }
        else {
            String targetReturnToId = outcome.substring(DialogConstants.DIALOG_CLOSE_OUTCOME.length() + 1);
            if ( log.isDebugEnabled() ) {
                log.debug("Multi level return to " + targetReturnToId); //$NON-NLS-1$
            }

            e = getViewStack().popTo(returnToId, targetReturnToId);
        }
        return e;
    }


    /**
     * @param returnToId
     * @return
     */
    private static String makeOverlayDialogClose ( String returnToId ) {
        return String.format("parent.AgNO3DialogOverlay.closeDialog({pfdlgcid:'%s'});", returnToId); //$NON-NLS-1$
    }


    /**
     * @return
     */
    private static ViewStack getViewStack () {
        return ViewStack.getViewStack(FacesContext.getCurrentInstance().getExternalContext());
    }


    /**
     * @param e
     * @return
     */
    private static String makeReturnOutcome ( StackEntry e ) {
        return e.getReturnUrl() + "&faces-redirect=true"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.NavigationHandlerWrapper#getWrapped()
     */
    @Override
    public NavigationHandler getWrapped () {
        return this.wrapped;
    }
}
