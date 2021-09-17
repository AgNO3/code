/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.el.MethodExpression;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.BehaviorEvent;
import javax.faces.event.FacesEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.event.SelectEvent;
import org.primefaces.util.Constants;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;


/**
 * @author mbechler
 * 
 */
@ResourceDependencies ( {
    @ResourceDependency ( library = "primefaces", name = "components.css" ),
    @ResourceDependency ( library = "primefaces", name = "jquery/jquery.js" ),
    @ResourceDependency ( library = "primefaces", name = "jquery/jquery-plugins.js" ),
    @ResourceDependency ( library = "primefaces", name = "core.js" ), @ResourceDependency ( library = "primefaces", name = "components.js" ),
    @ResourceDependency ( library = "agno3", name = "library.js" )
})
public class DialogOpenButton extends CommandButton implements DialogOpenComponent {

    private static final Logger log = Logger.getLogger(DialogOpenButton.class);

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
     * 
     */
    public DialogOpenButton () {}


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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.view.stacking.components.DialogOpenComponent#getDialog()
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
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.view.stacking.components.DialogOpenComponent#getReturnLabel()
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
     * @see javax.faces.component.UIComponentBase#isRendered()
     */
    @Override
    public boolean isRendered () {
        // fake rendered so that events are properly delivered
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIComponentBase#encodeAll(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeAll ( FacesContext ctx ) throws IOException {
        if ( super.isRendered() ) {
            super.encodeAll(ctx);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeBegin ( FacesContext ctx ) throws IOException {
        if ( super.isRendered() ) {
            super.encodeBegin(ctx);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeChildren ( FacesContext ctx ) throws IOException {
        if ( super.isRendered() ) {
            super.encodeChildren(ctx);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeEnd ( FacesContext ctx ) throws IOException {
        if ( super.isRendered() ) {
            super.encodeEnd(ctx);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.commandbutton.CommandButton#resolveStyleClass()
     */
    @Override
    public String resolveStyleClass () {
        return super.resolveStyleClass() + " ui-dialog-open"; //$NON-NLS-1$
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

        FacesContext fc = FacesContext.getCurrentInstance();
        if ( event instanceof DialogOpenEvent ) {
            String dialogOutcome = this.getDialog();
            String dialogClientId = this.getClientId();

            if ( this.preOpenVetoes() ) {
                log.debug("Pre-open method returned false"); //$NON-NLS-1$
                return;
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Open dialog %s with return component %s", dialogOutcome, dialogClientId)); //$NON-NLS-1$
            }

            DialogUtil.openDialog(fc, this);
            return;
        }

        super.broadcast(event);

    }


    /**
     * @return
     */
    private boolean preOpenVetoes () {
        MethodExpression preOpen = getPreOpen();
        if ( preOpen == null ) {
            return false;
        }

        Object ret = preOpen.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] {});
        if ( ret == null ) {
            return false;
        }
        return !(boolean) ret;
    }


    /**
     * @return the pre open handler, a method that might veto opening a dialog by returning false
     */
    public MethodExpression getPreOpen () {
        return (MethodExpression) getStateHelper().eval(DialogConstants.PRE_OPEN_HANDLER, null);
    }


    /**
     * @param ex
     */
    public void setPreOpen ( MethodExpression ex ) {
        getStateHelper().put(DialogConstants.PRE_OPEN_HANDLER, ex);
    }
}
