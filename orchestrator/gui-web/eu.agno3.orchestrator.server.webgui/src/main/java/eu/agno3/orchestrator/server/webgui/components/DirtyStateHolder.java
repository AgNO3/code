/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;


/**
 * 
 * 
 * @author mbechler
 * 
 */
public class DirtyStateHolder extends UIComponentBase {

    private static final String IGNORE_CHANGE = "ignoreChange"; //$NON-NLS-1$
    private static final String CLASS = "class"; //$NON-NLS-1$
    private static final String DIRTY = "dirty"; //$NON-NLS-1$
    private static final String INPUT_TYPE_HIDDEN = "hidden"; //$NON-NLS-1$
    private static final String INPUT_VALUE = "value"; //$NON-NLS-1$
    private static final String INPUT_NAME = "name"; //$NON-NLS-1$
    private static final String INPUT_TYPE = "type"; //$NON-NLS-1$
    private static final String INPUT_ELEMENT = "input"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.component.UIComponent#getFamily()
     */
    @Override
    public String getFamily () {
        return "eu.agno3.jsf.changes"; //$NON-NLS-1$
    }


    @Override
    public void encodeBegin ( FacesContext context ) throws java.io.IOException {
        @SuppressWarnings ( "resource" )
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(INPUT_ELEMENT, this);
        writer.writeAttribute(INPUT_TYPE, INPUT_TYPE_HIDDEN, null);
        writer.writeAttribute(CLASS, IGNORE_CHANGE, null);
        writer.writeAttribute(INPUT_NAME, DIRTY, null);
        writer.writeAttribute(INPUT_VALUE, String.valueOf(getStateHelper().eval(DIRTY, false)), null);
        writer.endElement(INPUT_ELEMENT);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
     */
    @Override
    public void decode ( FacesContext context ) {
        super.decode(context);
        String dirtyParam = context.getExternalContext().getRequestParameterMap().get(DIRTY);
        if ( dirtyParam == null ) {
            return;
        }
        getStateHelper().put(DIRTY, Boolean.parseBoolean(dirtyParam));
    }

}
