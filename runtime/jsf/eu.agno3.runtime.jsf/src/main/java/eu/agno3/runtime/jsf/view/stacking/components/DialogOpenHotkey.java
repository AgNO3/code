/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UINamingContainer;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.BehaviorEvent;
import javax.faces.event.FacesEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.component.api.PrimeClientBehaviorHolder;
import org.primefaces.component.hotkey.Hotkey;
import org.primefaces.event.SelectEvent;
import org.primefaces.util.Constants;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;


/**
 * @author mbechler
 *
 */
@ResourceDependencies ( value = {
    @ResourceDependency ( library = "primefaces", name = "jquery/jquery.js" ),
    @ResourceDependency ( library = "primefaces", name = "jquery/jquery-plugins.js" ),
    @ResourceDependency ( library = "primefaces", name = "core.js" ), @ResourceDependency ( library = "primefaces", name = "hotkey/hotkey.js" ),
    @ResourceDependency ( library = "agno3", name = "library.js" )
})
public class DialogOpenHotkey extends Hotkey implements DialogOpenComponent, ClientBehaviorHolder, PrimeClientBehaviorHolder {

    /**
     * 
     */
    private static final String WIDGET_VAR = "widgetVar"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DialogOpenHotkey.class);

    private static final String ALREADY_OPENED = "alreadyOpened"; //$NON-NLS-1$

    private static final Collection<String> EVENT_NAMES = Collections.unmodifiableCollection(Arrays.asList(
        "click", //$NON-NLS-1$
        "return")); //$NON-NLS-1$

    private static final Map<String, Class<? extends BehaviorEvent>> BEHAVIOR_EVENT_MAPPING = Collections
            .unmodifiableMap(new HashMap<String, Class<? extends BehaviorEvent>>() {

                /**
                 * 
                 */
                private static final long serialVersionUID = -9157368885711506951L;


                {
                    put("click", null); //$NON-NLS-1$
                    put("return", SelectEvent.class); //$NON-NLS-1$
                }
            });


    @Override
    public Map<String, Class<? extends BehaviorEvent>> getBehaviorEventMapping () {
        return BEHAVIOR_EVENT_MAPPING;
    }


    @Override
    public Collection<String> getEventNames () {
        return EVENT_NAMES;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.component.UIComponent#getFamily()
     */
    @Override
    public String getFamily () {
        return DialogConstants.COMPONENT_FAMILY;
    }


    /**
     * @return dialog view id
     */
    @Override
    public java.lang.String getDialog () {
        return (java.lang.String) getStateHelper().eval(DialogConstants.DIALOG_ATTR, null);
    }


    /**
     * @param dialog
     */
    public void setDialog ( java.lang.String dialog ) {
        getStateHelper().put(DialogConstants.DIALOG_ATTR, dialog);
    }


    /**
     * 
     * @param returnLabel
     */
    public void setReturnLabel ( String returnLabel ) {
        getStateHelper().put(DialogConstants.RETURN_LABEL, returnLabel);
    }


    /**
     * @return the return label
     */
    @Override
    public String getReturnLabel () {
        return (String) getStateHelper().eval(DialogConstants.RETURN_LABEL, null);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.view.stacking.components.DialogOpenComponent#isClosable()
     */
    @Override
    public boolean isClosable () {
        return (boolean) getStateHelper().eval(DialogConstants.CLOSABLE, false);
    }


    /**
     * 
     * @param closable
     */
    public void setClosable ( boolean closable ) {
        getStateHelper().put(DialogConstants.CLOSABLE, closable);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.view.stacking.components.DialogOpenComponent#hasReturnBehaviour()
     */
    @Override
    public boolean hasReturnBehaviour () {
        return this.getClientBehaviors().get("return") != null; //$NON-NLS-1$
    }


    @Override
    public boolean getAndSetOpened () {
        Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
        Boolean object = (Boolean) viewMap.get(ALREADY_OPENED);
        if ( log.isDebugEnabled() ) {
            log.debug("Setting to dialog open state, have " + object); //$NON-NLS-1$
        }
        viewMap.put(ALREADY_OPENED, true);
        return object != null ? object : false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.view.stacking.components.DialogOpenComponent#resetOpened()
     */
    @Override
    public void resetOpened () {
        log.debug("Resetting to closed state"); //$NON-NLS-1$
        Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
        viewMap.remove(ALREADY_OPENED);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.view.stacking.components.DialogOpenComponent#getWidgetVar()
     */
    @Override
    public String getWidgetVar () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.commandbutton.CommandButton#queueEvent(javax.faces.event.FacesEvent)
     */
    @Override
    public void queueEvent ( FacesEvent ev ) {
        if ( ev instanceof AjaxBehaviorEvent ) {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String eventName = params.get(Constants.RequestParams.PARTIAL_BEHAVIOR_EVENT_PARAM);

            if ( "dialogReturn".equals(eventName) ) { //$NON-NLS-1$
                log.debug("Discarding dialogReturn ajax event"); //$NON-NLS-1$
                return;
            }
            else if ( ev instanceof SelectEvent ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Queuing dialog return select event " + ev); //$NON-NLS-1$
                }
                DialogUtil.closed(FacesContext.getCurrentInstance(), this);
                getParent().queueEvent(ev);
                return;
            }
        }
        super.queueEvent(ev);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
     */
    @Override
    public void broadcast ( FacesEvent event ) throws AbortProcessingException {

        if ( event instanceof DialogOpenEvent ) {
            String dialogOutcome = this.getDialog();
            String dialogClientId = this.getClientId();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Open dialog %s with return component %s", dialogOutcome, dialogClientId)); //$NON-NLS-1$
            }

            DialogUtil.openDialog(FacesContext.getCurrentInstance(), this);
            return;
        }

        if ( event instanceof AjaxBehaviorEvent && ! ( event instanceof SelectEvent ) ) {
            return;
        }

        super.broadcast(event);
    }


    /**
     * @return the widget var
     */
    public String resolveWidgetVar () {
        FacesContext context = getFacesContext();
        String userWidgetVar = (String) getAttributes().get(WIDGET_VAR);

        if ( userWidgetVar != null ) {
            return userWidgetVar;
        }

        return "widget_" + //$NON-NLS-1$
                StringUtils.replaceChars(
                    getClientId(context),
                    "-|" + UINamingContainer.getSeparatorChar(context), //$NON-NLS-1$
                    "___"); //$NON-NLS-1$
    }
}
