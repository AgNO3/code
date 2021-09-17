/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import java.util.Map;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.FacesEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.menuitem.UIMenuItem;
import org.primefaces.event.SelectEvent;
import org.primefaces.util.Constants;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;


/**
 * @author mbechler
 *
 */
@ResourceDependencies ( {
    @ResourceDependency ( library = "agno3", name = "library.js" )
})
public class DialogOpenMenuItem extends UIMenuItem implements DialogOpenComponent {

    private static final Logger log = Logger.getLogger(DialogOpenMenuItem.class);


    /**
     * 
     */
    public DialogOpenMenuItem () {}


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
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.view.stacking.components.DialogOpenComponent#getAndSetOpened()
     */
    @Override
    public boolean getAndSetOpened () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.view.stacking.components.DialogOpenComponent#resetOpened()
     */
    @Override
    public void resetOpened () {

    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.menuitem.UIMenuItem#decode(javax.faces.context.FacesContext)
     */
    @Override
    public void decode ( FacesContext context ) {
        log.debug("Running decode"); //$NON-NLS-1$
        super.decode(context);

        if ( !this.isDisabled() ) {
            String param = this.getClientId(context);

            if ( context.getExternalContext().getRequestParameterMap().containsKey(param) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Publishing dialog open event for dialog " + this.getDialog()); //$NON-NLS-1$
                }

                this.queueEvent(new DialogOpenEvent(this));
            }
        }
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

        super.broadcast(event);
    }
}
