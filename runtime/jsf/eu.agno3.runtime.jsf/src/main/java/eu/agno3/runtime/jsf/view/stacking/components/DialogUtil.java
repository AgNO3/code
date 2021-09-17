/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.application.NavigationHandlerWrapper;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.shared.util.ViewProtectionUtils;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;
import eu.agno3.runtime.jsf.view.stacking.StackEntry;
import eu.agno3.runtime.jsf.view.stacking.ViewStack;
import eu.agno3.runtime.jsf.view.stacking.ViewStackException;
import eu.agno3.runtime.jsf.view.stacking.internal.FaceletsStateSaving;
import eu.agno3.runtime.jsf.view.stacking.overlay.OverlayDialog;


/**
 * @author mbechler
 * 
 */
public final class DialogUtil {

    private static final Logger log = Logger.getLogger(DialogUtil.class);

    private static final String URL_CHARSET = "UTF-8"; //$NON-NLS-1$


    /**
     * 
     */
    private DialogUtil () {}


    /**
     * @param context
     * @param returnComp
     */
    public static void openDialog ( FacesContext context, DialogOpenComponent returnComp ) {
        boolean canTrackReturn = returnComp.hasReturnBehaviour();
        String outcome = returnComp.getDialog();
        if ( returnComp instanceof UICommand ) {
            UICommand com = (UICommand) returnComp;
            if ( com.getActionExpression() != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Calling action on " + returnComp); //$NON-NLS-1$
                }
                outcome = (String) com.getActionExpression().invoke(context.getELContext(), new Object[] {});
                if ( outcome == null ) {
                    return;
                }
            }
        }

        if ( canTrackReturn ) {
            boolean wasOpen = returnComp.getAndSetOpened();
            if ( wasOpen ) {
                log.debug("Dialog is already opened " + returnComp); //$NON-NLS-1$
                return;
            }
        }
        else {
            log.warn("Cannot track dialog return, no return behavior " + returnComp); //$NON-NLS-1$
        }

        openDialog(
            context,
            canTrackReturn,
            outcome,
            returnComp.hasReturnBehaviour() ? returnComp.getClientId() : null,
            returnComp.hasReturnBehaviour() ? getWidgetVar(returnComp) : null,
            returnComp.getReturnLabel(),
            returnComp.isClosable());
    }


    /**
     * @param facesContext
     * @param outcome
     * @param returnComponent
     * @param closable
     */
    protected static void openDialog ( FacesContext facesContext, boolean canTrackReturn, String outcome, String returnComponent,
            String returnComponentWidget, String returnLabel, boolean closable ) {
        FaceletsStateSaving fss = new FaceletsStateSaving(facesContext);
        Object dumped = fss.dumpViewState(facesContext);
        Serializable viewKey = (Serializable) facesContext.getAttributes()
                .get("org.apache.myfaces.application.viewstate.ServerSideStateCacheImpl.RESTORED_SERIALIZED_VIEW_KEY"); //$NON-NLS-1$

        String stackId = UUID.randomUUID().toString();
        String currentParent = DialogContext.getCurrentParent();
        String returnURL = makeReturnUrl(facesContext, stackId, currentParent);

        StackEntry e = null;

        if ( DialogContext.isInDialog() ) {
            log.debug("Opening nested dialog"); //$NON-NLS-1$
            e = new StackEntry(stackId, returnURL, returnComponent, currentParent, returnLabel, (Serializable) dumped, viewKey, closable);
        }
        else {
            log.debug("Opening outermost dialog"); //$NON-NLS-1$
            e = new StackEntry(stackId, returnURL, returnComponent, returnLabel, (Serializable) dumped, viewKey, closable);
        }

        ViewStack viewStack = ViewStack.getViewStack(facesContext.getExternalContext());
        if ( !viewStack.isEmpty() && !DialogContext.isInDialog() ) {
            if ( !canTrackReturn ) {
                log.debug("Not opening dialog as another might be open"); //$NON-NLS-1$
                return;
            }
            log.debug("View stack is not empty but we are not in a dialog, clearing"); //$NON-NLS-1$
            viewStack.clear();
        }
        viewStack.pushEntry(stackId, e);

        ViewStack.cleanupExpired(facesContext);

        if ( DialogContext.isInDialog() ) {
            String sb = makeDialogOutcome(outcome, stackId);
            facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, "", sb); //$NON-NLS-1$
        }
        else {
            String url = getDialogUrl(facesContext, outcome, stackId);
            OverlayDialog.openDialogOverlay(stackId, url, returnComponent, returnComponentWidget, closable);
        }
    }


    /**
     * @param facesContext
     * @param outcome
     * @param stackId
     * @return
     */
    private static String getDialogUrl ( FacesContext facesContext, String outcome, String stackId ) {
        Map<String, List<String>> parameters = null;

        try {
            URI outcomeUrl = new URI(outcome);
            parameters = parseQuery(outcomeUrl.getQuery());
            addWindowParameters(facesContext, parameters);

        }
        catch (
            UnsupportedEncodingException |
            URISyntaxException e ) {
            log.warn("Failed to parse outcome query string " + outcome, e); //$NON-NLS-1$
            parameters = new HashMap<>();
        }
        parameters.put(DialogConstants.RETURN_TO_ATTR, Arrays.asList(stackId));
        NavigationCase navigationCase = getNavigationHandler(facesContext).getNavigationCase(facesContext, null, outcome);

        if ( navigationCase == null ) {
            log.warn("Failed to locate view for outcome " + outcome); //$NON-NLS-1$
            return null;
        }

        String viewId = navigationCase.getToViewId(facesContext);

        if ( ViewProtectionUtils.isViewProtected(facesContext, viewId) ) {
            log.debug("View is protected " + viewId); //$NON-NLS-1$
            parameters.put(
                ResponseStateManager.NON_POSTBACK_VIEW_TOKEN_PARAM,
                Arrays.asList(facesContext.getRenderKit().getResponseStateManager().getCryptographicallyStrongTokenFromSession(facesContext)));
        }

        ServletContext ctx = (ServletContext) facesContext.getExternalContext().getContext();
        return String.format(
            "%s%s%s", //$NON-NLS-1$
            ctx.getContextPath(),
            viewId,
            makeParams(parameters, facesContext.getExternalContext().getRequestCharacterEncoding()));
    }


    /**
     * @param parameters
     * @return
     */
    private static String makeParams ( Map<String, List<String>> parameters, String encoding ) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for ( Map.Entry<String, List<String>> pair : parameters.entrySet() ) {
            for ( String value : pair.getValue() ) {
                if ( !first ) {
                    sb.append('&');
                }
                else {
                    sb.append('?');
                    first = false;
                }

                sb.append(pair.getKey());
                sb.append('=');
                try {
                    sb.append(URLEncoder.encode(value, encoding));
                }
                catch ( UnsupportedEncodingException e ) {
                    throw new IllegalArgumentException("Unsupported: " + encoding, e); //$NON-NLS-1$
                }
            }
        }
        return sb.toString();
    }


    /**
     * @param facesContext
     * @param parameters
     */
    private static void addWindowParameters ( FacesContext facesContext, Map<String, List<String>> parameters ) {
        if ( facesContext.getExternalContext().getClientWindow() != null ) {
            for ( Entry<String, String> entry : facesContext.getExternalContext().getClientWindow().getQueryURLParameters(facesContext).entrySet() ) {

                List<String> values = parameters.get(entry.getKey());
                if ( values == null ) {
                    values = new LinkedList<>();
                    parameters.put(entry.getKey(), values);
                }
                values.add("dlg_" + entry.getValue()); //$NON-NLS-1$
            }
        }
    }


    // TODO: use some library?
    private static Map<String, List<String>> parseQuery ( String qString ) throws UnsupportedEncodingException {
        Map<String, List<String>> kv = new LinkedHashMap<>();
        String[] defs = StringUtils.split(qString, '&');
        if ( defs != null ) {
            for ( String def : defs ) {
                int idx = def.indexOf('=');
                String key = URLDecoder.decode(def.substring(0, idx), URL_CHARSET);

                if ( !kv.containsKey(key) ) {
                    kv.put(key, new LinkedList<String>());
                }

                if ( idx >= 0 ) {
                    String val = URLDecoder.decode(def.substring(idx + 1), URL_CHARSET);
                    kv.get(key).add(val);
                }
            }
        }
        return kv;
    }


    private static ConfigurableNavigationHandler getNavigationHandler ( FacesContext context ) {
        NavigationHandler navHandler = context.getApplication().getNavigationHandler();

        while ( navHandler instanceof NavigationHandlerWrapper ) {
            navHandler = ( (NavigationHandlerWrapper) navHandler ).getWrapped();
        }

        if ( ! ( navHandler instanceof ConfigurableNavigationHandler ) ) {
            throw new ViewStackException("Failed to locate proper NavigationHandler"); //$NON-NLS-1$
        }

        return (ConfigurableNavigationHandler) navHandler;
    }


    /**
     * @param outcome
     * @param stackId
     * @return
     */
    private static String makeDialogOutcome ( String outcome, String stackId ) {
        StringBuilder sb = new StringBuilder(outcome);

        int qpos = outcome.indexOf('?');
        if ( qpos == -1 ) {
            sb.append('?');
        }
        else if ( qpos != outcome.length() - 1 ) {
            sb.append('&');
        }

        sb.append("faces-redirect=true&"); //$NON-NLS-1$
        sb.append(DialogConstants.RETURN_TO_ATTR);
        sb.append('=');
        sb.append(stackId);
        return sb.toString();
    }


    /**
     * @param facesContext
     * @param stackId
     * @return
     */
    private static String makeReturnUrl ( FacesContext facesContext, String stackId, String currentReturnTo ) {
        String viewId = facesContext.getViewRoot().getViewId();

        Map<String, List<String>> params = new HashMap<>();
        params.put(DialogConstants.RETURN_ATTR, Arrays.asList(stackId));
        if ( currentReturnTo != null ) {
            params.put(DialogConstants.RETURN_TO_ATTR, Arrays.asList(currentReturnTo));
        }
        String returnURL = facesContext.getApplication().getViewHandler().getBookmarkableURL(facesContext, viewId, params, true);

        ServletContext ctx = (ServletContext) facesContext.getExternalContext().getContext();

        if ( returnURL.startsWith(ctx.getContextPath()) ) {
            returnURL = returnURL.substring(ctx.getContextPath().length());
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Built returnURL " + returnURL); //$NON-NLS-1$
        }

        return returnURL;
    }


    /**
     * @param dialogComponent
     * @return
     */
    private static String getWidgetVar ( DialogOpenComponent dialogComponent ) {

        if ( dialogComponent.getWidgetVar() != null ) {
            return dialogComponent.getWidgetVar();
        }

        String clientId = dialogComponent.getClientId();
        return "widget_".concat(clientId.replace(':', '_')); //$NON-NLS-1$
    }


    /**
     * @param currentInstance
     * @param dialogOpenButton
     */
    public static void closed ( FacesContext currentInstance, DialogOpenComponent dialogOpenButton ) {
        dialogOpenButton.resetOpened();
    }

}
