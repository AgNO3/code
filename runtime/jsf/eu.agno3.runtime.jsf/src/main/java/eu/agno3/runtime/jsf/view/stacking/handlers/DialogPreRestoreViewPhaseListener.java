/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.handlers;


import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.myfaces.lifecycle.DefaultRestoreViewSupport;
import org.apache.myfaces.lifecycle.RestoreViewSupport;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;
import eu.agno3.runtime.jsf.view.stacking.ReturnEntry;
import eu.agno3.runtime.jsf.view.stacking.StackEntry;
import eu.agno3.runtime.jsf.view.stacking.ViewStack;
import eu.agno3.runtime.jsf.view.stacking.internal.FaceletsStateSaving;


/**
 * @author mbechler
 * 
 */
public class DialogPreRestoreViewPhaseListener implements PhaseListener {

    private static final Logger log = Logger.getLogger(DialogPreRestoreViewPhaseListener.class);

    private static final String SERIALIZED_VIEW_SESSION_ATTR = "org.apache.myfaces.application.viewstate.ServerSideStateCacheImpl.SERIALIZED_VIEW"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = 5544105130585327375L;

    private DefaultRestoreViewSupport restoreViewSupport;


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void afterPhase ( PhaseEvent event ) {}


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void beforePhase ( PhaseEvent event ) {
        log.trace("Before RESTORE_VIEW"); //$NON-NLS-1$

        FacesContext context = event.getFacesContext();
        String returnId = getReturnId(context);
        String curId = getCurrentId(context);

        if ( context.isPostback() && returnId == null ) {
            String viewId = getRestoreViewSupport(context).calculateViewId(context);
            log.debug("Postback, but no param found " + viewId); //$NON-NLS-1$
        }

        if ( returnId != null ) {
            try {
                handleReturn(context, returnId, curId);
            }
            catch ( Exception e ) {
                log.warn("Returning to stored view failed:", e); //$NON-NLS-1$
            }
        }
    }


    private static String getCurrentId ( FacesContext context ) {
        if ( context.isPostback() ) {
            for ( Entry<String, String> param : context.getExternalContext().getRequestParameterMap().entrySet() ) {

                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Param %s -> %s", param.getKey(), param.getValue())); //$NON-NLS-1$
                }

                if ( param.getKey().endsWith("_curid") ) { //$NON-NLS-1$
                    return param.getValue();
                }
            }
        }

        return null;
    }


    private static String getReturnId ( FacesContext context ) {
        String returnId = context.getExternalContext().getRequestParameterMap().get(DialogConstants.RETURN_ATTR);

        if ( returnId != null ) {
            return returnId;
        }

        if ( context.isPostback() ) {
            for ( Entry<String, String> param : context.getExternalContext().getRequestParameterMap().entrySet() ) {

                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Param %s -> %s", param.getKey(), param.getValue())); //$NON-NLS-1$
                }

                if ( param.getKey().endsWith("_pfdlgcid") ) { //$NON-NLS-1$
                    return param.getValue();
                }
            }
        }

        return null;
    }


    protected RestoreViewSupport getRestoreViewSupport ( FacesContext context ) {
        if ( this.restoreViewSupport == null ) {
            this.restoreViewSupport = new DefaultRestoreViewSupport(context);
        }
        return this.restoreViewSupport;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    @Override
    public PhaseId getPhaseId () {
        return PhaseId.RESTORE_VIEW;
    }


    /**
     * @param context
     * @param returnId
     */
    private static void handleReturn ( final FacesContext context, String returnId, String curId ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Returned with id " + returnId); //$NON-NLS-1$
        }

        ViewStack viewStack = ViewStack.getViewStack(context.getExternalContext());
        final ReturnEntry returnEntry = viewStack.popReturnValue(returnId);

        if ( returnEntry != null ) {
            // would be nice to do this here, but it seems that there can still be incoming requests after we returned
            // removeViewState(context, returnEntry.getViewId(), returnEntry.getViewKey());
        }

        if ( curId != null ) {
            log.debug("Return to outermost"); //$NON-NLS-1$
            viewStack.popTo(curId, returnId);
        }

        StackEntry e = viewStack.popEntry(returnId);

        if ( e == null ) {
            return;
        }
        Object state = e.getState();
        Object viewKey = e.getViewKey();
        if ( state != null ) {
            FaceletsStateSaving fss = new FaceletsStateSaving(context);
            UIViewRoot root = fss.restoreViewState(context, state);

            context.getAttributes().put(
                "org.apache.myfaces.application.viewstate.ServerSideStateCacheImpl.RESTORED_SERIALIZED_VIEW_ID", //$NON-NLS-1$
                root.getViewId());

            context.getAttributes().put(
                "org.apache.myfaces.application.viewstate.ServerSideStateCacheImpl.RESTORED_SERIALIZED_VIEW_KEY", //$NON-NLS-1$
                viewKey);

            String returnComponentId = e.getReturnComponentId();

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Return value %s for %s", returnEntry != null ? returnEntry.getReturnValue() : null, returnComponentId)); //$NON-NLS-1$
            }

            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            HttpServletRequest req = (HttpServletRequest) ctx.getRequest();
            req.setAttribute(DialogConstants.REQATTR_COMPONENT_ID, returnComponentId);
            req.setAttribute(DialogConstants.REQATTR_RETURN_VALUE, returnEntry != null ? returnEntry.getReturnValue() : null);
            context.setViewRoot(root);

        }

        ViewStack.cleanupExpired(context);
    }


    /**
     * @param context
     * @param viewId
     * @param viewKey
     */
    static void removeViewState ( FacesContext context, String viewId, Object viewKey ) {
        Object viewCollection = context.getExternalContext().getSessionMap().get(SERIALIZED_VIEW_SESSION_ATTR);
        if ( viewCollection == null ) {
            return;
        }

        try {
            Field viewsF = viewCollection.getClass().getDeclaredField("_serializedViews"); //$NON-NLS-1$
            viewsF.setAccessible(true);
            @SuppressWarnings ( "unchecked" )
            Map<Object, Object> serializedViews = (Map<Object, Object>) viewsF.get(viewCollection);
            if ( serializedViews.remove(viewKey) != null ) {
                log.debug("Removed saved state of returned dialog " + viewId); //$NON-NLS-1$
            }
        }
        catch (
            NoSuchFieldException |
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException e ) {
            log.warn("Failed to remove returned dialog state", e); //$NON-NLS-1$
        }
    }

}
