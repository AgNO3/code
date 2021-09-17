/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.log4j.Logger;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;
import eu.agno3.runtime.jsf.view.stacking.ViewStackException;


/**
 * 
 * 
 * @author mbechler
 * 
 */
public class DialogContextHolder extends UIComponentBase {

    private static final String INPUT_TYPE_HIDDEN = "hidden"; //$NON-NLS-1$
    private static final String INPUT_VALUE = "value"; //$NON-NLS-1$
    private static final String INPUT_NAME = "name"; //$NON-NLS-1$
    private static final String INPUT_TYPE = "type"; //$NON-NLS-1$
    private static final String INPUT_ELEMENT = "input"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DialogContextHolder.class);

    private String returnTo;
    private boolean closable;


    /**
     * 
     */
    public DialogContextHolder () {
        super();
        this.returnTo = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(DialogConstants.RETURN_TO_ATTR);
        try {
            this.closable = DialogContext.isInDialog() && DialogContext.isCurrentClosable();
        }
        catch ( ViewStackException e ) {
            log.debug("Cannot determine dialog status", e); //$NON-NLS-1$
        }
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


    @Override
    public void encodeBegin ( FacesContext context ) throws java.io.IOException {
        if ( this.returnTo != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Writing return to pointer element " + this.returnTo); //$NON-NLS-1$
            }
            @SuppressWarnings ( "resource" )
            ResponseWriter writer = context.getResponseWriter();

            writer.startElement(INPUT_ELEMENT, this);
            writer.writeAttribute(INPUT_TYPE, INPUT_TYPE_HIDDEN, null);
            writer.writeAttribute(INPUT_NAME, DialogConstants.RETURN_TO_ATTR, null);
            writer.writeAttribute(INPUT_VALUE, this.returnTo, null);
            writer.endElement(INPUT_ELEMENT);

            writer.startElement(INPUT_ELEMENT, this);
            writer.writeAttribute(INPUT_TYPE, INPUT_TYPE_HIDDEN, null);
            writer.writeAttribute(INPUT_NAME, DialogConstants.CLOSABLE, null);
            writer.writeAttribute(INPUT_VALUE, this.closable, null);
            writer.endElement(INPUT_ELEMENT);
        }
        else {
            log.warn("No return pointer found"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.component.UIComponentBase#saveState(javax.faces.context.FacesContext)
     */
    @Override
    public Object saveState ( FacesContext context ) {
        log.trace("saveState"); //$NON-NLS-1$
        Object[] rtrn = new Object[3];
        rtrn[ 0 ] = super.saveState(context);
        rtrn[ 1 ] = this.returnTo;
        rtrn[ 2 ] = this.closable;
        return rtrn;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.component.UIComponentBase#restoreState(javax.faces.context.FacesContext, java.lang.Object)
     */
    @Override
    public void restoreState ( FacesContext context, Object state ) {
        log.trace("restoreState"); //$NON-NLS-1$
        Object[] s = (Object[]) state;
        super.restoreState(context, s[ 0 ]);
        this.returnTo = (String) s[ 1 ];
        this.closable = (boolean) s[ 2 ];
    }
}
